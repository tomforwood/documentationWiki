package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDeserializer;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoCollection;


@Path("/class")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClassResource extends ClassBasedResource{
	
	private MongoCollection<Document> reflectedClasses;
	private MongoCollection<Document> annotatedClasses;
	private Clock clock = Clock.systemUTC();

	public ClassResource(MongoCollection<Document> reflectedClasses, 
			MongoCollection<Document> annotatedClasses,
			ClassListResource classList) {
		super(classList);
		this.reflectedClasses = reflectedClasses;
		this.annotatedClasses = annotatedClasses;
	}
	
	@GET
	@Path("/{id}")
	public MergedClass<? extends TopLevelDocumentable> 
			getClass(@PathParam("id") String name,
					@QueryParam("version") Integer version) {
		boolean validClass = isValidClass(name);
		if (!validClass) return null;
		
		String reflectedJson = loadClasses(name, reflectedClasses, null);

		String annotatedJson = loadClasses(name, annotatedClasses, version);
		
		ObjectMapper mapper = getMapper();
		try {
			TopLevelDocumentable reflected=null;
			if (reflectedJson!=null) {
				reflected = read(mapper, reflectedJson);
			}
			TopLevelDocumentable annotated=null;
			if (annotatedJson!=null) {
				annotated = read(mapper, annotatedJson);
			}
			MergedClass<TopLevelDocumentable> result = MergedClass.createMergedClass(reflected, annotated);
			result.setLatest(version==null);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	public static ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule sm = new SimpleModule()
				.addDeserializer(TopLevelDocumentable.class, 
						new TopLevelDeserializer())
				.addDeserializer(Long.class, new JsonDeserializer<Long>(){

					@Override
					public Long deserialize(JsonParser p, DeserializationContext ctxt)
							throws IOException, JsonProcessingException {
						JsonNode node = p.getCodec().readTree(p);
						if (node.has("$numberLong")) {
							Long value = node.get("$numberLong").asLong();
							return value;
						}
						else {
							return node.asLong();
						}
					}});
		mapper.registerModule(sm);
		return mapper;
	}

	protected TopLevelDocumentable read(ObjectMapper mapper, String json) throws JsonParseException, JsonMappingException, IOException {
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

	private String loadClasses(String className, MongoCollection<Document> collection, Integer version) {
		Bson query = eq("name",className);
		if (version!=null) {
			query = and(query,eq("version",version));
		}
		Document retrievedDoc = collection.find(query).sort(descending("version")).first();
		if (retrievedDoc==null) return null;
		JsonWriterSettings settings = new JsonWriterSettings(JsonMode.STRICT);
		String reflectedJson = retrievedDoc.toJson(settings);
		System.out.println(reflectedJson);
		return reflectedJson;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	public Response save(MergedClass<?> mergedClass,
			@QueryParam("action") String action, @Context HttpServletRequest request) {
		TopLevelDocumentable toSave=null;
		if (mergedClass.getObjectType().getTypeName().equals("class")) {
			toSave = saveClass((MergedClass<ClassRepresentation>) mergedClass);
			
		}
		else if (mergedClass.getObjectType().getTypeName().equals("enum")) {
			toSave = saveEnum((MergedClass<EnumRepresentation>)mergedClass);
		}
		
		if ("revert".equals(action)) {
			toSave.setModifyAction("Reverted to version "+mergedClass.getAnnotatedVersion());
			toSave.setVersion(fechNextVersion(mergedClass.getName()));
		}
		else {

			toSave.setVersion(mergedClass.getAnnotatedVersion()+1);
		}
			
		
		toSave.setModifyTime(clock.millis());
		toSave.setIpAddress(request.getRemoteAddr());
		
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

	private int fechNextVersion(String name) {
		Document retrievedDoc = annotatedClasses.find(eq("name",name))
				.sort(descending("version"))
				.first();
		return retrievedDoc.getInteger("version")+1;
	}

	private void setCommon(MergedClass<?> mergedClass, TopLevelDocumentable toSave) {
		toSave.setComment(commentTrim(mergedClass.getComment()));
		
		toSave.setName(mergedClass.getName());
		toSave.setUserGenerated(true);
		toSave.setObjectType(mergedClass.getObjectType());
		toSave.setNamespaceName(mergedClass.getNamespace());
		toSave.getModifiers().addAll(mergedClass.getClassModifiers());
	}
	
	private TopLevelDocumentable saveEnum(MergedClass<EnumRepresentation> mergedClass) {
		EnumRepresentation enumRep = new EnumRepresentation();
		mergedClass.getEnumConsts().stream()
			.peek(ec->ec.setComment(commentTrim(ec.getComment())))
			.filter(ec->ec.getComment()!=null)
			.collect(Collectors.toCollection(()->enumRep.getEnumValues()));

		setCommon(mergedClass, enumRep);
		return enumRep;
	}

	private TopLevelDocumentable saveClass(MergedClass<ClassRepresentation> mergedClass) {
		//TODO look up latest version number
		ClassRepresentation classRep = new ClassRepresentation();
		classRep.setVarargs(mergedClass.getVarargs());
		classRep.getExtensions().addAll(mergedClass.getExtensions());
		
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
		
		setCommon(mergedClass, classRep);
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

	public void setClock(Clock clock) {
		this.clock = clock;
	}
}
