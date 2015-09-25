package com.cognifide.cq.cache.plugins.statistics.action;

import com.cognifide.cq.cache.cache.CacheEntity;
import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.plugins.statistics.StatisticsConstants;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.br;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.table;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.td;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.th;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.thead;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.tr;
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowDetailsAction implements StatisticsAction {

	private static final Logger logger = LoggerFactory.getLogger(ShowDetailsAction.class);

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final CacheHolder cacheHolder;

	public ShowDetailsAction(HttpServletRequest request, HttpServletResponse response, CacheHolder cacheHolder) {
		this.request = request;
		this.response = response;
		this.cacheHolder = cacheHolder;
	}

	@Override
	public void exectue() {
		String cacheName = request.getParameter(StatisticsConstants.CACHE_NAME_PARAMETER);
		if (StringUtils.isNotEmpty(cacheName)) {
			if (logger.isInfoEnabled()) {
				logger.info("Keys from {} cache will be collected", cacheName);
			}
			try {
				response.getWriter().write(generateContent(cacheName));
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				response.setContentType(MediaType.HTML_UTF_8.toString());
				response.setCharacterEncoding(Charsets.UTF_8.toString());
			} catch (IOException x) {
				logger.error("Error while generating markup.", x);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.error("Error while reading request. No {} parameter.", StatisticsConstants.CACHE_NAME_PARAMETER);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private String generateContent(String cacheName) throws IOException {
		StringBuilder markup = new StringBuilder(thead(tr(th("Key"), th("Size (bytes)"))));
		for (Map.Entry<String, CacheEntity> entry : cacheHolder.getValuesFor(cacheName).entrySet()) {
			markup.append(tr(td(entry.getKey()), td(entry.getValue().sizeInBytes())));
		}
		return br() + table(markup);
	}
}
