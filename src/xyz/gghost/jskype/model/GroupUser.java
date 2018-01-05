package xyz.gghost.jskype.model;

import xyz.gghost.jskype.internal.impl.GroupImpl;

public class GroupUser {

	private User user;

	public final Role role;
	private GroupImpl group;

	public GroupUser(User u, Role r, GroupImpl g) {
		user = u;
		role = r;
		group = g;
	}

	public GroupUser(Role r) {
		role = r;
	}

	@Override
	public String toString() {
		return user.getUsername();
	}

	public void setIsAdmin(boolean admin) {
		group.setAdmin(user.getUsername(), admin);
	}

	public User getUser() {
		return user;
	}

	public enum Role {
		MASTER,
		USER
		}
}
