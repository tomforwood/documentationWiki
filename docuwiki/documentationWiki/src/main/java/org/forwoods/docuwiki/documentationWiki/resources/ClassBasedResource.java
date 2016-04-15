package org.forwoods.docuwiki.documentationWiki.resources;

import org.forwoods.docuwiki.documentationWiki.api.FQClassName;

public abstract class ClassBasedResource {

	protected ClassListResource classList;
	
	

	public ClassBasedResource(ClassListResource classList) {
		this.classList = classList;
	}

	protected boolean isValidClass(String name) {
		boolean validClass=false;
		String namespace=null;
		int lastDot = name.lastIndexOf('.');
		if (lastDot>0) {
			namespace = name.substring(0, lastDot);
		}
		FQClassName fqc = new FQClassName(namespace, name, FQClassName.ALL);
		
		validClass = classList.getCachedClasses().contains(fqc);
		return validClass;
	}

}
