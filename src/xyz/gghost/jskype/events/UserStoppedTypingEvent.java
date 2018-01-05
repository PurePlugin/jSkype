package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

@Deprecated
/**
 * Event not in use - careful when using UserTypingEvent
 */

public class UserStoppedTypingEvent extends Event {

	private final Group group;
	private final User user;
	
	public UserStoppedTypingEvent(Group g, User u){
		group = g;
		user = u;
	}
}
