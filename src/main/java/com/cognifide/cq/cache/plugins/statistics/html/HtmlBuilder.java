package com.cognifide.cq.cache.plugins.statistics.html;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.cognifide.cq.cache.plugins.statistics.StatisticEntry;
import com.cognifide.cq.cache.plugins.statistics.Statistics;
import com.cognifide.cq.cache.plugins.statistics.action.ActionCreator;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlBuilder {

	private static final Logger logger = LoggerFactory.getLogger(HtmlBuilder.class);

	private static final String OPENING_TAG_SIGN = "<";

	private static final String CLOSING_TAG_SIGN = ">";

	private static final String SLASH = "/";

	private static final String CLASS_ATTRIBUTE = " class=\"%s\"";

	private static final String STYLE_ATTRIBUTE = " style=\"%s\"";

	private static final String BR_TAG = OPENING_TAG_SIGN + "br " + SLASH + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG = OPENING_TAG_SIGN + "div" + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "div" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String DIV_OPENING_TAG_WITH_CLASS_AND_STYLE_ATTRIBUTE = OPENING_TAG_SIGN + "div" + CLASS_ATTRIBUTE + STYLE_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String DIV_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "div" + CLOSING_TAG_SIGN;

	private static final String LI_OPENING_TAG = OPENING_TAG_SIGN + "li" + CLOSING_TAG_SIGN;

	private static final String LI_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "li" + CLOSING_TAG_SIGN;

	private static final String SPAN_OPENING_TAG = OPENING_TAG_SIGN + "span" + CLOSING_TAG_SIGN;

	private static final String SPAN_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "span" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String SPAN_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "span" + CLOSING_TAG_SIGN;

	private static final String TD_OPENING_TAG = OPENING_TAG_SIGN + "td" + CLOSING_TAG_SIGN;

	private static final String TD_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "td" + CLOSING_TAG_SIGN;

	private static final String TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE = OPENING_TAG_SIGN + "th" + CLASS_ATTRIBUTE + CLOSING_TAG_SIGN;

	private static final String TH_CLOSING_TAG = OPENING_TAG_SIGN + SLASH + "th" + CLOSING_TAG_SIGN;

	private static final String CLEAR_CACHE_JAVA_SCRIPT = "\n$('span.ui-icon.ui-icon-trash').click(function() {\n"
			+ "  var key = $(this).parent().parent().find('.resource-type').html();\n"
			+ "  $.ajax({\n"
			+ "    url: 'slingcacheinclude',\n"
			+ "    type: 'POST',\n"
			+ "    context: $(this),\n"
			+ "    data: {"
			+ "      '" + Statistics.CACHE_NAME_PARAMETER + "': key,"
			+ "      '" + ActionCreator.ACTION_PARAMETER + "': '" + ActionCreator.DELETE.getParameter() + "'"
			+ "    }\n"
			+ "  }).done(function(data) {\n"
			+ "    $(this).siblings('span').text('Cache cleared.');\n"
			+ "  }).fail(function(data) {\n"
			+ "    $(this).siblings('span').text('There were some errors. Please see log file for details.');\n"
			+ "  });\n"
			+ "});";

	private static final String SHOW_KEYS_JAVA_SCRIPT = "\n$('div.bIcon.ui-icon-triangle-1-e').click(function() {\n"
			+ "  var key = $(this).next().text();"
			+ "  $.ajax({\n"
			+ "    url: 'slingcacheinclude',\n"
			+ "    type: 'POST',\n"
			+ "    context: $(this),\n"
			+ "    data: {"
			+ "      '" + Statistics.CACHE_NAME_PARAMETER + "': key,"
			+ "      '" + ActionCreator.ACTION_PARAMETER + "': '" + ActionCreator.SHOW_KEYS.getParameter() + "'"
			+ "    }\n"
			+ "  }).done(function(data) {\n"
			+ "    $(this).siblings('div.cache-keys').append(data);"
			+ "    $(this).parent().next().text($(data).children().length);"
			+ "  }).fail(function(data) {\n"
			+ "    $(this).siblings('div.cache-keys').text('There were some errors. Please see log file for details.');"
			+ "  });\n"
			+ "  $(this).siblings('div.cache-keys').toggle();\n"
			+ "  $(this).toggleClass(\"ui-icon-triangle-1-e ui-icon-triangle-1-s\");\n"
			+ "});";

	private static final String[] CACHE_HEADERS = {"Number", "Resource type", "Entries", "Hits", "Misses", "Ratio", "Clear cache"};

	private static final String[] GUARDS_HEADERS = {"Number", "Cache name", "Key"};

	public static CharSequence br() {
		return BR_TAG;
	}

	public static CharSequence div(Object... objects) {
		return DIV_OPENING_TAG + StringUtils.join(objects) + DIV_CLOSING_TAG;
	}

	public static CharSequence div(String cssClasses, Object o) {
		return String.format(DIV_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + DIV_CLOSING_TAG;
	}

	public static CharSequence div(String cssClasses, String styles, Object o) {
		return String.format(DIV_OPENING_TAG_WITH_CLASS_AND_STYLE_ATTRIBUTE, cssClasses, styles) + o.toString() + DIV_CLOSING_TAG;
	}

	public static CharSequence li(Object o) {
		return LI_OPENING_TAG + o.toString() + LI_CLOSING_TAG;
	}

	public static CharSequence span(Object... objects) {
		return SPAN_OPENING_TAG + StringUtils.join(objects) + SPAN_CLOSING_TAG;
	}

	public static CharSequence span(String cssClasses, Object o) {
		return String.format(SPAN_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + SPAN_CLOSING_TAG;
	}

	public static CharSequence td(Object... objects) {
		return TD_OPENING_TAG + StringUtils.join(objects) + TD_CLOSING_TAG;
	}

	public static CharSequence th(String cssClasses, Object o) {
		return String.format(TH_OPENING_TAG_WITH_CLASS_ATTRIBUTE, cssClasses) + o.toString() + TH_CLOSING_TAG;
	}

	public String build(CacheHolder cacheHolder, GuardCollectionWalker guardCollectionWalker) {
		Set<StatisticEntry> statisticEntries = readStatisticsFromMBean(cacheHolder);

		StringBuilder markup = new StringBuilder();

		markup.append(br())
				.append("<div class=\"table\">")
				.append(div("ui-widget-header ui-corner-top buttonGroup", "Sling Caching Filter (Statistics)"))
				.append("<table id=\"configTable\" class=\"tablesorter nicetable noauto ui-widget\">");
		appendHeaders(markup, CACHE_HEADERS);
		appendData(markup, statisticEntries);
		markup.append("</table>")
				.append("</div>");

		markup.append(br())
				.append("<div class=\"table\">")
				.append(div("ui-widget-header ui-corner-top buttonGroup", "Sling Caching Filter (Guards statistics)"))
				.append("<table id=\"guardTable\" class=\"tablesorter nicetable noauto ui-widget\">");
		appendHeaders(markup, GUARDS_HEADERS);
		appendData(markup, guardCollectionWalker);
		markup.append("</table>")
				.append("</div>");

		markup.append(div("Number of guards: " + guardCollectionWalker.getGuards().size()));

		appendJavaScript(markup);

		return markup.toString();
	}

	private Set<StatisticEntry> readStatisticsFromMBean(CacheHolder cacheHolder) {
		Set<StatisticEntry> statisticEntries = Sets.newHashSet();

		try {
			for (String cacheName : cacheHolder.getCacheNames()) {
				statisticEntries.add(new StatisticEntry(cacheHolder, cacheName));
			}

		} catch (AttributeNotFoundException x) {
			logger.error("Error while gathering statistics", x);
		} catch (MBeanException x) {
			logger.error("Error while gathering statistics", x);
		} catch (MalformedObjectNameException x) {
			logger.error("Error while gathering statistics", x);
		} catch (InstanceNotFoundException x) {
			logger.error("Error while gathering statistics", x);
		} catch (ReflectionException x) {
			logger.error("Error while gathering statistics", x);
		}

		return statisticEntries;
	}

	private void appendHeaders(StringBuilder markup, String[] headers) {
		markup.append("<thead>")
				.append("<tr>");
		for (String header : headers) {
			markup.append(th("ui-widget-header", header));
		}
		markup.append("</tr>")
				.append("</thead>");
	}

	private void appendData(StringBuilder markup, Set<StatisticEntry> statisticEntries) {
		markup.append("<tbody class=\"ui-widget-content\">");
		int index = 1;
		for (StatisticEntry statisticEntry : statisticEntries) {
			markup.append("<tr class=\"").append(index % 2 == 0 ? "even" : "odd").append(" ui-state-default\">")
					.append(td(index))
					.append("<td>")
					.append(div("bIcon ui-icon ui-icon-triangle-1-e", "display: inline-block;", "&nbsp;"))
					.append(div("resource-type", "display: inline-block;", statisticEntry.getCacheName()))
					.append("<div class=\"cache-keys\" style=\"display: none;\">");
			markup.append("</div>")
					.append("</td>")
					.append(td("Click expand to see the number. Notice that number of hits will increase on refresh."))
					.append(td(statisticEntry.getCacheHits()))
					.append(td(statisticEntry.getCacheMisses()))
					.append(td(String.format("%.2f%%", statisticEntry.getCacheHitPercentage())))
					.append(td(span("ui-icon ui-icon-trash", "&nbsp"), span("&nbsp;")))
					.append("</tr>");
			index++;
		}
		markup.append("</tbody>");
	}

	private void appendData(StringBuilder markup, GuardCollectionWalker guardCollectionWalker) {
		int index = 1;
		for (ExpiryGuard guard : guardCollectionWalker.getGuards()) {
			markup.append("<tr class=\"").append(index % 2 == 0 ? "even" : "odd").append(" ui-state-default\">")
					.append(td(index))
					.append(td(guard.getCacheName()))
					.append(td(guard.getKey()))
					.append("</tr>");
			index++;
		}
	}

	private void appendJavaScript(StringBuilder markup) {
		markup.append("<script type=\"text/javascript\">");
		markup.append("$(document).ready(function() {");
		markup.append(SHOW_KEYS_JAVA_SCRIPT);
		markup.append(CLEAR_CACHE_JAVA_SCRIPT);
		markup.append("});");
		markup.append("</script>");
	}

}
