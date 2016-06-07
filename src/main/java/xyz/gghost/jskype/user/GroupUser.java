package xyz.gghost.jskype.user;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.internal.impl.GroupImpl;

/**
 * Created by Ghost on 19/09/2015.
 */
@AllArgsConstructor
public class GroupUser
{
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

	public User getUser()
	{
		return user;
	}

	public enum Role
	{
		MASTER,
		USER
	}
}