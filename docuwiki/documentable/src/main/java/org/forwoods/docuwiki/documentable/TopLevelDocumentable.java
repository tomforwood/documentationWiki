package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("_id")

public abstract class TopLevelDocumentable extends Member {

	@JsonProperty
	protected int version;
	@JsonProperty
	protected boolean userGenerated;
	@JsonProperty
	protected String namespaceName;
	@JsonProperty
	protected List<String> extensions = new ArrayList<>();

	public TopLevelDocumentable(ObjectType type) {
		this.objectType = type;
		version =1;
	}
	public TopLevelDocumentable(ObjectType type, boolean userGenerated, String name) {
		this(type);
		this.userGenerated = userGenerated;
		this.name = name;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isUserGenerated() {
		return userGenerated;
	}

	public void setUserGenerated(boolean userGenerated) {
		this.userGenerated = userGenerated;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}
	
	public List<String> getExtensions() {
		return extensions;
	}
	
	public void addExtension(String ext) {
		extensions.add(ext);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
		result = prime * result + ((modifiers == null) ? 0 : modifiers.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((namespaceName == null) ? 0 : namespaceName.hashCode());
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
		result = prime * result + (userGenerated ? 1231 : 1237);
		result = prime * result + version;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopLevelDocumentable other = (TopLevelDocumentable) obj;
		if (extensions == null) {
			if (other.extensions != null)
				return false;
		} else if (!extensions.equals(other.extensions))
			return false;
		if (modifiers == null) {
			if (other.modifiers != null)
				return false;
		} else if (!modifiers.equals(other.modifiers))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespaceName == null) {
			if (other.namespaceName != null)
				return false;
		} else if (!namespaceName.equals(other.namespaceName))
			return false;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		if (userGenerated != other.userGenerated)
			return false;
		if (version != other.version)
			return false;
		return true;
	}
	
	

}