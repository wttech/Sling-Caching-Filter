package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.plugins.statistics.action.ActionCreator;
import com.cognifide.cq.cache.plugins.statistics.html.HtmlStatisticsBuilder;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.net.MediaType;
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

	@Reference
	private CacheHolder cacheHolder;

	@Reference
	private GuardCollectionWalker guardCollectionWalker;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write(new HtmlStatisticsBuilder().build(cacheHolder, guardCollectionWalker));
		response.setContentType(MediaType.HTML_UTF_8.toString());
		response.setCharacterEncoding(Charsets.UTF_8.toString());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Optional<ActionCreator> actionCreator = ActionCreator.from(request);
		if (actionCreator.isPresent()) {
			actionCreator.get().create(request, response, cacheHolder).exectue();
		} else {
			logger.error("Error. No valid [action] prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
