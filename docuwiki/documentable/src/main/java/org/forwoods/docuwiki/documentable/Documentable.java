package org.forwoods.docuwiki.documentable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Documentable {
	@JsonProperty
	protected
	String comment;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	

}
