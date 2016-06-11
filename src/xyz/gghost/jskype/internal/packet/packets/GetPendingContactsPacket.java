package xyz.gghost.jskype.internal.packet.packets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.model.User;

@AllArgsConstructor
public class GetPendingContactsPacket
{
	private final SkypeAPI api;

	public ArrayList<User> getPending() throws NoPendingContactsException, BadResponseException
	{
		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.GET);
		packet.setUrl("https://api.skype.com/users/self/contacts/auth-request");
		String a = packet.makeRequest();
		if (a == null)
		{
			// api.getLogger().warning("Failed to get contact requests");
			throw new BadResponseException();
		}
		else
		{
			if (a.equals(""))
			{
				// api.getLogger().info("No contact requests available");
				throw new NoPendingContactsException();
			}
			ArrayList<User> pending = new ArrayList<User>();
			JSONArray json = new JSONArray(a);
			for (int i = 0; i < json.length(); i++)
			{
				JSONObject object = json.getJSONObject(i);
				pending.add(new GetProfilePacket(api).getUser(object.getString("sender")));
			}
			return pending;
		}

	}

	public void acceptRequest(String user)
	{
		boolean canLog = api.getClient().isAllowLogging();
		api.getClient().setAllowLogging(false);
		String URL = "https://api.skype.com/users/self/contacts/auth-request/" + user + "/accept";
		PacketBuilder packet = new PacketBuilder(api);
		packet.setData("");
		packet.setUrl(URL);
		packet.setIsForm(true);
		packet.setType(RequestType.PUT);
		packet.makeRequest();

		PacketBuilder accept = new PacketBuilder(api);
		accept.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/8:" + user);
		accept.setIsForm(true);
		accept.setType(RequestType.PUT);
		accept.makeRequest();

		String URL2 = "https://client-s.gateway.messenger.live.com/v1/users/ME/contacts/";
		PacketBuilder packet2 = new PacketBuilder(api);

		// TODO: Find a replacement for json.org that supports json building
		String data = "{\"contacts\": [";
		boolean first = true;
		for (User usr : api.getClient().getContacts())
		{
			data = data + (!first ? "," : "");
			data = data + "{\"id\": \"" + usr.getUsername() + "\"}";
			first = false;
		}
		data = data + (!first ? "," : "");
		data = data + "{\"id\": \"" + user + "\"}";
		data = data + "]}";
		packet2.setData(data);
		// end of json hackky code
		packet2.setUrl(URL2);
		packet2.setIsForm(true);
		packet2.setType(RequestType.POST);
		packet2.makeRequest();
		api.getClient().setAllowLogging(canLog);
	}

	public void acceptRequest(User usr)
	{
		acceptRequest(usr.getUsername());
	}

	public void sendRequest(String user)
	{
		sendRequest(user, "Hi, I'd like to add you as a contact. -Sent from jSkype");
	}

	public void sendRequest(String user, String message)
	{
		try
		{
			String URL = "https://api.skype.com/users/self/contacts/auth-request/" + user;
			PacketBuilder packet = new PacketBuilder(api);
			packet.setData("greeting=" + URLEncoder.encode(message, "UTF-8"));
			packet.setUrl(URL);
			packet.setIsForm(true);
			packet.setType(RequestType.PUT);
			packet.makeRequest();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
