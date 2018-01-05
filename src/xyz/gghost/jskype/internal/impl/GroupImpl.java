package xyz.gghost.jskype.internal.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.internal.packet.packets.PingPrepPacket;
import xyz.gghost.jskype.internal.packet.packets.SendMessagePacket;
import xyz.gghost.jskype.internal.packet.packets.UserManagementPacket;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.message.MessageHistory;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;

public class GroupImpl implements Group {

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setPictureUrl(String picture) {
		this.pictureUrl = picture;
	}

	@Override
	public String getPictureUrl() {
		return pictureUrl;
	}

	private final SkypeAPI api;
	private final String id;

	private String topic = "";
	private String pictureUrl = "";

	private List<GroupUser> clients = new ArrayList<GroupUser>();

	public GroupImpl(SkypeAPI api, String id) {
		this.api = api;
		this.id = id;
	}

	@Override
	public MessageHistory getMessageHistory() {
		if (api.getClient().getChatHistory().containsKey(id))
			return api.getClient().getChatHistory().get(id);

		MessageHistory history = new MessageHistory(id, api);
		api.getClient().getChatHistory().put(id, history);
		return history;
	}

	@Override
	public void kick(String usr) {
		new UserManagementPacket(api).kickUser(getLongId(), usr);
	}

	@Override
	public void add(String usr) {
		new UserManagementPacket(api).addUser(getLongId(), usr);
	}

	@Override
	public void setAdmin(String usr, boolean admin) {
		if (admin) {
			new UserManagementPacket(api).promoteUser(getLongId(), usr);
		} else
			add(usr);
	}

	@Override
	public String getId() {
		try {
			return id.split("@")[0].split(":")[1];
		} catch (Exception e) {
			try {
				return id.split("8:")[1];
			} catch (Exception je) {
				return id;
			}
		}
	}

	@Override
	public String getLongId() {
		return id;
	}

	@Override
	public Message sendMessage(Message msg) {
		return new SendMessagePacket(api).sendMessage(id, msg);
	}

	@Override
	public Message sendMessage(String msg) {
		return new SendMessagePacket(api).sendMessage(id, new Message(msg));
	}

	@Override
	public Message sendImage(File url) {
		return new SendMessagePacket(api).sendPing(id, new Message(""), new PingPrepPacket(api).urlToId(url, id));
	}

	@Override
	public Message sendImage(URL url) {
		return new SendMessagePacket(api).sendPing(id, new Message(""), new PingPrepPacket(api).urlToId(url.toString(), id));
	}

	@Override
	public List<GroupUser> getClients() {
		return clients;
	}

	@Override
	public String getTopic() {
		return topic;
	}

	@Override
	public boolean isUserChat() {
		return !getLongId().contains("19:");
	}

	@Override
	public void leave() {
		kick(api.getClient().getUsername());
	}

	@Override
	public boolean isAdmin() {
		for (GroupUser user : getClients())
			if (user.getUser().getUsername().equals(api.getClient().getUsername()) && user.role.equals(GroupUser.Role.MASTER))
				return true;

		return false;
	}

	@Override
	public boolean isAdmin(String usr) {
		for (GroupUser user : getClients())
			if (user.getUser().getUsername().equals(usr) && user.role.equals(GroupUser.Role.MASTER))
				return true;

		return false;
	}

	@Override
	public void changeTopic(String topic) {
		PacketBuilder pb = new PacketBuilder(api);
		pb.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + id + "/properties?name=topic");
		pb.setType(RequestType.PUT);
		pb.setData("{\"topic\":\"" + topic + "\"}");
		pb.makeRequest();
	}
}
