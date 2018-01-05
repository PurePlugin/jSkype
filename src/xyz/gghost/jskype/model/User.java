package xyz.gghost.jskype.model;

import xyz.gghost.jskype.SkypeAPI;

public interface User
{
	String getDisplayName();

	String getUsername();

	String getPictureUrl();

	Visibility getOnlineStatus();

	boolean isBlocked();

	String getMood();

	boolean isContact();

	void sendContactRequest(SkypeAPI api);

	void sendContactRequest(SkypeAPI api, String hello);

	Group getGroup(SkypeAPI api);
}