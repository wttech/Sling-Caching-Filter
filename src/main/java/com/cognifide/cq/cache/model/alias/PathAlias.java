package com.cognifide.cq.cache.model.alias;

import java.util.Collections;
import java.util.Set;

public class PathAlias {

	private final String name;

	private final Set<String> paths;

	public PathAlias(String name, Set<String> paths) {
		this.name = name;
		this.paths = paths;
	}

	public String getName() {
		return name;
	}

	public Set<String> getPaths() {
		return Collections.unmodifiableSet(paths);
	}

	public void addPath(String path) {
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
