package xyz.gghost.jskype.internal.threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.ChatPictureChangedEvent;
import xyz.gghost.jskype.events.TopicChangedEvent;
import xyz.gghost.jskype.events.UserChatEvent;
import xyz.gghost.jskype.events.UserImagePingEvent;
import xyz.gghost.jskype.events.UserJoinEvent;
import xyz.gghost.jskype.events.UserLeaveEvent;
import xyz.gghost.jskype.events.UserNewMovieAdsPingEvent;
import xyz.gghost.jskype.events.UserOtherFilesPingEvent;
import xyz.gghost.jskype.events.UserRoleChangedEvent;
import xyz.gghost.jskype.events.UserStatusChangedEvent;
import xyz.gghost.jskype.events.UserTypingEvent;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.GetProfilePacket;
import xyz.gghost.jskype.internal.packet.packets.GroupInfoPacket;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

@Data
@EqualsAndHashCode(callSuper = false)
public class Poller extends Thread
{
	private final List<Thread> threads = new ArrayList<Thread>();
	private final SkypeAPI api;

	private boolean breakl = false;

	@Override
	public void run()
	{
		while (this.isAlive() || breakl)
		{
			Thread a = new Thread()
			{
				@Override
				public void run()
				{
					poll();
				}
			};
			threads.add(a);
			a.start();

			try
			{
				sleep(250);
			}
			catch (Exception e)
			{
			}
		}
	}

	public void stopThreads()
	{
		breakl = true;
		threads.forEach(thread -> thread.interrupt());
	}

