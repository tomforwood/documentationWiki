package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassUses {
	@JsonProperty
	private String className;
	
	@JsonProperty
	List<ClassUse> usesReturns = new ArrayList<>();
	
	@JsonProperty
	List<ClassUse> usesParameters = new ArrayList<>();
		
	public ClassUses(String name) {
		className = name;
	}
	
	public ClassUses(){}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<ClassUse> getUsesReturns() {
		return usesReturns;
	}

	public void setUsesReturns(List<ClassUse> usesReturns) {
		this.usesReturns = usesReturns;
	}

	public List<ClassUse> getUsesParameters() {
		return usesParameters;
	}

	public void setUsesParameters(List<ClassUse> usesParameters) {
		this.usesParameters = usesParameters;
	}
}
