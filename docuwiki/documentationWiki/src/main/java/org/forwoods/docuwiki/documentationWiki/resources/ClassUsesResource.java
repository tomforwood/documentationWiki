package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.*;

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
import org.forwoods.docuwiki.documentationWiki.DocumentationWikiApplication;
import org.forwoods.docuwiki.documentationWiki.api.ClassUse;
import org.forwoods.docuwiki.documentationWiki.api.ClassUses;

import com.codahale.metrics.Timer;
import com.mongodb.client.MongoCollection;

@Path("/class/uses")
@Produces(MediaType.APPLICATION_JSON)
public class ClassUsesResource extends ClassBasedResource{
	private MongoCollection<Document> reflectedClasses;
	private Timer classUsesTimer;
	
	public ClassUsesResource(MongoCollection<Document> reflectedClasses,
			ClassListResource classList) {
		super(classList);
		this.reflectedClasses = reflectedClasses;
		classUsesTimer = DocumentationWikiApplication.metrics.timer("ClassUsesTimer");
	}
	
	@GET
	@Path("/{id}")
	public ClassUses getClass(@PathParam("id") String name) {
		try (Timer.Context context = classUsesTimer.time()) {
			boolean validClass = isValidClass(name);
			if (!validClass) return null;
			
			ClassUses cu = new ClassUses(name);		
			
			Bson b = memberSearch("instanceFields",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "F", 
					cr->cr.getInstanceFields()));
			
			 b = memberSearch("staticFields",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "F",
					cr->cr.getStaticFields()));
			
