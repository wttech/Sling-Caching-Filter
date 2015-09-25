package com.cognifide.cq.cache.plugins.statistics.html;

import com.cognifide.cq.cache.plugins.statistics.StatisticsConstants;
import com.google.common.collect.Maps;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.jackrabbit.oak.commons.IOUtils;

public class JavaScriptBuilder {

	private static final String ON_READY_JAVA_SCRIPT = "$(document).ready(function() {\n";

	private static final String END_JAVA_SCRIPT = "});";

	private static final StrSubstitutor SUBSTITUTOR;

	static {
		Map<String, String> map = Maps.newHashMap();
		map.put("parameter.cacheName", StatisticsConstants.CACHE_NAME_PARAMETER);
		map.put("parameter.action", StatisticsConstants.ACTION_PARAMETER);
		map.put("action.delete", StatisticsConstants.DELETE_ACTION);
		map.put("action.showDetails", StatisticsConstants.SHOW_DETAILS_ACTION);
		SUBSTITUTOR = new StrSubstitutor(map);
	}

	private JavaScriptBuilder() {
		throw new AssertionError();
	}

	public static String js(String filePath) {
		StringBuilder markup = new StringBuilder();
		InputStream is = null;
		try {
			is = JavaScriptBuilder.class.getResourceAsStream(filePath);
			Scanner scanner = new Scanner(is);
			while (scanner.hasNextLine()) {
				markup.append(scanner.nextLine()).append("\n");
			}
		} finally {
			if (null != is) {
				IOUtils.closeQuietly(is);
			}
		}

		return SUBSTITUTOR.replace(markup);
	}

	public static String onReady(Object... objects) {
		return ON_READY_JAVA_SCRIPT + StringUtils.join(objects) + END_JAVA_SCRIPT;
	}
}
