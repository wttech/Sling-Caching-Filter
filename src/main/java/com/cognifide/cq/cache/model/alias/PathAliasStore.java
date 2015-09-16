package com.cognifide.cq.cache.model.alias;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Store containing aliases for paths
 */
public interface PathAliasStore {

	/**
	 * Populates store with aliases.
	 *
	 * @param aliasesString - aliases string. Syntax: `$&lt;alias name&gt;|&lt;path 1&gt;|&lt;path 2&gt; |...`, where
	 * `$` is a mandatory character before alias name, and `|` is a separator between paths
	 */
	void populate(String[] aliasesString);

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
