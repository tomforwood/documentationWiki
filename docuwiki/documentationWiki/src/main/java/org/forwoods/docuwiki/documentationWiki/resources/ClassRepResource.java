package org.forwoods.docuwiki.documentationWiki.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.forwoods.docuwiki.documentationWiki.api.ClassRepresentation;

@Path("/class")
@Produces(MediaType.APPLICATION_JSON)
public class ClassRepResource {
	
	public ClassRepResource() {
		//probably want mongo stuff as a param
	}
	
	@GET
	public ClassRepresentation getClass(@QueryParam("className") String name) {
		return new ClassRepresentation(false, "GameEvents");
	}
	

}
