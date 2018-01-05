package xyz.gghost.jskype.internal.threads;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.packets.PingPacket;

@Getter
public class Ping extends Thread
{
	private final PingPacket ping;

	public Ping(SkypeAPI api)
	{
		ping = new PingPacket(api);
	}

	@Override
	public void run()
	{
		while (this.isAlive())
		{
			ping.init();

			try
			{
				Thread.sleep(4000);
			}
			catch (InterruptedException ignored)
			{
			}
		}
	}
}