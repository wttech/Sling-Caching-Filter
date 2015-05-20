package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.filter.cache.action.CacheAction;
import com.cognifide.cq.cache.plugins.statistics.action.DeleteAction;
import com.cognifide.cq.cache.plugins.statistics.action.ShowKeysAction;
import com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component
@Service(value = {javax.servlet.Servlet.class, Statistics.class})
@Properties({
	@Property(name = "felix.webconsole.label", value = "slingcacheinclude"),
	@Property(name = "felix.webconsole.title", value = "Sling Cache Include"),
	@Property(name = "felix.webconsole.category", value = "Sling")})
public class StatisticsImpl extends HttpServlet implements Statistics {

	private static final long serialVersionUID = -5474968941326180454L;

	private static final Log log = LogFactory.getLog(StatisticsImpl.class);

	private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<String, Entry>();

	@Override
	public void cacheMiss(String resourceType, String key, CacheAction cacheAction) {
		Entry entry = getEntry(resourceType);
		entry.addKey(key);
		entry.addCacheAction(cacheAction);
		entry.cacheMiss();
	}

	private synchronized Entry getEntry(String resourceType) {
		return entries.containsKey(resourceType)
				? entries.get(resourceType) : createEntry(resourceType);
	}

	private Entry createEntry(String resourceType) {
		Entry entry = new Entry();
		entries.put(resourceType, entry);
		return entry;
	}

	@Override
	public void cacheHit(String resourceType) {
		Entry entry = entries.get(resourceType);
		entry.cacheHit();
	}

	@Override
	public void clearStatistics() {
		entries.clear();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write(new HtmlBuilder().build(entries));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter(ACTION_PARAMETER);
		if (StringUtils.isNotEmpty(action)) {
			if (DELETE_ACTION_PARAMETER_VALUE.equals(action)) {
				new DeleteAction(entries, request, response).exectue();
			} else if (SHOW_KEYS_ACTION_PARAMETER_VALUE.equals(action)) {
				new ShowKeysAction(entries, request, response).exectue();
			} else {
				log.error("Error while reciving request. Invalid prameter.");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			log.error("Error while reciving request. No action prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
