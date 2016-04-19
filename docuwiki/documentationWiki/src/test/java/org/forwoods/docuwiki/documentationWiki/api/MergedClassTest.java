package org.forwoods.docuwiki.documentationWiki.api;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;

import org.assertj.core.api.Condition;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MergedClassTest {

	ClassRepresentation reflectedClass;
	ClassRepresentation annotatedClass;
	
	Condition<Documentable> commented = new Condition<>(ec->ec.getComment()!=null && ec.getComment().length()>0,"hasComment");
	Condition<Documentable> orphaned = new Condition<>(Documentable::getIsOrphaned, "orphaned");
	
	
	@Before
	public void setUp() throws Exception {
		ObjectMapper mapper = ClassResource.createMapper();
		InputStream reflectedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/reflectedClass.json");
		reflectedClass = mapper.readValue(reflectedStream, ClassRepresentation.class);
		InputStream annotatedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/annotatedClass.json");
		annotatedClass = mapper.readValue(annotatedStream, ClassRepresentation.class);
	}
	
	@Test
	public void testMergedClass() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(1);
		assertThat(result).isNot(orphaned);
	}
	
	@Test
	public void testMissingAnnotated() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, null);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(1);
		assertThat(result.getInstanceFields().size()).isEqualTo(22);
		assertThat(result.getInstanceFields()).areNot(commented);
		assertThat(result).isNot(orphaned);
	}
	
	@Test
	public void testMissingReflected() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(null, annotatedClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result).is(orphaned);
		assertThat(result.getNested().size()).isEqualTo(1);
		assertThat(result.getInstanceFields().size()).isEqualTo(17);
		assertThat(result.getInstanceFields()).areExactly(3, commented);
	}
	
	@Test
	public void testAnnotatedField() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(1);
		
		assertThat(result.getInstanceFields()).areExactly(3, commented);
		
	}
	
	@Test
	public void testAnnotatedMethod() {
		annotatedClass.getInstanceMethods().get(0).setComment("A method but not an important one");
		
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass);
		
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getInstanceMethods()).areExactly(1, commented);
		assertThat(result.getNested().size()).isEqualTo(1);
		
	}

	@Test
	public void testMergeEnumOrphan() {
		EnumRepresentation reflectedEnum = (EnumRepresentation) reflectedClass.getNested().get(0);
		EnumRepresentation annotatedEnum = (EnumRepresentation) annotatedClass.getNested().get(0);
		
		annotatedEnum.getEnumValues().get(2).setName("Orphan");
		
		MergedClass<EnumRepresentation> result = MergedClass.createMergedClass(reflectedEnum, annotatedEnum);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("enum");
		assertThat(result.getName()).isEqualTo("AttachNode+NodeType");
		
		assertThat(result.getEnumConsts()).areExactly(3, commented)
			.areExactly(1, orphaned);
	}

}
