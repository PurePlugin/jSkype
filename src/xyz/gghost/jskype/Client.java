package xyz.gghost.jskype;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Data;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
import xyz.gghost.jskype.internal.auth.Auth;
import xyz.gghost.jskype.internal.impl.UserImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.PacketBuilderUploader;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.GetPendingContactsPacket;
import xyz.gghost.jskype.internal.packet.packets.GetProfilePacket;
import xyz.gghost.jskype.internal.packet.packets.GroupInfoPacket;
import xyz.gghost.jskype.internal.packet.packets.UserManagementPacket;
import xyz.gghost.jskype.internal.threads.ContactUpdater;
import xyz.gghost.jskype.internal.threads.ConvoUpdater;
import xyz.gghost.jskype.internal.threads.PendingContactEventThread;
import xyz.gghost.jskype.internal.threads.Ping;
import xyz.gghost.jskype.internal.threads.Poller;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.LocalAccount;
import xyz.gghost.jskype.model.LoginTokens;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

@Data
public class Client
{
	private final UUID uniqueId = UUID.randomUUID();

	private final List<User> contacts = new ArrayList<>();
	private final List<Group> groups = new ArrayList<>();
	private final Map<String, MessageHistory> chatHistory = new HashMap<>();
	private final LoginTokens loginTokens = new LoginTokens();

	private final SkypeAPI api;
	private final String username;
	private final String password;

	private Visibility visibility = Visibility.ONLINE;

	private boolean relog = false;

	private boolean allowLogging = true;
	private boolean loaded;
	private Poller poller;
	private Thread contactUpdater;
	private Thread pinger;
	private ConvoUpdater convoUpdater;
	private PendingContactEventThread pendingContactThread;

	public void login() throws Exception
	{
		new Auth().login(api);

		relog = true;

		pinger = new Ping(api);
		pinger.start();

		contactUpdater = new ContactUpdater(api);
		contactUpdater.start();

		pendingContactThread = new PendingContactEventThread(api);
		pendingContactThread.start();

		poller = new Poller(api);
		poller.start();

		convoUpdater = new ConvoUpdater(api);
		convoUpdater.start();
	}

	public void logout() throws Exception
	{
		poller.stopThreads();
		pinger.interrupt();
		contactUpdater.interrupt();
		poller.interrupt();
		convoUpdater.interrupt();
		pendingContactThread.interrupt();
	}

	public LocalAccount getAccount()
	{
		return new GetProfilePacket(api).getMe();
	}

	public void setProfilePicture(String file)
	{
		try
		{
			PacketBuilderUploader uploader = new PacketBuilderUploader(api);
			uploader.setSendLoginHeaders(true);
			uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
			uploader.setType(RequestType.PUT);
			uploader.makeRequest(new FileInputStream(file));
		}
		catch (Exception e)
		{
			api.getLogger().severe("Unable to set profile picture, " + e.getLocalizedMessage());
		}
	}

	public Optional<User> getUser(String username)
	{
		return contacts.stream().filter(contact -> contact.getUsername().equalsIgnoreCase(username)).findFirst();
	}

	public User getSimpleUser(String username)
	{
		if (getUser(username).isPresent())
			return getUser(username).get();

		return new UserImpl(username);
	}

	public User getUserByUsername(String username)
	{
		if (getUser(username).isPresent())
			return getUser(username).get();

		return new GetProfilePacket(api).getUser(username);
	}

	public Optional<Group> getGroup(String id)
	{
		return groups.stream().filter(group -> group.getId().equals(id)).findFirst();
	}

	public void sendContactRequest(String username)
	{
		new GetPendingContactsPacket(api).sendRequest(username);
	}

	public void sendContactRequest(String username, String greeting)
	{
		new GetPendingContactsPacket(api).sendRequest(username, greeting);
	}

	public void acceptContactRequest(String username)
	{
		new GetPendingContactsPacket(api).acceptRequest(username);
	}

	public ArrayList<User> getContactRequests() throws BadResponseException, NoPendingContactsException
	{
		return new GetPendingContactsPacket(api).getPending();
	}

