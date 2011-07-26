package com.cognifide.cq.cache.refresh.jcr;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.cognifide.cq.cache.model.InvalidationPathUtil;

/**
 * @author Bartosz Rudnicki
 */
public class TagJcrRefreshPolicy extends JcrRefreshPolicy {

	private static final long serialVersionUID = 4121969163004277696L;

	public TagJcrRefreshPolicy(String key, int duration, String selfPath, String patterns) {
		super(key, duration);

		// self invalidation
		if (StringUtils.isNotBlank(selfPath)) {
			invalidatePaths.add(Pattern.compile(selfPath + ".*"));
		}

		// provided patterns
		if (StringUtils.isNotBlank(patterns)) {
			String[] patternsArray = patterns.split(";");
			List<String> invalidationPaths = InvalidationPathUtil.getInvalidationPaths(patternsArray);
			for (String path : invalidationPaths) {
				invalidatePaths.add(Pattern.compile(path));
			}
		}
	}
}
