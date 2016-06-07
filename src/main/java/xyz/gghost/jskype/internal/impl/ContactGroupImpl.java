package xyz.gghost.jskype.internal.impl;

import java.util.ArrayList;
import java.util.List;

import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.packet.packets.SendMessagePacket;
import xyz.gghost.jskype.message.Message;
import xyz.gghost.jskype.user.GroupUser;

/**
 * Created by Ghost on 19/09/2015.
 */
public class ContactGroupImpl extends GroupImpl implements Group
{
	private final SkypeAPI api;
	private final String id;

	public ContactGroupImpl(SkypeAPI api, String longId)
	{
		super(api, longId);

		this.api = api;
		this.id = longId;
	}

	@Override
	public void kick(String usr)
	{
	}

	@Override
	public void add(String usr)
	{
	}

	@Override
	public void leave()
	{
	}

	@SuppressWarnings("unused")
	@Override
	public List<GroupUser> getClients()
	{
		List<GroupUser> users = new ArrayList<GroupUser>();
		// TODO: add the two people
		return new ArrayList<GroupUser>();
	}

	@Override
	public String getId()
	{
		return id.split("8:")[1];
	}

	public String getUsername()
	{
		return id.split("8:")[1];
	}

	@Override
	public String getLongId()
	{
		return id;
	}

	@Override
	public Message sendMessage(Message msg)
	{
		return new SendMessagePacket(api).sendMessage(id, msg);
	}

	@Override
	public Message sendMessage(String msg)
	{
		return new SendMessagePacket(api).sendMessage(id, new Message(msg));
	}

	@Override
	public String getTopic()
	{
		return getUsername();
	}

	@Override
	public boolean isAdmin()
	{
		return false;
	}

	@Override
	public boolean isAdmin(String usr)
	{
		return false;
	}
}