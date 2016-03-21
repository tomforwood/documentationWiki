package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;


@Path("/class")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClassResource {
	
	private MongoCollection<Document> reflectedClasses;
	private MongoCollection<Document> annotatedClasses;
	private ClassListResource classList;

	public ClassResource(MongoCollection<Document> reflectedClasses, 
			MongoCollection<Document> annotatedClasses,
			ClassListResource classList) {
		this.reflectedClasses = reflectedClasses;
		this.annotatedClasses = annotatedClasses;
		this.classList = classList;
		
	}
	
	@GET
	@Path("/{id}")
	public MergedClass getClass(@PathParam("id") String name) {
		System.out.println("Searching for "+name);
		String namespace=null;
		String className=null;
		int lastDot = name.lastIndexOf('.');
		if (lastDot>0) {
			namespace = name.substring(0, lastDot);
			className = name.substring(lastDot+1);
		}
		else {
			className = name;
		}
		FQClassName fqc = new FQClassName(namespace, className, FQClassName.ALL);
		
		if (!classList.getCachedClasses().contains(fqc)) {
			//TODO unknown class
			return null;
		}
		
		
		String reflectedJson = loadClasses(className, reflectedClasses);

		String annotatedJson = loadClasses(className, annotatedClasses);
		//TODO deal with one or other being null
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			ClassRepresentation reflected = mapper.readValue(reflectedJson, ClassRepresentation.class);
			ClassRepresentation annotated = mapper.readValue(annotatedJson, ClassRepresentation.class);
			return new MergedClass(reflected, annotated);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	private String loadClasses(String className, MongoCollection<Document> collection) {
		//TODO include namespace in query
		Document reflectedDoc = collection.find(eq("name",className)).sort(descending("version")).first();
		System.out.println(reflectedDoc.toJson());
		String reflectedJson = reflectedDoc.toJson();
		return reflectedJson;
	}
	
	@POST
	public void save(MergedClass mergedClass) {
		String name = mergedClass.getName();
		ClassRepresentation oldAnnotated = loadOld(name);
		System.out.println(mergedClass);
		if (oldAnnotated==null) {
			oldAnnotated = new ClassRepresentation();
		}
		else {
			oldAnnotated.setVersion(oldAnnotated.getVersion()+1);
		}
		
		oldAnnotated.setComment(sanitizeUserText(mergedClass.getComment()));
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValueAsString(oldAnnotated);
			String json = mapper.writeValueAsString(oldAnnotated);
			Document document = Document.parse(json);
			annotatedClasses.insertOne(document);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//TODO return some kind of error
		}
		
	}

	private ClassRepresentation loadOld(String name) {
		ClassRepresentation oldAnnotated=null;
		Document annotatedDoc = annotatedClasses.find(eq("name",name)).sort(descending("version")).first();
		if (annotatedDoc!=null) {
			String annotatedJson = annotatedDoc.toJson();
			
			ObjectMapper mapper = new ObjectMapper();
			try {
				oldAnnotated = mapper.readValue(annotatedJson, ClassRepresentation.class);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		return oldAnnotated;
	}
	
	private String sanitizeUserText(String userText) {
		//TODO this probably needs more
		return userText.replace('<', '[').replace('>', ']');
	}
	

}
