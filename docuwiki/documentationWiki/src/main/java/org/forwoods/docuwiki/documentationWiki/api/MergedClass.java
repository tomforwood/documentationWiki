package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
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
	
	@JsonProperty
	private List<MethodRepresentation> instanceMethods;
	@JsonProperty
	private List<MethodRepresentation> staticMethods;
	
	@JsonProperty
	private List<MethodRepresentation> constructors;
	
	@JsonProperty
	private List<MergedClass<?>> nested;
	
	public MergedClass() {
	}
	
	private MergedClass(A reflected, A annotated) {
		populateAnnotated(annotated);
		populateReflected(reflected);//fields from reflected get priority of annotated
	}
	
	public static <T extends TopLevelDocumentable> MergedClass<T> createMergedClass(T reflected, T annotated) {
		
		MergedClass<T> mergedClass = new MergedClass<>(reflected, annotated);
		
		String typeName = reflected==null?
				annotated.getObjectType().getTypeName():
					reflected.getObjectType().getTypeName();
		if (typeName.equals("class") || typeName.equals("interface") || typeName.equals("struct")) {
			mergeClass(reflected, annotated, mergedClass);
		}
		else if (typeName.equals("enum")) {
			mergeEnum(reflected, annotated, mergedClass);
		}
		mergedClass.objectType = reflected==null?annotated.getObjectType():reflected.getObjectType();

		mergedClass.populateAnnotated(annotated);
		mergedClass.populateReflected(reflected);//fields from reflected get priority over annotated
		
		return mergedClass;
	}

	private static <T extends TopLevelDocumentable> void mergeClass(T reflected, T annotated, MergedClass<T> mergedClass) {
		ClassRepresentation refClass = (ClassRepresentation) reflected;
		ClassRepresentation annClass = (ClassRepresentation) annotated;
		
		if (refClass!=null) mergedClass.extensions = refClass.getExtensions();
		
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
		
		mergedClass.instanceProperties.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		mergedClass.staticProperties.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		
		mergedClass.instanceMethods = new ArrayList<>();
		List<MethodRepresentation> refInstMethods = refClass==null?new ArrayList<>():refClass.getInstanceMethods();
		List<MethodRepresentation> annInstMethods = annClass==null?new ArrayList<>():annClass.getInstanceMethods();
		mergeMethods(mergedClass.instanceMethods, refInstMethods, annInstMethods);
		
		mergedClass.staticMethods = new ArrayList<>();
		List<MethodRepresentation> refStatMethods = refClass==null?new ArrayList<>():refClass.getStaticMethods();
		List<MethodRepresentation> annStatMethods = annClass==null?new ArrayList<>():annClass.getStaticMethods();
		mergeMethods(mergedClass.staticMethods, refStatMethods, annStatMethods);
		
		mergedClass.instanceMethods.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));
		mergedClass.staticMethods.sort((a,b)->a.getName().compareToIgnoreCase(b.getName()));

		mergedClass.constructors = new ArrayList<>();
		List<MethodRepresentation> refConstructors = refClass==null?new ArrayList<>():refClass.getConstructors();
		List<MethodRepresentation> annConstructors = annClass==null?new ArrayList<>():annClass.getConstructors();
		mergeMethods(mergedClass.constructors, refConstructors, annConstructors);
		
		mergedClass.nested = new ArrayList<>();
		List<TopLevelDocumentable> refNested = refClass==null?new ArrayList<>():refClass.getNested();
		List<TopLevelDocumentable> annNested = annClass==null?new ArrayList<>():annClass.getNested();
		mergeNested(mergedClass.nested, refNested, annNested);
		
		if (refClass!=null) {
			mergedClass.varargs = refClass.getVarargs();
		}
		
		
	}

	private static void mergeNested(List<MergedClass<?>> nested, List<TopLevelDocumentable> refNested,
			List<TopLevelDocumentable> annNested) {
		for (TopLevelDocumentable tld: refNested) {
			String name = tld.getName();
			TopLevelDocumentable annTLD=null;
			
			for (TopLevelDocumentable ansTLD: annNested) {
				if (ansTLD.getName().equals(name)) {
					annTLD = ansTLD;
					annNested.remove(ansTLD);
					break;
				}
			}
			nested.add(MergedClass.createMergedClass(tld, annTLD));
		}
		for (TopLevelDocumentable tld: annNested) {
			MergedClass<TopLevelDocumentable> mc = MergedClass.createMergedClass(null, tld);
			mc.setIsOrphaned(true);
			nested.add(mc);
		}
		
	}

	private static void mergeMethods(List<MethodRepresentation> mergedMethods,
			List<MethodRepresentation> refMethods, List<MethodRepresentation> annMethods) {
		for (MethodRepresentation method: refMethods) {
			MethodRepresentation result = new MethodRepresentation();
			result.setName(method.getName());
			result.setObjectType(method.getObjectType());
			result.parameters = method.parameters;
			result.getModifiers().addAll(method.getModifiers());
			result.setInheritedFrom(method.getInheritedFrom());
			int match = annMethods.indexOf(method);
			if (match>=0) {
				MethodRepresentation annMethod = annMethods.remove(match);
				result.setComment(annMethod.getComment());
			}
			mergedMethods.add(result);
		}
		for (MethodRepresentation method: annMethods) {
			MethodRepresentation result = new MethodRepresentation();
			result.setName(method.getName());
			result.setObjectType(method.getObjectType());
			result.parameters = method.parameters;
			result.getModifiers().addAll(method.getModifiers());
			result.setComment(method.getComment());
			result.setIsOrphaned(true);
			mergedMethods.add(result);
		}
		
	}

	private static void mergeFields(List<FieldRepresentation> fields,
			List<FieldRepresentation> refFields, List<FieldRepresentation> annFields) {
		for (FieldRepresentation field : refFields) {
			FieldRepresentation result = new FieldRepresentation();
			result.setName(field.getName());
			result.setObjectType(field.getObjectType());
			result.getModifiers().addAll(field.getModifiers());
			result.setInheritedFrom(field.getInheritedFrom());
			result.assignment = field.assignment;
			int match = annFields.indexOf(field);
			if (match>=0) {
				FieldRepresentation annField = annFields.remove(match);
				result.setComment(annField.getComment());
				if (field.assignment==null) result.assignment=annField.assignment;
			}
			fields.add(result);			
		}
		for (FieldRepresentation annField:annFields){
			FieldRepresentation result = new FieldRepresentation();
			result.getModifiers().addAll(annField.getModifiers());
			result.setName(annField.getName());
			result.setObjectType(annField.getObjectType());
			result.setComment(annField.getComment());
			result.setIsOrphaned(true);
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
			result.setInheritedFrom(prop.getInheritedFrom());
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
			result.setIsOrphaned(true);
			fields.add(result);
		}
	}

	private static <T extends TopLevelDocumentable> void mergeEnum(T reflected, T annotated,
			MergedClass<T> mergedClass) {
		mergedClass.enumConsts = new ArrayList<>();
		EnumRepresentation refEnum = (EnumRepresentation) reflected;
		EnumRepresentation annEnum = (EnumRepresentation) annotated;
		List<EnumConstant> enumValues = annEnum==null?new ArrayList<>():annEnum.getEnumValues();
		for (EnumConstant refConst:refEnum.getEnumValues()) {
			EnumConstant result = new EnumConstant(refConst.getName());
			result.setEnumValue(refConst.getEnumValue());
			
			int match = enumValues.indexOf(refConst);
			if (match>=0) {
				EnumConstant annConst = enumValues.remove(match);
				result.setComment(annConst.getComment());
			}
			mergedClass.enumConsts.add(result);
		}
		for (EnumConstant annConst:enumValues){
			EnumConstant result = new EnumConstant(annConst.getName());
			result.setEnumValue(annConst.getEnumValue());
			result.setComment(annConst.getComment());
			result.setIsOrphaned(true);
			mergedClass.enumConsts.add(result);
		}
	}
	
	private void populateAnnotated(A annotated) {
		if (annotated==null)return;
		populateEither(annotated);
		annotatedVersion= annotated.getVersion();
		setComment(annotated.getComment());
	}

	private void populateEither(A either) {
		namespace = either.getNamespaceName();
		classModifiers = either.getModifiers();
		name = either.getName();
		extensions = either.getExtensions();
	}
	
	private void populateReflected(A reflected) {
		if (reflected==null)return;
		reflectedVersion = reflected.getVersion();
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

	public List<EnumConstant> getEnumConsts() {
		return enumConsts;
	}

	public List<FieldRepresentation> getInstanceFields() {
		return instanceFields;
	}

	public List<FieldRepresentation> getStaticFields() {
		return staticFields;
	}

	public List<PropertyRepresentation> getInstanceProperties() {
		return instanceProperties;
	}

	public List<PropertyRepresentation> getStaticProperties() {
		return staticProperties;
	}

	public List<MethodRepresentation> getInstanceMethods() {
		return instanceMethods;
	}

	public List<MethodRepresentation> getStaticMethods() {
		return staticMethods;
	}

	public List<ObjectType> getVarargs() {
		return varargs;
	}

	public List<String> getExtensions() {
		return extensions;
	}

	public List<MethodRepresentation> getConstructors() {
		return constructors;
	}

	public List<MergedClass<?>> getNested() {
		return nested;
	}
}
