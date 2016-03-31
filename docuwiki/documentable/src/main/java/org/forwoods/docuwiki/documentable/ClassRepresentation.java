package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("_id")
public class ClassRepresentation extends TopLevelDocumentable{

	@JsonProperty
	List<FieldRepresentation> instanceFields = new ArrayList<>();
	
	public ClassRepresentation(boolean userGenerated, String name) {
		super("class",userGenerated,name);
	}

	public ClassRepresentation() {
		super("class");
		version = 1;
	}
	
	public void addInstanceField(FieldRepresentation field) {
		instanceFields.add(field);
	}
	
	public static class FieldRepresentation extends Member {
		public String assignment;
		
		public boolean equals(Object o) {
			FieldRepresentation other = (FieldRepresentation)o;
			return name.equals(other.name);
		}
	}

	public List<FieldRepresentation> getInstanceFields() {
		return instanceFields;
	}
}
