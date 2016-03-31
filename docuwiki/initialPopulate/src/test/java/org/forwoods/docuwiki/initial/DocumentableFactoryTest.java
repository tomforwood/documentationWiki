package org.forwoods.docuwiki.initial;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.junit.Before;
import org.junit.Test;

public class DocumentableFactoryTest {

	private DocumentableFactory documentableFactory;

	@Before
	public void setUp() throws Exception {
		documentableFactory = new DocumentableFactory();
	}

	@Test
	public void test() throws IOException {
		File f = new File("C:/Users/Tom/Source/Repos/XML-Documentation-for-the-KSP-API/src/ControlTypes.cs");
		BufferedReader reader = new BufferedReader(new FileReader(f));
		TopLevelDocumentable top = documentableFactory.createTopLevel(reader);
		
		assertThat(top).isNotNull().isInstanceOf(EnumRepresentation.class);
	}

}
