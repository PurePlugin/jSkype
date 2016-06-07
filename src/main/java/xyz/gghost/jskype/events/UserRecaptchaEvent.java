package xyz.gghost.jskype.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.gghost.jskype.event.Event;

@EqualsAndHashCode(callSuper = false)
@Data
public class UserRecaptchaEvent extends Event
{
	private final String image;
	private final String username;
	private String answer;
}