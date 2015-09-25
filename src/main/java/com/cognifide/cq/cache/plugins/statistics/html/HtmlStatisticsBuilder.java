package com.cognifide.cq.cache.plugins.statistics.html;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.cognifide.cq.cache.expiry.collection.GuardCollectionWalker;
import com.cognifide.cq.cache.expiry.guard.ExpiryGuard;
import com.cognifide.cq.cache.plugins.statistics.entry.StatisticEntry;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.br;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.css;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.div;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.id;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.script;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.span;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.style;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.table;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.tbody;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.td;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.th;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.thead;
import static com.cognifide.cq.cache.plugins.statistics.html.HtmlBuilder.tr;
import static com.cognifide.cq.cache.plugins.statistics.html.JavaScriptBuilder.js;
import static com.cognifide.cq.cache.plugins.statistics.html.JavaScriptBuilder.onReady;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HtmlStatisticsBuilder {

	private static final List<String> GUARDS_HEADERS = Lists.newArrayList("Number", "Cache name", "Key");

	public String build(CacheHolder cacheHolder, GuardCollectionWalker guardCollectionWalker) {

		StringBuilder markup = new StringBuilder();

		Set<StatisticEntry> statisticEntries = readStatisticsFromMBean(cacheHolder);
		markup.append(statisticEntiresDetails(statisticEntries));
		markup.append(guardsDetails(guardCollectionWalker));

		return markup.append(generateJs()).toString();
	}

	private Set<StatisticEntry> readStatisticsFromMBean(CacheHolder cacheHolder) {
		Set<StatisticEntry> statisticEntries = Sets.newHashSet();

		for (String cacheName : cacheHolder.getCacheNames()) {
			statisticEntries.add(new StatisticEntry.Builder(cacheHolder, cacheName).build());
		}

		return statisticEntries;
	}

	private Collection<String> entriesHeaders(Set<StatisticEntry> statisticEntries) {
		List<String> headers = Lists.newArrayList("Entries", "Resource type", "Number");
		Iterator<StatisticEntry> iterator = statisticEntries.iterator();
		headers.addAll(iterator.hasNext() ? iterator.next().findHeaders() : Arrays.asList(""));
		headers.add("Clear cache");
		return headers;
	}

	private String headers(Collection<String> headers) {
		StringBuilder markup = new StringBuilder();

		for (String header : headers) {
			markup.append(th(header));
		}

		return thead(tr(markup));
	}

	private StringBuilder statisticEntiresDetails(Set<StatisticEntry> statisticEntries) {
		return new StringBuilder(br())
				.append(
						css(div(
								css(div("Sling Caching Filter (Statistics)"), "ui-widget-header ui-corner-top buttonGroup"),
								id(css(table(
										headers(entriesHeaders(statisticEntries)),
										allStatisticsEntriesDetails(statisticEntries)),
										"tablesorter nicetable noauto ui-widget"),
										"configTable")),
								"table"));
	}

	private String allStatisticsEntriesDetails(Set<StatisticEntry> statisticEntries) {
		StringBuilder markup = new StringBuilder();
		int index = 1;
		for (StatisticEntry statisticEntry : statisticEntries) {
			markup.append(css(tr(statisticEntryDetail(index, statisticEntry)), rowCssClass(index)));
			index++;
		}
		return tbody(markup);
	}

	private StringBuilder statisticEntryDetail(int index, StatisticEntry statisticEntry) {
		StringBuilder markup = new StringBuilder(td(index))
				.append(td(
						css(style(div("&nbsp;"), "display: inline-block;"), "bIcon ui-icon ui-icon-triangle-1-e"),
						css(style(div(statisticEntry.getCacheName()), "display: inline-block;"), "resource-type"),
						css(style(div("&nbsp;"), "display: none;"), "cache-keys")))
				.append(td("Click expand to see the number. Notice that number of hits will increase on refresh."));

		for (Number value : statisticEntry.findValues()) {
			markup.append(td(value));
		}

		return markup.append(td(css(span("&nbsp"), "ui-icon ui-icon-trash"), span("&nbsp;")));
	}

	private String rowCssClass(int index) {
		return (index % 2 == 0 ? "even" : "odd") + "  ui-state-default";
	}

	private StringBuilder guardsDetails(GuardCollectionWalker guardCollectionWalker) {
		return new StringBuilder(br())
				.append(
						css(div(
								css(div("Sling Caching Filter (Guards statistics)"), "ui-widget-header ui-corner-top buttonGroup"),
								id(css(table(
										headers(GUARDS_HEADERS),
										allGuardDetails(guardCollectionWalker)),
										"tablesorter nicetable noauto ui-widget"),
										"guardTable")),
								"table"))
				.append(div("Number of guards: " + guardCollectionWalker.getGuards().size()));
	}

	private String allGuardDetails(GuardCollectionWalker guardCollectionWalker) {
		StringBuilder markup = new StringBuilder();
		int row = 1;
		for (ExpiryGuard guard : guardCollectionWalker.getGuards()) {
			markup.append(
					css(tr(
							td(row),
							td(guard.getCacheName()),
							td(guard.getKey())),
							rowCssClass(row)));
			row++;
		}
		return tbody(markup);
	}

	private String generateJs() {
		return script(onReady(
				js("/js/statistics/showDetails.js"),
				js("/js/statistics/clearCache.js")));
	}
}
