package org.forwoods.docuwiki.documentationWiki.api;

import java.util.function.BiFunction;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FQClassName implements Comparable<FQClassName> {

	@JsonProperty
	String namespace;
	@JsonProperty
	String className;
	@JsonProperty
	int subset;
	
	public final static int REFLECTED=1;
	public final static int ANNOTATED=2;
	public static final int ALL = REFLECTED|ANNOTATED;
	
	public FQClassName(String namespace, String className, int set) {
		super();
		this.namespace = namespace;
		this.className = className;
		this.subset=set;
	}
	
	public FQClassName addSubset(int set) {
		subset|=set;
		return this;
	}
	
	public static BiFunction<FQClassName,FQClassName,FQClassName> mergeFunction = 
			(a,b) -> {return a.addSubset(b.getSubset());};
	
	@Override
	public int compareTo(FQClassName o) {
		int nsComp = 0;
		if (this.namespace!=null && o.namespace!=null) {
			nsComp= this.namespace.compareTo(o.namespace);
		}
		return nsComp==0?this.className.compareTo(o.className):nsComp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
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
		FQClassName other = (FQClassName) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	public int getSubset() {
		return subset;
	}

	@Override
	public String toString() {
		return "FQClassName [namespace=" + namespace + ", className=" + className + ", subset=" + subset + "]";
	}
	
	
	
}
