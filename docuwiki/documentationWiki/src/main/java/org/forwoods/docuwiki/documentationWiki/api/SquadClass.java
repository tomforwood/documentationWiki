package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.PropertyRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;


public class SquadClass extends Documentable{

	String name;
	String fileName;
	
	List<EnumConstant> enums = new ArrayList<>();
	List<SquadClass> nested = new ArrayList<>();
	
	private List<FieldRepresentation> fields = new ArrayList<>();
	private List<MethodRepresentation> methods = new ArrayList<>();
	private List<PropertyRepresentation> properties = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<EnumConstant> getEnums() {
		return enums;
	}

	public void setEnums(List<EnumConstant> enums) {
		this.enums = enums;
	}

	public List<FieldRepresentation> getFields() {
		return fields;
	}

	public List<MethodRepresentation> getMethods() {
		return methods;
	}

	public List<PropertyRepresentation> getProperties() {
		return properties;
	}

	public List<SquadClass> getNested() {
		return nested;
	}
}
