package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.FormalParamContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.MethodDeclarationContext;

public class MethodListener extends MemberListener {
	public MethodListener(Deque<Member> stack, List<String> usings) {
		super(stack, usings);
	}

	@Override
	public void exitMethodDeclaration(MethodDeclarationContext ctx) {
		MethodRepresentation member = new MethodRepresentation();
		member.setComment(readComment(ctx.comment));
		
		readMods(ctx.methodMods, member);
		
		if (ctx.methodParams.paramList.children!=null) {
			for (ParseTree pt:ctx.methodParams.paramList.children) {
				if (!(pt instanceof FormalParamContext)) continue;
				FormalParamContext param = (FormalParamContext)pt;
				Member paramMember = new Member();
				paramMember.setName(param.paramName.getText());
				String paramType = link(param.paramType.getText());
				paramMember.setObjectType(new ObjectType(paramType));
				member.parameters.add(paramMember);
			}
		}
				
		member.setName(ctx.methodName.getText());
		member.setObjectType(new ObjectType(link(ctx.methodType.getText())));
		
		Documentable peek = stack.peek();
		if (peek==null) return;
		if (peek instanceof ClassRepresentation) {
			ClassRepresentation rep = (ClassRepresentation) peek;
			if (member.getModifiers().contains(Modifier.STATIC)) {
				rep.addStaticMethod(member);
			}
			else {
				rep.addInstanceMethod(member);
			}
		}
		
	}
	
}
