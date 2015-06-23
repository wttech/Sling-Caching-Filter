package com.cognifide.cq.cache.definition;

/**
 * Holds resource type definition, that will be cached.
 */
public interface ResourceTypeCacheDefinition extends CacheConfigurationEntry {

	/**
	 * Tells weather cache is enabled/disabled on given component. By default false is returned.
	 *
	 * @return true if cache is enabled, false otherwise
	 */
	Boolean isEnabled();

	/**
	 * When set to true cached instance will be refreshed if it has been changed. By default set to true.
	 *
	 * @return true if resource should invalidate on itself, false otherwise
	 */
	Boolean isInvalidateOnSelf();

	/**
	 * When set to true cached instance will be refreshed if page containing this instance has been changed
	 *
	 * @return true if resource should invalidate on containing page change, false otherwise
	 */
	Boolean isInvalidateOnContainingPage();

	/**
	 * List of component fields that store links to content/configuration/etc. pages. Links from those fields are loaded
	 * and each content change inside nodes pointed to by those links will invalidate cache of the current component.
	 * Empty array will be returned by default.
	 *
	 * @return array of referenced fields, does not contain null or blank values
	 */
	String[] getInvalidateOnReferencedFields();

	/**
	 * List of paths (regular expressions). If a path of any changed JCR node matches any path from the list then the
	 * cache of the current component is invalidated. Empty array will be returned by default.
	 *
	 * @return array of paths on which invalidation should be triggered, does not contain null or blank values
	 */
	String[] getInvalidateOnPaths();

	/**
	 * Check definition validity
	 *
	 * @return true if definition is valid, false otherwise
	 */
	boolean isValid();
}
