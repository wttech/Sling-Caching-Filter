package com.cognifide.cq.cache.tag;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cognifide.cq.cache.filter.ComponentCacheFilter;
import com.cognifide.cq.cache.model.CacheKeyGenerator;
import com.cognifide.cq.cache.refresh.jcr.JcrEventListener;
import com.cognifide.cq.cache.refresh.jcr.JcrEventsService;
import com.cognifide.cq.cache.refresh.jcr.TagJcrRefreshPolicy;
import com.cognifide.cq.cache.test.utils.ReflectionHelper;
import com.cognifide.cq.cache.test.utils.ServletContextStub;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;

/**
 * @author Bartosz Rudnicki
 */
public class CacheTagTest {

	private static final String KEY = "key";

	private static final String GENERATED_KEY = "generatedKey";

	private static final String CACHED_DATA = "cachedData";

	private CacheTag tag;

	private PageContextStub pageContext;

	private ServletContextStub servletContext;

	private ServletCacheAdministrator cacheAdministratorMock;

	private JspWriter jspWriterMock;

	private Cache cacheMock;

	private SlingHttpServletRequest slingHttpServletRequest;

	@Before
	public void setUp() throws Exception {
		tag = new CacheTag();
		tag.setBodyContent(null);

		pageContext = new PageContextStub();
		tag.setPageContext(pageContext);

		servletContext = new ServletContextStub();
		pageContext.setServletContext(servletContext);

		cacheAdministratorMock = createMock(ServletCacheAdministrator.class);
		servletContext.setAttribute(ServletContextStub.CACHE_ADMIN_KEY, cacheAdministratorMock);
		assertEquals(cacheAdministratorMock, ServletCacheAdministrator.getInstance(servletContext));

		jspWriterMock = createMock(JspWriter.class);
		pageContext.setJspWriter(jspWriterMock);

		cacheMock = createMock(Cache.class);

		slingHttpServletRequest = createMock(SlingHttpServletRequest.class);
		pageContext.setServletRequest(slingHttpServletRequest);

		servletContext.setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, true);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testDisabled() throws JspException {
		servletContext.setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, null);

		assertEquals(Tag.EVAL_BODY_INCLUDE, tag.doStartTag());
		assertEquals(0, tag.doAfterBody());

