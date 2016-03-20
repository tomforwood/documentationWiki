package org.forwoods.docuwiki.documentable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("_id")
public class ClassRepresentation extends Documentable{
	@JsonProperty
	private int version;
	
	private boolean userGenerated;
	
	private String namespaceName;
	@JsonProperty
	private Modifier classModifier;
	@JsonProperty
	private String name;
	
	public ClassRepresentation(boolean userGenerated, String name) {
		version =1;
		this.userGenerated = userGenerated;
		this.name = name;
	}

	public ClassRepresentation() {
		version = 1;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isUserGenerated() {
		return userGenerated;
	}

	public void setUserGenerated(boolean userGenerated) {
		this.userGenerated = userGenerated;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public Modifier getClassModifier() {
		return classModifier;
	}

	public void setClassModifier(Modifier classModifier) {
		this.classModifier = classModifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
