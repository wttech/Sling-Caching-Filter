package com.cognifide.cq.cache.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class InvalidationPathUtil {

	private static PathAliasStore pathAliasStore = PathAliasStore.getInstance();

	public static List<String> getInvalidationPaths(String[] paths) {
		List<String> result = new ArrayList<String>();
		for (String path : paths) {
			if (StringUtils.isNotBlank(path)) {
				if (pathAliasStore.isAlias(path)) {
					Collection<String> pathsForAlias = pathAliasStore.getPathsForAlias(path);
					for (String realPath : pathsForAlias) {
						result.add(realPath);
					}
				} else {
					result.add(path);
				}
			}
		}
		return result;
	}

}
