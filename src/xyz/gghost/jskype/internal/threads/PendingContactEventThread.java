package xyz.gghost.jskype.internal.threads;

import java.util.ArrayList;
import java.util.List;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserPendingContactRequestEvent;
import xyz.gghost.jskype.model.User;

public class PendingContactEventThread extends Thread {
	private List<String> lastUsers = new ArrayList<>();
	private final SkypeAPI api;

	private boolean firstTime = true;
	
	public PendingContactEventThread(SkypeAPI a){
		api = a;
	}
	
	@Override
	public void run() {
		while (this.isAlive()) {
			List<User> optional = api.getClient().getContactRequests();

			if (optional != null) {
				List<User> newRequests = optional;

				if (!firstTime) {
					// Allows other clients to accept the request!
					List<String> newLastUsers = new ArrayList<>();

					for (User user : newRequests) {
						if (!lastUsers.contains(user.getUsername()))
							api.getEventBus().post(new UserPendingContactRequestEvent(user.getUsername()));

						newLastUsers.add(user.getUsername());
					}
					lastUsers = newLastUsers;
				} else {
					for (User user : newRequests)
						lastUsers.add(user.getUsername());
				}
			}

			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException ignored) {
			}

			firstTime = false;
		}
	}
}
