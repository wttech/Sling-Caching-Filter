package com.cognifide.cq.cache.cache.callback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletException;

/**
 * Callback interface used when entry is missing in cache
 */
public interface MissingCacheEntryCallback {

	/**
	 * Execute callback and generates content
	 *
	 * @return content
	 * @throws IOException
	 * @throws ServletException
	 */
	ByteArrayOutputStream doCallback() throws IOException, ServletException;

}
