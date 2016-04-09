package org.forwoods.docuwiki.documentationWiki.db;

import com.mongodb.MongoClient;

import io.dropwizard.lifecycle.Managed;

public class MongoManaged implements Managed {
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
		mongo.close();
	}

}
