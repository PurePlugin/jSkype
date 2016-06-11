package xyz.gghost.jskype;

import xyz.gghost.jskype.events.UserChatEvent;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		SkypeAPI api = new SkypeAPI("pure.bot", "94olmfg9R3").login();

		api.getEventBus().register(UserChatEvent.class, event ->
		{
			System.out.println("Chat message from " + event.getUser().getUsername() + ", " + event.getMsg().getMessage());
		});
	}
}