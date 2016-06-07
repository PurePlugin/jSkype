package xyz.gghost.jskype.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.event.Event;
import xyz.gghost.jskype.user.GroupUser;
import xyz.gghost.jskype.user.User;

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