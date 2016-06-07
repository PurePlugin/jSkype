package xyz.gghost.jskype.internal.packet.packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.FailedToGetContactsException;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.user.User;

@AllArgsConstructor
public class GetContactsPacket
{
	private final SkypeAPI api;

	public void setupContact() throws FailedToGetContactsException, BadResponseException
	{
		List<User> contacts;
		HashMap<String, Boolean> blocked = new HashMap<String, Boolean>();
		ArrayList<String> usernames = new ArrayList<String>();

		PacketBuilder packet = new PacketBuilder(api);
		packet.setUrl("https://contacts.skype.com/contacts/v1/users/" + api.getUsername().toLowerCase() + "/contacts?filter=contacts");
		packet.setType(RequestType.OPTIONS);
		packet.getData();
		packet.setType(RequestType.GET);

		String a = packet.makeRequest();

		if (a == null)
		{
			api.log("Failed to request Skype for your contacts.");
			api.log("Code: " + packet.getCode() + "\nData: " + packet.getData() + "\nURL: " + packet.getUrl());

			if (api.getContacts().size() == 0)
				api.getContacts().add(api.getSimpleUser(api.getUsername()));

			return;
		}

		try
		{
			JSONObject jsonObject = new JSONObject(a);
			JSONArray lineItems = jsonObject.getJSONArray("contacts");

			for (Object o : lineItems)
			{
				JSONObject jsonLineItem = (JSONObject) o;

				usernames.add(jsonLineItem.getString("id"));
				blocked.put(jsonLineItem.getString("id"), jsonLineItem.getBoolean("blocked"));
			}

			contacts = new GetProfilePacket(api).getUsers(usernames);
			if (contacts != null)
			{
				for (User user : contacts)
				{
					((UserImpl) user).setContact(true);
					((UserImpl) user).setBlocked(blocked.get(user.getUsername()));
					api.updateContact(user);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}