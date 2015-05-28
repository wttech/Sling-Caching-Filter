package com.cognifide.cq.cache.expiry.collection;

import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import java.util.Collection;

public interface GuardCollectionWalker {

	Collection<ExpiryGuard> getGuards();

}
