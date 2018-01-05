package xyz.gghost.jskype.command;

import java.util.ArrayList;
import java.util.List;

import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserChatEvent;
import xyz.gghost.jskype.events.base.EventBus;
import xyz.gghost.jskype.events.base.Event;

public class CommandBus {

	private String prefix = ".";

	private List<Command> commands = new ArrayList<>();

	public CommandBus(final SkypeAPI api) {
		api.getEventBus().register(UserChatEvent.class, new EventBus.Consumer<UserChatEvent>(){

				@Override
				public void accept(UserChatEvent event) {
					String message = event.getMsg().getMessage();

					if (!message.startsWith(prefix))
						return;

					String username = event.getUser().getUsername();

					Command toExecute = null;

					for (Command command : commands) {
						if (command.getAliases().contains(message.split(" ")[0].replace(prefix, ""))) {
							toExecute = command;
							break;
						}
					}

					if (toExecute == null)
						return;

					api.getLogger().info(username + " executed the command '" + message + "'");

					toExecute.setChat(event.getGroup());
					toExecute.setSender(event.getUser());
					toExecute.setArgs(message.split(" "));
					toExecute.execute();
				}
			});
	}

	public boolean register(Command command) {
		return commands.add(command);
	}
}
