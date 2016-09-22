package org.forwoods.docuwiki.documentable;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Documentable {
	@JsonProperty
	protected String comment;
	@JsonProperty
	protected String squadComment;
	@JsonProperty
	protected List<String> attributes;
	
	@JsonProperty
	protected boolean isOrphaned=false;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSquadComment() {
		return squadComment;
	}

	public void setSquadComment(String squadComment) {
		this.squadComment = squadComment;
	}

	public boolean getIsOrphaned() {
		return isOrphaned;
	}

	public void setIsOrphaned(boolean isOrphaned) {
		this.isOrphaned = isOrphaned;
	}

	public List<String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
	

}
