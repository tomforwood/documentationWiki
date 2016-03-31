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
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	public MergedClass<? extends TopLevelDocumentable> getClass(@PathParam("id") String name) {
		System.out.println("Searching for "+name);
		String namespace=null;
		int lastDot = name.lastIndexOf('.');
		if (lastDot>0) {
			namespace = name.substring(0, lastDot);
		}
		FQClassName fqc = new FQClassName(namespace, name, FQClassName.ALL);
		
		if (!classList.getCachedClasses().contains(fqc)) {
			//TODO unknown class
			return null;
		}
		
		
		String reflectedJson = loadClasses(name, reflectedClasses);

		String annotatedJson = loadClasses(name, annotatedClasses);
		//TODO deal with one or other being null
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			TopLevelDocumentable reflected = read(mapper, reflectedJson);
			TopLevelDocumentable annotated = read(mapper, annotatedJson);
			return MergedClass.createMergedClass(reflected, annotated);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	private TopLevelDocumentable read(ObjectMapper mapper, String json) throws JsonParseException, JsonMappingException, IOException {
		Document doc = Document.parse(json);
		String objectType = doc.getString("objectType");
		Class<? extends TopLevelDocumentable> c=null;
		if (objectType.equals("class")) {
			c= ClassRepresentation.class;
		}
		if (objectType.equals("enum")) {
			c= EnumRepresentation.class;
		}
		return mapper.readValue(json, c);
	}

	private String loadClasses(String className, MongoCollection<Document> collection) {
		//TODO include namespace in query
		Document reflectedDoc = collection.find(eq("name",className)).sort(descending("version")).first();
		
		JsonWriterSettings settings = new JsonWriterSettings(JsonMode.STRICT);
		String reflectedJson = reflectedDoc.toJson(settings);
		System.out.println(reflectedJson);
		return reflectedJson;
	}
	
	@POST
	public void save(MergedClass<?> mergedClass) {
		String name = mergedClass.getName();
		TopLevelDocumentable oldAnnotated=null;
		if (mergedClass.getObjectType().equals("class")) {
			ClassRepresentation classRep=loadOld(name, ClassRepresentation.class);
			
			if (classRep==null) {
				classRep = new ClassRepresentation();
				classRep.setVersion(0);//will get incremented to 1
			}
			oldAnnotated = classRep;
		}
		else if (mergedClass.getObjectType().equals("enum")) {
			EnumRepresentation enumRep=loadOld(name, EnumRepresentation.class);
			
			if (enumRep==null) {
				enumRep = new EnumRepresentation();
				enumRep.setVersion(0);//will get incremented to 1
			}
			oldAnnotated = enumRep;
		}
		System.out.println(mergedClass);
		
		oldAnnotated.setVersion(oldAnnotated.getVersion()+1);
		oldAnnotated.setUserGenerated(true);
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

	private <T extends TopLevelDocumentable> T loadOld(String name, Class<T> clazz) {
		//TODO is this a duplicate of other load methods?
		T oldAnnotated=null;
		Document annotatedDoc = annotatedClasses.find(eq("name",name)).sort(descending("version")).first();
		if (annotatedDoc!=null) {
			String annotatedJson = annotatedDoc.toJson();
			
			ObjectMapper mapper = new ObjectMapper();
			try {
				oldAnnotated = mapper.readValue(annotatedJson, clazz);
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