	private void poll()
	{
		PacketBuilder poll = new PacketBuilder(api);
		poll.setType(RequestType.POST);
		poll.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/SELF/subscriptions/0/poll");
		poll.setData(" ");
		String data = poll.makeRequest();

		if (data == null || data.equals("") || data.equals("{}"))
			return;

		JSONObject messagesAsJson = new JSONObject(data);
		JSONArray json = messagesAsJson.getJSONArray("eventMessages");

		for (int i = 0; i < json.length(); i++)
		{
			JSONObject object = json.getJSONObject(i);
			try
			{
				if (!(object.isNull("type") && object.isNull("resourceType")))
				{
					Group chat = null;

					if (object.getString("resourceLink").contains("conversations/19:") || object.getString("resourceLink").contains("8:"))
					{
						if (!object.getString("resourceLink").contains("8:"))
						{
							String idShort = object.getString("resourceLink").split("conversations/19:")[1].split("@thread")[0];
							addGroupToRecent(object);
							chat = api.getClient().getGroup(idShort).get();
							// Old skype for web bug that affects some users
						}
						else if (!object.getString("resourceLink").contains("@"))
						{
							chat = new ContactGroupImpl(api, "8:" + object.getString("resourceLink").split("/8:")[1].split("/")[0]);
						}
					}
					else
					{
						// api.log("Non-group data received from skype. This is
						// ignorable.");
					}

					if (object.getString("resourceType").equals("UserPresence") && object.getJSONObject("resource").getString("id").equals("messagingService"))
					{
						Visibility status = Visibility.OFFLINE;
						if (object.getJSONObject("resource").getString("status").equals("Online"))
							status = Visibility.ONLINE;
						if (object.getJSONObject("resource").getString("status").equals("Busy"))
							status = Visibility.BUSY;
						User user;
						try
						{
							user = api.getClient().getUserByUsername(object.getJSONObject("resource").getString("selfLink").split("/8:")[1].split("/")[0]);
							if (!user.getOnlineStatus().name().equals(status.name()))
								api.getEventBus().post(new UserStatusChangedEvent(user, status));

							UserImpl userImpl = (UserImpl) api.getClient().getUserByUsername(object.getJSONObject("resource").getString("selfLink").split("/8:")[1].split("/")[0]);
							userImpl.setOnlineStatus(status);

							Iterator<User> iterator = api.getClient().getContacts().iterator();

							while (iterator.hasNext())
							{
								User u = iterator.next();

								if (u.getUsername().equals(userImpl.getUsername()))
								{
									iterator.remove();
									api.getClient().getContacts().add(userImpl);
									break;
								}
							}
						}
						catch (Exception e)
						{
							// We came online
							api.getClient().setVisibility(Visibility.ONLINE);
						}
					}

					// thread update
					if (object.getString("resourceType").equals("ThreadUpdate"))
						hackyThreadUpdate(object);

					// resource json
					JSONObject resource = object.getJSONObject("resource");

					// Get topic update
					if (!resource.isNull("messagetype") && resource.getString("messagetype").equals("ThreadActivity/TopicUpdate"))
					{
						String topic = "";

						try
						{
							topic = resource.getString("content").split("<value>")[1].split("<\\/value>")[0];
							topic = FormatUtils.decodeText(topic);
						}
						catch (Exception e)
						{
							for (GroupUser user : chat.getClients())
								topic = topic + ", " + user.getUser().getUsername();

							topic = topic.replaceFirst(", ", "");
						}

						String username = resource.getString("content").split("<initiator>8:")[1].split("<\\/initiator>")[0];
						String oldTopic = api.getClient().getGroup(chat.getId()).get().getTopic();

						User user = getUser(username, chat);

						TopicChangedEvent event = new TopicChangedEvent(chat, user, topic, oldTopic);

						api.getEventBus().post(event);

						((GroupImpl) chat).setTopic(event.isCancelled() ? oldTopic : topic);
					}

					if (!resource.isNull("messagetype") && resource.getString("messagetype").equals("ThreadActivity/PictureUpdate"))
					{
						String content = resource.getString("content");
						User user = api.getClient().getUserByUsername(content.split("<initiator>8:")[1].split("<")[0]);
						String newUrl = content.split("<value>URL@")[1].split("<")[0];
						api.getEventBus().post(new ChatPictureChangedEvent(chat, user, newUrl));
					}

					// Get Typing
					if (!resource.isNull("messagetype") && resource.getString("messagetype").equals("Control/Typing"))
					{
						User from = getUser(resource.getString("from").split("8:")[1], chat);
						api.getEventBus().post(new UserTypingEvent(chat, from));
					}

					// modern pings video
					if (!resource.isNull("messagetype") && (resource.getString("messagetype").equals("RichText/Media_FlikMsg")))
					{
						String id = resource.getString("content").split("om/pes/v1/items/")[1].split("\\\"")[0];
						User user = getUser(resource.getString("from").split("8:")[1], chat);
						api.getEventBus().post(new UserNewMovieAdsPingEvent(user, chat, id));
					}

					// Get message
					if (!resource.isNull("messagetype") && (resource.getString("messagetype").equals("RichText") || resource.getString("messagetype").equals("Text")))
					{

						Message message = new Message();
						User user = getUser(resource.getString("from").split("8:")[1], chat);

						String content = "";

						if (!resource.isNull("content"))
							content = FormatUtils.decodeText(resource.getString("content"));

						if (!resource.isNull("clientmessageid"))
							message.setId(resource.getString("clientmessageid"));

						if (!resource.isNull("skypeeditedid"))
						{
							content = content.replaceFirst("Edited previous message: ", "").split("<e_m")[0];
							message.setId(resource.getString("skypeeditedid"));
							message.setEdited(true);
						}

						message.setSender(user);
						message.setTime(resource.getString("originalarrivaltime"));
						message.setUpdateUrl(object.getString("resourceLink").split("/messages/")[0] + "/messages");
						message.setMessage(content);

						api.getEventBus().post(new UserChatEvent(chat, user, message));

					}

					// pings
					if (!resource.isNull("messagetype") && resource.getString("messagetype").startsWith("RichText/"))
					{
						User user = getUser(resource.getString("from").split("8:")[1], chat);
						String content = FormatUtils.decodeText(resource.getString("content"));

						if (content.contains("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?"))
						{
							String id = content.split("To view this shared photo, go to: <a href=\"https://api.asm.skype.com/s/i?")[1].split("\">")[0];
							String url = ("https://api.asm.skype.com/v1/objects/" + id + "/views/imgo").replace("objects/?", "objects/");
							api.getEventBus().post(new UserImagePingEvent(chat, user, url));
							return;
						}
						if (content.contains("<files alt=\"") && content.contains("<file size="))
						{
							api.getEventBus().post(new UserOtherFilesPingEvent(chat, user));
							return;
						}
					}
				}
			}
			catch (Exception e)
			{
				api.getLogger().severe("Failed to process data from skype.\nMessage: " + object + "Data: " + data + "\nError: " + e.getMessage());
				api.getLogger().severe("Is this a new convo?\nWait a few seconds!");
				e.printStackTrace();
			}
		}
	}

