package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserLeaveEvent extends Event {
	private final Group group;
	private final User user;

	public UserLeaveEvent(Group g, User u) {
		this.group = g;
		this.user = u;
	}
}
