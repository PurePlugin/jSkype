package xyz.gghost.jskype.events;


import xyz.gghost.jskype.events.base.CancellableEvent;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class TopicChangedEvent extends CancellableEvent {
	private Group group;
	private User user;
	private String topic;
	private String oldTopic;

	public TopicChangedEvent(Group g, User u, String t, String o) {
		group = g;
		user = u;
		topic = t;
		oldTopic = o;
	}
}
