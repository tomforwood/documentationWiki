package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDeserializer;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.FQClassName;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
	public MergedClass<? extends TopLevelDocumentable> 
			getClass(@PathParam("id") String name) {
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
		
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule sm = new SimpleModule()
				.addDeserializer(TopLevelDocumentable.class, 
						new TopLevelDeserializer());
		mapper.registerModule(sm);
		try {
			TopLevelDocumentable reflected=null;
			if (reflectedJson!=null) {
				reflected = read(mapper, reflectedJson);
			}
			TopLevelDocumentable annotated=null;
			if (annotatedJson!=null) {
				annotated = read(mapper, annotatedJson);
			}
			return MergedClass.createMergedClass(reflected, annotated);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	private TopLevelDocumentable read(ObjectMapper mapper, String json) throws JsonParseException, JsonMappingException, IOException {
		Document doc = Document.parse(json);
		String objectType = ((Document)doc.get("objectType")).getString("typeName");
		Class<? extends TopLevelDocumentable> c=null;
		if (objectType.equals("class")) {
			c= ClassRepresentation.class;
		}
		if (objectType.equals("interface")) {
			c= ClassRepresentation.class;
		}
		if (objectType.equals("struct")) {
			c= ClassRepresentation.class;
		}
		if (objectType.equals("enum")) {
			c= EnumRepresentation.class;
		}
		return mapper.readValue(json, c);
	}

	private String loadClasses(String className, MongoCollection<Document> collection) {
		//TODO include namespace in query
		Document retrievedDoc = collection.find(eq("name",className)).sort(descending("version")).first();
		if (retrievedDoc==null) return null;
		JsonWriterSettings settings = new JsonWriterSettings(JsonMode.STRICT);
		String reflectedJson = retrievedDoc.toJson(settings);
		return reflectedJson;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	public Response save(MergedClass<?> mergedClass) {
		TopLevelDocumentable toSave=null;
		if (mergedClass.getObjectType().getTypeName().equals("class")) {
			toSave = saveClass((MergedClass<ClassRepresentation>) mergedClass);
			
		}
		else if (mergedClass.getObjectType().getTypeName().equals("enum")) {
			toSave = saveEnum((MergedClass<EnumRepresentation>)mergedClass);
		}
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValueAsString(toSave);
			String json = mapper.writeValueAsString(toSave);
			Document document = Document.parse(json);
			annotatedClasses.insertOne(document);
			URI created = new URI("/api/class/"+toSave.getName());
			return Response.created(created).build();
		} catch (JsonProcessingException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//TODO return some kind of error
		}
		return Response.serverError().entity("Server Error saving class").type("text/plain").build();
		
	}
	
	private TopLevelDocumentable saveEnum(MergedClass<EnumRepresentation> mergedClass) {
		EnumRepresentation enumRep = new EnumRepresentation();
		enumRep.setComment(commentTrim(mergedClass.getComment()));
		enumRep.setName(mergedClass.getName());
		enumRep.setUserGenerated(true);
		enumRep.setVersion(mergedClass.getAnnotatedVersion()+1);
		enumRep.setObjectType(mergedClass.getObjectType());
		mergedClass.getEnumConsts().stream()
			.peek(ec->ec.setComment(commentTrim(ec.getComment())))
			.filter(ec->ec.getComment()!=null)
			.collect(Collectors.toCollection(()->enumRep.getEnumValues()));

		return enumRep;
	}

	private TopLevelDocumentable saveClass(MergedClass<ClassRepresentation> mergedClass) {
		//TODO look up latest version number
		ClassRepresentation classRep = new ClassRepresentation();
		classRep.setComment(commentTrim(mergedClass.getComment()));
		
		classRep.setName(mergedClass.getName());
		classRep.setNamespaceName(mergedClass.getNamespace());
		classRep.setObjectType(mergedClass.getObjectType());
		classRep.setVarargs(mergedClass.getVarargs());
		classRep.setVersion(mergedClass.getAnnotatedVersion()+1);
		classRep.getModifiers().addAll(mergedClass.getClassModifiers());
		classRep.getExtensions().addAll(mergedClass.getExtensions());
		classRep.setUserGenerated(true);
		
		//copy all instance fields that have non-empty comments to the new rep
		compactify(mergedClass.getInstanceFields(),classRep.getInstanceFields());
		
		//copy all static fields that have non-empty comments to the new rep
		compactify(mergedClass.getStaticFields(), classRep.getStaticFields());
		
		//copy all instance properties that have non-empty comments to the new rep
		compactify(mergedClass.getInstanceProperties(), classRep.getInstanceProperties());
		
		//copy all static properties that have non-empty comments to the new rep
		compactify(mergedClass.getStaticProperties(), classRep.getStaticProperties());
		
		//copy all instance Methods that have non-empty comments to the new rep
		compactify(mergedClass.getInstanceMethods(), classRep.getInstanceMethods());
		
		//copy all static Methods that have non-empty comments to the new rep
		compactify(mergedClass.getStaticMethods(), classRep.getStaticMethods());
		
		//contructors
		compactify(mergedClass.getConstructors(), classRep.getConstructors());
		
		compactifyNested(mergedClass.getNested(), classRep.getNested());
		
		return classRep;
	}
	
	@SuppressWarnings("unchecked")
	private void compactifyNested(List<MergedClass<?>> nested, List<TopLevelDocumentable> out) {
		for (MergedClass<?> mergedClass: nested) {
			if (mergedClass.getObjectType().getTypeName().equals("class")) {
				out.add(saveClass((MergedClass<ClassRepresentation>) mergedClass));
				
			}
			else if (mergedClass.getObjectType().getTypeName().equals("enum")) {
				out.add(saveEnum((MergedClass<EnumRepresentation>)mergedClass));
			}
		}
	}

	private <M extends Member> void compactify(Collection<M> members, Collection<M> output) {
		members.stream().peek(mem->mem.setComment(commentTrim(mem.getComment())))
		.filter(mem->mem.getComment()!=null)
		.collect(Collectors.toCollection(()->output));
	}
	
	private String commentTrim(String rawComment) {
		if (rawComment==null) return null;
		String stripped = rawComment.replaceFirst("\\s+$", "");
		if (stripped.length()==0) return null;
		return stripped;		
	}
	

}
