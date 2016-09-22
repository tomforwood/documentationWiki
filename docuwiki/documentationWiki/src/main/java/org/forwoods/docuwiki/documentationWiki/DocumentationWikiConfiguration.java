package org.forwoods.docuwiki.documentationWiki;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.knowm.dropwizard.sundial.SundialConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class DocumentationWikiConfiguration extends Configuration {
	
	private boolean mongoSecured=true;
	
	@NotEmpty
    private String mongoUsername;
    
    @NotEmpty
    private String mongoDatabase;
    
    @NotEmpty
    private String xmlFileLocation;
    
    @NotEmpty
    private String squadXMLFileLocation;
    
    @NotEmpty
    private String squadXMLFileSource;
    
    @Valid
    @NotNull
    public SundialConfiguration sundialConfiguration = new SundialConfiguration();

    public String getXmlFileLocation() {
		return xmlFileLocation;
	}

	public void setXmlFileLocation(String xmlFile) {
		this.xmlFileLocation = xmlFile;
	}

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

	public String getSquadXMLFileLocation() {
		return squadXMLFileLocation;
	}

	public void setSquadXMLFileLocation(String squadXMLFileLocation) {
		this.squadXMLFileLocation = squadXMLFileLocation;
	}

    public String getSquadXMLFileSource() {
		return squadXMLFileSource;
	}

	public void setSquadXMLFileSource(String squadXMLFileSource) {
		this.squadXMLFileSource = squadXMLFileSource;
	}

	@JsonProperty("sundial")
    public SundialConfiguration getSundialConfiguration() {

      return sundialConfiguration;
    }
}
