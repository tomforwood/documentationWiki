package org.forwoods.docuwiki.documentationWiki.resources;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
//import static com.mongodb.client.model.Filters.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;
import org.forwoods.docuwiki.documentationWiki.testUtil.FindIterableStub;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class ClassResourceTest {

	//mocked objects
	private MongoCollection<Document> annotated;
	private MongoCollection<Document> reflected;
	private ClassListResource classList;
	
	private List<FQClassName> classes = new ArrayList<>();
	
	private ClassResource classResource;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		annotated = mock(MongoCollection.class);
		reflected = mock(MongoCollection.class);
		classList = mock(ClassListResource.class);
		
		classResource = new ClassResource(reflected, annotated, classList);
		
		when(classList.getCachedClasses()).thenReturn(classes); 
	}

	@Test
	public void testGetClass() {
		classes.add(new FQClassName("test", "test.TestClass", FQClassName.ALL));
		FindIterable<Document> findIterableRef = 
				FindIterableStub.loadResources("json/reflectedClass.json");
		FindIterable<Document> findIterableAnn = 
				FindIterableStub.loadResources("json/annotatedClass.json");
		when(reflected.find(any(Bson.class))).thenReturn(findIterableRef);
		//Bson query = eq("name","test.TestClass");
		when(annotated.find(any(Bson.class))).thenReturn(findIterableAnn);
		
		MergedClass<? extends TopLevelDocumentable> class1 = 
				classResource.getClass("test.TestClass",null);
		assertThat(class1).isNotNull();
		assertThat(class1.isLatest()).isTrue();
	}
	
	@Test
	public void testGetClassMissing() {
		classes.add(new FQClassName("test", "Test.TestClass", FQClassName.ALL));
		
		MergedClass<? extends TopLevelDocumentable> class1 = classResource.getClass("test.MissingClass",null);
		assertThat(class1).isNull();
	}
	
	@Test
	public void testGetClassVersion() {
		classes.add(new FQClassName("test", "test.TestClass", FQClassName.ALL));
		FindIterable<Document> findIterableRef = 
				FindIterableStub.loadResources("json/reflectedClass.json");
		FindIterable<Document> findIterableAnn = 
				FindIterableStub.loadResources("json/annotatedClass.json");
		when(reflected.find(any(Bson.class))).thenReturn(findIterableRef);
		//Bson query = and(eq("name","test.TestClass"),eq("version",1));
		when(annotated.find(any(Bson.class))).thenReturn(findIterableAnn);
		
		MergedClass<? extends TopLevelDocumentable> class1 = 
				classResource.getClass("test.TestClass",1);
		assertThat(class1).isNotNull();
		assertThat(class1.isLatest()).isFalse();
	}

	@Test
	public void testSave() throws JsonParseException, JsonMappingException, IOException {
		
		classResource.setClock(Clock.fixed(Instant.ofEpochMilli(12345678), ZoneId.systemDefault()));
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");
		ArgumentCaptor<Document> saver = ArgumentCaptor.forClass(Document.class);
		
		InputStream reflectedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/mergedClass.json");
		MergedClass<?> mergedClass = classResource.getMapper()
				.readValue(reflectedStream, MergedClass.class);

		Response response = classResource.save(mergedClass,null, request);
		
		verify(annotated).insertOne(saver.capture());
		Document saved = saver.getValue();
		ClassRepresentation doc = (ClassRepresentation)classResource.read(classResource.getMapper(), saved.toJson());
		
		assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(201);
		assertThat(doc.getName()).isEqualTo("AeroFXState");
		assertThat(doc.getInstanceFields().size()).isEqualTo(1);
		assertThat(doc.getInstanceFields().get(0).getComment()).isNotNull();
		assertThat(doc.getModifyTime()).isEqualTo(12345678);
		assertThat(doc.getIpAddress()).isEqualTo("127.0.0.1");
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSaveRevert() throws JsonParseException, JsonMappingException, IOException {
		
		classResource.setClock(Clock.fixed(Instant.ofEpochMilli(12345678), ZoneId.systemDefault()));
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");
		FindIterable<Document> findIterable = mock(FindIterable.class);
		when(annotated.find(any(Bson.class))).thenReturn(findIterable);
		when(findIterable.sort(any(Bson.class))).thenReturn(findIterable);
		when(findIterable.first()).thenReturn(Document.parse("{version:5}"));
		
		ArgumentCaptor<Document> saver = ArgumentCaptor.forClass(Document.class);
		
		InputStream reflectedStream = this.getClass().getClassLoader()
				.getResourceAsStream("json/mergedClass.json");
		MergedClass<?> mergedClass = classResource.getMapper()
				.readValue(reflectedStream, MergedClass.class);

		Response response = classResource.save(mergedClass,"revert", request);
		
		verify(annotated).insertOne(saver.capture());
		Document saved = saver.getValue();
		ClassRepresentation savedClass = (ClassRepresentation)classResource.read(classResource.getMapper(), saved.toJson());
		
		assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(201);
		assertThat(savedClass.getName()).isEqualTo("AeroFXState");
		assertThat(savedClass.getInstanceFields().size()).isEqualTo(1);
		assertThat(savedClass.getInstanceFields().get(0).getComment()).isNotNull();
		assertThat(savedClass.getModifyTime()).isEqualTo(12345678);
		assertThat(savedClass.getIpAddress()).isEqualTo("127.0.0.1");
		assertThat(savedClass.getModifyAction()).isEqualTo("Reverted to version 1");
		assertThat(savedClass.getVersion()).isEqualTo(6);
		
		
	}
}
