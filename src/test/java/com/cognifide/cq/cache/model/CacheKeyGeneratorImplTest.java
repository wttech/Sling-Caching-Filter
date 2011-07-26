package com.cognifide.cq.cache.model;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.apache.sling.api.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bartosz Rudnicki
 */
public class CacheKeyGeneratorImplTest {

	private CacheKeyGeneratorImpl keyGenerator;

	private Resource resourceMock;

	@Before
	public void setUp() {
		keyGenerator = new CacheKeyGeneratorImpl();
		resourceMock = createMock(Resource.class);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGenerateKeyFromResourceWithNegativeCacheLevel() {
		expect(resourceMock.getPath()).andReturn("/some/path");
		replay(resourceMock);
		String key = keyGenerator.generateKey(-1, resourceMock, "text.txt");
		verify(resourceMock);
		assertEquals("/some/path.text.txt", key);
	}

	@Test
	public void testGenerateKeyFromResourceWithZeroCacheLevel() {
		expect(resourceMock.getResourceType()).andReturn("/some/resource/path");
		replay(resourceMock);
		String key = keyGenerator.generateKey(0, resourceMock, null);
		verify(resourceMock);
		assertEquals("/some/resource/path", key);
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel2() {
		expect(resourceMock.getResourceType()).andReturn("/some/resource/type/path");
		expect(resourceMock.getPath()).andReturn("/some/resource/path");
		replay(resourceMock);
		String key = keyGenerator.generateKey(2, resourceMock, "txt");
		verify(resourceMock);
		assertEquals("/some/resource/type/path/some/resource.txt", key);
	}

	@Test
	public void testGenerateKeyFromResourceWithPositiveCacheLevel3() {
		expect(resourceMock.getResourceType()).andReturn("some/resource/type/path");
		expect(resourceMock.getPath()).andReturn("/some/resource/path");
		replay(resourceMock);
		String key = keyGenerator.generateKey(3, resourceMock, null);
		verify(resourceMock);
		assertEquals("/apps/some/resource/type/path/some/resource/path", key);
	}

	@Test
	public void testGenerateKeyFromStringsWithNegativeCacheLevel1() {
		assertEquals("/prefix/pagePath",
				keyGenerator.generateKey(Integer.MIN_VALUE, "/prefix", "/pagePath", ""));
	}

	@Test
	public void testGenerateKeyFromStringsWithNegativeCacheLevel2() {
		assertEquals("/prefix/pagePath.txt",
				keyGenerator.generateKey(Integer.MIN_VALUE, "/prefix", "/pagePath", "txt"));
	}

	@Test
	public void testGenerateKeyFromSringsWithZeroCacheLevel1() {
		assertEquals("/prefix", keyGenerator.generateKey(0, "/prefix", null, null));
	}

	@Test
	public void testGenerateKeyFromSringsWithZeroCacheLevel2() {
		assertEquals("/prefix.ajax", keyGenerator.generateKey(0, "/prefix", null, "ajax"));
	}

	@Test
	public void testGenerateKeyFromStringsWithPositiveCacheLevel1() {
		assertEquals("/prefix/some.txt",
				keyGenerator.generateKey(1, "/prefix", "/some/page/path/string", "txt"));
	}

	@Test
	public void testGenerateKeyFromStringsWithPositiveCacheLevel2() {
		assertEquals("/prefix/some/page",
				keyGenerator.generateKey(2, "/prefix", "/some/page/path/string", null));
	}

	@Test
	public void testGenerateKeyFromStringsWithPositiveCacheLevel3() {
		assertEquals("prefix/some/page/path.txt.ajax",
				keyGenerator.generateKey(3, "prefix", "/some/page/path/string", "txt.ajax"));
	}

	@Test
	public void testGenerateKeyFromStringsWithPositiveCacheLevel4() {
		assertEquals("prefixpath.json", keyGenerator.generateKey(4, "prefix", "path", "json"));
	}
}
