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
			if (logger.isInfoEnabled()) {
				logger.info("Cache {} will be cleared", cacheName);
			}
			cacheHolder.clear(cacheName);
		} else {
			logger.error("Error while reciving request. No {} parameter.", Statistics.CACHE_NAME_PARAMETER);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
