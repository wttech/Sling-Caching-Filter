package com.cognifide.cq.cache.refresh.jcr;

import static junit.framework.Assert.assertEquals;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.cognifide.cq.cache.model.ResourceTypeCacheConfiguration;
import com.cognifide.cq.cache.test.utils.ReflectionHelper;

/**
 * @author Bartosz Rudnicki
 */
public class FilterJcrRefreshPolicyTest extends JcrRefreshPolicyTestBase<FilterJcrRefreshPolicy> {

	private static final String RESOURCE_TYPE_PATH = "/apps/components/test";

	private static final String RESOURCE_TYPE_PATH_REGEX = "/apps/components/test.*";

	@Override
	protected FilterJcrRefreshPolicy getNewRefreshPolicyInstance(String key, String[] paths) {
		ResourceTypeCacheConfiguration resourceTypeCacheConfiguration = new ResourceTypeCacheConfiguration(
				(String) null, DEFAULT_REFRESH_TIME);

		for (String path : paths) {
			resourceTypeCacheConfiguration.addInvalidatePath(path);
		}

		return new FilterJcrRefreshPolicy(key, resourceTypeCacheConfiguration);
	}

	@Test
	public void testObjectCreation() throws Exception {
		ResourceTypeCacheConfiguration resourceTypeCacheConfiguration = new ResourceTypeCacheConfiguration(
				(String) null, DEFAULT_REFRESH_TIME);
		resourceTypeCacheConfiguration.addInvalidatePath(INSTANCE_INVALIDATE_PATH);
		resourceTypeCacheConfiguration.addInvalidatePath(OTHER_INVALIDATE_PATH);
		resourceTypeCacheConfiguration.setResourceTypePath(RESOURCE_TYPE_PATH);

		FilterJcrRefreshPolicy policy = new FilterJcrRefreshPolicy(INSTANCE_KEY,
				resourceTypeCacheConfiguration);

		assertEquals(INSTANCE_KEY,
				ReflectionHelper.get(FilterJcrRefreshPolicy.class, "cacheEntryKey", policy));
		assertEquals(DEFAULT_REFRESH_TIME, policy.getRefreshPeriod());

		List<Pattern> invalidatePaths = ReflectionHelper.get(FilterJcrRefreshPolicy.class, "invalidatePaths",
				policy);

		assertEquals(3, invalidatePaths.size());
		assertEquals(INSTANCE_INVALIDATE_PATH, invalidatePaths.get(0).pattern());
		assertEquals(OTHER_INVALIDATE_PATH, invalidatePaths.get(1).pattern());
		assertEquals(RESOURCE_TYPE_PATH_REGEX, invalidatePaths.get(2).pattern());
	}
}
