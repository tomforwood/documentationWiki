package org.forwoods.docuwiki.documentationWiki;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.resources.ClassListResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DocumentationWikiApplication extends Application<DocumentationWikiConfiguration> {

    MongoDatabase database;
	private MongoClient client;
	
	public static void main(final String[] args) throws Exception {
        new DocumentationWikiApplication().run(args);
    }

    @Override
    public String getName() {
        return "Documentation Wiki";
    }

    @Override
    public void initialize(final Bootstrap<DocumentationWikiConfiguration> bootstrap) {

    	client = new MongoClient();
    	database = client.getDatabase("docuWiki");
    	//TODO add shutdown hook to close client or something
    	
    	AssetsBundle assets = new AssetsBundle("/assets", "/", "index.html");
    	bootstrap.addBundle(assets);
    	bootstrap.getObjectMapper()
    		.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    }

    @Override
    public void run(final DocumentationWikiConfiguration configuration,
                    final Environment environment) {
    	MongoCollection<Document> reflectedClasses = database.getCollection("reflectedClasses");
    	
		MongoCollection<Document> annotatedClasses = database.getCollection("annotatedClasses");
		ClassListResource classList = new ClassListResource(reflectedClasses, annotatedClasses);
        ClassResource resource = new ClassResource(reflectedClasses, annotatedClasses, classList);
        environment.jersey().register(resource);
    }

}
