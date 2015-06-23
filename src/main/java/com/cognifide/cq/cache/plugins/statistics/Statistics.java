package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.plugins.statistics.action.ClearAction;
import com.cognifide.cq.cache.plugins.statistics.action.ShowKeysAction;
import com.cognifide.cq.cache.plugins.statistics.action.StatisticsAction;
import com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service(value = {javax.servlet.Servlet.class})
@Properties({
	@Property(name = "felix.webconsole.label", value = "slingcacheinclude"),
	@Property(name = "felix.webconsole.title", value = "Sling Cache Include"),
	@Property(name = "felix.webconsole.category", value = "Sling")})
public class Statistics extends HttpServlet {

	private static final long serialVersionUID = -5474968941326180454L;

	private static final Logger logger = LoggerFactory.getLogger(Statistics.class);

	public static final String CACHE_NAME_PARAMETER = "cacheName";

	public static final String ACTION_PARAMETER = "action";

	public static final String DELETE_ACTION_PARAMETER_VALUE = "delete";

	public static final String SHOW_KEYS_ACTION_PARAMETER_VALUE = "showKeys";

	@Reference
	private CacheHolder cacheHolder;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write(new HtmlBuilder().build(cacheHolder));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StatisticsAction statisticsAction = readActionFromRequest(request, response);
		if (null != statisticsAction) {
			statisticsAction.exectue();
		} else {
			logger.error("Error. No valid [action] prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private StatisticsAction readActionFromRequest(HttpServletRequest request, HttpServletResponse response) {
		StatisticsAction statisticsAction = null;

		String action = request.getParameter(ACTION_PARAMETER);
		if (DELETE_ACTION_PARAMETER_VALUE.equals(action)) {
			statisticsAction = new ClearAction(request, response, cacheHolder);
		} else if (SHOW_KEYS_ACTION_PARAMETER_VALUE.equals(action)) {
			statisticsAction = new ShowKeysAction(request, response, cacheHolder);
		}

		return statisticsAction;
	}

}
