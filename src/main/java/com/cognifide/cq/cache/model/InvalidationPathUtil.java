package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.model.alias.PathAliasStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class InvalidationPathUtil {

	public static List<String> getInvalidationPaths(PathAliasStore pathAliasStores, String[] paths) {
		List<String> result = new ArrayList<String>();
		for (String path : paths) {
			if (StringUtils.isNotBlank(path)) {
				if (pathAliasStores.isAlias(path)) {
					Collection<String> pathsForAlias = pathAliasStores.getPathsForAlias(path);
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
