package org.forwoods.docuwiki.documentationWiki.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassRepresentation {
	public enum Modifier{
		PUBLIC("public");
		
		private String stringRep;

		Modifier(String stringRep){
			this.stringRep = stringRep;			
		}
	}
	@JsonProperty
	private int version;
	
	private boolean usergenerated;
	
	private String namespace;
	private Modifier classModifier;
	@JsonProperty
	private String name;
	@JsonProperty
	private String typeComment;
	
	public ClassRepresentation(boolean usergenerated, String name) {

		
		version =1;
		this.usergenerated = usergenerated;
		this.name = name;
	}

	public String getTypeComment() {
		return typeComment;
	}

	public void setTypeComment(String typeComment) {
		this.typeComment = typeComment;
	}
	
	
	
	
	
}
