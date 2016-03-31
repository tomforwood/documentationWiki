package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ClassDeclarationContext;

public class ClassListener extends DocumentableListener{

	public ClassListener(Deque<Documentable> stack) {
		super(stack);
	}

	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		System.out.println("popping "+this.toString());
		//System.out.println(stack.size());
		System.out.println(ctx.getText());
		ClassRepresentation rep = (ClassRepresentation) stack.pop();
		//System.out.println(rep);
		if (ctx.name==null) return;
		rep.setName(ctx.name.getText());
		rep.getModifiers().clear();
		if (ctx.classmods!=null) {
			for (ParseTree pt:ctx.classmods.children) {
				rep.addModifier(Modifier.lookup(pt.getText()));
			}
		}
		rep.setComment(readComment(ctx.comment));
	}

}
