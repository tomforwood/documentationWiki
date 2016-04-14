package org.forwoods.docuwiki.documentationWiki;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.db.MongoManaged;
import org.forwoods.docuwiki.documentationWiki.health.MongoHealthCheck;
import org.forwoods.docuwiki.documentationWiki.resources.ClassBasedResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassListResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassUsesResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassVersionsResource;

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
    	
    	
    	AssetsBundle assets = new AssetsBundle("/assets", "/", "index.html", "assets");
    	bootstrap.addBundle(assets);
    	
    	bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars", null, "webjars"));
    	
    	bootstrap.getObjectMapper()
    		.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    }

    @Override
    public void run(final DocumentationWikiConfiguration configuration,
                    final Environment environment) {
    	if (configuration.isMongoSecured()) {
    		
    		String password = System.getenv("MONGO_PASS");
    		
	    	MongoCredential mongoCred = MongoCredential.createCredential(
	    			configuration.getMongoUsername(),
	    			configuration.getMongoDatabase(), 
	    			password.toCharArray());
			List<MongoCredential> creds = Stream.of(mongoCred).collect(Collectors.toList());
			ServerAddress addr = new ServerAddress(configuration.getMongoHost(), 
					configuration.getMongoPort());
			
			client = new MongoClient(addr, creds);
    	}
    	else {
    		client = new MongoClient();
    	}
    	database = client.getDatabase(configuration.getMongoDatabase());
    	
    	MongoManaged mongoManaged= new MongoManaged(client); 
    	
    	environment.lifecycle().manage(mongoManaged);
    	environment.healthChecks().register("mongoHealthchack", new MongoHealthCheck(client));
    	
    	
    	MongoCollection<Document> reflectedDocuments = database.getCollection("reflectedClasses");
		MongoCollection<Document> annotatedDocuments = database.getCollection("annotatedClasses");
		
		ClassListResource classList = new ClassListResource(reflectedDocuments, annotatedDocuments);
        ClassVersionsResource versions = new ClassVersionsResource(annotatedDocuments, classList);
        
        ClassBasedResource classes = new ClassResource(reflectedDocuments, annotatedDocuments, classList);
		ClassUsesResource uses = new ClassUsesResource(reflectedDocuments, classList);
        
        
        environment.jersey().register(classes);
        environment.jersey().register(classList);
        environment.jersey().register(uses);
        environment.jersey().register(versions);
    }

}
