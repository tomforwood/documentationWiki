package org.forwoods.docuwiki.documentationWiki.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.function.Predicate;

import org.assertj.core.api.Condition;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.forwoods.docuwiki.documentationWiki.testUtil.FindIterableStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class ClassListResourceTest {

	private ClassListResource classList;
	private MongoCollection<Document> reflectedClasses;
	private MongoCollection<Document> annotatedClasses;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
    	reflectedClasses = mock(MongoCollection.class);
    	annotatedClasses = mock(MongoCollection.class);
    	
		classList = new ClassListResource(reflectedClasses, annotatedClasses);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		FindIterable<Document> reflected = mock(FindIterable.class);
		FindIterable<Document> refProj = 
				FindIterableStub.createFromJson("[{name:'Class1', namespace:'ns'}]");
		when(reflectedClasses.find()).thenReturn(reflected);
		when(reflected.projection(any(Bson.class))).thenReturn(refProj);
		
		FindIterable<Document> annotated = mock(FindIterable.class);
		FindIterable<Document> annProj = 
				FindIterableStub.createFromJson("[{name:'Class1', namespace:'ns'}]");
		when(annotatedClasses.find()).thenReturn(reflected);
		when(annotated.projection(any(Bson.class))).thenReturn(annProj);
		
		Collection<FQClassName> classes = classList.getClassList();
		Condition<Integer> allCondition = new Condition<>(Predicate.isEqual(FQClassName.ALL),"type");
		//List<FQClassName> collect = classes.stream().filter(fqcn->fqcn.getSubset()==FQClassName.ALL).collect(Collectors.toList());
		assertThat(classes).extracting(fqcn->fqcn.getSubset())
			.areAtLeastOne(allCondition);
	}
	
	@After
	public void tearDown() {
		//client.close();
	}
	
	

}
