package org.forwoods.docuwiki.documentationWiki.api;

import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Modifier;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergedClass extends Documentable{
	
	private int reflectedVersion;
	private int annotatedVersion;
	
	@JsonProperty
	private String namespace;
	@JsonProperty
	private List<Modifier> classModifiers;
	@JsonProperty
	private String name;
	
	public MergedClass() {
		
	}
	
	public MergedClass(ClassRepresentation reflected, ClassRepresentation annotated) {
		populateAnnotated(annotated);
		populateReflected(reflected);//fields from reflected get priority of annotated
		
	}
	
	private void populateAnnotated(ClassRepresentation annotated) {
		annotatedVersion= annotated.getVersion();
		namespace = annotated.getNamespaceName();
		classModifiers = annotated.getClassModifiers();
		name = annotated.getName();
		setComment(annotated.getComment());
	}
	
	private void populateReflected(ClassRepresentation reflected) {
		reflectedVersion = reflected.getVersion();
		namespace = reflected.getNamespaceName();
		classModifiers = reflected.getClassModifiers();
		name = reflected.getName();
	}

	public int getReflectedVersion() {
		return reflectedVersion;
	}

	public int getAnnotatedVersion() {
		return annotatedVersion;
	}

	public String getNamespace() {
		return namespace;
	}

	public List<Modifier> getClassModifiers() {
		return classModifiers;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "MergedClass [reflectedVersion=" + reflectedVersion + ", annotatedVersion=" + annotatedVersion
				+ ", namespace=" + namespace + ", classModifiers=" + classModifiers + ", name=" + name + 
				 ", comment="+comment+"]";
	}
	
	
}
