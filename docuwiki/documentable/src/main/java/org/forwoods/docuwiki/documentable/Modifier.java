package org.forwoods.docuwiki.documentable;

import java.util.HashMap;
import java.util.Map;

public enum Modifier{
	PUBLIC("public"),
	PROTECTED("protected"),
	STATIC("static"),
	SEALED("sealed"),
	CONSTANT("const"),
	ABSTRACT("abstract"),
	VIRTUAL("virtual");

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
	
	public String toString() {
		return stringRep;
	}
}