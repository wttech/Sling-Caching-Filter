/*
 * Copyright 2015 Wunderman Thompson Technology
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
package com.cognifide.cq.cache.plugins.statistics.action;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.plugins.statistics.Statistics;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearAction implements StatisticsAction {

	private static final Logger logger = LoggerFactory.getLogger(ClearAction.class);

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final CacheHolder cacheHolder;

	public ClearAction(HttpServletRequest request, HttpServletResponse response, CacheHolder cacheHolder) {
		this.request = request;
		this.response = response;
		this.cacheHolder = cacheHolder;
	}

	@Override
	public void exectue() {
		String cacheName = request.getParameter(Statistics.CACHE_NAME_PARAMETER);
		if (StringUtils.isNotEmpty(cacheName)) {
			logger.info("Cache {} will be cleared", cacheName);
			cacheHolder.clear(cacheName);
		} else {
			logger.error("Error while reciving request. No key prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
