package org.forwoods.docuwiki.documentable;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TopLevelDeserializer extends JsonDeserializer<TopLevelDocumentable> {

	@Override
	public TopLevelDocumentable deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = p.getCodec().readTree(p);
		System.out.println(node);
		JsonNode typeNode = node.get("objectType");
		String type = typeNode.get("typeName").asText();
		ObjectMapper m = (ObjectMapper)p.getCodec();
		if (type.equals("enum")) {
			return p.getCodec().treeToValue(node, EnumRepresentation.class);
		}
		else {
			return p.getCodec().treeToValue(node, ClassRepresentation.class);
		}
	}


}
