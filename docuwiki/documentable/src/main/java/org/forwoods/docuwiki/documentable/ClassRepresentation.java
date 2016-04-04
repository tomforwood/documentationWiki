package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;

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
	
	@JsonProperty
	List<MethodRepresentation> instanceMethods = new ArrayList<>();
	
	@JsonProperty
	List<MethodRepresentation> staticMethods = new ArrayList<>();
	
	@JsonProperty
	List<MethodRepresentation> constructors = new ArrayList<>();
	
	@JsonProperty
	List<TopLevelDocumentable> nested = new ArrayList<>();
	
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
	
	public void addInstanceMethod(MethodRepresentation method) {
		instanceMethods.add(method);
	}
	
	public void addStaticMethod(MethodRepresentation method) {
		staticMethods.add(method);
	}
	
	public void addConstructor(MethodRepresentation method) {
		constructors.add(method);
	}
	
	public void addNested(TopLevelDocumentable nested) {
		this.nested.add(nested);
	}
	
	public static class FieldRepresentation extends Member {
		public String assignment;
		
		public boolean equals(Object o) {
			FieldRepresentation other = (FieldRepresentation)o;
			return name.equals(other.name);
		}
		public int hashCode() {
			return name.hashCode();
		}
	}
	
	public static class PropertyRepresentation extends Member {
		public boolean getter;
		public boolean setter;
		
		public boolean equals(Object o) {
			PropertyRepresentation other = (PropertyRepresentation)o;
			return name.equals(other.name);
		}
		public int hashCode() {
			return name.hashCode();
		}
	}
	
	public static class MethodRepresentation extends Member {
		@JsonProperty
		public List<Member> parameters = new ArrayList<>();
		
		public boolean equals(Object o) {
			MethodRepresentation other = (MethodRepresentation)o;
			//methods are equal if the names are equal and the parameter types are equal
			if (!name.equals(other.name)) return false;
			if (parameters.size()!=other.parameters.size()) return false;
			for (int i=0;i<parameters.size();i++) {
				if (!parameters.get(i).getObjectType().typeNameEquals(
						other.parameters.get(i).getObjectType())) {
					return false;
				}
			}
			return true;
		}
		public int hashCode() {
			int hash= name.hashCode();
			for (Member m:parameters) {
				hash=31*hash + m.getObjectType().typeName.hashCode();
			}
			return hash;
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

	public List<MethodRepresentation> getStaticMethods() {
		return staticMethods;
	}
	
	public List<MethodRepresentation> getInstanceMethods() {
		return instanceMethods;
	}

	public List<MethodRepresentation> getConstructors() {
		return constructors;
	}

	public List<TopLevelDocumentable> getNested() {
		return nested;
	}
}
