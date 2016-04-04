package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDeserializer;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.ClassUse;
import org.forwoods.docuwiki.documentationWiki.api.ClassUses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoCollection;

@Path("/class/uses")
@Produces(MediaType.APPLICATION_JSON)
public class ClassUsesResource {
	private MongoCollection<Document> reflectedClasses;
	
	ObjectMapper mapper = new ObjectMapper();
	
	public ClassUsesResource(MongoCollection<Document> reflectedClasses) {
		this.reflectedClasses = reflectedClasses;
		SimpleModule sm = new SimpleModule()
				.addDeserializer(TopLevelDocumentable.class, 
						new TopLevelDeserializer());
		mapper.registerModule(sm);
	}
	
	@GET
	@Path("/{id}")
	public ClassUses getClass(@PathParam("id") String name) {
		ClassUses cu = new ClassUses(name);
		
		
		
		Bson b = memberSearch("instanceFields",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getInstanceFields()));
		
		 b = memberSearch("staticFields",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getStaticFields()));
		
		 b = memberSearch("instanceProperties",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getInstanceProperties()));
			
		b = memberSearch("staticProperties",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getStaticProperties()));
		
		b = memberSearch("staticMethods",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getStaticMethods()));
		
		b = memberSearch("instanceMethods",name);
		reflectedClasses.find(b)
		.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, cr->cr.getInstanceMethods()));
		
		//now methods with matching parameter type
		b=parameterSearch("instanceMethods", name);
		reflectedClasses.find(b)
		.forEach(new ParameterTypeConsumer(cu.getUsesParameters(), name, cr->cr.getInstanceMethods()));
		//now methods with matching parameter type
		b=parameterSearch("staticMethods", name);
		reflectedClasses.find(b)
		.forEach(new ParameterTypeConsumer(cu.getUsesParameters(), name, cr->cr.getStaticMethods()));
				
				
		return cu;
	}
	
	private Bson memberSearch(String memberCollection, String type) {
		
		return or(eq(memberCollection+".objectType.typeName",type),eq(memberCollection+".objectType.varargs.typeName",type));
	}
	
	private Bson parameterSearch(String memberCollection, String type) {
		return or(eq(memberCollection+".parameters.objectType.typeName",type),eq(memberCollection+"parameters.objectType.varargs.typeName",type));
	}
	
	private class MemberTypeConsumer implements Consumer<Document> {

		List<ClassUse> uses;
		String matchType;
		private Function<ClassRepresentation, List<? extends Member>> membersFunc;
		
		private MemberTypeConsumer(List<ClassUse> uses, String type, 
				Function<ClassRepresentation, List<? extends Member>> members) {
			this.uses = uses;
			this.matchType = type;
			this.membersFunc = members;
		}

		@Override
		public void accept(Document doc) {
			ClassRepresentation cr;
			try {
				cr = mapper.readValue(doc.toJson(), ClassRepresentation.class);
			List<? extends Member> members = membersFunc.apply(cr);
			for (Member member:members){
				boolean varArgsMatch = false;
				if (member.getObjectType().getVarargs()!=null)
					varArgsMatch=member.getObjectType().getVarargs().stream().anyMatch(va->va.getTypeName().equals(matchType));
				if (varArgsMatch || 
						member.getObjectType().getTypeName().equals(matchType)){
					//this member uses matchType in its type
					ClassUse cu = new ClassUse();
					cu.setUsingClassName(cr.getName());
					cu.setUsingMember(member.getName());
					uses.add(cu);
				}
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private class ParameterTypeConsumer implements Consumer<Document> {

		List<ClassUse> uses;
		String matchType;
		private Function<ClassRepresentation, List<MethodRepresentation>> membersFunc;
		
		private ParameterTypeConsumer(List<ClassUse> uses, String type, 
				Function<ClassRepresentation, List<MethodRepresentation>> members) {
			this.uses = uses;
			this.matchType = type;
			this.membersFunc = members;
		}

		@Override
		public void accept(Document doc) {
			ClassRepresentation cr;
			try {
				cr = mapper.readValue(doc.toJson(), ClassRepresentation.class);
			List<MethodRepresentation> members = membersFunc.apply(cr);
			for (MethodRepresentation method:members){
				for (Member ot: method.parameters) {
					boolean varArgsMatch = false;
					if (ot.getObjectType().getVarargs()!=null)
						varArgsMatch=ot.getObjectType().getVarargs().stream().anyMatch(va->va.getTypeName().equals(matchType));
					if (varArgsMatch || 
							ot.getObjectType().getTypeName().equals(matchType)){
						//this member uses matchType in its type
						ClassUse cu = new ClassUse();
						cu.setUsingClassName(cr.getName());
						cu.setUsingMember(method.toString());
						uses.add(cu);
					}
				
				}
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
