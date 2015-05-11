package com.cognifide.cq.cache.model.alias;

import java.util.Collection;

/**
 * Store containing aliases for paths
 */
public interface PathAliasStore {

	/**
	 * Adds path alias to the store
	 *
	 * @param alias - path alias to be added
	 */
	void addAlias(PathAlias alias);

	/**
	 * Adds collection of path aliases to the store
	 *
	 * @param aliases - path aliases to be added
	 */
	void addAliases(Collection<PathAlias> aliases);

	/**
	 * Searches for alias and returns paths that were assigned to it
	 *
	 * @param aliasName - name of alias
	 * @return path assigned to given alias, empty collection otherwise
	 */
	Collection<String> getPathsForAlias(String aliasName);

	/**
	 * Checks if provided path is alias
	 *
	 * @param path - path to be checked
	 * @return true if given path is in store, false otherwise
	 */
	boolean isAlias(String path);

	/**
	 * Removes all stored aliases
	 */
	void clear();
}
