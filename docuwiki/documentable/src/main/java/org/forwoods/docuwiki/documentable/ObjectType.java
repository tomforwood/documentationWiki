package org.forwoods.docuwiki.documentable;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectType {
	@JsonProperty
	String typeName;
	@JsonProperty
	List<ObjectType> varargs;
	
	public ObjectType(String name) {
		typeName = name;
	}
	
	@SuppressWarnings("unused")//json constructor
	private ObjectType() {
		
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public List<ObjectType> getVarargs() {
		return varargs;
	}

	public void setVarargs(List<ObjectType> varargs) {
		this.varargs = varargs;
	}
}
