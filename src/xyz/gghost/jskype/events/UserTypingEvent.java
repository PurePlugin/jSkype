package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserTypingEvent extends Event {
	private final Group chat;
	private final User user;
	
	public UserTypingEvent(Group c, User u){
		chat=c;user=u;
	}
}
