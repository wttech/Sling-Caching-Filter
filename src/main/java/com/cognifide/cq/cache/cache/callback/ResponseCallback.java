package com.cognifide.cq.cache.cache.callback;

import com.cognifide.cq.cache.cache.ByteStreamEntity;
import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.filter.CacheHttpServletResponseWrapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.sling.api.SlingHttpServletRequest;

public class ResponseCallback implements MissingCacheEntryCallback {

	private final FilterChain filterChain;

	private final SlingHttpServletRequest request;

	private final HttpServletResponse response;

	public ResponseCallback(FilterChain filterChain, SlingHttpServletRequest request, HttpServletResponse response) {
		this.filterChain = Preconditions.checkNotNull(filterChain);
		this.request = Preconditions.checkNotNull(request);
		this.response = Preconditions.checkNotNull(response);
	}

	@Override
	public CacheEntity generateCacheEntity() throws IOException, ServletException {
		CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper(response);
		filterChain.doFilter(request, cacheResponse);
		cacheResponse.getWriter().flush();
		return new ByteStreamEntity(cacheResponse.getContentType(), cacheResponse.getContent());
	}
}
