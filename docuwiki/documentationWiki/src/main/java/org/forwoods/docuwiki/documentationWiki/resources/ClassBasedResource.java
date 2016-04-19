package org.forwoods.docuwiki.documentationWiki.resources;

import java.io.IOException;

import org.forwoods.docuwiki.documentable.TopLevelDeserializer;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class ClassBasedResource {

	protected ClassListResource classList;
	protected ObjectMapper mapper;
	
	public  ClassBasedResource(){
		mapper=createMapper();
	}

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule sm = new SimpleModule()
				.addDeserializer(TopLevelDocumentable.class, 
						new TopLevelDeserializer())
				.addDeserializer(Long.class, new JsonDeserializer<Long>(){

					@Override
					public Long deserialize(JsonParser p, DeserializationContext ctxt)
							throws IOException, JsonProcessingException {
						JsonNode node = p.getCodec().readTree(p);
						if (node.has("$numberLong")) {
							Long value = node.get("$numberLong").asLong();
							return value;
						}
						else {
							return node.asLong();
						}
					}});
		mapper.registerModule(sm);
		return mapper;
	}

	public ClassBasedResource(ClassListResource classList) {
		this();
		this.classList = classList;
	}

	protected boolean isValidClass(String name) {
		boolean validClass=false;
		String namespace=null;
		int lastDot = name.lastIndexOf('.');
		if (lastDot>0) {
			namespace = name.substring(0, lastDot);
		}
		FQClassName fqc = new FQClassName(namespace, name, FQClassName.ALL);
		
		validClass = classList.getCachedClasses().contains(fqc);
		return validClass;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

}
