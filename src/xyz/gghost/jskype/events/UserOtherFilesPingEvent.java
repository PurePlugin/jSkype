package xyz.gghost.jskype.events;


import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserOtherFilesPingEvent extends Event {
	private final Group group;
	private final User user;
	public UserOtherFilesPingEvent(Group g, User u){
		group = g;
		user = u;
	}
}
