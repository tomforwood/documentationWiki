package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.PropertyRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergedClass<A extends TopLevelDocumentable> extends Documentable{
	
	private int reflectedVersion;
	private int annotatedVersion;
	
	@JsonProperty
	private String namespace;
	@JsonProperty
	private ObjectType objectType;
	@JsonProperty
	private List<Modifier> classModifiers;
	@JsonProperty
	private String name;	
	@JsonProperty
	List<ObjectType> varargs;
	@JsonProperty
	private List<String> extensions;
	
	@JsonProperty
	private List<EnumConstant> enumConsts;
	
	@JsonProperty
	private List<FieldRepresentation> instanceFields;
	@JsonProperty
	private List<FieldRepresentation> staticFields;
	
	@JsonProperty
	private List<PropertyRepresentation> instanceProperties;
	@JsonProperty
	private List<PropertyRepresentation> staticProperties;
	
	
	public MergedClass() {
	}
	
	private MergedClass(A reflected, A annotated) {
		populateAnnotated(annotated);
		populateReflected(reflected);//fields from reflected get priority of annotated
	}
	
	public static <T extends TopLevelDocumentable> MergedClass<T> createMergedClass(T reflected, T annotated) {
		
		MergedClass<T> mergedClass = new MergedClass<>(reflected, annotated);
		
		if (reflected.getObjectType().getTypeName().equals("class")) {
			mergeClass(reflected, annotated, mergedClass);
		}
		else if (reflected.getObjectType().getTypeName().equals("enum")) {
			mergeEnum(reflected, annotated, mergedClass);
		}
		mergedClass.objectType = reflected.getObjectType();

		mergedClass.populateAnnotated(annotated);
		mergedClass.populateReflected(reflected);//fields from reflected get priority over annotated
		
		return mergedClass;
	}

	private static <T extends TopLevelDocumentable> void mergeClass(T reflected, T annotated, MergedClass<T> mergedClass) {
		ClassRepresentation refClass = (ClassRepresentation) reflected;
		ClassRepresentation annClass = (ClassRepresentation) annotated;
		
		mergedClass.instanceFields = new ArrayList<>();
		List<FieldRepresentation> refInst = refClass==null?new ArrayList<>():refClass.getInstanceFields();
		List<FieldRepresentation> annInst = annClass==null?new ArrayList<>():annClass.getInstanceFields();
		mergeFields(mergedClass.instanceFields, refInst, annInst);
		
		mergedClass.staticFields = new ArrayList<>();
		List<FieldRepresentation> refStat = refClass==null?new ArrayList<>():refClass.getStaticFields();
		List<FieldRepresentation> annStat = annClass==null?new ArrayList<>():annClass.getStaticFields();
		mergeFields(mergedClass.staticFields, refStat, annStat);
		
		mergedClass.instanceFields.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		mergedClass.staticFields.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		
		mergedClass.instanceProperties = new ArrayList<>();
		List<PropertyRepresentation> refInstProps = refClass==null?new ArrayList<>():refClass.getInstanceProperties();
		List<PropertyRepresentation> annInstProps = annClass==null?new ArrayList<>():annClass.getInstanceProperties();
		mergeProperties(mergedClass.instanceProperties, refInstProps, annInstProps);
		
		mergedClass.staticProperties = new ArrayList<>();
		List<PropertyRepresentation> refStatProps = refClass==null?new ArrayList<>():refClass.getStaticProperties();
		List<PropertyRepresentation> annStatProps = annClass==null?new ArrayList<>():annClass.getStaticProperties();
		mergeProperties(mergedClass.staticProperties, refStatProps, annStatProps);
		
		mergedClass.instanceFields.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		mergedClass.staticFields.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));

		
		if (refClass!=null) {
			mergedClass.varargs = refClass.getVarargs();
		}
		
		
	}

	private static void mergeFields(List<FieldRepresentation> fields,
			List<FieldRepresentation> refInst, List<FieldRepresentation> annInst) {
		for (FieldRepresentation field : refInst) {
			FieldRepresentation result = new FieldRepresentation();
			result.setName(field.getName());
			result.setObjectType(field.getObjectType());
			result.getModifiers().addAll(field.getModifiers());
			int match = annInst.indexOf(field);
			if (match>=0) {
				FieldRepresentation annField = annInst.remove(match);
				result.setComment(annField.getComment());
				//TODO reflected can't have defaults but annotated ones can?
				result.assignment=annField.assignment;
			}
			fields.add(result);			
		}
		for (FieldRepresentation annField:annInst){
			FieldRepresentation result = new FieldRepresentation();
			result.getModifiers().addAll(annField.getModifiers());
			result.setName(annField.getName());
			result.setObjectType(annField.getObjectType());
			result.setComment(annField.getComment());
			result.setOrphaned(true);
			result.assignment=annField.assignment;
			fields.add(result);
		}
	}
	
	private static void mergeProperties(List<PropertyRepresentation> fields,
			List<PropertyRepresentation> refInst, List<PropertyRepresentation> annInst) {
		for (PropertyRepresentation prop : refInst) {
			PropertyRepresentation result = new PropertyRepresentation();
			result.setName(prop.getName());
			result.setObjectType(prop.getObjectType());
			result.getModifiers().addAll(prop.getModifiers());
			result.getter=prop.getter;
			result.setter=prop.setter;
			int match = annInst.indexOf(prop);
			if (match>=0) {
				PropertyRepresentation annProp = annInst.remove(match);
				result.setComment(annProp.getComment());
			}
			fields.add(result);			
		}
		for (PropertyRepresentation annProp:annInst){
			PropertyRepresentation result = new PropertyRepresentation();
			result.getModifiers().addAll(annProp.getModifiers());
			result.setName(annProp.getName());
			result.setObjectType(annProp.getObjectType());
			result.setComment(annProp.getComment());
			result.setOrphaned(true);
			fields.add(result);
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
		if (annotated==null)return;
		populateEither(annotated);
		setComment(annotated.getComment());
	}

	private void populateEither(A either) {
		annotatedVersion= either.getVersion();
		namespace = either.getNamespaceName();
		classModifiers = either.getModifiers();
		name = either.getName();
		extensions = either.getExtensions();
	}
	
	private void populateReflected(A reflected) {
		if (reflected==null)return;
		populateEither(reflected);
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

	public ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(ObjectType objectType) {
		this.objectType = objectType;
	}

	@Override
	public String toString() {
		return "MergedClass [reflectedVersion=" + reflectedVersion + ", annotatedVersion=" + annotatedVersion
				+ ", namespace=" + namespace + ", classModifiers=" + classModifiers + ", name=" + name + 
				 ", comment="+comment+"]";
	}
	
	
}
