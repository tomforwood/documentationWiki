package org.forwoods.docuwiki.documentationWiki.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassVersion {
	@JsonProperty
	String name;
	
	@JsonProperty
	String editingAddress;
	
	@JsonProperty
	Long editTimestamp;
	
	@JsonProperty
	int version;

	@JsonProperty
	String action;
	
	public ClassVersion(String name, String editingAddress, Long editTimestamp, int version, String action) {
		super();
		this.name = name;
		this.editingAddress = editingAddress;
		this.editTimestamp = editTimestamp;
		this.version = version;
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEditingAddress() {
		return editingAddress;
	}

	public void setEditingAddress(String editingAddress) {
		this.editingAddress = editingAddress;
	}

	public long getEditTimestamp() {
		return editTimestamp;
	}

	public void setEditTimestamp(Long editTimestamp) {
		this.editTimestamp = editTimestamp;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
