package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.User;

@Getter
@AllArgsConstructor
public class UserTypingEvent extends Event
{
	private final Group chat;
	private final User user;
}