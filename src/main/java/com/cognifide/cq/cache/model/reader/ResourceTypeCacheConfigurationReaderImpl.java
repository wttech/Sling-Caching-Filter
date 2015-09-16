package com.cognifide.cq.cache.model.reader;

import com.cognifide.cq.cache.definition.ResourceTypeCacheDefinition;
import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.model.alias.PathAliasStore;
import com.google.common.base.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bartosz Rudnicki
 */
@Service
@Component(immediate = true)
public class ResourceTypeCacheConfigurationReaderImpl implements ResourceTypeCacheConfigurationReader {

	private static final Logger logger = LoggerFactory.getLogger(ResourceTypeCacheConfigurationReaderImpl.class);

	@Reference(
			referenceInterface = ResourceTypeCacheDefinition.class,
			policy = ReferencePolicy.DYNAMIC,
			cardinality = ReferenceCardinality.MANDATORY_MULTIPLE,
			updated = "updateResourceTypeCacheDefinition",
			strategy = ReferenceStrategy.EVENT)
	private final ConcurrentMap<String, ResourceTypeCacheDefinition> resourceTypeCacheDefinitions
			= new ConcurrentHashMap<String, ResourceTypeCacheDefinition>(8);

	@Reference
	private PathAliasStore pathAliasStore;

	protected synchronized void bindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		String resourceType = resourceTypeCacheDefinition.getResourceType();
		if (resourceTypeCacheDefinitions.containsKey(resourceType)) {
			logger.warn("Resource type cache definition was already defined for {}", resourceType);
		}
		resourceTypeCacheDefinitions.putIfAbsent(resourceType, resourceTypeCacheDefinition);
	}

	protected synchronized void updateResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		resourceTypeCacheDefinitions.put(resourceTypeCacheDefinition.getResourceType(), resourceTypeCacheDefinition);
	}

	protected synchronized void unbindResourceTypeCacheDefinition(ResourceTypeCacheDefinition resourceTypeCacheDefinition) {
		resourceTypeCacheDefinitions.remove(resourceTypeCacheDefinition.getResourceType());
	}

	@Override
	public Optional<ResourceTypeCacheConfiguration> readComponentConfiguration(SlingHttpServletRequest request) {
		ResourceTypeCacheDefinition resourceTypeCacheDefinition
				= resourceTypeCacheDefinitions.get(request.getResource().getResourceType());
		return null != resourceTypeCacheDefinition
				? Optional.of(new ResourceTypeCacheConfiguration(resourceTypeCacheDefinition, pathAliasStore))
				: Optional.<ResourceTypeCacheConfiguration>absent();
	}
}
