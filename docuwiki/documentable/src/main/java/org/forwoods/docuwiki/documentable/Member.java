package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Member extends Documentable {

	@JsonProperty
	protected String objectType;
	@JsonProperty
	protected List<Modifier> modifiers = new ArrayList<>();
	@JsonProperty
	protected String name;
	
	
	public String getObjectType() {
		return objectType;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	public void addModifier(Modifier modifier) {
		modifiers.add(modifier);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	

}
