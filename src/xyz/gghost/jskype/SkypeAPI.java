package xyz.gghost.jskype;

import java.util.logging.Logger;
import xyz.gghost.jskype.command.CommandBus;
import xyz.gghost.jskype.events.base.EventBus;
import xyz.gghost.jskype.model.Visibility;

public class SkypeAPI {
	private final Logger logger = Logger.getLogger("jSkype");
	private final EventBus eventBus;
	private final CommandBus commandBus;
	private final Client client;

	public SkypeAPI(String username, String password) {
		eventBus = new EventBus();
		commandBus = new CommandBus(this);
		client = new Client(this, username, password);
	}

	public SkypeAPI login() throws Exception {
		client.login();
		client.setVisibility(Visibility.ONLINE);
		return this;
	}

	public SkypeAPI stop() throws Exception {
		client.logout();
		return this;
	}

	public Logger getLogger() {
		return logger;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public CommandBus getCommandBus() {
		return commandBus;
	}

	public Client getClient() {
		return client;
	}
}
