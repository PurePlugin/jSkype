package xyz.gghost.jskype.internal.packet.packets;

import org.json.JSONArray;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

@AllArgsConstructor
public class GetConvos
{
	private final SkypeAPI api;

	public void setupRecent() throws AccountUnusableForRecentException
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
					api.updateGroup(new ContactGroupImpl(api, recent.getString("id")));
				}
				else
				{
					String id = recent.getString("id");

					if (id.endsWith("@thread.skype"))
					{
						try
						{
							api.updateGroup(new GroupInfoPacket(api).getGroup(id));
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
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}