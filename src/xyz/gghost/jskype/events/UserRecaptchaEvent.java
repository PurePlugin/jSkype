package xyz.gghost.jskype.events;

import xyz.gghost.jskype.events.base.Event;

public class UserRecaptchaEvent extends Event {
	private String image;
	private String username;
	private String answer;
}
