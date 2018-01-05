package xyz.gghost.jskype.message;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.packets.SendMessagePacket;
import xyz.gghost.jskype.model.User;

public class Message {
	private User sender;
	private String message;
	private String updateUrl;
	private boolean edited = false;
	private String time;
	private String id;
	private int timestamp;

	public Message(String message) {
		this.message = message;
	}

	public Message() {
	}

	public String getId() {
		return id;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public String getMessage() {
		return message;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setUpdateUrl(String split) {
		updateUrl=split;
	}

	public void setTime(String string) {
		time=string;
	}

	public void setSender(User user) {
		sender=user;
	}

	public void setEdited(boolean p0) {
		edited=p0;
	}
	
	public void setId(String string) {
		id = string;
	}

	/**
	 * Edit the message
	 */
	public Message editMessage(SkypeAPI api, String edit) {
		setMessage(edit);
		edited = true;
		return new SendMessagePacket(api).editMessage(this, message);
	}

	/**
	 * Once setMessage has edited the message locally, this will update the edit
	 * on skypes servers
	 *
	 * @param api
	 *            SkypeAPI
	 * @return the message
	 */
	public Message updateEdit(SkypeAPI api) {
		edited = true;
		return new SendMessagePacket(api).editMessage(this, message);
	}

	public void setMessage(String m) {
		message = m;
	}
}