	public Client setVisibility(Visibility visibility)
	{
		PacketBuilder packet = new PacketBuilder(api);
		packet.setData("{\"status\":\"" + Character.toString(visibility.name().charAt(0)).toUpperCase() + (visibility.name().substring(1).toLowerCase()) + "\"}");
		packet.setType(RequestType.PUT);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService");
		packet.makeRequest();
		return this;
	}

	public List<User> searchSkypeDB(String keywords)
	{
		try
		{
			PacketBuilder packet = new PacketBuilder(api);
			packet.setType(RequestType.GET);
			packet.setUrl("https://api.skype.com/search/users/any?keyWord=" + URLEncoder.encode(keywords, "UTF-8") + "&contactTypes[]=skype");
			String data = packet.makeRequest();

			if (data == null)
				return null;

			JSONArray jsonArray = new JSONArray(data);
			ArrayList<String> usernames = new ArrayList<String>();

			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject contact = jsonArray.getJSONObject(i);
				usernames.add(contact.getJSONObject("ContactCards").getJSONObject("Skype").getString("SkypeName"));
			}

			return new GetProfilePacket(api).getUsers(usernames);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a new group
	 */
	public Group createNewGroup()
	{
		JSONObject json = new JSONObject().put("members", new JSONArray().put(new JSONObject().put("id", "8:" + username).put("role", "Admin")));
		PacketBuilder buildGroup = new PacketBuilder(api);
		buildGroup.setData(json.toString());
		buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
		buildGroup.setType(RequestType.POST);
		buildGroup.makeRequest();
		String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
		PacketBuilder pb = new PacketBuilder(api);
		pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + idLong + "/properties?name=topic");
		pb.setType(RequestType.PUT);
		pb.setData("{\"topic\":\"New Group!\"}");
		pb.makeRequest();
		return new GroupInfoPacket(api).getGroup(idLong);
	}

	/**
	 * Attempts to make a group with users... might not work
	 */
	public Group createNewGroupWithUsers(ArrayList<String> users)
	{
		JSONArray members = new JSONArray().put(new JSONObject().put("id", "8:" + username).put("role", "Admin"));

		for (String user : users)
			members.put(new JSONObject().put("id", "8:" + user).put("role", "User"));

		JSONObject json = new JSONObject().put("members", members);
		PacketBuilder buildGroup = new PacketBuilder(api);
		buildGroup.setData(json.toString());
		buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
		buildGroup.setType(RequestType.POST);
		buildGroup.makeRequest();
		String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
		PacketBuilder pb = new PacketBuilder(api);
		pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + idLong + "/properties?name=topic");
		pb.setType(RequestType.PUT);
		pb.setData("{\"topic\":\"New Group!\"}");
		pb.makeRequest();
		return new GroupInfoPacket(api).getGroup(idLong);
	}

	/**
	 * Join a group from a skype invite link
	 */
	public void joinInviteLink(String url)
	{
		PacketBuilder getId = new PacketBuilder(api);
		System.out.println(url);
		getId.setUrl("https://join.skype.com/api/v1/meetings/" + url.split(".com/")[1]);
		getId.setType(RequestType.GET);
		String a = getId.makeRequest();

		if (a == null)
			return;

		PacketBuilder getLongId = new PacketBuilder(api);
		getLongId.setUrl("https://api.scheduler.skype.com/conversation/" + new JSONObject(a).get("longId"));
		getLongId.setType(RequestType.GET);
		String b = getLongId.makeRequest();

		if (b == null)
			return;

		reJoinGroup(new JSONObject(b).getString("ThreadId"));

	}

	/**
	 * Kicked and the group is still joionable? Use this method!
	 */
	public void reJoinGroup(Group group)
	{
		reJoinGroup(group.getLongId());
	}

	/**
	 * Join a joinable group from it's long id
	 * 
	 * @param longId
	 */
	public void reJoinGroup(String longId)
	{
		new UserManagementPacket(api).addUser(longId, username);
	}
}