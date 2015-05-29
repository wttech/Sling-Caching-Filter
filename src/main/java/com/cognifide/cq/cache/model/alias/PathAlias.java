package com.cognifide.cq.cache.model.alias;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

public class PathAlias {

	private final String name;

	private final Set<Pattern> paths;

	PathAlias(String name, Set<Pattern> paths) {
		this.name = name;
		this.paths = paths;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PathAlias other = (PathAlias) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
