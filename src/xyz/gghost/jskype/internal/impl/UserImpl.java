package xyz.gghost.jskype.internal.impl;

import lombok.Data;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;
import xyz.gghost.jskype.model.Visibility;

@Data
public class UserImpl implements User
{
	private String displayName;
	private String username;
	private String pictureUrl = "https://swx.cdn.skype.com/assets/v/0.0.213/images/avatars/default-avatar-group_46.png";
	private String mood = "";
	private boolean isContact = false;
	private boolean blocked = false;
	private String firstName = "";
	private String lastName = "";
	private Visibility onlineStatus = Visibility.OFFLINE;

	public UserImpl(String username)
	{
		displayName = username;
		this.username = username;
	}

	@Override
	public void sendContactRequest(SkypeAPI api)
	{
		api.getClient().sendContactRequest(username);
	}

	@Override
	public void sendContactRequest(SkypeAPI api, String message)
	{
		api.getClient().sendContactRequest(username, message);
	}

	@Override
	public Group getGroup(SkypeAPI api)
	{
		return new ContactGroupImpl(api, "8:" + username);
	}
}