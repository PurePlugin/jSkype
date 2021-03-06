package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

@Getter
@AllArgsConstructor
public class UserChatEvent extends Event
{
	private final Group group;
	private final User user;
	private final Message msg;

	public boolean isEdited()
	{
		return msg.isEdited();
	}
}