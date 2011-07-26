package com.cognifide.cq.cache.algorithm;

/**
 * @author Bartosz Rudnicki
 */
public class FIFOCache extends com.opensymphony.oscache.base.algorithm.FIFOCache {

	private static final long serialVersionUID = -1110164468839138386L;

	@Override
	protected void itemRemoved(Object key) {
		super.itemRemoved(key);
		if (key instanceof String) {
			SilentRemovalNotificator.registerSilentlyRemovedEntry((String) key);
		}
	}

	@Override
	protected Object removeItem() {
		Object removedItem = super.removeItem();
		if (removedItem instanceof String) {
			SilentRemovalNotificator.registerSilentlyRemovedEntry((String) removedItem);
		}
		return removedItem;
	}
}
