package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.forwoods.docuwiki.documentable.ClassRepresentation.PropertyRepresentation;
import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.PropertyDeclarationContext;

public class PropertyListener extends DocumentableListener {

	public PropertyListener(Deque<Documentable> stack) {
		super(stack);
	}

	@Override
	public void exitPropertyDeclaration(PropertyDeclarationContext ctx) {
		PropertyRepresentation member = new PropertyRepresentation();
		member.setComment(readComment(ctx.comment));
		member.setName(ctx.propName.getText());
		if (ctx.propMods!=null && ctx.propMods.children!=null) {
			for (ParseTree pt:ctx.propMods.children) {
				member.addModifier(Modifier.lookup(pt.getText()));
			}
		}
		
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
