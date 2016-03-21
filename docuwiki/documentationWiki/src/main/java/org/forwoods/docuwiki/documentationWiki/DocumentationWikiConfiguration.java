package org.forwoods.docuwiki.documentationWiki;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class DocumentationWikiConfiguration extends Configuration {
	
	private boolean mongoSecured=true;
	
	@NotEmpty
    private String mongoUsername;

    @NotEmpty
    private String mongoPassword;
    
    @NotEmpty
    private String mongoDatabase;

    private int mongoPort = 27017;
    
    public int getMongoPort() {
		return mongoPort;
	}

	public void setMongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
	}

	@NotEmpty
    private String mongoHost;

    public String getMongoHost() {
		return mongoHost;
	}

	public void setMongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
	}

	@JsonProperty
    public String getMongoUsername() {
        return mongoUsername;
    }

    @JsonProperty
    public void setMongoUsername(String mongoUsername) {
        this.mongoUsername = mongoUsername;
    }

    @JsonProperty
    public String getMongoPassword() {
        return mongoPassword;
    }

    @JsonProperty
    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

	public String getMongoDatabase() {
		return mongoDatabase;
	}

	public void setMongoDatabase(String mongoDatabase) {
		this.mongoDatabase = mongoDatabase;
	}

	public boolean isMongoSecured() {
		return mongoSecured;
	}

	public void setMongoSecured(boolean mongoSecured) {
		this.mongoSecured = mongoSecured;
	}
}
