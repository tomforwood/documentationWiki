package org.forwoods.docuwiki.documentationWiki;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.resources.ClassListResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassUsesResource;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
    	
    	
    	AssetsBundle assets = new AssetsBundle("/assets", "/", "index.html");
    	bootstrap.addBundle(assets);
    	bootstrap.getObjectMapper()
    		.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    }

    @Override
    public void run(final DocumentationWikiConfiguration configuration,
                    final Environment environment) {
    	if (configuration.isMongoSecured()) {
	    	MongoCredential mongoCred = MongoCredential.createCredential(
	    			configuration.getMongoUsername(),
	    			configuration.getMongoDatabase(), 
	    			configuration.getMongoPassword().toCharArray());
			List<MongoCredential> creds = Stream.of(mongoCred).collect(Collectors.toList());
			ServerAddress addr = new ServerAddress(configuration.getMongoHost(), 
					configuration.getMongoPort());
			
			client = new MongoClient(addr, creds);
    	}
    	else {
    		client = new MongoClient();
    	}
    	database = client.getDatabase(configuration.getMongoDatabase());
    	//TODO add shutdown hook to close client or something
    	
    	
    	MongoCollection<Document> reflectedClasses = database.getCollection("reflectedClasses");
    	
		MongoCollection<Document> annotatedClasses = database.getCollection("annotatedClasses");
		ClassListResource classList = new ClassListResource(reflectedClasses, annotatedClasses);
        ClassResource classes = new ClassResource(reflectedClasses, annotatedClasses, classList);
        ClassUsesResource uses = new ClassUsesResource(reflectedClasses);
        environment.jersey().register(classes);
        environment.jersey().register(classList);
        environment.jersey().register(uses);
    }

}
