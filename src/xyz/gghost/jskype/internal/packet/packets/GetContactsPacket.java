package xyz.gghost.jskype.internal.packet.packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.Packet;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.model.User;

public class GetContactsPacket extends Packet {
	public GetContactsPacket(SkypeAPI api) {
		super(api);
	}

	@Override
	public void init() {
		List<User> contacts;
		HashMap<String, Boolean> blocked = new HashMap<String, Boolean>();
		ArrayList<String> usernames = new ArrayList<String>();

		PacketBuilder packet = new PacketBuilder(api);
		packet.setUrl("https://contacts.skype.com/contacts/v1/users/" + api.getClient().getUsername().toLowerCase() + "/contacts?filter=contacts");
		packet.setType(RequestType.OPTIONS);
		packet.setType(RequestType.GET);

		String data = packet.makeRequest();

		if (data == null) {
			api.getLogger().severe("Failed to request Skype for your contacts.");
			api.getLogger().severe("Code: " + packet.getCode() + "\nData: " + packet.getData() + "\nURL: " + packet.getUrl());

			if (api.getClient().getContacts().size() == 0) {
				User optional = api.getClient().getUser(api.getClient().getUsername());

				if (optional != null)
					api.getClient().getContacts().add(optional);
			}
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray lineItems = jsonObject.getJSONArray("contacts");

			for (int i = 0; i < lineItems.length(); i++) {
				Object o = lineItems.get(i);
				JSONObject jsonLineItem = (JSONObject) o;

				usernames.add(jsonLineItem.getString("id"));
				blocked.put(jsonLineItem.getString("id"), jsonLineItem.getBoolean("blocked"));
			}

			contacts = new GetProfilePacket(api).getUsers(usernames);

			if (contacts != null) {
				for (User user : contacts) {
					((UserImpl) user).setContact(true);
					((UserImpl) user).setBlocked(blocked.get(user.getUsername()));

					Iterator<User> iterator = api.getClient().getContacts().iterator();

					while (iterator.hasNext()) {
						User u = iterator.next();

						if (u.getUsername().equals(user.getUsername())) {
							iterator.remove();
							api.getClient().getContacts().add(user);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
