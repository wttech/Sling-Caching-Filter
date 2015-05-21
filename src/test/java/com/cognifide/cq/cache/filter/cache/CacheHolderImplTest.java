/*
 * Copyright 2015 Cognifide Polska Sp. z o. o..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cognifide.cq.cache.filter.cache;

import com.cognifide.cq.cache.algorithm.SilentRemovalNotificator;
import com.cognifide.cq.cache.filter.cache.action.CacheAction;
import com.cognifide.cq.cache.filter.osgi.CacheConfiguration;
import com.cognifide.cq.cache.plugins.statistics.Statistics;
import com.cognifide.cq.cache.refresh.jcr.JcrRefreshPolicy;
import com.opensymphony.oscache.base.Cache;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.web.ServletCacheAdministrator;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletCacheAdministrator.class, SilentRemovalNotificator.class})
public class CacheHolderImplTest {

	private static final String CACHE_ADMINISTRATOR_FIELD_NAME = "cacheAdministrator";

	private static final String STATISTICS_FIELD_NAME = "statistics";

	private static final String CACHE_CONFIGURATION_FIELD_NAME = "cacheConfiguration";

	private static final String RESOURCE_TYPE = "resource type";

	private static final String KEY = "key";

	private static final boolean NOT_IMPORTANT = false;

	@Mock
	private JcrRefreshPolicy jcrRefreshPolicy;

	@Mock
	private ByteArrayOutputStream byteArrayOutputStream;

	@Mock
	private Cache cache;

	@Mock
	private ServletCacheAdministrator servletCacheAdministrator;

	@Mock
	private ServletContext servletContext;

	@Mock
	private Properties properties;

	@Mock
	private Statistics statistics;

	@Mock
	private CacheConfiguration cacheConfiguration;

	private CacheHolderImpl testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		testedObject = new CacheHolderImpl();
		Whitebox.setInternalState(testedObject, STATISTICS_FIELD_NAME, statistics);
		Whitebox.setInternalState(testedObject, CACHE_CONFIGURATION_FIELD_NAME, cacheConfiguration);
		when(cacheConfiguration.getCacheProperties()).thenReturn(properties);
	}

	@Test
	public void shouldCreateCacheAdministraotrWhenCacheAdministratorIsNull() {
		//given
		PowerMockito.mockStatic(ServletCacheAdministrator.class);

		//when
		testedObject.create(servletContext, NOT_IMPORTANT);

		//then
		PowerMockito.verifyStatic();
		ServletCacheAdministrator.getInstance(servletContext, properties);
	}

	@Test
	public void shouldCreateCacheAdministraotrWhenCacheAdministratorIsNotNullButOverwriteIsSetToTrue() {
		//given
		PowerMockito.mockStatic(ServletCacheAdministrator.class);
		Whitebox.setInternalState(testedObject, CACHE_ADMINISTRATOR_FIELD_NAME, servletCacheAdministrator);

		//when
		testedObject.create(servletContext, true);

		//then
		PowerMockito.verifyStatic();
		ServletCacheAdministrator.getInstance(servletContext, properties);
	}

	@Test
	public void shouldNotCreateCacheAdministraotrWhenCacheAdministratorIsNotNullAndOverwriteIsSetToFalse() {
		//given
		PowerMockito.mockStatic(ServletCacheAdministrator.class);
		Whitebox.setInternalState(testedObject, CACHE_ADMINISTRATOR_FIELD_NAME, servletCacheAdministrator);

		//when
		testedObject.create(servletContext, false);

		//then
		PowerMockito.verifyStatic(never());
		ServletCacheAdministrator.getInstance(servletContext, properties);
	}

	@Test
	public void shouldPutDataInCacheAndRegisterRefreshPolicy() {
		//given
		setUpCacheHolder();
		PowerMockito.mockStatic(SilentRemovalNotificator.class);
		setUpCache();

		//when
		testedObject.put(KEY, byteArrayOutputStream, jcrRefreshPolicy);

		//then
		verify(cache).putInCache(KEY, byteArrayOutputStream, jcrRefreshPolicy);
		PowerMockito.verifyStatic();
		SilentRemovalNotificator.notifyListeners(cache);
		verify(cache).addCacheEventListener(jcrRefreshPolicy);
	}

	private void setUpCacheHolder() {
		PowerMockito.mockStatic(ServletCacheAdministrator.class);
		PowerMockito.when(ServletCacheAdministrator.getInstance(servletContext, properties)).thenReturn(servletCacheAdministrator);
		testedObject.create(servletContext, NOT_IMPORTANT);
	}

	@Test
	public void shouldGetOutputStreamWhenCacheDoNotRefreshForGivenKey() throws NeedsRefreshException {
		//given
		setUpCacheHolder();
		setUpCache();
		when(cache.getFromCache(KEY)).thenReturn(byteArrayOutputStream);

		//when
		ByteArrayOutputStream actual = testedObject.get(RESOURCE_TYPE, KEY);

		//then
		assertThat("get should return cache output stream without exception ", actual, is(byteArrayOutputStream));
		verify(statistics).cacheHit(RESOURCE_TYPE);
		verify(statistics, never()).cacheMiss(anyString(), anyString(), any(CacheAction.class));
	}

	@Test
	public void shouldThrowNeedsRefreshExceptionWhenGivenEntityNeedsRefresh() throws NeedsRefreshException {
		//given
		setUpCacheHolder();
		setUpCache();
		when(cache.getFromCache(KEY)).thenThrow(NeedsRefreshException.class);
		exception.expect(NeedsRefreshException.class);

		//when
		ByteArrayOutputStream actual = testedObject.get(RESOURCE_TYPE, KEY);

		//then
		verify(cache).getFromCache(KEY);
		verify(statistics, never()).cacheHit(RESOURCE_TYPE);
		verify(statistics).cacheMiss(eq(RESOURCE_TYPE), eq(KEY), any(CacheAction.class));
	}

	private void setUpCache() {
		when(servletCacheAdministrator.getAppScopeCache(servletContext)).thenReturn(cache);
	}

	@Test
	public void shouldRemoveKeyFromCache() {
		//given
		setUpCacheHolder();
		setUpCache();

		//when
		testedObject.remove(KEY);

		//then
		verify(cache).removeEntry(KEY);
	}

	@Test
	public void shouldClearStatisticsAndCacheAdministratorInstanceOnDestroy() {
		//given
		setUpCacheHolder();
		PowerMockito.mockStatic(ServletCacheAdministrator.class);

		//when
		testedObject.destroy();

		//then
		verifyAndAssertThatAllDataWasCleared();
	}

	private void verifyAndAssertThatAllDataWasCleared() {
		verify(statistics).clearStatistics();
		PowerMockito.verifyStatic();
		ServletCacheAdministrator.destroyInstance(servletContext);
		assertThat(Whitebox.getInternalState(testedObject, CACHE_ADMINISTRATOR_FIELD_NAME), is(nullValue()));
	}

	@Test
	public void shouldClearStatisticsAndCacheAdministratorInstanceOnDeactivate() {
		//given
		setUpCacheHolder();
		PowerMockito.mockStatic(ServletCacheAdministrator.class);

		//when
		testedObject.activate();
		testedObject.deactivate();

		//then
		verifyAndAssertThatAllDataWasCleared();
	}
}
