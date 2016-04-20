package org.forwoods.docuwiki.documentationWiki.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.MongoClient;

public class MongoHealthCheck extends HealthCheck {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoHealthCheck.class);

	MongoClient mongo;

	public MongoHealthCheck(MongoClient mongo) {
		super();
		this.mongo = mongo;
	}
	

	@Override
	protected Result check() throws Exception {
		LOGGER.info("Performing a healthcheck on Mongo");
		mongo.listDatabaseNames();
		return Result.healthy();
	}

}
