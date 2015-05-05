package com.cognifide.cq.cache.definition;

/**
 * Holds resource type definition, that will be cached.
 */
public interface ResourceTypeCacheDefinition {

	/**
	 * Tells weather cache is enabled/disabled on given component. By default false is returned.
	 *
	 * @return true if cache is enabled, false otherwise
	 */
	Boolean isEnabled();

	/**
	 * Resource type of resource, for which cache definition was created.
	 *
	 * @return resource type
	 */
	String getResourceType();

	/**
	 * Specifies cache entry validity time in seconds. If not set duration property read from the OSGi console will be
	 * used.
	 *
	 * @return validity time in seconds
	 */
	Integer getValidityTimeInSeconds();

	/**
	 * Specifies the level of component caching. By default set to -1.
	 *
	 * @return level of cache
	 */
	Integer getCacheLevel();

	/**
	 * When set to true cached instance will be refreshed if it has been changed. By default set to true.
	 *
	 * @return true if resource should invalidate on itself, false otherwise
	 */
	Boolean isInvalidateOnSelf();

	/**
	 * List of component fields that store links to content/configuration/etc. pages. Links from those fields are loaded
	 * and each content change inside nodes pointed to by those links will invalidate cache of the current component.
	 * Empty array will be returned by default.
	 *
	 * @return array of referenced fields
	 */
	String[] getInvalidateOnReferencedFields();

	/**
	 * List of paths (regular expressions). If a path of any changed JCR node matches any path from the list then the
	 * cache of the current component is invalidated. Empty array will be returned by default.
	 *
	 * @return array of paths on which invalidation should be triggered
	 */
	String[] getInvalidateOnPaths();

}
