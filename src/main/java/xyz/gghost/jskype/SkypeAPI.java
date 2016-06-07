package xyz.gghost.jskype;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.event.EventBus;
import xyz.gghost.jskype.exception.BadResponseException;
import xyz.gghost.jskype.exception.NoPendingContactsException;
import xyz.gghost.jskype.internal.auth.Auth;
import xyz.gghost.jskype.internal.auth.LoginTokens;
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
import xyz.gghost.jskype.user.LocalAccount;
import xyz.gghost.jskype.user.OnlineStatus;
import xyz.gghost.jskype.user.User;

public class SkypeAPI
{
	@Getter
	private List<Group> groups = new ArrayList<Group>();
	@Getter
	private List<User> contacts = new ArrayList<User>();
	@Getter
	private LoginTokens loginTokens = new LoginTokens();
	@Getter
	private EventBus eventBus = new EventBus();
	@Getter
	private HashMap<String, MessageHistory> a = new HashMap<String, MessageHistory>();
	@Setter
	@Getter
	private boolean allowLogging = true;
	@Getter
	private String username;
	@Getter
	UUID uuid = UUID.randomUUID();
	@Getter
	private String password;
	@Getter
	@Setter
	private boolean loaded;
	private OnlineStatus s = OnlineStatus.ONLINE;
	private Poller poller;
	private Thread contactUpdater;
	private Thread pinger;
	private ConvoUpdater convoUpdater;
	private PendingContactEventThread pendingContactThread;
	@Getter
	@Setter
	private boolean debugMode = false;
	@Getter
	@Setter
	private boolean reloggin = false;

