package com.cognifide.cq.cache.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;

@Component(immediate = true)
public class PathAliasStore {

	private final Map<String, PathAlias> aliases;

	public PathAliasStore() {
		aliases = new HashMap<String, PathAlias>();
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
