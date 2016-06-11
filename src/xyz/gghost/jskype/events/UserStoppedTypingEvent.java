package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

@Deprecated
/**
 * Event not in use - careful when using UserTypingEvent
 */
@Getter
@AllArgsConstructor
public class UserStoppedTypingEvent extends Event
{

	private final Group group;
	private final User user;
}