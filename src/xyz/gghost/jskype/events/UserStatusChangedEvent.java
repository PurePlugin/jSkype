package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

public class UserStatusChangedEvent extends Event {
	private final User user;
	private final Visibility visibility;

	public UserStatusChangedEvent(User user, Visibility visiblity) {
		this.user = user;
		this.visibility = visiblity;
	}
}
