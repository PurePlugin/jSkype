package xyz.gghost.jskype.user;

import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;

public interface User
{
	String getDisplayName();

	String getUsername();

	String getPictureUrl();

	OnlineStatus getOnlineStatus();

	boolean isBlocked();

	String getMood();

	boolean isContact();

	void sendContactRequest(SkypeAPI api);

	void sendContactRequest(SkypeAPI api, String hello);

	Group getGroup(SkypeAPI api);
}