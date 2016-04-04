package org.forwoods.docuwiki.documentationWiki.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassUse {
	@JsonProperty
	String usingClassName;
	@JsonProperty
	String usingMember;
	public String getUsingClassName() {
		return usingClassName;
	}
	public void setUsingClassName(String usingClassName) {
		this.usingClassName = usingClassName;
	}
	public String getUsingMember() {
		return usingMember;
	}
	public void setUsingMember(String usingMember) {
		this.usingMember = usingMember;
	}
	
	
}
