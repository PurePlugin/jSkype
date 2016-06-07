package xyz.gghost.jskype.internal.threads;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.packets.GetContactsPacket;

@AllArgsConstructor
public class ContactUpdater extends Thread
{
	private final SkypeAPI api;

	@Override
	public void run()
	{
		while (this.isAlive())
		{
			try
			{
				new GetContactsPacket(api).setupContact();
				Thread.sleep(7000);
			}
			catch (InterruptedException ignored)
			{
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}