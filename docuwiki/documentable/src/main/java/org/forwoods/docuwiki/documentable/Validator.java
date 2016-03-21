package org.forwoods.docuwiki.documentable;

import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class Validator {
	private JsonSchema schema;

	public Validator() throws URISyntaxException, ProcessingException {
		URI uri = getClass().getResource("/schema/ClassRepresentation.json").toURI();
		ValidationConfiguration validationCfg = ValidationConfiguration.newBuilder().setDefaultVersion(SchemaVersion.DRAFTV4).freeze();
		JsonSchemaFactory factory = JsonSchemaFactory.newBuilder().setValidationConfiguration(validationCfg).freeze();
		schema = factory.getJsonSchema(uri.toString());
	}
	
	public ProcessingReport validate(JsonNode data) throws ProcessingException {
		return schema.validate(data);
	}
}
