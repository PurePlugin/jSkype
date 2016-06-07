package xyz.gghost.jskype.command;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.user.User;

@Data
public abstract class Command
{
	private List<String> aliases;
	private User sender;
	private Group chat;
	private String[] args;

	public Command(String... aliases)
	{
		this.aliases = Arrays.asList(aliases);
	}

	public abstract void execute();
}