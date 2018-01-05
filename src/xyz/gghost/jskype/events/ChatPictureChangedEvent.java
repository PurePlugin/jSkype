package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class ChatPictureChangedEvent extends Event {
	private final Group group;
	private final User user;
	private final String newPicture;

	public ChatPictureChangedEvent(Group g, User u, String n) {
		group = g;
		user = u;
		newPicture = n;
	}
}
