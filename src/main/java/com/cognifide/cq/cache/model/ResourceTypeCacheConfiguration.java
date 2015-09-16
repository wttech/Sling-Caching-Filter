package com.cognifide.cq.cache.model;

import com.cognifide.cq.cache.definition.CacheConfigurationEntryImpl;
import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * @author Bartosz Rudnicki
 */
public class ResourceTypeCacheConfiguration extends CacheConfigurationEntryImpl {

	private final boolean enabled;

	private final ResourceTypeCacheDefinition resourceTypeCacheDefinition;

	private final PathAliasStore pathAliasStore;

	private final Set<String> invalidationPathPrefixes;

	private final Set<Pattern> invalidationPatterns;

	public ResourceTypeCacheConfiguration(ResourceTypeCacheDefinition resourceTypeCacheDefinition, PathAliasStore pathAliasStore) {
		super(resourceTypeCacheDefinition);

		this.enabled = resourceTypeCacheDefinition.isEnabled();
		this.resourceTypeCacheDefinition = resourceTypeCacheDefinition;
		this.pathAliasStore = Preconditions.checkNotNull(pathAliasStore);

		this.invalidationPathPrefixes = Sets.newHashSet();
		this.invalidationPatterns = Sets.newHashSet();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Iterable<String> getInvalidationPathPrefixes() {
		return Collections.unmodifiableSet(invalidationPathPrefixes);
	}

	public Iterable<Pattern> getInvalidationPatterns() {
		return Collections.unmodifiableSet(invalidationPatterns);
	}

	public void generateInvalidationPathsFor(SlingHttpServletRequest request) {
		new DefinitionPathTranslator(pathAliasStore, resourceTypeCacheDefinition, this, request.getResource())
				.translatePaths();
	}

	void addInvalidationPathPrefix(String invalidationPathPrefix) {
		if (StringUtils.isNotBlank(invalidationPathPrefix)) {
			this.invalidationPathPrefixes.add(invalidationPathPrefix);
		}
	}

	void addInvalidationPattern(Pattern pattern) {
		this.invalidationPatterns.add(pattern);
	}

	void addInvalidationPatterns(Collection<Pattern> patterns) {
		invalidationPatterns.addAll(patterns);
	}
}
