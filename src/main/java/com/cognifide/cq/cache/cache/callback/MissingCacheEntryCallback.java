package com.cognifide.cq.cache.cache.callback;

import com.cognifide.cq.cache.cache.CacheEntity;
import java.io.IOException;
import javax.servlet.ServletException;

/**
 * Callback interface used when there is no entity in cache
 */
public interface MissingCacheEntryCallback {

	/**
	 * Executes callback and generates entity.
	 *
	 * @return entity stored in cache
	 * @throws IOException
	 * @throws ServletException
	 */
	CacheEntity generateCacheEntity() throws IOException, ServletException;

}
