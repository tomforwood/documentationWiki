package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassExtensions {
	@JsonProperty
	private String className;
	
	@JsonProperty
	private Set<String> extendingClasses = new HashSet<String>();
	
	private int versionComputed;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getVersionComputed() {
		return versionComputed;
	}

	public void setVersionComputed(int versionComputed) {
		this.versionComputed = versionComputed;
	}

	public Set<String> getExtendingClasses() {
		return extendingClasses;
	}
}
