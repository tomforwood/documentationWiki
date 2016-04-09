package org.forwoods.docuwiki.documentationWiki.health;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.MongoClient;

public class MongoHealthCheck extends HealthCheck {

	MongoClient mongo;

	public MongoHealthCheck(MongoClient mongo) {
		super();
		this.mongo = mongo;
	}
	

	@Override
	protected Result check() throws Exception {
		mongo.listDatabaseNames();
		return Result.healthy();
	}

}
