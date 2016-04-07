package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ClassDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.InterfaceDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.StructDeclarationContext;

public class ClassListener extends MemberListener{

	public ClassListener(Deque<Member> stack, List<String> usings) {
		super(stack, usings);
	}

	@Override
	public void exitClassDeclaration(ClassDeclarationContext ctx) {
		//System.out.println(stack.size());
		System.out.println(ctx.getText());
		ClassRepresentation rep = (ClassRepresentation) stack.pop();
		//System.out.println(rep);
		if (ctx.name==null) return;
		String name = ctx.name.getText();
		rep.setName(name);
		for (TopLevelDocumentable tld:rep.getNested()){
			//update nested classes with their nested name
			tld.setName(name+"+"+tld.getName());
		}
		rep.getModifiers().clear();
		readMods(ctx.classmods, rep);
		rep.setComment(readComment(ctx.comment));
	}
	
	@Override
	public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		//System.out.println(stack.size());
		System.out.println(ctx.getText());
		ClassRepresentation rep = (ClassRepresentation) stack.pop();
		//System.out.println(rep);
		if (ctx.name==null) return;
		rep.setName(ctx.name.getText());
		rep.getModifiers().clear();
		readMods(ctx.classmods, rep);
		rep.setComment(readComment(ctx.comment));
	}
	
	@Override
	public void exitStructDeclaration(StructDeclarationContext ctx) {
		//System.out.println(stack.size());
		System.out.println(ctx.getText());
		ClassRepresentation rep = (ClassRepresentation) stack.pop();
		//System.out.println(rep);
		if (ctx.name==null) return;
		rep.setName(ctx.name.getText());
		rep.getModifiers().clear();
		readMods(ctx.classmods, rep);
		rep.setComment(readComment(ctx.comment));
	}

}
