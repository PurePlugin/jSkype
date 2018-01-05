package xyz.gghost.jskype.events.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * A simple, thread safe and reflectionless event handling system.
 * 
 * @author Connor Spencer Harries
 */
public final class EventBus {
	private final Multimap<Class<? extends Event>, Consumer<Event>> consumerMap;
	private final Lock lock;

	public EventBus() {
		this.consumerMap = ArrayListMultimap.create();
		this.lock = new ReentrantLock();
	}

	@SuppressWarnings("unchecked")
	public <T extends Event> boolean register(Class<T> clazz, Consumer<T> consumer) {
		if (consumer == null) throw new RuntimeException("consumer cannot be null");
		if (clazz == null) throw new RuntimeException("clazz cannot be null");
		lock.lock();

		try {
			Consumer<Event> handler = (Consumer<Event>) consumer;
			return consumerMap.put(clazz, handler);
		} finally {
			lock.unlock();
		}
	}

	public <T extends Event> boolean unregister(Consumer<T> consumer) {
		if(consumer == null) throw new RuntimeException("consumer cannot be null");
		lock.lock();
		try {
			boolean removed = false;
			for (Class<? extends Event> clazz : consumerMap.keySet()) {
				Collection<Consumer<Event>> consumers = consumerMap.get(clazz);
				for (Iterator<Consumer<Event>> iterator = consumers.iterator(); iterator.hasNext();) {
					if (iterator.next() == consumer) {
						iterator.remove();
						removed = true;
					}
				}
			}
			return removed;
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T extends Event> T post(T event) {
		if (event == null) throw new RuntimeException("event cannot be null");

		lock.lock();
		try {
			for (Class zuper = event.getClass(); zuper != null && zuper != Event.class; zuper = zuper.getSuperclass()) {
//				consumerMap.get(zuper).forEach(> consumer.accept(event));
			}
			return event;
		} finally {
			lock.unlock();
		}
	}
	
	public static interface Consumer<T> {
		public void accept(T t);
	}
}
