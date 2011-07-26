package com.cognifide.cq.cache.algorithm;

/**
 * @author Bartosz Rudnicki
 */
public class LRUCache extends com.opensymphony.oscache.base.algorithm.LRUCache {

	private static final long serialVersionUID = -5293166118057771294L;

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
