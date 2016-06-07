package xyz.gghost.jskype.internal.threads;

import lombok.Getter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.packets.PingPacket;

public class Ping extends Thread
{
	@Getter
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
			ping.doNow();
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