		servletContext.setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_ENABLED, false);

		assertEquals(Tag.EVAL_BODY_INCLUDE, tag.doStartTag());
		assertEquals(0, tag.doAfterBody());
	}

	@Test(expected = IllegalStateException.class)
	public void testBlankKey() throws JspException {
		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheMock);
		replayMocks();
		tag.doStartTag();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetBlankKey() {
		tag.setKey(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testKeyGenerationNoHttpServletRequest() throws JspException {
		pageContext.setServletRequest(createMock(ServletRequest.class));
		tag.setKey(KEY);
		Cache cacheMock = createMock(Cache.class);
		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheMock);
		replayMocks(cacheMock);
		tag.doStartTag();
	}

	@Test
	public void testKeyGeneration() throws Exception {
		tag.setKey(KEY);
		tag.setCacheLevel(Integer.MIN_VALUE);

		expect(slingHttpServletRequest.getRequestURI()).andReturn("/home/hello.html");

		CacheKeyGenerator keyGenerator = new CacheKeyGenerator() {

			@Override
			public String generateKey(int cacheLevel, String prefix, String pagePath, String selectorString) {
				assertEquals(Integer.MIN_VALUE, cacheLevel);
				assertEquals(KEY, prefix);
				assertEquals("/home/hello", pagePath);
				assertEquals("", selectorString);
				return GENERATED_KEY;
			}

			@Override
			public String generateKey(int cacheLevel, Resource resource, String selectorString) {
				throw new UnsupportedOperationException();
			}
		};
		ReflectionHelper.set(CacheTag.class, "keyGenerator", tag, keyGenerator);

		replayMocks();
		assertEquals(GENERATED_KEY, ReflectionHelper.invoke(CacheTag.class, "getKey", tag));
		verifyMocks();
	}

	@Test
	public void testKeyGenerationWithDotts() throws Exception {
		tag.setKey(KEY);
		tag.setCacheLevel(Integer.MIN_VALUE);

		expect(slingHttpServletRequest.getRequestURI()).andReturn("/home/hello.selector.html");

		CacheKeyGenerator keyGenerator = new CacheKeyGenerator() {

			@Override
			public String generateKey(int cacheLevel, String prefix, String pagePath, String selectorString) {
				assertEquals(Integer.MIN_VALUE, cacheLevel);
				assertEquals(KEY, prefix);
				assertEquals("/home/hello", pagePath);
				assertEquals("selector", selectorString);
				return GENERATED_KEY;
			}

			@Override
			public String generateKey(int cacheLevel, Resource resource, String selectorString) {
				throw new UnsupportedOperationException();
			}
		};
		ReflectionHelper.set(CacheTag.class, "keyGenerator", tag, keyGenerator);

		replayMocks();
		assertEquals(GENERATED_KEY, ReflectionHelper.invoke(CacheTag.class, "getKey", tag));
		verifyMocks();
	}

	@Test
	public void testKeyGenerationWithOutAnyDotts() throws Exception {
		tag.setKey(KEY);
		tag.setCacheLevel(Integer.MIN_VALUE);

		expect(slingHttpServletRequest.getRequestURI()).andReturn("/home/hello");

		CacheKeyGenerator keyGenerator = new CacheKeyGenerator() {

			@Override
			public String generateKey(int cacheLevel, String prefix, String pagePath, String selectorString) {
				assertEquals(Integer.MIN_VALUE, cacheLevel);
				assertEquals(KEY, prefix);
				assertEquals("/home/hello", pagePath);
				assertEquals("", selectorString);
				return GENERATED_KEY;
			}

			@Override
			public String generateKey(int cacheLevel, Resource resource, String selectorString) {
				throw new UnsupportedOperationException();
			}
		};
		ReflectionHelper.set(CacheTag.class, "keyGenerator", tag, keyGenerator);

		replayMocks();
		assertEquals(GENERATED_KEY, ReflectionHelper.invoke(CacheTag.class, "getKey", tag));
		verifyMocks();
	}

	@Test
	public void testDoStartTag() throws Exception {
		ReflectionHelper.set(CacheTag.class, "currentKey", tag, GENERATED_KEY);

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheMock);
		expect(cacheMock.getFromCache(GENERATED_KEY)).andReturn(CACHED_DATA);
		jspWriterMock.write(CACHED_DATA);

		replayMocks();
		assertEquals(Tag.SKIP_BODY, tag.doStartTag());
		verifyMocks();
	}

	@Test
	public void testDoStartTagNeedsRefreshException() throws Exception {
		ReflectionHelper.set(CacheTag.class, "currentKey", tag, GENERATED_KEY);

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheMock);
		expect(cacheMock.getFromCache(GENERATED_KEY)).andThrow(new NeedsRefreshException(null));

		replayMocks();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, tag.doStartTag());
		verifyMocks();
	}

	@Test
	public void testDoEndTag() throws JspException {
		assertEquals(Tag.EVAL_PAGE, tag.doEndTag());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDoAfterBody1() throws Exception {
		CacheStub cacheStub = new CacheStub();

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheStub);
		ReflectionHelper.set(CacheTag.class, "currentKey", tag, GENERATED_KEY);

		BodyContent bodyContent = createMock(BodyContent.class);
		tag.setBodyContent(bodyContent);

		JspWriter writer = createMock(JspWriter.class);

		expect(bodyContent.getString()).andReturn(CACHED_DATA);
		expect(bodyContent.getEnclosingWriter()).andReturn(writer);
		bodyContent.writeOut(writer);

		tag.setTime(100);
		tag.setInvalidationSelf(false);

		JcrEventsService.clearEventListeners();

		replayMocks(bodyContent, writer);
		tag.doAfterBody();
		verifyMocks(bodyContent, writer);

		assertEquals(1,
				((List<JcrEventListener>) ReflectionHelper.get(JcrEventsService.class, "listeners")).size());
		assertTrue(cacheStub.isAddCacheEventListenerExecuted());
		assertNotNull(cacheStub.getCacheEventListener());
		assertTrue(cacheStub.isPutInCacheExecuted());
		assertEquals(GENERATED_KEY, cacheStub.getKey());
		assertEquals(CACHED_DATA, cacheStub.getContent());
		assertNotNull(cacheStub.getRefreshPolicy());
		assertTrue(cacheStub.getRefreshPolicy().equals(cacheStub.getCacheEventListener()));
		assertTrue(cacheStub.getRefreshPolicy() instanceof TagJcrRefreshPolicy);

		TagJcrRefreshPolicy policy = (TagJcrRefreshPolicy) cacheStub.getRefreshPolicy();
		assertEquals(100, policy.getRefreshPeriod());

		List<Pattern> invalidatePaths = ReflectionHelper.get(TagJcrRefreshPolicy.class, "invalidatePaths",
				policy);
		assertTrue(invalidatePaths.isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDoAfterBody2() throws Exception {
		CacheStub cacheStub = new CacheStub();

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheStub);
		ReflectionHelper.set(CacheTag.class, "currentKey", tag, GENERATED_KEY);

		BodyContent bodyContent = createMock(BodyContent.class);
		tag.setBodyContent(bodyContent);

		JspWriter writer = createMock(JspWriter.class);

		expect(bodyContent.getString()).andReturn(CACHED_DATA);
		expect(bodyContent.getEnclosingWriter()).andReturn(writer);
		bodyContent.writeOut(writer);

		servletContext.setAttribute(ComponentCacheFilter.SERVLET_CONTEXT_CACHE_DURATION, 1000);

		JcrEventsService.clearEventListeners();

		tag.setInvalidationSelf(true);
		tag.setInvalidationPaths("path1;path2");

		Resource resource = createMock(Resource.class);
		expect(slingHttpServletRequest.getResource()).andReturn(resource);
		expect(resource.getPath()).andReturn("resourcePath");

		replayMocks(bodyContent, writer, resource);
		tag.doAfterBody();
		verifyMocks(bodyContent, writer, resource);

		assertEquals(1,
				((List<JcrEventListener>) ReflectionHelper.get(JcrEventsService.class, "listeners")).size());
		assertTrue(cacheStub.isAddCacheEventListenerExecuted());
		assertNotNull(cacheStub.getCacheEventListener());
		assertTrue(cacheStub.isPutInCacheExecuted());
		assertEquals(GENERATED_KEY, cacheStub.getKey());
		assertEquals(CACHED_DATA, cacheStub.getContent());
		assertNotNull(cacheStub.getRefreshPolicy());
		assertTrue(cacheStub.getRefreshPolicy().equals(cacheStub.getCacheEventListener()));
		assertTrue(cacheStub.getRefreshPolicy() instanceof TagJcrRefreshPolicy);

		TagJcrRefreshPolicy policy = (TagJcrRefreshPolicy) cacheStub.getRefreshPolicy();
		assertEquals(1000, policy.getRefreshPeriod());

		List<Pattern> invalidatePaths = ReflectionHelper.get(TagJcrRefreshPolicy.class, "invalidatePaths",
				policy);
		assertEquals(3, invalidatePaths.size());
		assertEquals("resourcePath.*", invalidatePaths.get(0).pattern());
		assertEquals("path1", invalidatePaths.get(1).pattern());
		assertEquals("path2", invalidatePaths.get(2).pattern());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDoAfterBody3() throws Exception {
		CacheStub cacheStub = new CacheStub();

		expect(cacheAdministratorMock.getAppScopeCache(servletContext)).andReturn(cacheStub);
		ReflectionHelper.set(CacheTag.class, "currentKey", tag, GENERATED_KEY);

		BodyContent bodyContent = createMock(BodyContent.class);
		tag.setBodyContent(bodyContent);

		JspWriter writer = createMock(JspWriter.class);

		expect(bodyContent.getString()).andReturn(CACHED_DATA);
		expect(bodyContent.getEnclosingWriter()).andReturn(writer);
		bodyContent.writeOut(writer);

		tag.setInvalidationSelf(false);

		JcrEventsService.clearEventListeners();

		replayMocks(bodyContent, writer);
		tag.doAfterBody();
		verifyMocks(bodyContent, writer);

		assertEquals(1,
				((List<JcrEventListener>) ReflectionHelper.get(JcrEventsService.class, "listeners")).size());
		assertTrue(cacheStub.isAddCacheEventListenerExecuted());
		assertNotNull(cacheStub.getCacheEventListener());
		assertTrue(cacheStub.isPutInCacheExecuted());
		assertEquals(GENERATED_KEY, cacheStub.getKey());
		assertEquals(CACHED_DATA, cacheStub.getContent());
		assertNotNull(cacheStub.getRefreshPolicy());
		assertTrue(cacheStub.getRefreshPolicy().equals(cacheStub.getCacheEventListener()));
		assertTrue(cacheStub.getRefreshPolicy() instanceof TagJcrRefreshPolicy);

		TagJcrRefreshPolicy policy = (TagJcrRefreshPolicy) cacheStub.getRefreshPolicy();
		assertEquals(-1, policy.getRefreshPeriod());

		List<Pattern> invalidatePaths = ReflectionHelper.get(TagJcrRefreshPolicy.class, "invalidatePaths",
				policy);
		assertTrue(invalidatePaths.isEmpty());
	}

	private void replayMocks(Object... mocks) {
		replay(cacheAdministratorMock, jspWriterMock, cacheMock, slingHttpServletRequest);
		if (mocks != null) {
			replay(mocks);
		}
	}

	private void verifyMocks(Object... mocks) {
		verify(cacheAdministratorMock, jspWriterMock, cacheMock, slingHttpServletRequest);
		if (mocks != null) {
			verify(mocks);
		}
	}

}
