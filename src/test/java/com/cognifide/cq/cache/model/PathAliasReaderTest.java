package com.cognifide.cq.cache.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class PathAliasReaderTest {

	@Test
	public void testReadAliases() {
		PathAliasReader reader = new PathAliasReader();
		String[] aliasStrings = new String[] { "$alias1|path1/content-asd/|/path2_example/asd.html",
				"$alias2|||||", "$alias3|||/asd|" };
		Set<PathAlias> aliases = reader.readAliases(aliasStrings);
		assertNotNull(aliases);
		assertEquals(2, aliases.size());

		aliasStrings = new String[] { "$alias|path1/content-asd/|/path2_example/asd.html", "$alias|||||",
				"$alias|||/asd;" };
		aliases = reader.readAliases(aliasStrings);
		assertNotNull(aliases);
		assertEquals(1, aliases.size());
	}

	@Test
	public void testReadAlias() {
		PathAliasReader reader = new PathAliasReader();
		String aliasString = "$alias|path1/content-asd/|/path2_example/asd.html";
		PathAlias alias = reader.readAlias(aliasString);
		assertNotNull(alias);
		assertEquals("$alias", alias.getName());
		Set<String> paths = alias.getPaths();
		assertEquals(2, paths.size());
		assertTrue(paths.contains("path1/content-asd/"));
		assertTrue(paths.contains("/path2_example/asd.html"));

		aliasString = "$alias|||/asd;";
		alias = reader.readAlias(aliasString);
		assertNotNull(alias);
		assertEquals("$alias", alias.getName());
		paths = alias.getPaths();
		assertEquals(1, paths.size());
		assertTrue(paths.contains("/asd;"));

		aliasString = "alias/as|path1/content-asd/|/path2_example/asd.html";
		alias = reader.readAlias(aliasString);
		assertNull(alias);
		
		aliasString = "alias|path1/content-asd/|/path2_example/asd.html";
		alias = reader.readAlias(aliasString);
		assertNull(alias);

		aliasString = "alias$as|path1/content-asd/|/path2_example/asd.html";
		alias = reader.readAlias(aliasString);
		assertNull(alias);

		aliasString = "$alias|||||";
		alias = reader.readAlias(aliasString);
		assertNull(alias);
	}

}
