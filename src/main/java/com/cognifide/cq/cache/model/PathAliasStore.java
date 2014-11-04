package com.cognifide.cq.cache.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PathAliasStore {

	private final Map<String, PathAlias> aliases = new HashMap<String, PathAlias>();

	private static final PathAliasStore INSTANCE = new PathAliasStore();

	private PathAliasStore() {
	}

	public static PathAliasStore getInstance() {
		return INSTANCE;
	}

	public boolean isAlias(String path) {
		return aliases.containsKey(path);
	}

	public Collection<String> getPathsForAlias(String aliasName) {
		Collection<String> paths = null;
		if (aliasName != null) {
			PathAlias pathAlias = aliases.get(aliasName);
			paths = pathAlias.getPaths();
		}
		return paths;
	}

	public void addAlias(PathAlias alias) {
		aliases.put(alias.getName(), alias);
	}

	public void addAliases(Collection<PathAlias> aliases) {
		for (PathAlias alias : aliases) {
			addAlias(alias);
		}
	}

	public void clear() {
		aliases.clear();
	}
}
