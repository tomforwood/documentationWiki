package org.forwoods.docuwiki.documentable;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.print.Doc;

import org.apache.commons.io.IOUtils;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BsonDocument;
import org.bson.BsonSerializationException;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSONCallback;


public class EnumRepresentationTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testWriteRead() throws JsonParseException, JsonMappingException, IOException {
		EnumRepresentation enumRep = new EnumRepresentation(true, "ControlEnum");
		enumRep.addModifier(Modifier.PUBLIC);
		enumRep.comment = "An enum type";
		EnumRepresentation.EnumConstant econst = 
				new EnumRepresentation.EnumConstant("EnumConst");
		econst.setEnumValue("10000000000");
		enumRep.addEnumValue(econst);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.valueToTree(enumRep);
		System.out.println(data);
		
		EnumRepresentation readValue = mapper.readValue(data.toString(), EnumRepresentation.class);
		
		/*BsonDocument doc = BsonDocument.parse(data.toString());
		
		String bsonText = doc.toString();
		
		BasicBSONDecoder decoder = new BasicBSONDecoder();
		JSONCallback callback = new JSONCallback();
		int readObject = decoder.decode(bsonText.getBytes(), callback);
		Object o = callback.get();*/
		assertThat(readValue).isEqualTo(enumRep);
		
	}

}
