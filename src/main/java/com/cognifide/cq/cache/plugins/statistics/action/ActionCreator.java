package com.cognifide.cq.cache.plugins.statistics.action;

import com.cognifide.cq.cache.cache.CacheHolder;
import com.google.common.base.Optional;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

public enum ActionCreator {
	DELETE("delete") {

		@Override
		public StatisticsAction create(HttpServletRequest request, HttpServletResponse response, CacheHolder cacheHolder) {
			return new ClearAction(request, response, cacheHolder);
		}
	},
	SHOW_KEYS("showKeys") {

		@Override
		public StatisticsAction create(HttpServletRequest request, HttpServletResponse response, CacheHolder cacheHolder) {
			return new ShowKeysAction(request, response, cacheHolder);
		}
	};

	public static final String ACTION_PARAMETER = "action";

	private final String parameter;

	private ActionCreator(String parameter) {
		this.parameter = parameter;
	}

	public String getParameter() {
		return parameter;
	}

	public abstract StatisticsAction create(HttpServletRequest request, HttpServletResponse response, CacheHolder cacheHolder);

	public static Optional<ActionCreator> from(ServletRequest request) {
		Optional<ActionCreator> result = Optional.absent();
		String actionParameter = request.getParameter(ACTION_PARAMETER);
		if (StringUtils.isNotBlank(actionParameter)) {
			for (ActionCreator action : ActionCreator.values()) {
				if (action.getParameter().equalsIgnoreCase(actionParameter)) {
					result = Optional.of(action);
					break;
				}
			}
		}
		return result;
	}
}
