package com.cognifide.cq.cache.refresh.jcr;

import static junit.framework.Assert.assertEquals;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.cognifide.cq.cache.test.utils.ReflectionHelper;

/**
 * @author Bartosz Rudnicki
 */
public class TagJcrRefreshPolicyTest extends JcrRefreshPolicyTestBase<TagJcrRefreshPolicy> {

	private static final String INSTANCE_INVALIDATE_PATH_REGEX = INSTANCE_INVALIDATE_PATH + ".*";

	@Override
	protected TagJcrRefreshPolicy getNewRefreshPolicyInstance(String key, String[] paths) {
		StringBuilder patterns = new StringBuilder();
		for (String path : paths) {
			if (patterns.length() > 0) {
				patterns.append(";");
			}
			patterns.append(path);
		}

		return new TagJcrRefreshPolicy(key, DEFAULT_REFRESH_TIME, null, patterns.toString());
	}

	@Test
	public void testCreation() throws Exception {
		TagJcrRefreshPolicy policy = new TagJcrRefreshPolicy(INSTANCE_KEY, DEFAULT_REFRESH_TIME,
				INSTANCE_INVALIDATE_PATH, OTHER_KEY + ";" + OTHER_INVALIDATE_PATH);

		assertEquals(INSTANCE_KEY, ReflectionHelper.get(TagJcrRefreshPolicy.class, "cacheEntryKey", policy));
		assertEquals(DEFAULT_REFRESH_TIME, policy.getRefreshPeriod());

		List<Pattern> invalidatePaths = ReflectionHelper.get(TagJcrRefreshPolicy.class, "invalidatePaths",
				policy);

		assertEquals(3, invalidatePaths.size());
		assertEquals(INSTANCE_INVALIDATE_PATH_REGEX, invalidatePaths.get(0).pattern());
		assertEquals(OTHER_KEY, invalidatePaths.get(1).pattern());
		assertEquals(OTHER_INVALIDATE_PATH, invalidatePaths.get(2).pattern());
	}
}
