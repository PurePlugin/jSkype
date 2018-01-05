package xyz.gghost.jskype.internal.packet;

import lombok.Data;
import xyz.gghost.jskype.SkypeAPI;

@Data
public abstract class Packet
{
	protected final SkypeAPI api;

	public abstract void init();
}