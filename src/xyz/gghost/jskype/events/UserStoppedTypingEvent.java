package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

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