package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import java.util.Collection;

/**
 * Enables consumer to walk through expiry guards
 */
public interface GuardCollectionWalker {

	/**
	 *
	 * @return unmodifiable guards collection
	 */
	Collection<ExpiryGuard> getGuards();

}
