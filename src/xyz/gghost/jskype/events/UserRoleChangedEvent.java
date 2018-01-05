package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.GroupUser;
import xyz.gghost.jskype.model.User;

public class UserRoleChangedEvent extends Event {
	private final Group group;
	private final User user;
	private final GroupUser.Role role;

	public UserRoleChangedEvent(Group g, User u, GroupUser.Role r){
		this.group = g;
		this.user = u;
		this.role = r;
	}
	
	public boolean isPromoted() {
		return role == GroupUser.Role.MASTER;
	}
	
	public boolean isDemoted() {
		return role != GroupUser.Role.MASTER;
	}
}
