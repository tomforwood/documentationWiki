package org.forwoods.docuwiki.initial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;


public class InitialPopulate {

	private DB database;
	private String collectionName= "annotatedClasses";


	@SuppressWarnings("deprecation")
	public InitialPopulate() {
		MongoClient mongo = new MongoClient();
		database = mongo.getDB("docuWiki");
	}
	
	public void populateAll(File directory) {
		
	}
	
	public void convertOne(InputStream file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file));
		ClassRepresentationFactory factory = new ClassRepresentationFactory();
		ClassRepresentation rep =factory.createClassRep(reader);
		
		DBCollection dbCollection = database.getCollection(collectionName);
		JacksonDBCollection<ClassRepresentation, Object> coll = JacksonDBCollection.wrap(dbCollection , ClassRepresentation.class);
		WriteResult<ClassRepresentation, Object> insert = coll.insert(rep);
		//BsonDocument.parse(json) TODO use this method?
		System.out.println(insert.getSavedId());
	}
	
	
	public static void main(String[] args) throws IOException {
		InitialPopulate pop = new InitialPopulate();
		pop.convertOne(pop.getClass().getClassLoader().getResourceAsStream("Part.cs"));
		
	}
}
