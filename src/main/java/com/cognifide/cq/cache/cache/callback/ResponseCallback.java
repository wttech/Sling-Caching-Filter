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
package com.cognifide.cq.cache.cache.callback;

import com.cognifide.cq.cache.filter.CacheHttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
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
		this.filterChain = filterChain;
		this.request = request;
		this.response = response;
	}

	@Override
	public ByteArrayOutputStream doCallback() throws IOException, ServletException {
		CacheHttpServletResponseWrapper cacheResponse = new CacheHttpServletResponseWrapper(response);
		filterChain.doFilter(request, cacheResponse);
		cacheResponse.getWriter().flush();
		return cacheResponse.getContent();
	}
}
