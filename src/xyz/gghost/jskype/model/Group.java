package xyz.gghost.jskype.model;

import java.io.File;
import java.net.URL;
import java.util.List;

import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.message.MessageHistory;

public interface Group
{
	void kick(String usr);

	void add(String usr);

	void leave();

	void setAdmin(String user, boolean admin);

	void changeTopic(String topic);

	boolean isAdmin();

	boolean isAdmin(String usr);

	boolean isUserChat();

	MessageHistory getMessageHistory();

	String getId();

	String getPictureUrl();

	String getTopic();

	String getLongId();

	Message sendMessage(Message msg);

	Message sendMessage(String msg);

	Message sendImage(File url);

	Message sendImage(URL url);

	List<GroupUser> getClients();
}