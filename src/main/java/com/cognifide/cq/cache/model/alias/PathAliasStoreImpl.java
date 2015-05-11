package com.cognifide.cq.cache.model.alias;

import java.util.Collection;
import java.util.Collections;
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
		Collection<String> paths = Collections.emptySet();
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

	@Override
	public void clear() {
		aliases.clear();
	}
}
