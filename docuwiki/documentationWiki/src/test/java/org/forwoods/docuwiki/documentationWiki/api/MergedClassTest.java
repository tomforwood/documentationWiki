package org.forwoods.docuwiki.documentationWiki.api;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.assertj.core.api.Condition;
import org.assertj.core.api.iterable.Extractor;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentationWiki.core.SquadClassLoader;
import org.forwoods.docuwiki.documentationWiki.core.SquadZipFileLoader;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MergedClassTest {

	ClassRepresentation reflectedClass;
	ClassRepresentation annotatedClass;
	SquadClass squadClass;
	
	Condition<Documentable> commented = new Condition<>(ec->ec.getComment()!=null && ec.getComment().length()>0,"hasComment");
	Condition<Documentable> orphaned = new Condition<>(Documentable::getIsOrphaned, "orphaned");
	private SquadClassLoader loader;
	
	
	@Before
	public void setUp() throws Exception {
		ObjectMapper mapper = ClassResource.createMapper();
		InputStream reflectedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/reflectedClass.json");
		reflectedClass = mapper.readValue(reflectedStream, ClassRepresentation.class);
		InputStream annotatedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/annotatedClass.json");
		annotatedClass = mapper.readValue(annotatedStream, ClassRepresentation.class);
		
		URL zipIn = getClass().getClassLoader().getResource("xml.zip");
		SquadZipFileLoader fileLoader = new SquadZipFileLoader(zipIn);
		loader = new SquadClassLoader(fileLoader);
	}
	
	@Test
	public void testMergedClass() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass, squadClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(2);
		assertThat(result).isNot(orphaned);
	}
	
	@Test
	public void testMissingAnnotated() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, null, squadClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(2);
		assertThat(result.getInstanceFields().size()).isEqualTo(22);
		assertThat(result.getInstanceFields()).areNot(commented);
		assertThat(result).isNot(orphaned);
	}
	
	@Test
	public void testMissingReflected() {
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(null, annotatedClass, squadClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result).is(orphaned);
		assertThat(result.getNested().size()).isEqualTo(1);
		assertThat(result.getInstanceFields().size()).isEqualTo(17);
		assertThat(result.getInstanceFields()).areExactly(3, commented);
	}
	
	@Test
	public void testAnnotatedField() throws IOException {
		squadClass = loader.loadClass("GameEvents");
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass, squadClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getNested().size()).isEqualTo(2);
		
		assertThat(result.getStaticFields())
		.filteredOn(f->f.getName().equals("onAsteroidSpawned"))
		.extracting(f->f.getSquadComment())
		.contains("<para>Event called when an asteroid is spawned. </para>");
		
		assertThat(result.getInstanceFields()).areExactly(3, commented);
		
	}
	
	@Test
	public void testAnnotatedMethod() throws IOException {
		annotatedClass.getInstanceMethods().get(0).setComment("A method but not an important one");
		squadClass = loader.loadClass("Contracts.Contract");
		
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass, squadClass);
		
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getInstanceMethods()).areExactly(1, commented);
		assertThat(result.getInstanceMethods())
			.filteredOn(m->m.getName().equals("CanBeCancelled"))
			.extracting(m->m.getSquadComment())
			.contains("<para>If this contract can be cancelled </para>");
		assertThat(result.getNested().size()).isEqualTo(2);
		
	}
	
	@Test
	public void testAnnotatedClass() throws IOException {
		String comment = "This class does stuff, probably";
		annotatedClass.setComment(comment);
		squadClass = loader.loadClass("GameEvents");
		
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass, squadClass);
				
		assertThat(result.getObjectType().getTypeName()).isEqualTo("class");
		assertThat(result.getComment()).isEqualTo(comment);
		assertThat(result.getSquadComment()).startsWith("<para>Static manager");
		assertThat(result.getNested().size()).isEqualTo(2);
		assertThat(result.getSquadApiFile()).isEqualTo("class_game_events");
		
	}
	
	@Test
	public void testNestedEnum() throws IOException {
		squadClass = loader.loadClass("Contracts.Contract");
		MergedClass<ClassRepresentation> result = MergedClass.createMergedClass(reflectedClass, annotatedClass, squadClass);
		
		Extractor<MergedClass<?>, List<EnumConstant>> enumValues = tld->tld.getEnumConsts();
		assertThat(result.getNested())
		.filteredOn(n->n.getName().equals("Contracts.Contract+ContractPrestige"))
		.flatExtracting(enumValues)
		.extracting(c->c.getSquadComment())
		.contains("Not important");
		
	}

	@Test
	public void testMergeEnumOrphan() throws IOException {
		EnumRepresentation reflectedEnum = (EnumRepresentation) reflectedClass.getNested().get(0);
		EnumRepresentation annotatedEnum = (EnumRepresentation) annotatedClass.getNested().get(0);
		annotatedEnum.getEnumValues().get(2).setName("Orphan");
		
		MergedClass<EnumRepresentation> result = MergedClass.createMergedClass(reflectedEnum, annotatedEnum, squadClass);
		
		assertThat(result.getObjectType().getTypeName()).isEqualTo("enum");
		assertThat(result.getName()).isEqualTo("AttachNode+NodeType");
		
		assertThat(result.getEnumConsts()).areExactly(3, commented)
			.areExactly(1, orphaned);
	}

}
