package xyz.gghost.jskype.internal.packet.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.GroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.message.FormatUtils;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;
import xyz.gghost.jskype.model.User;

@AllArgsConstructor
public class GroupInfoPacket
{
	private final SkypeAPI api;

	public Group getGroup(String longId)
	{
		PacketBuilder members = new PacketBuilder(api);
		members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + longId + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
		members.setType(RequestType.GET);

		String data = members.makeRequest();

		if (data == null)
			return null;

		GroupImpl group = new GroupImpl(api, new JSONObject(data).getString("id"));

		JSONObject properties = new JSONObject(data).getJSONObject("properties");
		if (!properties.isNull("topic"))
			group.setTopic(properties.getString("topic"));

		if (!properties.isNull("picture"))
			group.setPictureUrl(properties.getString("picture").split("@")[1]);

		group.setTopic(FormatUtils.decodeText(group.getTopic()));

		JSONArray membersArray = new JSONObject(data).getJSONArray("members");
		for (int ii = 0; ii < membersArray.length(); ii++)
		{
			JSONObject member = membersArray.getJSONObject(ii);

			// Skip over phone numbers
			if (member.getString("id").startsWith("4"))
				continue;

			try
			{
				GroupUser.Role role = GroupUser.Role.USER;

				User ussr = api.getClient().getSimpleUser(member.getString("id").split("8:")[1]);

				if (!member.getString("role").equals("User"))
					role = GroupUser.Role.MASTER;

				group.getClients().add(new GroupUser(ussr, role, group));
			}
			catch (Exception e)
			{
				api.getLogger().log(Level.SEVERE, "Unable to get a users info", e);
			}
		}
		return group;
	}

	public List<GroupUser> getUsers(String id)
	{
		try
		{
			ArrayList<GroupUser> groupMembers = new ArrayList<GroupUser>();

			PacketBuilder members = new PacketBuilder(api);
			members.setUrl("https://db3-client-s.gateway.messenger.live.com/v1/threads/" + id + "?startTime=143335&pageSize=100&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
			members.setType(RequestType.GET);

			String data = members.makeRequest();

			if (data == null)
				return null;

			JSONArray membersArray = new JSONObject(data).getJSONArray("members");
			for (int ii = 0; ii < membersArray.length(); ii++)
			{
				JSONObject member = membersArray.getJSONObject(ii);
				try
				{

					GroupUser.Role role = GroupUser.Role.USER;

					User usr = api.getClient().getSimpleUser(member.getString("id").split("8:")[1]);

					if (!member.getString("role").equals("User"))
						role = GroupUser.Role.MASTER;

					groupMembers.add(new GroupUser(usr, role, new GroupImpl(api, id)));

				}
				catch (Exception e)
				{
					api.getLogger().severe("Failed to get a members info");
					api.getLogger().severe(e.getLocalizedMessage());

				}
			}
			return groupMembers;
		}
		catch (NullPointerException e)
		{
			return null;
		}
		catch (JSONException e)
		{
			return null;
		}

	}
}