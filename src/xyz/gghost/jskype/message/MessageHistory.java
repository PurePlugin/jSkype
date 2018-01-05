package xyz.gghost.jskype.message;

import java.util.ArrayList;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;
import xyz.gghost.jskype.model.User;

public class MessageHistory
{
	private String longId;
	private SkypeAPI api;
	private String nextUrl = null;
	@Getter
	private ArrayList<Message> knownMessages = new ArrayList<Message>();

	public MessageHistory(String longId, SkypeAPI api)
	{
		this.longId = longId;
		this.api = api;

		loadMoreMessages();
	}

	public void loadMoreMessages()
	{
		Group convo = api.getClient().getGroup(longId.contains("@") ? longId.split(":")[1].split("@")[0] : longId.split("8:")[1]).get();

		String nextUrl = this.nextUrl;

		if (nextUrl == null)
			nextUrl = "https://client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages?startTime=0&pageSize=51&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread";

		PacketBuilder builder = new PacketBuilder(api);
		builder.setType(RequestType.GET);
		builder.setUrl(nextUrl);

		String data = builder.makeRequest();

		if (data == null)
			return;

		JSONObject json = new JSONObject(data);

		try
		{
			if (!json.getJSONObject("_metadata").isNull("syncState"))
				this.nextUrl = json.getJSONObject("_metadata").getString("syncState");
		}
		catch (Exception e)
		{
			// out of pages
		}

		JSONArray jsonArray = json.getJSONArray("messages");
		for (int i = 0; i < jsonArray.length(); i++)
		{
			JSONObject jsonMessage = jsonArray.getJSONObject(i);
			if (jsonMessage.getString("type").equals("Message"))
			{
				Message message = new Message(FormatUtils.decodeText(jsonMessage.getString("content")));
				User user;

				try
				{
					user = getUser(jsonMessage.getString("from").split("8:")[1], convo);
				}
				catch (Exception e)
				{
					continue;
				}

				String content = "";
				if (!jsonMessage.isNull("content"))
					content = FormatUtils.decodeText(jsonMessage.getString("content"));
				if (!jsonMessage.isNull("clientmessageid"))
					message.setId(jsonMessage.getString("clientmessageid"));
				if (!jsonMessage.isNull("skypeeditedid"))
				{
					content = FormatUtils.decodeText(content.replaceFirst("Edited previous message: ", "").split("<e_m")[0]);
					message.setId(jsonMessage.getString("skypeeditedid"));
					message.setEdited(true);
				}

				message.setSender(user);
				message.setTime(jsonMessage.getString("originalarrivaltime"));
				message.setUpdateUrl("https://db3-client-s.gateway.messenger.live.com/v1/users/ME/conversations/" + longId + "/messages");
				message.setMessage(content);
				knownMessages.add(message);
			}
		}
	}

	public int knownMessagesCount()
	{
		return knownMessages.size();
	}

	private User getUser(String username, Group chat)
	{
		User user = null;

		Optional<User> optional = api.getClient().getUser(username);

		if (optional.isPresent())
		{
			user = optional.get();
		}
		else
		{
			try
			{
				for (GroupUser users : chat.getClients())
				{
					if (users.getUser().getUsername().equals(username))
						user = users.getUser();
				}
			}
			catch (NullPointerException e)
			{
			}
		}

		if (user == null)
			user = api.getClient().getUser(username).get();

		return user;
	}
}