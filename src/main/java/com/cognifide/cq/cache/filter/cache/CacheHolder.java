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
package com.cognifide.cq.cache.filter.cache;

import com.cognifide.cq.cache.refresh.jcr.JcrRefreshPolicy;
import com.opensymphony.oscache.base.NeedsRefreshException;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.servlet.ServletContext;

public interface CacheHolder {

	/**
	 * Creates underlying cache instance.
	 *
	 * @param servletContext - current servlet context
	 * @param properties
	 * @param overwrite decides if existing cache should be overwritten
	 */
	void create(ServletContext servletContext, Properties properties, boolean overwrite);

	/**
	 * Destroys underlying instance of cache. After this step this class in unusable.
	 */
	void destroy();

	/**
	 * Find and return data with given key. If entry needs refresh throws exception.
	 *
	 * @param resourcType resource type
	 * @param key for which data will be searched
	 * @return stored data or throws exception
	 * @throws NeedsRefreshException thrown when data needs refresh
	 */
	ByteArrayOutputStream get(String resourcType, String key) throws NeedsRefreshException;

	/**
	 * Adds data under given key to cache and registers cache event listers with entry refresh policy
	 *
	 * @param key under which data will be stored
	 * @param data to be stored
	 * @param refreshPolicy - cache event listener and entry refresh policy
	 */
	void put(String key, ByteArrayOutputStream data, JcrRefreshPolicy refreshPolicy);

	/**
	 * Removes entries with given key
	 *
	 * @param key to be removed
	 */
	void remove(String key);

}
