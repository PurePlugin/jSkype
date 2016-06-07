package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.APILoadedEvent;
import xyz.gghost.jskype.exception.AccountUnusableForRecentException;
import xyz.gghost.jskype.internal.packet.packets.GetConvos;

public class ConvoUpdater extends Thread
{
	private final SkypeAPI api;
	private boolean first = true;
	private boolean groupFail = false;

	public ConvoUpdater(SkypeAPI api)
	{
		this.api = api;
	}

	@Override
	public void run()
	{
		while (this.isAlive())
		{
			try
			{
				if (!groupFail)
				{
					new GetConvos(api).setupRecent();
					api.setLoaded(true);
					api.getEventBus().post(new APILoadedEvent());
				}
			}
			catch (AccountUnusableForRecentException e)
			{
				if (first)
					groupFail = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				Thread.sleep(15000);
			}
			catch (InterruptedException ignored)
			{
			}
			first = false;
		}
	}
}