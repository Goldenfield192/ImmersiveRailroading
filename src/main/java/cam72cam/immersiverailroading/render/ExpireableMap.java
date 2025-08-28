package cam72cam.immersiverailroading.render;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;
import java.util.function.BiConsumer;

public class ExpireableMap<K,V> {
	private final Map<K, V> map = new Object2ObjectOpenHashMap<>();
	private final Map<K, Long> lastUsedTime = new Object2LongOpenHashMap<>();
	private long lastTime = timeS();

	private final int lifeSpan;
	private final boolean refreshWhenAccess;
	private final BiConsumer<K, V> removal;

	public ExpireableMap() {
		this(10, true, (k, v) -> {});
	}

	public ExpireableMap(int lifeSpan) {
		this(lifeSpan, true, (k, v) -> {});
	}

	public ExpireableMap(BiConsumer<K, V> removal){
		this(10, true, removal);
	}

	public ExpireableMap(int lifeSpan, boolean refreshWhenAccess){
		this(lifeSpan, refreshWhenAccess, (k, v) -> {});
	}

	public ExpireableMap(int lifeSpan, boolean refreshWhenAccess, BiConsumer<K, V> removal){
		this.lifeSpan = lifeSpan;
		this.refreshWhenAccess = refreshWhenAccess;
		this.removal = removal;
	}

	private static long timeS() {
		return System.currentTimeMillis() / 1000L;
	}

	public synchronized V get(K key) {
		clearUnused();

		if (map.containsKey(key)) {
			if (refreshWhenAccess) {
				lastUsedTime.put(key, timeS());
			}
			return map.get(key);
		}
		return null;
	}

	public synchronized void put(K key, V val) {
		if (val == null) {
			remove(key);
		} else {
			lastUsedTime.put(key, timeS());
			map.put(key, val);
		}
	}

	public synchronized void remove(K key) {
		if (map.containsKey(key)) {
			removal.accept(key, map.get(key));
			map.remove(key);
			lastUsedTime.remove(key);
		}
	}

	public synchronized int size(){
		clearUnused();

		return map.size();
	}

	public synchronized boolean containsKey(K key){
		clearUnused();

		return map.containsKey(key);
	}

	public synchronized Collection<V> values() {
		clearUnused();

		return map.values();
	}

	private synchronized void clearUnused(){
		if (lastTime + lifeSpan < timeS()) {
			// clear unused
            Set<K> ks = new HashSet<>(map.keySet());
			for (K dk : ks) {
				if (lastUsedTime.get(dk) + lifeSpan < timeS()) {
					removal.accept(dk, map.get(dk));
					map.remove(dk);
					lastUsedTime.remove(dk);
				}
			}
			lastTime = timeS();
		}
	}
}
