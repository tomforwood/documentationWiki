package org.forwoods.docuwiki.initial;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
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
	public void testNestedEnum() throws IOException {
		InputStream csStream = getClass().getClassLoader().getResourceAsStream("AttachNode.cs");
		BufferedReader reader = new BufferedReader(new InputStreamReader(csStream));
		TopLevelDocumentable top = documentableFactory.createTopLevel(reader);
		
		assertThat(top).isNotNull().isInstanceOf(ClassRepresentation.class);
		ClassRepresentation classRep = (ClassRepresentation) top;
		List<TopLevelDocumentable> nested = classRep.getNested();
		assertThat(nested.get(0).getObjectType().getTypeName()).isEqualTo("enum");
		assertThat(((EnumRepresentation)nested.get(0)).getEnumValues().size()).isEqualTo(3);
	}

}
