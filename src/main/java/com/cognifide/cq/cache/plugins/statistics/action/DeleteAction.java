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
package com.cognifide.cq.cache.plugins.statistics.action;

import com.cognifide.cq.cache.plugins.statistics.Entry;
import com.cognifide.cq.cache.plugins.statistics.Statistics;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeleteAction implements StatisticsAction {

	private static final Log log = LogFactory.getLog(DeleteAction.class);

	private final ConcurrentMap<String, Entry> entries;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	public DeleteAction(ConcurrentMap<String, Entry> entries, HttpServletRequest request, HttpServletResponse response) {
		this.entries = entries;
		this.request = request;
		this.response = response;
	}

	@Override
	public void exectue() {
		String key = request.getParameter(Statistics.KEY_PARAMETER);
		if (StringUtils.isNotEmpty(key)) {
			log.info("Received key " + key);
			removeKey(key);
		} else {
			log.error("Error while reciving request. No key prameter.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	private void removeKey(String key) {
		if (entries.containsKey(key)) {
			log.info("Statistics contain entry with key " + key);
			Entry entry = entries.remove(key);
			entry.executeCacheActions();
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
		} else {
			log.error("Statistics do not contain entry with key " + key);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
