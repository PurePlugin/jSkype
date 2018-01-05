package xyz.gghost.jskype.internal.impl;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

public class UserImpl implements User {

	private String displayName;
	private String username;
	private String pictureUrl = "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png";
	private String mood = "";
	private boolean isContact = false;
	private boolean blocked = false;
	private String firstName = "";
	private String lastName = "";
	private Visibility onlineStatus = Visibility.OFFLINE;

	public UserImpl(String username) {
		displayName = username;
		this.username = username;
	}

	public void setOnlineStatus(Visibility status) {
		onlineStatus = status;
	}

	public void setBlocked(Boolean get) {
		blocked = get;
	}

	public void setContact(boolean p0) {
		isContact = p0;
	}

	public void setLastName(String string) {
		lastName = string;
	}

	public void setFirstName(String string) {
		firstName = string;
	}



	@Override
	public void sendContactRequest(SkypeAPI api) {
		api.getClient().sendContactRequest(username);
	}

	@Override
	public void sendContactRequest(SkypeAPI api, String message) {
		api.getClient().sendContactRequest(username, message);
	}

	@Override
	public Group getGroup(SkypeAPI api) {
		return new ContactGroupImpl(api, "8:" + username);
	}


	@Override
	public String getDisplayName() {
		// TODO: Implement this method
		return displayName;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPictureUrl() {
		// TODO: Implement this method
		return pictureUrl;
	}

	@Override
	public Visibility getOnlineStatus() {
		// TODO: Implement this method
		return onlineStatus;
	}

	@Override
	public boolean isBlocked() {
		// TODO: Implement this method
		return blocked;
	}

	@Override
	public String getMood() {
		// TODO: Implement this method
		return mood;
	}

	@Override
	public boolean isContact() {
		// TODO: Implement this method
		return isContact;
	}

	public void setMood(String string) {
		mood = string;
	}

	public void setDisplayName(String string) {
		displayName = string;
	}

	public void setPictureUrl(String string) {
		pictureUrl = string;
	}

	public void setUsername(String string) {
		username = string;
	}
}
