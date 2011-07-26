package com.cognifide.cq.cache.refresh.jcr;

/**
 * @author Bartosz Rudnicki
 */
public interface JcrEventListener {

	/**
	 * Called when content has been changed.
	 * 
	 * @param path path to the changed resource
	 */
	boolean contentChanged(String path);
}