	public void hackyThreadUpdate(JSONObject object)
	{
		try
		{
			Group oldGroup = null;

			ArrayList<String> oldUsers2 = new ArrayList<String>();
			ArrayList<String> oldUsers = new ArrayList<String>();
			ArrayList<String> newUsers = new ArrayList<String>();

			String shortId = object.getString("resourceLink").split("19:")[1].split("@")[0];

			for (Group groups : api.getClient().getGroups())
			{
				if (groups.getId().equals(shortId))
				{
					for (GroupUser usr : groups.getClients())
					{
						oldUsers.add(usr.getUser().getUsername().toLowerCase());
						oldUsers2.add(usr.getUser().getUsername().toLowerCase());
					}
					oldGroup = groups;
				}
			}

			if (oldGroup != null)
			{
				JSONObject resource = object.getJSONObject("resource");

				String topic = resource.getJSONObject("properties").isNull("properties") ? "" : resource.getJSONObject("properties").getString("properties");

				if ((api.getClient().getGroup(shortId).get() != null))
					topic = api.getClient().getGroup(shortId).get().getTopic();

				String picture = resource.getJSONObject("properties").isNull("picture") ? "" : resource.getJSONObject("properties").getString("picture");

				if ((api.getClient().getGroup(shortId).get() != null) && (!api.getClient().getGroup(shortId).get().getPictureUrl().equals(picture)))
					picture = api.getClient().getGroup(shortId).get().getPictureUrl();

				GroupImpl group = new GroupImpl(api, "19:" + object.getString("resourceLink").split("19:")[1].split("@")[0] + "@thread.skype");

				group.setPictureUrl(picture);
				group.setTopic(topic);

				for (int ii = 0; ii < object.getJSONObject("resource").getJSONArray("members").length(); ii++)
				{
					JSONObject user = object.getJSONObject("resource").getJSONArray("members").getJSONObject(ii);

					// add username
					newUsers.add(user.getString("id").split("8:")[1]);

					try
					{

						GroupUser.Role role = GroupUser.Role.USER;

						// get user without searching skypes database
						User ussr = api.getClient().getSimpleUser(user.getString("id").split("8:")[1]);

						if (!user.getString("role").equals("User"))
							role = GroupUser.Role.MASTER;

						if (oldUsers.contains(ussr.getUsername()))
							for (GroupUser users : oldGroup.getClients())
								if (users.getUser().getUsername().equals(ussr.getUsername()))
									if (role != users.role)
										api.getEventBus().post(new UserRoleChangedEvent(oldGroup, users.getUser(), role));

						GroupUser gu = new GroupUser(ussr, role, group);
						group.getClients().add(gu);

					}
					catch (Exception ignored)
					{
					}
				}

				oldUsers.removeAll(newUsers);
				newUsers.removeAll(oldUsers2);

				for (String old : oldUsers)
					api.getEventBus().post(new UserLeaveEvent(group, new GetProfilePacket(api).getUser(old)));

				for (String news : newUsers)
					api.getEventBus().post(new UserJoinEvent(group, new GetProfilePacket(api).getUser(news)));

				Iterator<Group> iterator = api.getClient().getGroups().iterator();

				while (iterator.hasNext())
				{
					Group g = iterator.next();

					if (g.getId().equals(group.getId()))
					{
						iterator.remove();
						api.getClient().getGroups().add(group);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			api.getLogger().severe("#################################################");

			if (api.getClient().isAllowLogging())
				e.printStackTrace();

			api.getLogger().severe("#################################################");
			api.getLogger().severe("Failed to update group info. Have we just loaded?");

			try
			{
				api.getLogger().severe("Resource Link: " + object.getString("resourceLink"));
				api.getLogger().severe("Group ID: " + object.getString("resourceLink").split("19:")[1].split("@")[0] + "@thread.skype");
			}
			catch (Exception ea)
			{
			}
		}
	}

	// OLD METHODS
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
			catch (NullPointerException ignored)
			{
			}
		}

		// If failed to get user - get the users info by calling skypes api
		if (user == null)
			user = new GetProfilePacket(api).getUser(username);

		return user;
	}

	private void addGroupToRecent(JSONObject object)
	{
		if (object.getString("resourceLink").contains("endpoint"))
			return;
		try
		{
			String idLong = object.getString("resourceLink").split("conversations/")[1].split("/")[0];
			String idShort = object.getString("resourceLink").split("conversations/19:")[1].split("@thread")[0];

			for (Group group : api.getClient().getGroups())
				if (group.getId().equals(idShort))
					return;

			Group gNew = new GroupInfoPacket(api).getGroup(idLong);

			Iterator<Group> iterator = api.getClient().getGroups().iterator();

			while (iterator.hasNext())
			{
				Group g = iterator.next();

				if (g.getId().equals(gNew.getId()))
				{
					iterator.remove();
					api.getClient().getGroups().add(gNew);
					break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println(object.getString("resourceLink"));
			e.printStackTrace();
		}
	}
}