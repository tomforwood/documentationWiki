package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.DocumentationWikiApplication;
import org.forwoods.docuwiki.documentationWiki.api.ClassExtensions;

import com.codahale.metrics.Timer;
import com.mongodb.Function;
import com.mongodb.client.MongoCollection;

@Path("/class/extensions")
@Produces(MediaType.APPLICATION_JSON)
public class ClassesExtendingResource extends ClassBasedResource {
	private MongoCollection<Document> reflectedClasses;
	private Timer classExtensionsTimer;
	
	private Map<String, ClassExtensions> cache = new ConcurrentHashMap<>();
	
	public ClassesExtendingResource(MongoCollection<Document> reflectedDocuments, ClassListResource classList) {
		super(classList);
		this.reflectedClasses =reflectedDocuments;
		classExtensionsTimer = DocumentationWikiApplication.metrics.timer("ClassExtensionsTimer");
	}

	@GET
	@Path("/{id}")
	public ClassExtensions getExtensions(@PathParam("id") String name) {
		boolean validClass = isValidClass(name);
		if (!validClass) return null;
		try (Timer.Context context = classExtensionsTimer.time()){
			int version = getVersion(name);
			ClassExtensions cached = cache.get(name);
			if (cached!=null && cached.getVersionComputed()==version) {
				return cached;
			}
			
			ClassExtensions extensions = new ClassExtensions();
			extensions.setClassName(name);
			extensions.setVersionComputed(version);
			
			Set<String> extending = extensions.getExtendingClasses();
			findExtending(name, extending);
			
			cache.put(name, extensions);
			return extensions;
		}
		
	}

	private void findExtending(String name, Set<String> extending) {
		Set<String> candidates = new HashSet<>();
		reflectedClasses.find(eq("extensions",name))
			.map(mapper)
			.into(candidates);
		
		for (String candidate : candidates) {
			extending.add(candidate);
			findExtending(candidate, extending);
		}
	}

	private int getVersion(String name) {
		Object version = reflectedClasses.find(eq("name",name))
			.projection(include("version"))
			.first().get("version");
		return (int)version;
	}
	
	private Function<Document, String> mapper = new Function<Document, String>(){
		@Override
		public String apply(Document t) {
			String className = t.getString("name");
			return className;
		}
	};
	
}
