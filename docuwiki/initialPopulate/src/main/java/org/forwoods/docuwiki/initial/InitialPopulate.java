package org.forwoods.docuwiki.initial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Validator;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;


public class InitialPopulate {

	private DB database;
	private String collectionName= "annotatedClasses";
	private Validator validator;


	@SuppressWarnings("deprecation")
	public InitialPopulate() throws URISyntaxException, ProcessingException {
		//TODO password should be env var or command line
		
		MongoCredential mongoCred = MongoCredential.createCredential("docuWikiUser", "docuWiki", "***REMOVED***".toCharArray());
		List<MongoCredential> creds = Stream.of(mongoCred).collect(Collectors.toList());
		ServerAddress addr = new ServerAddress("127.0.0.1");
		MongoClient mongo = new MongoClient(addr, creds);
		database = mongo.getDB("docuWiki");
		validator = new Validator();
	}
	
	public void populateAll(File directory) {
		
	}
	
	public void convertOne(InputStream file) throws IOException, IllegalArgumentException, ProcessingException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		ClassRepresentationFactory factory = new ClassRepresentationFactory();
		ClassRepresentation rep =factory.createClassRep(reader);
		
		if (rep==null) return;
		
		ObjectMapper mapper = new ObjectMapper();
		System.out.println(mapper.writeValueAsString(rep));
		
		ProcessingReport validate = validator.validate(mapper.valueToTree(rep));
		if (!validate.isSuccess()) {
			throw new ProcessingException("Validation failed"+validate.toString());
		}
		DBCollection dbCollection = database.getCollection(collectionName);
		JacksonDBCollection<ClassRepresentation, Object> coll = JacksonDBCollection.wrap(dbCollection , ClassRepresentation.class);
		WriteResult<ClassRepresentation, Object> insert = coll.insert(rep);
		//BsonDocument.parse(json) TODO use this method?
		
		System.out.println(insert.getSavedId());
	}
	
	
	public static void main(String[] args) throws IOException, URISyntaxException, ProcessingException {
		InitialPopulate pop = new InitialPopulate();
		
		Path directory= Paths.get("C:/Users/Tom/Source/Repos/XML-Documentation-for-the-KSP-API/src");
		Files.newDirectoryStream(directory, "*.cs").
			forEach(path->{try {
				System.out.println(path.getFileName());
				pop.convertOne(Files.newInputStream(path));
			} catch (Exception e) {
			
			throw new RuntimeException(e);
		}});
		
		
	}
}
