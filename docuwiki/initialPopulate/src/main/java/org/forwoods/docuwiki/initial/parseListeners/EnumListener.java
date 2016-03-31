package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.EnumConstantContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.EnumDeclarationContext;

public class EnumListener extends DocumentableListener {
	
	public EnumListener(Deque<Documentable> stack) {
		super(stack);
	}

	@Override
	public void exitEnumDeclaration(EnumDeclarationContext ctx) {
		System.out.println("popping "+this.toString());
		EnumRepresentation rep = (EnumRepresentation)stack.pop();
		rep.setName(ctx.enumName.getText());
		rep.getModifiers().clear();
		if (ctx.enumMods!=null) {
			for (ParseTree pt:ctx.enumMods.children) {
				rep.addModifier(Modifier.lookup(pt.getText()));
			}
		}
		rep.setComment(readComment(ctx.comment));
	}

	@Override
	public void exitEnumConstant(EnumConstantContext ctx) {
		EnumRepresentation rep = (EnumRepresentation)stack.peek();
		String docComment = ctx.dockBlock.getText();
		String val = ctx.ident.getText();
		EnumConstant ec = new EnumRepresentation.EnumConstant(val);
	
		if (ctx.intVal!=null) {
			String value = ctx.intVal.getText();
			ec.setEnumValue(value);
		}
		ec.setComment(docComment);
		rep.addEnumValue(ec);
	}
	
	
	
	
}
