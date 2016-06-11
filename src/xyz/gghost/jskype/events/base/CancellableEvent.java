package xyz.gghost.jskype.events.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellableEvent extends Event
{
	private boolean cancelled;
}