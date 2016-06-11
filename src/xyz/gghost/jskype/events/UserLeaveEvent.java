package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

@Getter
@AllArgsConstructor
public class UserLeaveEvent extends Event
{
	private final Group group;
	private final User user;
}