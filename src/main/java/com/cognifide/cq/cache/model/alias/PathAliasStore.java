package com.cognifide.cq.cache.model.alias;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Store containing aliases for paths
 */
public interface PathAliasStore {

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
	Collection<Pattern> getPathsForAlias(String aliasName);

	/**
	 * Checks if provided path is alias
	 *
	 * @param path - path to be checked
	 * @return true if given path is in store, false otherwise
	 */
	boolean isAlias(String path);
}
