package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.OnlineStatus;
import xyz.gghost.jskype.user.User;

@Getter
@AllArgsConstructor
public class UserStatusChangedEvent extends Event
{
	private final User user;
	private final OnlineStatus status;
}