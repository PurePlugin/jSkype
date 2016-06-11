package xyz.gghost.jskype.command;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.events.UserChatEvent;

@Getter
public class CommandBus
{
	@Setter
	private String prefix = ".";

	private final SkypeAPI api;

	private List<Command> commands = new ArrayList<>();

	public CommandBus(SkypeAPI api)
	{
		this.api = api;

		api.getEventBus().register(UserChatEvent.class, event ->
		{
			String message = event.getMsg().getMessage();

			if (!message.startsWith(prefix))
				return;

			String username = event.getUser().getUsername();

			Command toExecute = null;

			for (Command command : commands)
			{
				if (command.getAliases().contains(message.split(" ")[0].replace(".", " ")))
				{
					toExecute = command;
					break;
				}
			}

			if (toExecute == null)
				return;

			api.getLogger().info(username + " executed the command '" + message + "'");

			toExecute.setChat(event.getGroup());
			toExecute.setSender(event.getUser());
			toExecute.setArgs(event.getMsg().getMessage().split(" "));
			toExecute.execute();
		});
	}

	public boolean register(Command command)
	{
		return commands.add(command);
	}
}