package org.forwoods.docuwiki.documentationWiki;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.forwoods.docuwiki.documentationWiki.api.ClassExtensions;
import org.forwoods.docuwiki.documentationWiki.core.SquadClassLoader;
import org.forwoods.docuwiki.documentationWiki.core.SquadFileLoader;
import org.forwoods.docuwiki.documentationWiki.core.SquadZipFileLoader;
import org.forwoods.docuwiki.documentationWiki.db.MongoManaged;
import org.forwoods.docuwiki.documentationWiki.health.MongoHealthCheck;
import org.forwoods.docuwiki.documentationWiki.jobs.XMLDownloadJob;
import org.forwoods.docuwiki.documentationWiki.resources.ClassListResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassUsesResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassVersionsResource;
import org.forwoods.docuwiki.documentationWiki.resources.ClassesExtendingResource;
import org.forwoods.docuwiki.documentationWiki.resources.XMLDocResource;
import org.knowm.dropwizard.sundial.SundialBundle;
import org.knowm.dropwizard.sundial.SundialConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DocumentationWikiApplication extends Application<DocumentationWikiConfiguration> {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentationWikiApplication.class);

    MongoDatabase database;
	private MongoClient client;
	public static MetricRegistry metrics = new MetricRegistry();
	
	public static void main(final String[] args) throws Exception {
        new DocumentationWikiApplication().run(args);
    }

    @Override
    public String getName() {
        return "Documentation Wiki";
    }

    @Override
    public void initialize(final Bootstrap<DocumentationWikiConfiguration> bootstrap) {
    	
    	bootstrap.setConfigurationSourceProvider(
    			new SubstitutingSourceProvider(new ResourceConfigurationSourceProvider(),  
    					new EnvironmentVariableSubstitutor()));
    	
    	AssetsBundle assets = new AssetsBundle("/assets", "/", "index.html", "assets");
    	bootstrap.addBundle(assets);
    	
    	//AssetsBundle assets2 = new AssetsBundle(, "/", "index.html", "assets");
    	//.addBundle(assets);
    	
    	bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars", null, "webjars"));
    	
    	bootstrap.getObjectMapper()
    		.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    	
    	metrics = bootstrap.getMetricRegistry();
    	
    	//add job running bundle
    	bootstrap.addBundle(new SundialBundle<DocumentationWikiConfiguration>() {

    	    @Override
    	    public SundialConfiguration getSundialConfiguration(DocumentationWikiConfiguration configuration) {
    	      return configuration.getSundialConfiguration();
    	    }
    	  });

    }

    @Override
    public void run(final DocumentationWikiConfiguration configuration,
                    final Environment environment) throws MalformedURLException {

		File docSaveLocation = new File(configuration.getSquadXMLFileLocation());
		
		SquadFileLoader fileLoader = new SquadZipFileLoader(docSaveLocation.toURI().toURL());
		SquadClassLoader squadLoader = new SquadClassLoader(fileLoader);
    	
    	if (configuration.isMongoSecured()) {
    		logger.info("Connecting to mongo as user {}", configuration.getMongoUsername());
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
    	environment.healthChecks().register("mongoHealthcheck", new MongoHealthCheck(client));
    	
    	
    	MongoCollection<Document> reflectedDocuments = database.getCollection("reflectedClasses");
		MongoCollection<Document> annotatedDocuments = database.getCollection("annotatedClasses");
		
		ClassListResource classList = new ClassListResource(reflectedDocuments, annotatedDocuments);
        ClassVersionsResource versions = new ClassVersionsResource(annotatedDocuments, classList);
        ClassesExtendingResource extensions = new ClassesExtendingResource(reflectedDocuments, classList);
		
        ClassResource classes = new ClassResource(reflectedDocuments, annotatedDocuments, 
        		classList, squadLoader, extensions);
		ClassUsesResource uses = new ClassUsesResource(reflectedDocuments, classList);
		
        String xmlLoc=configuration.getXmlFileLocation();
		XMLDocResource xmlDoc = new XMLDocResource(new File(xmlLoc), annotatedDocuments, classes);
        
        environment.jersey().register(classes);
        environment.jersey().register(classList);
        environment.jersey().register(uses);
        environment.jersey().register(extensions);
        environment.jersey().register(versions);
        environment.jersey().register(xmlDoc);
        
        
        logger.info("Registering AssetBundle with name: {} for path {}", "xmlFiles", xmlLoc + "/*");
        
        //run the squad xml download job
        try {
			XMLDownloadJob.setUrl(new URL(configuration.getSquadXMLFileSource()));
			if (!docSaveLocation.exists()) {
				docSaveLocation.getParentFile().mkdirs();
			}
			XMLDownloadJob.setDocSaveLocation(docSaveLocation);
			XMLDownloadJob.setSquadLoader(squadLoader);
		} catch (MalformedURLException e) {
			logger.error("malformed squad doc URL",e);
		}
        new XMLDownloadJob().doRun();

    }

}
