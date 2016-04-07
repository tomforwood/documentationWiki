package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.FieldDeclarationContext;

public class FieldListener extends MemberListener {

	public FieldListener(Deque<Member> stack, List<String> usings) {
		super(stack, usings);
	}

	@Override
	public void exitFieldDeclaration(FieldDeclarationContext ctx) {
		FieldRepresentation member = new FieldRepresentation();
		member.setComment(readComment(ctx.comment));
		member.setName(ctx.fieldName.getText());
		readMods(ctx.fieldMods,member);
		member.setObjectType(new ObjectType(ctx.fieldType.getText()));
		
		if (ctx.fieldAssignment!=null) {
			member.assignment=ctx.fieldAssignment.getText();
		}
		
		Documentable peek = stack.peek();
		if (peek==null) return;
		if (peek instanceof ClassRepresentation) {
			ClassRepresentation rep = (ClassRepresentation) peek;
			if (member.getModifiers().contains(Modifier.STATIC) ||
					member.getModifiers().contains(Modifier.CONSTANT)) {
				rep.addStaticField(member);
			}
			else {
				rep.addInstanceField(member);
			}
		}
		
	}
	
	

}
