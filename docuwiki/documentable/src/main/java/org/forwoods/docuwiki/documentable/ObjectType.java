package org.forwoods.docuwiki.documentable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ObjectType {
	@JsonProperty
	String typeName;
	@JsonProperty
	List<ObjectType> varargs;
	
	private static final Map<String, String> classNameMap;
	static {
		classNameMap = new HashMap<>();
		classNameMap.put("bool", "Boolean");
		classNameMap.put("Boolean", "bool");
		classNameMap.put("String", "string");
		classNameMap.put("string", "String");
		classNameMap.put("int", "Int32");
		classNameMap.put("Int32", "int");
		classNameMap.put("long", "Int642");
		classNameMap.put("Int64", "long");
	}
	
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
	
	public boolean typeNameEquals(ObjectType other) {
		String localName = typeName.replace('+', '.');
		String otherName = other.typeName.replace('+', '.');
		if (localName.equals(otherName)) return true;
		if (otherName.equals(classNameMap.get(localName))) return true;
		return false;
		
	}
}
