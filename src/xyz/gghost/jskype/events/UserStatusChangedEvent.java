package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

@Getter
@AllArgsConstructor
public class UserStatusChangedEvent extends Event
{
	private final User user;
	private final Visibility visibility;
}