			 b = memberSearch("instanceProperties",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "P",
					cr->cr.getInstanceProperties()));
				
			b = memberSearch("staticProperties",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "P",
					cr->cr.getStaticProperties()));
			
			b = memberSearch("staticMethods",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "M",
					cr->cr.getStaticMethods()));
			
			b = memberSearch("instanceMethods",name);
			reflectedClasses.find(b)
			.forEach(new MemberTypeConsumer(cu.getUsesReturns(), name, "M",
					cr->cr.getInstanceMethods()));
			
			//now methods with matching parameter type
			b=parameterSearch("instanceMethods", name);
			reflectedClasses.find(b)
			.forEach(new ParameterTypeConsumer(cu.getUsesParameters(), name, "M",
					cr->cr.getInstanceMethods()));
			//now methods with matching parameter type
			b=parameterSearch("staticMethods", name);
			reflectedClasses.find(b)
			.forEach(new ParameterTypeConsumer(cu.getUsesParameters(), name, "M",
					cr->cr.getStaticMethods()));
			b=parameterSearch("constructors", name);
			reflectedClasses.find(b)
			.forEach(new ParameterTypeConsumer(cu.getUsesParameters(), name, "C",
					cr->cr.getInstanceMethods()));
			
			
			//now for annotations
			
			b = attributeSearch("instanceFields",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesAttributes(), name, "F",
					cr->cr.getInstanceFields()));
			
			 b = attributeSearch("staticFields",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesAttributes(), name, "F",
					cr->cr.getStaticFields()));
			
			 b = attributeSearch("instanceProperties",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesAttributes(), name, "P",
					cr->cr.getInstanceProperties()));
				
			b = attributeSearch("staticProperties",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesAttributes(), name, "P",
					cr->cr.getStaticProperties()));
			
			b = attributeSearch("staticMethods",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesAttributes(), name, "M",
					cr->cr.getStaticMethods()));
			
			b = attributeSearch("instanceMethods",name);
			reflectedClasses.find(b)
			.forEach(new AttributeTypeConsumer(cu.getUsesReturns(), name, "M",
					cr->cr.getInstanceMethods()));
			
					
			return cu;
		}
	}
	
	private Bson memberSearch(String memberCollection, String type) {		
		return or(eq(memberCollection+".objectType.typeName",type),eq(memberCollection+".objectType.varargs.typeName",type));
	}
	
	private Bson parameterSearch(String memberCollection, String type) {
		return or(eq(memberCollection+".parameters.objectType.typeName",type),eq(memberCollection+"parameters.objectType.varargs.typeName",type));
	}
	
	private Bson attributeSearch(String memberCollection, String type) {
		return regex(memberCollection+".attributes", "\\["+type);
	}
	
	private class MemberTypeConsumer implements Consumer<Document> {

		List<ClassUse> uses;
		String matchType;
		private Function<ClassRepresentation, List<? extends Member>> membersFunc;
		private String useType;
		
		private MemberTypeConsumer(List<ClassUse> uses, String type, String useType,
				Function<ClassRepresentation, List<? extends Member>> members) {
			this.uses = uses;
			this.matchType = type;
			this.useType = useType;
			this.membersFunc = members;
		}

		@Override
		public void accept(Document doc) {
			ClassRepresentation cr;
			try {
				cr = mapper.readValue(doc.toJson(), ClassRepresentation.class);
			List<? extends Member> members = membersFunc.apply(cr);
			for (Member member:members){
				if (member.getInheritedFrom()!=null) continue;
				boolean varArgsMatch = false;
				if (member.getObjectType().getVarargs()!=null)
					varArgsMatch=member.getObjectType().getVarargs().stream().anyMatch(va->va.getTypeName().equals(matchType));
				if (varArgsMatch || 
						member.getObjectType().getTypeName().equals(matchType)){
					//this member uses matchType in its type
					ClassUse cu = new ClassUse();
					cu.setUsingClassName(cr.getName());
					cu.setUsingMember(member.getName());
					cu.setUseType(useType);
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
		private String useType;
		
		private ParameterTypeConsumer(List<ClassUse> uses, String type, String useType,
				Function<ClassRepresentation, List<MethodRepresentation>> members) {
			this.uses = uses;
			this.matchType = type;
			this.useType = useType;
			this.membersFunc = members;
		}

		@Override
		public void accept(Document doc) {
			ClassRepresentation cr;
			try {
				cr = mapper.readValue(doc.toJson(), ClassRepresentation.class);
			List<MethodRepresentation> members = membersFunc.apply(cr);
			for (MethodRepresentation method:members){
				if (method.getInheritedFrom()!=null) continue;
				for (Member ot: method.parameters) {
					boolean varArgsMatch = false;
					if (ot.getObjectType().getVarargs()!=null)
						varArgsMatch=ot.getObjectType().getVarargs().stream().anyMatch(va->va.getTypeName().equals(matchType));
					if (varArgsMatch || 
							ot.getObjectType().getTypeName().equals(matchType)){
						//this member uses matchType in its type
						ClassUse cu = new ClassUse();
						cu.setUsingClassName(cr.getName());
						cu.setUsingMember(method.getName());
						cu.setUseType(useType);
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
	
	private class AttributeTypeConsumer implements Consumer<Document> {

		List<ClassUse> uses;
		String matchType;
		private Function<ClassRepresentation, List<? extends Member>> membersFunc;
		private String useType;
		
		private AttributeTypeConsumer(List<ClassUse> uses, String type, String useType,
				Function<ClassRepresentation, List<? extends Member>> members) {
			this.uses = uses;
			this.matchType = type;
			this.useType = useType;
			this.membersFunc = members;
		}

		@Override
		public void accept(Document doc) {
			ClassRepresentation cr;
			try {
				cr = mapper.readValue(doc.toJson(), ClassRepresentation.class);

				String match = "["+matchType;
				List<? extends Member> members = membersFunc.apply(cr);
				for (Member member:members){
					if (member.getInheritedFrom()!=null) continue;
					boolean attributeMatch = false;
					if (member.getAttributes()==null) continue;
					for (String s:member.getAttributes()){
						attributeMatch|=s.startsWith(match);
					}
					if (attributeMatch){
						//this member uses matchType in its type
						ClassUse cu = new ClassUse();
						cu.setUsingClassName(cr.getName());
						cu.setUsingMember(member.getName());
						cu.setUseType(useType);
						uses.add(cu);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
