package org.forwoods.docuwiki.documentable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Documentable {
	@JsonProperty
	protected
	String comment;
	
	@JsonProperty
	protected boolean isOrphaned=false;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean getIsOrphaned() {
		return isOrphaned;
	}

	public void setOrphaned(boolean isOrphaned) {
		this.isOrphaned = isOrphaned;
	}

}
