package org.forwoods.docuwiki.documentable;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class ClassRepresentationTest {

	private Validator valid;

	@Before
	public void setUp() throws Exception {
		valid = new Validator();
	}

	@Test
	public void test() throws IOException, ProcessingException, URISyntaxException {
		ClassRepresentation classRep = new ClassRepresentation(false, "Test");
		classRep.addClassModifier(Modifier.PUBLIC);
		classRep.comment="A test class";
		//JsonNode schemaNode = JsonLoader.fromResource("/schema/ClassRepresentation.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.valueToTree(classRep);
		
		System.out.println(data);
		
		ProcessingReport report = valid.validate(data);
		System.out.println(report);
		//report.
		assertThat(report.isSuccess()).isTrue();
	}

}
