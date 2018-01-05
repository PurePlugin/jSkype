package xyz.gghost.jskype;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

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
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;

public class Client {
	private final UUID uniqueId = UUID.randomUUID();

	private final Auth auth = new Auth(this);

	private final List<User> contacts = new ArrayList<>();
	private final List<Group> groups = new ArrayList<>();
	private final Map<String, MessageHistory> chatHistory = new HashMap<>();

	private final SkypeAPI api;
	private final String username;
	private final String password;

	private Visibility visibility = Visibility.ONLINE;

	private Poller poller;
	private Thread contactUpdater;
	private Thread pinger;
	private ConvoUpdater convoUpdater;
	private PendingContactEventThread pendingContactThread;

	public Client(SkypeAPI api, String username, String password) {
		this.api = api;
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public SkypeAPI getApi() {
		return api;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public String getUsername() {
		return username;
	}

	public List<User> getContacts() {
		return contacts;
	}

	public void login() throws Exception {
		auth.login();

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

	public void logout() throws Exception {
		pinger.interrupt();
		contactUpdater.interrupt();
		poller.interrupt();
		convoUpdater.interrupt();
		pendingContactThread.interrupt();
	}

	public LocalAccount getAccount() {
		return new GetProfilePacket(api).getMe();
	}

	public void setProfilePicture(String file) throws Exception {
		PacketBuilderUploader uploader = new PacketBuilderUploader(api);
		uploader.setSendLoginHeaders(true);
		uploader.setUrl("https://api.skype.com/users/" + username + "/profile/avatar");
		uploader.setType(RequestType.PUT);
		uploader.makeRequest(new FileInputStream(file));
	}

	public User getUser(String username) {
		User optional = null; //contacts.stream().filter(> ).findFirst();

		for (User contact : contacts) {
			if (contact.getUsername().equalsIgnoreCase(username)) {
				optional = contact;
			}
		}

		if (optional == null)
			return new UserImpl(username);

		return optional;
	}

	public User getUserByUsername(String username) {
		if (getUser(username) != null)
			return getUser(username);

		return new GetProfilePacket(api).getUser(username);
	}

	public Group getGroup(String id) {
		Group g = null;

		for (Group group : groups) {
			if (group.getId().equals(id)) {
				g = group;
			}
		}

		return g;
	}

	public void sendContactRequest(String username) {
		new GetPendingContactsPacket(api).sendRequest(username);
	}

	public void sendContactRequest(String username, String greeting) {
		new GetPendingContactsPacket(api).sendRequest(username, greeting);
	}

	public void acceptContactRequest(String username) {
		new GetPendingContactsPacket(api).acceptRequest(username);
	}

	public List<User> getContactRequests() {
		return new GetPendingContactsPacket(api).getPenidngContacts();
	}

	public Client setVisibility(Visibility visibility) {
		PacketBuilder packet = new PacketBuilder(api);
		packet.setData("{\"status\":\"" + Character.toString(visibility.name().charAt(0)).toUpperCase() + (visibility.name().substring(1).toLowerCase()) + "\"}");
		packet.setType(RequestType.PUT);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/presenceDocs/messagingService");
		packet.makeRequest();
		return this;
	}

	public List<User> searchSkypeDB(String keywords) throws Exception {
		PacketBuilder packet = new PacketBuilder(api);
		packet.setType(RequestType.GET);
		packet.setUrl("https://api.skype.com/search/users/any?keyWord=" + URLEncoder.encode(keywords, "UTF-8") + "&contactTypes[]=skype");

		String data = packet.makeRequest();

		if (data == null)
			return null;

		JSONArray jsonArray = new JSONArray(data);
		ArrayList<String> usernames = new ArrayList<String>();

		for (int i = 0; i < jsonArray.length(); i++)
			usernames.add(jsonArray.getJSONObject(i).getJSONObject("ContactCards").getJSONObject("Skype").getString("SkypeName"));

		return new GetProfilePacket(api).getUsers(usernames);
	}

	public Group createNewGroup() {
		try {
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
		} catch (Exception e) {
			return null;
		}
	}

	public Group createNewGroupWithUsers(String... users) {
		try {
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
		} catch (Exception e) {
			return null;
		}
	}

	public void joinInviteLink(String url) {
		PacketBuilder getId = new PacketBuilder(api);
		getId.setUrl("https://join.skype.com/api/v1/meetings/" + url.split(".com/")[1]);
		getId.setType(RequestType.GET);
		String a = getId.makeRequest();

		if (a == null)
			return;
		
		try {
			PacketBuilder getLongId = new PacketBuilder(api);
			getLongId.setUrl("https://api.scheduler.skype.com/conversation/" + new JSONObject(a).get("longId"));
			getLongId.setType(RequestType.GET);
			String b = getLongId.makeRequest();

			if (b == null)
				return;

			reJoinGroup(new JSONObject(b).getString("ThreadId"));
		} catch (Exception e) {

		}
	}

	public void reJoinGroup(String longId) {
		new UserManagementPacket(api).addUser(longId, username);
	}

	public Auth getAuth() {
		return auth;
	}

	public Map<String, MessageHistory> getChatHistory() {
		return chatHistory;
	}
}
