package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserNewMovieAdsPingEvent extends Event
{
	private final User user;
	private final Group group;
	private final String id;

	public UserNewMovieAdsPingEvent(User u, Group g, String i){
		this.user = u;
		this.group = g;
		id = i;
	}
}
