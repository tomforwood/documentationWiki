package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.EnumConstantContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.EnumDeclarationContext;

public class EnumListener extends MemberListener {
	
	public EnumListener(Deque<Member> stack) {
		super(stack);
	}

	@Override
	public void exitEnumDeclaration(EnumDeclarationContext ctx) {
		EnumRepresentation rep = (EnumRepresentation)stack.pop();
		rep.setName(ctx.enumName.getText());
		rep.getModifiers().clear();
		readMods(ctx.enumMods, rep);
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
