package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.FieldDeclarationContext;

public class FieldListener extends DocumentableListener {

	public FieldListener(Deque<Documentable> stack) {
		super(stack);
	}

	@Override
	public void exitFieldDeclaration(FieldDeclarationContext ctx) {
		FieldRepresentation member = new FieldRepresentation();
		member.setComment(readComment(ctx.comment));
		member.setName(ctx.fieldName.getText());
		if (ctx.fieldMods!=null) {
			for (ParseTree pt:ctx.fieldMods.children) {
				member.addModifier(Modifier.lookup(pt.getText()));
			}
		}
		member.setObjectType(new ObjectType(ctx.fieldType.getText()));
		
		if (ctx.fieldAssignment!=null) {
			member.assignment=ctx.fieldAssignment.getText();
		}
		
		Documentable peek = stack.peek();
		if (peek==null) return;
		if (peek instanceof ClassRepresentation) {
			ClassRepresentation rep = (ClassRepresentation) peek;
			if (member.getModifiers().contains(Modifier.STATIC)) {
				rep.addStaticField(member);
			}
			else {
				rep.addInstanceField(member);
			}
		}
		
	}
	
	

}
