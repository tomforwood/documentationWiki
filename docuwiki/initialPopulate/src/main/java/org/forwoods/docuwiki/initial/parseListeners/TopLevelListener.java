package org.forwoods.docuwiki.initial.parseListeners;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.EnumRepresentation;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ClassDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.EnumDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.InterfaceDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.NamespaceContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.StructDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.UsingContext;

public class TopLevelListener extends MemberListener {
	
	TopLevelDocumentable topItem;
	
	public TopLevelListener(BasicCSharpParser parser) {
		super(new LinkedList<>(), new ArrayList<>());
		parser.addParseListener(new ClassListener(stack, usings));
		parser.addParseListener(new EnumListener(stack, usings));
		parser.addParseListener(new FieldListener(stack, usings));
		parser.addParseListener(new PropertyListener(stack, usings));
		parser.addParseListener(new MethodListener(stack, usings));
		parser.addParseListener(new ConstructorListener(stack, usings));
	}

	public TopLevelDocumentable getRep() {
		return topItem;
	}

	@Override
	public void enterClassDeclaration(ClassDeclarationContext ctx) {
		ClassRepresentation rep = new ClassRepresentation();
		if (stack.isEmpty()) {
			topItem = rep;
		}
		else {
			ClassRepresentation parent = (ClassRepresentation) stack.peek();
			parent.addNested(rep);
		}
		stack.push(rep);
	}

	@Override
	public void enterEnumDeclaration(EnumDeclarationContext ctx) {
		EnumRepresentation rep = new EnumRepresentation();
		if (stack.isEmpty()) {
			topItem = rep;
		}
		else {
			ClassRepresentation parent = (ClassRepresentation) stack.peek();
			parent.addNested(rep);
		}
		stack.push(rep);
	}

	@Override
	public void enterStructDeclaration(StructDeclarationContext ctx) {
		ClassRepresentation rep = new ClassRepresentation();
		rep.getObjectType().setTypeName("struct");
		if (stack.isEmpty()) {
			topItem = rep;
		}
		else {
			ClassRepresentation parent = (ClassRepresentation) stack.peek();
			parent.addNested(rep);
		}
		stack.push(rep);
	}
	
	@Override
	public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		ClassRepresentation rep = new ClassRepresentation();
		rep.getObjectType().setTypeName("interface");
		if (stack.isEmpty()) {
			topItem = rep;
		}
		else {
			ClassRepresentation parent = (ClassRepresentation) stack.peek();
			parent.addNested(rep);
		}
		stack.push(rep);
	}

	@Override
	public void exitNamespace(NamespaceContext ctx) {
		String ns = ctx.namespaceName.getText();
		topItem.setNamespaceName(ns);
		topItem.setName(ns+"."+topItem.getName());
	}

	@Override
	public void exitUsing(UsingContext ctx) {
		usings.add(ctx.useName.getText());
	}

	public List<String> getUsings() {
		return usings;
	}
	
	
}