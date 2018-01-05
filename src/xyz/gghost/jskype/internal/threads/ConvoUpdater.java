package xyz.gghost.jskype.internal.threads;


import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.APILoadedEvent;
import xyz.gghost.jskype.internal.packet.packets.GetConvosPacket;

public class ConvoUpdater extends Thread {
	private final SkypeAPI api;

	private boolean first = true;
	private boolean groupFail = false;
	
	public ConvoUpdater(SkypeAPI a){
		api = a;
	}
	
	@Override
	public void run() {
		while (this.isAlive()) {
			try {
				if (!groupFail) {
					new GetConvosPacket(api).init();
					api.getEventBus().post(new APILoadedEvent());
				}

			} catch (Exception e) {
				if (first)
					groupFail = true;

				e.printStackTrace();
			}

			try {
				Thread.sleep(15000);
			} catch (InterruptedException ignored) {
			}
			first = false;
		}
	}
}
