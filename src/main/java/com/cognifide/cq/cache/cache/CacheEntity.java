package com.cognifide.cq.cache.cache;

import java.io.ByteArrayOutputStream;

/**
 * Holds cache entity content with content type
 */
public interface CacheEntity {

	/**
	 *
	 * @return content type
	 */
	String getContentType();

	/**
	 *
	 * @return content
	 */
	ByteArrayOutputStream getContent();
}
