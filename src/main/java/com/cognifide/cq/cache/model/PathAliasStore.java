package com.cognifide.cq.cache.model;

import java.util.Collection;

public interface PathAliasStore {

	void addAlias(PathAlias alias);

	void addAliases(Collection<PathAlias> aliases);

	Collection<String> getPathsForAlias(String aliasName);

	boolean isAlias(String path);

}
