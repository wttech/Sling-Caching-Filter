/*
 * Copyright 2015 Cognifide Polska Sp. z o. o..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.cache;

import com.cognifide.cq.cache.cache.callback.MissingCacheEntryCallback;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;

public interface CacheHolder {

	/**
	 *
	 * @return
	 */
	URI getCacheManagerURI();

	/**
	 *
	 * @return
	 */
	Iterable<String> getCacheNames();

	/**
	 *
	 * @param cacheName
	 * @return
	 */
	Collection<String> getKeysFor(String cacheName);

	/**
	 *
	 * @param request
	 * @param resourceTypeCacheConfiguration
	 * @param callback
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	ByteArrayOutputStream putOrGet(SlingHttpServletRequest request,
			ResourceTypeCacheConfiguration resourceTypeCacheConfiguration, MissingCacheEntryCallback callback)
			throws IOException, ServletException;

	/**
	 *
	 * @param cacheName
	 * @param key
	 */
	void remove(String cacheName, String key);

	/**
	 *
	 * @param cacheName
	 */
	void clear(String cacheName);
}
