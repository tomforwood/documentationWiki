package org.forwoods.docuwiki.documentable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
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

	public void setIsOrphaned(boolean isOrphaned) {
		this.isOrphaned = isOrphaned;
	}

}
