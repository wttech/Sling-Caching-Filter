package com.cognifide.cq.cache.model.alias;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

class PathAlias {

	private final String name;

	private final Set<Pattern> paths;

	PathAlias(String name, Set<Pattern> paths) {
		this.name = Preconditions.checkNotNull(name);
		this.paths = Preconditions.checkNotNull(paths);
	}

	String getName() {
		return name;
	}

	Set<Pattern> getPaths() {
		return Collections.unmodifiableSet(paths);
	}

	void addPath(Pattern path) {
		paths.add(path);
	}

}
