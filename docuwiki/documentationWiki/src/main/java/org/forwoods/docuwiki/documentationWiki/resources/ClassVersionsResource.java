package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Projections.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.forwoods.docuwiki.documentationWiki.DocumentationWikiApplication;
import org.forwoods.docuwiki.documentationWiki.api.ClassVersion;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.mongodb.client.MongoCollection;

@Path("/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClassVersionsResource extends ClassBasedResource{
	

	private MongoCollection<Document> annotatedClasses;
	private Timer classVersionsTimer;
	private Histogram classVersionsHist;
	public ClassVersionsResource(MongoCollection<Document> annotatedClasses, ClassListResource classList) {
		super(classList);
		this.annotatedClasses = annotatedClasses;
		classVersionsTimer = DocumentationWikiApplication.metrics.timer("ClassVersionsTimer");
		classVersionsHist = DocumentationWikiApplication.metrics.histogram("ClassVersionsHist");
	}
	
	@GET
	@Path("/{id}")
	public List<ClassVersion> getClassVersions(@PathParam("id") String name) {
		try (Timer.Context context = classVersionsTimer.time()){
			List<ClassVersion> result;
			
			boolean validClass = isValidClass(name);
			if (!validClass) return null;
			
			List<Document> tlds = new ArrayList<>();
			Bson query = eq("name",name);
			Bson projection = include("name","version","ipAddress","modifyTime", 
					"modifyAction");
			annotatedClasses.find(query)
				.projection(projection)
				.sort(descending("version"))
				.into(tlds);
			
			result = tlds.stream().map(tld->{
				Long modifyTime = tld.getLong("modifyTime");
				modifyTime = modifyTime==null?-1:modifyTime;
				ClassVersion cv = new ClassVersion(tld.getString("name"), 
							tld.getString("ipAddress"), 
							modifyTime, 
							tld.getInteger("version"),
							tld.getString("modifyAction"));
					return cv;
				}).collect(Collectors.toList());
			
			classVersionsHist.update(result.size());
			return result;
		}
	}
	
}
