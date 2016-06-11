package xyz.gghost.jskype;

import java.util.logging.Logger;

import lombok.Data;
import xyz.gghost.jskype.command.CommandBus;
import xyz.gghost.jskype.events.base.EventBus;
import xyz.gghost.jskype.model.Visibility;

@Data
public class SkypeAPI
{
	private final Logger logger = Logger.getLogger("jSkype");
	private final EventBus eventBus = new EventBus();
	private final CommandBus commandBus = new CommandBus(this);

	private final Client client;

	public SkypeAPI(String username, String password)
	{
		client = new Client(this, username, password);
	}

	public SkypeAPI login() throws Exception
	{
		client.login();
		client.setVisibility(Visibility.ONLINE);
		return this;
	}

	public SkypeAPI stop() throws Exception
	{
		client.logout();
		return this;
	}
}