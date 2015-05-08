package com.cognifide.cq.cache.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(immediate = true)
@Service
public class PathAliasStoreImpl implements PathAliasStore {

	private final Map<String, PathAlias> aliases;

	public PathAliasStoreImpl() {
		aliases = new HashMap<String, PathAlias>();
	}

	@Override
	public boolean isAlias(String path) {
		return aliases.containsKey(path);
	}

	@Override
	public Collection<String> getPathsForAlias(String aliasName) {
		Collection<String> paths = null;
		if (aliasName != null) {
			PathAlias pathAlias = aliases.get(aliasName);
			paths = pathAlias.getPaths();
		}
		return paths;
	}

	@Override
	public void addAlias(PathAlias alias) {
		aliases.put(alias.getName(), alias);
	}

	@Override
	public void addAliases(Collection<PathAlias> aliases) {
		for (PathAlias alias : aliases) {
			addAlias(alias);
		}
	}

	public void clear() {
		aliases.clear();
	}
}
