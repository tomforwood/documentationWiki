package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergedClass<A extends TopLevelDocumentable> extends Documentable{
	
	private int reflectedVersion;
	private int annotatedVersion;
	
	@JsonProperty
	private String namespace;
	@JsonProperty
	private String objectType;
	@JsonProperty
	private List<Modifier> classModifiers;
	@JsonProperty
	private String name;
	@JsonProperty
	private List<String> extensions;
	
	@JsonProperty
	private List<EnumConstant> enumConsts;
	
	@JsonProperty
	private List<FieldRepresentation> instanceFields;
	
	
	public MergedClass() {
	}
	
	private MergedClass(A reflected, A annotated) {
		populateAnnotated(annotated);
		populateReflected(reflected);//fields from reflected get priority of annotated
	}
	
	public static <T extends TopLevelDocumentable> MergedClass<T> createMergedClass(T reflected, T annotated) {
		MergedClass<T> mergedClass = new MergedClass<>(reflected, annotated);
		
		if (reflected.getObjectType().equals("class")) {
			mergeClass(reflected, annotated, mergedClass);
		}
		else if (reflected.getObjectType().equals("enum")) {
			mergeEnum(reflected, annotated, mergedClass);
		}
		mergedClass.objectType = reflected.getObjectType();

		mergedClass.populateAnnotated(annotated);
		mergedClass.populateReflected(reflected);//fields from reflected get priority over annotated
		
		return mergedClass;
	}

	private static <T extends TopLevelDocumentable> void mergeClass(T reflected, T annotated, MergedClass<T> mergedClass) {
		mergedClass.instanceFields = new ArrayList<>();
		ClassRepresentation refClass = (ClassRepresentation) reflected;
		ClassRepresentation annClass = (ClassRepresentation) annotated;
		
		for (FieldRepresentation field : refClass.getInstanceFields()) {
			FieldRepresentation result = new FieldRepresentation();
			result.setName(field.getName());
			result.setObjectType(field.getObjectType());
			result.getModifiers().addAll(field.getModifiers());
			int match = annClass.getInstanceFields().indexOf(field);
			if (match>=0) {
				FieldRepresentation annField = annClass.getInstanceFields().remove(match);
				result.setComment(annField.getComment());
				//TODO reflected can't have defaults but annotated ones correct?
				result.assignment=annField.assignment;
			}
			mergedClass.instanceFields.add(result);			
		}
		for (FieldRepresentation annField:annClass.getInstanceFields()){
			FieldRepresentation result = new FieldRepresentation();
			result.getModifiers().addAll(annField.getModifiers());
			result.setName(annField.getName());
			result.setObjectType(annField.getObjectType());
			result.setComment(annField.getComment());
			result.setOrphaned(true);
			result.assignment=annField.assignment;
			mergedClass.instanceFields.add(result);
		}
	}

	private static <T extends TopLevelDocumentable> void mergeEnum(T reflected, T annotated,
			MergedClass<T> mergedClass) {
		mergedClass.enumConsts = new ArrayList<>();
		EnumRepresentation refEnum = (EnumRepresentation) reflected;
		EnumRepresentation annEnum = (EnumRepresentation) annotated;
		for (EnumConstant refConst:refEnum.getEnumValues()) {
			EnumConstant result = new EnumConstant(refConst.getName());
			result.setEnumValue(refConst.getEnumValue());
			
			int match = annEnum.getEnumValues().indexOf(refConst);
			if (match>=0) {
				EnumConstant annConst = annEnum.getEnumValues().remove(match);
				result.setComment(annConst.getComment());
			}
			mergedClass.enumConsts.add(result);
		}
		for (EnumConstant annConst:annEnum.getEnumValues()){
			EnumConstant result = new EnumConstant(annConst.getName());
			result.setEnumValue(annConst.getEnumValue());
			result.setComment(annConst.getComment());
			result.setOrphaned(true);
			mergedClass.enumConsts.add(result);
		}
	}
	
	private void populateAnnotated(A annotated) {
		pupulateEither(annotated);
		setComment(annotated.getComment());
	}

	private void pupulateEither(A annotated) {
		annotatedVersion= annotated.getVersion();
		namespace = annotated.getNamespaceName();
		classModifiers = annotated.getModifiers();
		name = annotated.getName();
		extensions = annotated.getExtensions();
	}
	
	private void populateReflected(A reflected) {
		pupulateEither(reflected);
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

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@Override
	public String toString() {
		return "MergedClass [reflectedVersion=" + reflectedVersion + ", annotatedVersion=" + annotatedVersion
				+ ", namespace=" + namespace + ", classModifiers=" + classModifiers + ", name=" + name + 
				 ", comment="+comment+"]";
	}
	
	
}
