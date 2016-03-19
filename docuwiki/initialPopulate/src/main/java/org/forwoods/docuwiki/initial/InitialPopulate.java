package org.forwoods.docuwiki.initial;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;


public class InitialPopulate {

	private DB database;
	private String collectionName= "annotatedClasses";


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
		System.out.println(insert.getSavedId());
	}
	
	
	public static void main(String[] args) throws IOException {
		InitialPopulate pop = new InitialPopulate();
		pop.convertOne(pop.getClass().getClassLoader().getResourceAsStream("Part.cs"));
		
	}
}
