package xyz.gghost.jskype.internal.packet;

import xyz.gghost.jskype.SkypeAPI;

public abstract class Packet {
	protected SkypeAPI api;

	public Packet(SkypeAPI a){
		api = a;
	}
	
	public abstract void init();
}
