package com.cognifide.cq.cache.model.alias;

import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import org.hamcrest.core.StringContains;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class PathAliasReaderTest {

	PathAliasReader testedObject;

	@Before
	public void setUp() {
		this.testedObject = new PathAliasReader();
	}

	@Test
	public void shouldParseSingleValidAlias() {
		//given
		String[] aliases = new String[]{"$alias|/path/one/|/path/two.html"};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(1));
	}

	@Test
	public void shouldParseMultipleValidAlias() {
		//given
		String[] aliases = new String[]{
			"$alias|/path/one/|/path/two.html",
			"$alias2|/path2/one/|/path2/two.html"};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(2));
	}

	@Test
	public void shouldUseFirstAliasWhenAliasesHaveSameName() {
		//given
		String[] aliases = new String[]{
			"$alias|/path/one/|/path/two.html",
			"$alias|/path2/one/|/path2/two.html"};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(1));
		assertThat(getFirstPathFromAliases(actual), StringContains.containsString("/path/"));
	}

	private String getFirstPathFromAliases(Set<PathAlias> pathAliases) {
		return pathAliases.iterator().next().getPaths().iterator().next();
	}

	@Test
	public void shouldNotAddAliasWithoutPaths() {
		//given
		String[] aliases = new String[]{"$alias|||||"};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(0));
	}

	@Test
	public void shouldNotAddInvalidPathsToAlias() {
		//given
		String[] aliases = new String[]{"$alias|||||/path/one"};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(1));
		assertThat(getFirstPathAlias(actual).getPaths().size(), is(1));
	}

	private PathAlias getFirstPathAlias(Set<PathAlias> pathAliases) {
		return pathAliases.iterator().next();
	}

	@Test
	public void shouldParseMalformedConfiguration() {
		//given
		String[] aliases = new String[]{
			"$alias|/path/one|/path/two",
			"$alias2|||||/path2/one",
			"$alias3||/path3/one||/path3/two",
			"$alias3|||||/path/one"
		};

		//when
		Set<PathAlias> actual = testedObject.readAliases(aliases);

		//then
		assertThat(actual, is(not(nullValue())));
		assertThat(actual.size(), is(3));
	}
}
