package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;
import xyz.gghost.jskype.model.User;

@Getter
@AllArgsConstructor
public class UserRoleChangedEvent extends Event
{
	private final Group group;
	private final User user;
	private final GroupUser.Role role;

	public boolean isPromoted()
	{
		return role == GroupUser.Role.MASTER;
	}

	public boolean isDemoted()
	{
		return role != GroupUser.Role.MASTER;
	}
}