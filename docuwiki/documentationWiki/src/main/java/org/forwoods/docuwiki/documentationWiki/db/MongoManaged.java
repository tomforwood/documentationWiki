package org.forwoods.docuwiki.documentationWiki.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;

import io.dropwizard.lifecycle.Managed;

public class MongoManaged implements Managed {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoManaged.class);
	
	MongoClient mongo;
	
	public MongoManaged(MongoClient mongo) {
		super();
		this.mongo = mongo;
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Stopping mongo client");
		mongo.close();
	}

}
