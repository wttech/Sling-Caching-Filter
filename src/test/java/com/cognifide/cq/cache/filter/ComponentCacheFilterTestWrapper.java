package com.cognifide.cq.cache.filter;

import org.osgi.service.component.ComponentContext;

/**
 * @author Bartosz Rudnicki
 */
public class ComponentCacheFilterTestWrapper extends ComponentCacheFilter {

	@Override
	public void activate(ComponentContext context) {
		super.activate(context);
	}

	@Override
	public void deactivate(ComponentContext context) {
		super.deactivate(context);
	}
}
