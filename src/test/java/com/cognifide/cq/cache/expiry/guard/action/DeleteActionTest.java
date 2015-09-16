package com.cognifide.cq.cache.expiry.guard.action;

import com.cognifide.cq.cache.cache.CacheHolder;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DeleteActionTest {

	@Mock
	private CacheHolder cacheHolder;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Test
	public void shouldThrowNullPointerExceptionWhenCacheHolderIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new DeleteAction(null, StringUtils.EMPTY, StringUtils.EMPTY);
	}

	@Test
	public void shouldThrowNullPointerExceptionWhenCacheNameIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new DeleteAction(cacheHolder, null, StringUtils.EMPTY);
	}

	@Test
	public void shouldThrowNullPointerExceptionWhenKeyIsNull() {
		exception.expect(NullPointerException.class);

		//when
		new DeleteAction(cacheHolder, StringUtils.EMPTY, null);
	}
}
