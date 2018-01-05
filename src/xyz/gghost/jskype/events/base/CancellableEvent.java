package xyz.gghost.jskype.events.base;

public class CancellableEvent extends Event {
	private boolean cancelled;
	
	public boolean isCancelled(){
		return cancelled;
	}
}
