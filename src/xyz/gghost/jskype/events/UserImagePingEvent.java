package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserImagePingEvent extends Event {
	private final Group group;
	private final User user;
	private final String imageUrl;

	public UserImagePingEvent(Group g, User u, String i) {
		group = g;
		user = u;
		imageUrl = i;
	}
}
