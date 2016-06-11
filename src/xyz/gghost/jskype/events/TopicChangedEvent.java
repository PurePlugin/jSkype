package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.CancellableEvent;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

@Getter
@AllArgsConstructor
public class TopicChangedEvent extends CancellableEvent
{
	private final Group group;
	private final User user;
	private final String topic;
	private final String oldTopic;
}