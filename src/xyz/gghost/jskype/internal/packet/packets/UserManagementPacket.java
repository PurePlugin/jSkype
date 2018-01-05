package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONObject;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.Packet;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import org.json.JSONException;

public class UserManagementPacket extends Packet {
	
	public UserManagementPacket(SkypeAPI api) {
		super(api);
	}
	
	/**
	 * @return true = done / false = no perm
	 */
	public boolean kickUser(String groupId, String username) {
		PacketBuilder packet = new PacketBuilder(api);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
		packet.setType(RequestType.DELETE);
		return packet.makeRequest() != null;
	}

	/**
	 * @return true = done / false = no perm
	 */
	public boolean addUser(String groupId, String username) {
		PacketBuilder packet = new PacketBuilder(api);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
		try {
			packet.setData(new JSONObject().put("Role", "User").toString());
		} catch (JSONException e) {}
		packet.setType(RequestType.PUT);
		return packet.makeRequest() != null;
	}

	/**
	 * @return true = done / false = no perm
	 */
	public boolean promoteUser(String groupId, String username) {
		PacketBuilder packet = new PacketBuilder(api);
		packet.setUrl("https://client-s.gateway.messenger.live.com/v1/threads/" + groupId + "/members/8:" + username);
		try {
			packet.setData(new JSONObject().put("Role", "Admin").toString());
		} catch (JSONException e) {}
		packet.setType(RequestType.PUT);
		return packet.makeRequest() != null;
	}

	@Override
	public void init() {
	}
}
