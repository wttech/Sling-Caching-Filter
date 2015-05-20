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
package com.cognifide.cq.cache.filter.cache.action;

import com.cognifide.cq.cache.filter.cache.CacheHolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class DeleteActionTest {

	private static final String KEY = "key";

	@Mock
	private CacheHolder cacheHolder;

	private DeleteAction testedObject;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldThrowExceptionWhenCacheHolderIsNull() {
		//given
		exception.expect(NullPointerException.class);
		testedObject = new DeleteAction(null, null);

		//when
		testedObject.execute();
	}

	@Test
	public void shouldCallRemoveOnCacheHolder() {
		//given
		testedObject = new DeleteAction(cacheHolder, KEY);

		//when
		testedObject.execute();

		//then
		verify(cacheHolder).remove(KEY);
	}
}
