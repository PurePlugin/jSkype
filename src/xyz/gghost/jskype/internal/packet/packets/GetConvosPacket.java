package xyz.gghost.jskype.internal.packet.packets;

import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.packet.Packet;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;
import xyz.gghost.jskype.model.Group;

public class GetConvosPacket extends Packet
{
	public GetConvosPacket(SkypeAPI api)
	{
		super(api);
	}

	@Override
	public void init()
	{
		try
		{
			PacketBuilder packet = new PacketBuilder(api);
			packet.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/conversations?startTime=0&pageSize=200&view=msnp24Equivalent&targetType=Passport|Skype|Lync|Thread");
			packet.setData("");
			packet.setType(RequestType.GET);

			String data = packet.makeRequest();

			if (data == null || data.equals(""))
				throw new AccountUnusableForRecentException();

			JSONArray jsonArray = new JSONObject(data).getJSONArray("conversations");
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject recent = jsonArray.getJSONObject(i);

				if (recent.getString("targetLink").contains("/contacts/8:"))
				{
					updateGroup(new ContactGroupImpl(api, recent.getString("id")));
				}
				else
				{
					String id = recent.getString("id");

					if (id.endsWith("@thread.skype"))
					{
						try
						{
							updateGroup(new GroupInfoPacket(api).getGroup(id));
						}
						catch (Exception e)
						{
							// you've been rate limited
						}
					}
				}
			}
		}
		catch (AccountUnusableForRecentException e)
		{
			api.getLogger().log(Level.SEVERE, "Account unusable for recent", e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void updateGroup(Group group)
	{
		Group oldGroup = null;

		for (Group groupA : api.getClient().getGroups())
		{
			if (groupA.getId().equals(group.getId()))
				oldGroup = groupA;
		}

		if (oldGroup != null)
			api.getClient().getGroups().remove(oldGroup);

		api.getClient().getGroups().add(group);
	}
}