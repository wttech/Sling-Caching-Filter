package com.cognifide.cq.cache.plugins.statistics;

import com.cognifide.cq.cache.filter.cache.action.CacheAction;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.div;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.li;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.span;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.td;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.th;
import java.io.IOException;
import java.util.Map;
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

		StringBuilder markup = new StringBuilder();

		String[] headers = {"Number", "Resource type", "Entries", "Hits", "Misses", "Ratio", "Clear cache"};

		markup.append("<br />")
				.append("<div class=\"table\">")
				.append(div("ui-widget-header ui-corner-top buttonGroup", "Sling Caching Filter (Statistics)"))
				.append("<table id=\"configTable\" class=\"tablesorter nicetable noauto ui-widget\">")
				.append("<thead>")
				.append("<tr>");
		for (String header : headers) {
			markup.append(th("ui-widget-header", header));
		}
		markup.append("</tr>")
				.append("</thead>")
				.append("<tbody class=\"ui-widget-content\">");
		int index = 1;
		for (Map.Entry<String, Entry> entrySet : entries.entrySet()) {
			String resourceType = entrySet.getKey();
			markup.append("<tr class=\"").append(index % 2 == 0 ? "even" : "odd").append(" ui-state-default\">")
					.append(td(index))
					.append("<td>")
					.append(div("bIcon ui-icon ui-icon-triangle-1-e", "display: inline-block;", "&nbsp;"))
					.append(div("resource-type", "display: inline-block;", resourceType))
					.append("<div class=\"cache-keys\" style=\"display: none;\">")
					.append("<ul>");

			Entry value = entrySet.getValue();
			for (String key : value.getKeys()) {
				markup.append(li(key));
			}

			long hits = value.getHits();
			long misses = value.getMisses();
			markup.append("</ul>")
					.append("</div>")
					.append("</td>")
					.append(td(value.getKeys().size()))
					.append(td(hits))
					.append(td(misses))
					.append(td(String.format("%.2f%%", computeHitRatio(misses, hits))))
					.append(td(span("ui-icon ui-icon-trash", "&nbsp"), span("&nbsp;")))
					.append("</tr>");
			index++;
		}

		markup.append("</tbody>")
				.append("</table>")
				.append("</div>");

		resp.getWriter().print(markup.toString());

		StringBuilder javaScript = new StringBuilder();

		javaScript.append("<script type=\"text/javascript\">");
		javaScript.append("$(document).ready(function() {");
		javaScript.append("$('div.bIcon.ui-icon-triangle-1-e').click(function() {\n"
				+ "  $(this).siblings('div.cache-keys').toggle();\n"
				+ "  $(this).toggleClass(\"ui-icon-triangle-1-e ui-icon-triangle-1-s\");\n"
				+ "});");
		javaScript.append("$('span.ui-icon.ui-icon-trash').click(function() {\n"
				+ "  var key = $(this).parent().parent().find('.resource-type').html();\n"
				+ "  console.info(key);\n"
				+ "  $.ajax({\n"
				+ "    url: 'slingcacheinclude',\n"
				+ "    type: 'POST',\n"
				+ "    context: $(this),\n"
				+ "    data: {'key': key}\n"
				+ "  }).done(function(data) {\n"
				+ "    $(this).siblings('span').text('Cache cleared.');\n"
				+ "  }).fail(function(data) {\n"
				+ "    $(this).siblings('span').text('There were some errors. Please see log');\n"
				+ "  });\n"
				+ "});");
		javaScript.append("});");
		javaScript.append("</script>");
		resp.getWriter().write(javaScript.toString());
	}

	private double computeHitRatio(long misses, long hits) {
		return (hits * 100L) / (double) (hits + misses);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String key = request.getParameter("key");
		if (StringUtils.isNotEmpty(key)) {
			log.info("Received key " + key);
			if (entries.containsKey(key)) {
				log.info("Statistics contain entry with key " + key);
				Entry entry = entries.remove(key);
				entry.executeCacheActions();
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
			} else {
				log.error("Statistics do not contain entry with key " + key);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

		} else {
			log.error("Error while reciving request. No key prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