	public SkypeAPI(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	/**
	 * Login to skype
	 * 
	 * @return "Builder"
	 * @throws Exception
	 *             Failed to login/badusernamepassword exception
	 */
	public SkypeAPI login() throws Exception
	{
		new Auth().login(this);
		reloggin = true;
		init();
		updateStatus(OnlineStatus.ONLINE);
		return this;
	}

	/**
	 * Make shift logger... rly bad. Just System.out.println if
	 * SkypeAPI#allowLogging is true
	 */
	public void log(String msg)
	{
		if (allowLogging)
			System.out.println(msg);
	}

	/**
	 * Start threads
	 */
	private void init()
	{
		pinger = new Ping(this);
		pinger.start();
		contactUpdater = new ContactUpdater(this);
		contactUpdater.start();
		pendingContactThread = new PendingContactEventThread(this);
		pendingContactThread.start();
		poller = new Poller(this);
		poller.start();
		convoUpdater = new ConvoUpdater(this);
		convoUpdater.start();
	}

	/**
	 * Attempt to stop all skype threads
	 * 
	 * @return
	 */
	public void stop()
	{
		poller.stopThreads();
		pinger.stop();
		contactUpdater.stop();
		poller.stop();
		convoUpdater.stop();
		pendingContactThread.stop();
	}

	/**
	 * Get your current online status
	 * 
	 * @return
	 */
	public OnlineStatus getOnlineStatus()
	{
		return s;
	}

	/**
	 * Do not use
	 * 
	 * @param status
	 */
	public void s(OnlineStatus status)
	{
		s = status;
	}

	/**
	 * This method will get as much data as possible about a user without
	 * contacting to skype
	 */
	public User getSimpleUser(String username)
	{
		User user = getContact(username);
		return user != null ? user : new UserImpl(username);
	}

	/**
	 * get user by username
	 */
	public User getUserByUsername(String username)
	{
		User user = getContact(username);
		return user != null ? user : new GetProfilePacket(this).getUser(username);
	}

	/**
	 * Get contact by username
	 */
	public User getContact(String username)
	{
		for (User contact : getContacts())
		{
			if (contact.getUsername().equalsIgnoreCase(username))
				return contact;
		}
		return null;
	}

	/**
	 * Get the group by shorter id
	 * 
	 * @param shortId
	 * @return null if the api hasn't loaded or the group is unknown to us
	 */
	public Group getGroupById(String shortId)
	{
		for (Group group : groups)
		{
			if (group.getId().equals(shortId))
				return group;
		}
		return null;
	}

	/**
	 * Set your online status
	 * 
	 * @param a
	 */
	public void updateStatus(OnlineStatus a)
	{
		PacketBuilder packet = new PacketBuilder(this);
		packet.setData("{\"status\":\"" + Character.toString(a.name().charAt(0)).toUpperCase() + (a.name().substring(1).toLowerCase()) + "\"}");
		packet.setType(RequestType.PUT);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService");
		packet.makeRequest();
	}

	/**
	 * Update a group object in the recent array
	 */
	public void updateGroup(Group group)
	{
		Group oldGroup = null;
		for (Group groupA : groups)
		{
			if (groupA.getId().equals(group.getId()))
				oldGroup = groupA;
		}
		if (oldGroup != null)
			getGroups().remove(oldGroup);
		getGroups().add(group);
	}

	/**
	 * Update a User object in the contacts array
	 */
	public void updateContact(User newUser)
	{
		User oldUser = null;
		for (User user : getContacts())
		{
			if (user.getUsername().equals(newUser.getUsername()))
				oldUser = user;
		}
		if (oldUser != null)
			getContacts().remove(oldUser);
		getContacts().add(newUser);
	}

	/**
	 * Attempts to send a contact request
	 */
	public void sendContactRequest(String username)
	{
		new GetPendingContactsPacket(this).sendRequest(username);
	}

	/**
	 * Attempts to send a contact request with a custom greeting
	 */
	public void sendContactRequest(String username, String greeting)
	{
		new GetPendingContactsPacket(this).sendRequest(username, greeting);
	}

	/**
	 * Skype db lookup / search
	 */
	public List<User> searchSkypeDB(String keywords)
	{
		PacketBuilder packet = new PacketBuilder(this);
		packet.setType(RequestType.GET);
		packet.setUrl("https://api.skype.com/search/users/any?keyWord=" + URLEncoder.encode(keywords) + "&contactTypes[]=skype");
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

		return new GetProfilePacket(this).getUsers(usernames);
	}

	/**
	 * Get user info about the account
	 */
	public LocalAccount getAccountInfo()
	{
		return new GetProfilePacket(this).getMe();
	}

	/**
	 * Change profile picture
	 */
	public void changePictureFromFile(String url)
	{
		try
		{
			// No point of making a new class just for this one small method
			PacketBuilderUploader uploader = new PacketBuilderUploader(this);
			uploader.setSendLoginHeaders(true);
			uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
			uploader.setType(RequestType.PUT);
			uploader.makeRequest(new FileInputStream(url));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Update your profile picture from a url
	 */
	public void changePictureFromUrl(String url)
	{
		try
		{
			// No point of making a new class just for this one small method
			PacketBuilderUploader uploader = new PacketBuilderUploader(this);
			uploader.setSendLoginHeaders(true);
			uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
			uploader.setType(RequestType.PUT);
			URL image = new URL(url);
			InputStream data = image.openStream();
			uploader.makeRequest(data);
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
	}

	/**
	 * Create a new group
	 */
	public Group createNewGroup()
	{
		JSONObject json = new JSONObject().put("members", new JSONArray().put(new JSONObject().put("id", "8:" + getUsername()).put("role", "Admin")));
		PacketBuilder buildGroup = new PacketBuilder(this);
		buildGroup.setData(json.toString());
		buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
		buildGroup.setType(RequestType.POST);
		buildGroup.makeRequest();
		String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
		PacketBuilder pb = new PacketBuilder(this);
		pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + idLong + "/properties?name=topic");
		pb.setType(RequestType.PUT);
		pb.setData("{\"topic\":\"New Group!\"}");
		pb.makeRequest();
		return new GroupInfoPacket(this).getGroup(idLong);
	}

	/**
	 * Attempts to make a group with users... might not work
	 */
	public Group createNewGroupWithUsers(ArrayList<String> users)
	{
		JSONArray members = new JSONArray().put(new JSONObject().put("id", "8:" + getUsername()).put("role", "Admin"));

		for (String user : users)
			members.put(new JSONObject().put("id", "8:" + user).put("role", "User"));

		JSONObject json = new JSONObject().put("members", members);
		PacketBuilder buildGroup = new PacketBuilder(this);
		buildGroup.setData(json.toString());
		buildGroup.setUrl("https://client-s.gateway.messenger.live.com/v1/threads");
		buildGroup.setType(RequestType.POST);
		buildGroup.makeRequest();
		String idLong = buildGroup.getCon().getHeaderFields().get("Location").get(0).split("/threads/")[1];
		PacketBuilder pb = new PacketBuilder(this);
		pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + idLong + "/properties?name=topic");
		pb.setType(RequestType.PUT);
		pb.setData("{\"topic\":\"New Group!\"}");
		pb.makeRequest();
		return new GroupInfoPacket(this).getGroup(idLong);
	}

	/**
	 * Join a group from a skype invite link
	 */
	public void joinInviteLink(String url)
	{
		PacketBuilder getId = new PacketBuilder(this);
		System.out.println(url);
		getId.setUrl("https://join.skype.com/api/v1/meetings/" + url.split(".com/")[1]);
		getId.setType(RequestType.GET);
		String a = getId.makeRequest();
		if (a == null)
			return;
		PacketBuilder getLongId = new PacketBuilder(this);
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
		new UserManagementPacket(this).addUser(longId, getUsername());
	}

	/**
	 * Get contact requests
	 * 
	 * @return
	 * @throws BadResponseException
	 * @throws NoPendingContactsException
	 */
	public ArrayList<User> getContactRequests() throws BadResponseException, NoPendingContactsException
	{
		return new GetPendingContactsPacket(this).getPending();
	}

	public void acceptContactRequest(String username)
	{
		new GetPendingContactsPacket(this).acceptRequest(username);
	}

}
