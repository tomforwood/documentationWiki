package org.forwoods.docuwiki.documentationWiki.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ClassListResourceTest {

	private MongoClient client;
	private ClassListResource classList;
	@Before
	public void setUp() throws Exception {
		client = new MongoClient();
    	MongoDatabase database = client.getDatabase("docuWiki");
    	MongoCollection<Document> reflectedClasses = database.getCollection("reflectedClasses");
    	
		MongoCollection<Document> annotatedClasses = database.getCollection("annotatedClasses");
		classList = new ClassListResource(reflectedClasses, annotatedClasses);
        
		
	}

	@Test
	public void test() {
		/*//TODO read from file instead of mongo
		Collection<FQClassName> classes = classList.getClassList();
		Condition<Integer> allCondition = new Condition<>(Predicate.isEqual(FQClassName.ALL),"type");
		List<FQClassName> collect = classes.stream().filter(fqcn->fqcn.getSubset()==FQClassName.ALL).collect(Collectors.toList());
		assertThat(classes).extracting(fqcn->fqcn.getSubset())
			.areAtLeastOne(allCondition);*/
	}
	
	@After
	public void tearDown() {
		//client.close();
	}

}
