package com.cognifide.cq.cache.model.alias;

import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class PathAliasReaderTest {

	private static final String[] INVALID_ALIASES = new String[]{"alias", "$alias|", "$alias||"};

	private static final String[] VALID_ALIASES = new String[]{"$alias|/path/1|/path/2"};

	private PathAliasReader testedObject;

	@Before
	public void setUp() {
		this.testedObject = new PathAliasReader();
	}

	@Test
	public void shouldNotReadInvalidAliases() {
		//when
		Set<PathAlias> actual = testedObject.readAliases(INVALID_ALIASES);

		//then
		assertThat(actual).isEmpty();
	}

	@Test
	public void shouldReadValidAliases() {
		//when
		Set<PathAlias> actual = testedObject.readAliases(VALID_ALIASES);

		//then
		assertThat(actual).hasSize(1);
		assertThat(actual).extracting("name").contains("$alias");
	}
}
