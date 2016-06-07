package xyz.gghost.jskype.event;

import lombok.Data;

@Data
public abstract class Event
{
	private boolean cancelled;
}