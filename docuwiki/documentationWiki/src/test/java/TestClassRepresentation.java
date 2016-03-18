import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.forwoods.docuwiki.documentationWiki.api.ClassRepresentation;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class TestClassRepresentation {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException, ProcessingException {
		ClassRepresentation classRep = new ClassRepresentation(false, "Test");
		classRep.setTypeComment("A Test Type");
		JsonNode schemaNode = JsonLoader.fromResource("/schema/ClassRepresentation.json");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.valueToTree(classRep);
		ValidationConfiguration validationCfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV4).freeze();
		JsonSchemaFactory factory = JsonSchemaFactory.newBuilder().setValidationConfiguration(validationCfg).freeze();
		JsonSchema schema = factory.getJsonSchema(schemaNode);
		System.out.println(data);
		
		ProcessingReport report = schema.validate(data);
		System.out.println(report);
		//report.
		assertThat(report.isSuccess()).isTrue();
	}

}
