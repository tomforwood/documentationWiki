package org.forwoods.docuwiki.documentable;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ClassRepresentation extends Documentable{
	public enum Modifier{
		
		
		PUBLIC("public"),
		PRIVATE("private");

		static Map<String, Modifier> lookup = new HashMap<>();
		
		static {
			for (Modifier mod:Modifier.values()) {
				lookup.put(mod.stringRep, mod);
			}
		}
		
		private String stringRep;

		Modifier(String stringRep){
			this.stringRep = stringRep;
		}
		
		public static Modifier lookup(String s) {
			return lookup.get(s);
		}
	}
	@JsonProperty
	private int version;
	
	private boolean usergenerated;
	
	private String namespace;
	private Modifier classModifier;
	@JsonProperty
	private String name;
	
	public ClassRepresentation(boolean usergenerated, String name) {
		version =1;
		this.usergenerated = usergenerated;
		this.name = name;
	}

	public ClassRepresentation() {
		version = 1;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isUsergenerated() {
		return usergenerated;
	}

	public void setUsergenerated(boolean usergenerated) {
		this.usergenerated = usergenerated;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Modifier getClassModifier() {
		return classModifier;
	}

	public void setClassModifier(Modifier classModifier) {
		this.classModifier = classModifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
