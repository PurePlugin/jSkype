package xyz.gghost.jskype.command;

import java.util.Arrays;
import java.util.List;

import xyz.gghost.jskype.model.Group;
import xyz.gghost.jskype.model.User;

public abstract class Command {
	private List<String> aliases;
	private User sender;
	private Group chat;
	private String[] args;

	public Command(String... aliases) {
		this.aliases = Arrays.asList(aliases);
	}

	public abstract void execute();
	
	public List<String> getAliases(){
		return aliases;
	}
	
	public void setSender(User sender){
		this.sender = sender;
	}
	
	public void setChat(Group c){
		this.chat = c;
	}
	
	public void setArgs(String[] args){
		this.args = args;
	}
}
