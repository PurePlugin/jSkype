package xyz.gghost.jskype.internal.packet.packets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.Packet;
import xyz.gghost.jskype.internal.packet.PacketBuilder;
import xyz.gghost.jskype.internal.packet.RequestType;

public class PingPacket extends Packet
{
	public PingPacket(SkypeAPI api)
	{
		super(api);
	}

	@Override
	public void init()
	{
		PacketBuilder ping = new PacketBuilder(api);
		ping.setType(RequestType.POST);
		ping.setUrl("https://web.skype.com/api/v1/session-ping");
		ping.setData("sessionId=" + api.getClient().getUniqueId().toString());
		ping.setIsForm(true);
		String data = ping.makeRequest();

		if (data == null || data.equals("---"))
		{
			api.getLogger().severe("Skype login expired... Reconnecting");

			try
			{
				api.login();
			}
			catch (Exception e)
			{
				api.getLogger().severe("Failed to reconnect. ");
			}
		}

		try
		{
			PacketBuilder online = new PacketBuilder(api);
			online.setType(RequestType.POST);
			online.setUrl("https://client-s.gateway.messenger.live.com/v1/users/ME/endpoints/" + URLEncoder.encode(api.getClient().getAuth().getLoginToken().getEndPoint(), "UTF-8") + "/active");
			online.setData("{\"timeout\":7}");
			online.makeRequest();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}