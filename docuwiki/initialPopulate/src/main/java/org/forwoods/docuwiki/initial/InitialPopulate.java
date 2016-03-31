package org.forwoods.docuwiki.initial;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Validator;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class InitialPopulate {

	private MongoDatabase database;
	private String collectionName= "annotatedClasses";
	private Validator validator;
	private MongoClient mongo;


	@SuppressWarnings("deprecation")
	public InitialPopulate() throws URISyntaxException, ProcessingException {
		//TODO password should be env var or command line
		
		mongo = new MongoClient();
		database = mongo.getDatabase("docuWiki");
		validator = new Validator();
	}
	
	public Document convertOne(Path file)  {
		String stringRep=null;
		try {
			System.out.println(file);
			BufferedReader reader = Files.newBufferedReader(file);
			DocumentableFactory factory = new DocumentableFactory();
			Member rep =factory.createTopLevel(reader);
			
			if (rep==null) return null;
			
			ObjectMapper mapper = new ObjectMapper();
			stringRep = mapper.writeValueAsString(rep);
			
			/*ProcessingReport validate = validator.validate(mapper.valueToTree(rep));
			if (!validate.isSuccess()) {
				throw new ProcessingException("Validation failed"+validate.toString());
			}*/
			System.out.println("Stringrep="+stringRep);
			Document bson = Document.parse(stringRep); 
			
			return bson;
		} catch (IllegalArgumentException | IOException e) {
			throw new RuntimeException("Error reading "+file+ stringRep,e);
		}
	}
	
	
	public void convertAll() throws IOException {
		MongoCollection<Document> dbCollection = database.getCollection(collectionName);
		dbCollection.drop();
		Path directory= Paths.get("C:/Users/Tom/Source/Repos/XML-Documentation-for-the-KSP-API/src");
		
		List<Document> batch = new ArrayList<>();
		int count = 0;
		for (Path path : Files.newDirectoryStream(directory, "*.cs")) {
			if (batch.size()>20) {
				dbCollection.insertMany(batch);
				batch = new ArrayList<>();
			}
			Document doc = convertOne(path);
			if (doc!=null) {
				batch.add(doc);
				count++;
			}
		}
		if (!batch.isEmpty()) {
			dbCollection.insertMany(batch);
		}

		System.out.println(count);
		
	}
	
	
	
	public static void main(String[] args) throws IOException, URISyntaxException, ProcessingException {
		InitialPopulate pop = new InitialPopulate();
		pop.convertAll();
		//pop.mongo.close();
	}
}
