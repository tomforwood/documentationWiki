package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.PropertyRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.PropertyDeclarationContext;

public class PropertyListener extends MemberListener {

	public PropertyListener(Deque<Member> stack) {
		super(stack);
	}

	@Override
	public void exitPropertyDeclaration(PropertyDeclarationContext ctx) {
		PropertyRepresentation member = new PropertyRepresentation();
		member.setComment(readComment(ctx.comment));
		member.setName(ctx.propName.getText());

		readMods(ctx.propMods,member); 
		
		member.setObjectType(new ObjectType(ctx.propType.getText()));
		//I don't really need any of the rest
		
		Documentable peek = stack.peek();
		if (peek==null) return;
		if (peek instanceof ClassRepresentation) {
			ClassRepresentation rep = (ClassRepresentation) peek;
			if (member.getModifiers().contains(Modifier.STATIC)) {
				rep.addStaticProperty(member);
			}
			else {
				rep.addInstanceProperty(member);
			}
		}
	}

}
