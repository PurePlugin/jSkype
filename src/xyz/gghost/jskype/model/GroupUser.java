package xyz.gghost.jskype.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.gghost.jskype.internal.impl.GroupImpl;

@AllArgsConstructor
public class GroupUser
{
	@Getter
	private User user;

	public final Role role;
	private GroupImpl group;

	@Override
	public String toString()
	{
		return user.getUsername();
	}

	public void setIsAdmin(boolean admin)
	{
		group.setAdmin(user.getUsername(), admin);
	}

	public enum Role
	{
		MASTER,
		USER
	}
}