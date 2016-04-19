package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.DocumentationWikiApplication;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;

import com.codahale.metrics.Timer;
import com.mongodb.Function;
import com.mongodb.client.MongoCollection;

@Path("/classList")
@Produces(MediaType.APPLICATION_JSON)
public class ClassListResource {
	private MongoCollection<Document> reflectedClasses;
	private MongoCollection<Document> annotatedClasses;
	private Collection<FQClassName> cachedClasses;
	private Timer classListTimer;
	
	public ClassListResource(MongoCollection<Document> reflectedClasses, MongoCollection<Document> annotatedClasses) {
		this.reflectedClasses = reflectedClasses;
		this.annotatedClasses = annotatedClasses;
		
		classListTimer = DocumentationWikiApplication.metrics.timer("ClassListTimer");
	}
	
	@GET
	public Collection<FQClassName> getClassList() {
		SortedMap<FQClassName, FQClassName> retrieved = new TreeMap<>();
		getClassNames(annotatedClasses, retrieved, FQClassName.ANNOTATED);
		getClassNames(reflectedClasses, retrieved, FQClassName.REFLECTED);

		
		cachedClasses = retrieved.values();
		return cachedClasses;
	}
	
	private void getClassNames(MongoCollection<Document> collection, 
			SortedMap<FQClassName, FQClassName> retrieved,int set) {
		try (Timer.Context context = classListTimer.time()) {
			Function<Document, FQClassName> mapper = new Function<Document, FQClassName>(){
				@Override
				public FQClassName apply(Document t) {
					String namespace = t.getString("namespaceName");
					String className = t.getString("name");
					return new FQClassName(namespace, className, set);
				}};
				
			
			Consumer<FQClassName> consumer = fqc->retrieved.merge(fqc, fqc, FQClassName.mergeFunction);
			
			collection.find()
				.projection(fields(include("namespaceName","name"), excludeId()))
				.map(mapper)
				.forEach(consumer);
		}
	}
	
	public Collection<FQClassName> getCachedClasses() {
		if (cachedClasses==null) {
			getClassList();
		}
		return cachedClasses;
	}
}
