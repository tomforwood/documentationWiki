package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("_id")
public class ClassRepresentation extends TopLevelDocumentable{

	@JsonProperty
	List<ObjectType> varargs;
	
	@JsonProperty
	List<FieldRepresentation> instanceFields = new ArrayList<>();
	
	@JsonProperty
	List<FieldRepresentation> staticFields = new ArrayList<>();
	
	@JsonProperty
	List<PropertyRepresentation> instanceProperties = new ArrayList<>();
	
	@JsonProperty
	List<PropertyRepresentation> staticProperties = new ArrayList<>();
	
	public ClassRepresentation(boolean userGenerated, String name) {
		super(new ObjectType("class"),userGenerated,name);
	}

	public ClassRepresentation() {
		super(new ObjectType("class"));
		version = 1;
	}
	
	public void addInstanceField(FieldRepresentation field) {
		instanceFields.add(field);
	}
	
	public void addStaticField(FieldRepresentation field) {
		staticFields.add(field);
	}
	
	public void addInstanceProperty(PropertyRepresentation prop) {
		instanceProperties.add(prop);
	}
	
	public void addStaticProperty(PropertyRepresentation prop) {
		staticProperties.add(prop);
	}
	
	public static class FieldRepresentation extends Member {
		public String assignment;
		
		public boolean equals(Object o) {
			FieldRepresentation other = (FieldRepresentation)o;
			return name.equals(other.name);
		}
	}
	
	public static class PropertyRepresentation extends Member {
		public boolean getter;
		public boolean setter;
		
		public boolean equals(Object o) {
			PropertyRepresentation other = (PropertyRepresentation)o;
			return name.equals(other.name);
		}
	}

	public List<FieldRepresentation> getInstanceFields() {
		return instanceFields;
	}
	public List<FieldRepresentation> getStaticFields() {
		return staticFields;
	}

	public List<ObjectType> getVarargs() {
		return varargs;
	}

	public void setVarargs(List<ObjectType> varargs) {
		this.varargs = varargs;
	}

	public List<PropertyRepresentation> getInstanceProperties() {
		return instanceProperties;
	}

	public List<PropertyRepresentation> getStaticProperties() {
		return staticProperties;
	}
	
}
