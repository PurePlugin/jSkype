package xyz.gghost.jskype.events;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.base.Event;

public class UserPendingContactRequestEvent extends Event {
	private final String user;

	public UserPendingContactRequestEvent(String u){
		user = u;
	}
	
	public void accept(SkypeAPI api) {
		api.getClient().acceptContactRequest(user);
	}
}
