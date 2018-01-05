package xyz.gghost.jskype.events;


import xyz.gghost.jskype.events.base.Event;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public class UserChatEvent extends Event {
	private final Group group;
	private final User user;
	private final Message msg;

	public UserChatEvent(Group g, User u, Message m) {
		group = g;
		user = u;
		msg = m;
	}

	public Group getGroup() {
		return group;
	}

	public User getUser() {
		return user;
	}

	public boolean isEdited() {
		return msg.isEdited();
	}
	
	public Message getMsg(){
		return msg;
	}
}
