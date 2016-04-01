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

public class TopLevelListener extends DocumentableListener {
	
	TopLevelDocumentable topItem;
	List<String> usings = new ArrayList<>();
	
	public TopLevelListener(BasicCSharpParser parser) {
		super(new LinkedList<>());
		parser.addParseListener(new ClassListener(stack));
		parser.addParseListener(new EnumListener(stack));
		parser.addParseListener(new FieldListener(stack));
		parser.addParseListener(new PropertyListener(stack));
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
		System.out.println("pushing");
		stack.push(rep);
	}

	@Override
	public void enterEnumDeclaration(EnumDeclarationContext ctx) {
		EnumRepresentation rep = new EnumRepresentation();
		if (stack.isEmpty()) {
			topItem = rep;
		}
		System.out.println("pushing");
		stack.push(rep);
	}

	@Override
	public void enterStructDeclaration(StructDeclarationContext ctx) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
		// TODO Auto-generated method stub
	}

	@Override
	public void exitNamespace(NamespaceContext ctx) {
		topItem.setNamespaceName(ctx.namespaceName.getText());
	}

	@Override
	public void exitUsing(UsingContext ctx) {
		usings.add(ctx.useName.getText());
	}

	public List<String> getUsings() {
		return usings;
	}
	
	
}