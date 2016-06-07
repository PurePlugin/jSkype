package xyz.gghost.jskype.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellableEvent extends Event
{
	private boolean cancelled;
}