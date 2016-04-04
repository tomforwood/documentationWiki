package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.antlr.v4.runtime.tree.ParseTree;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ConstructorDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.FormalParamContext;

public class ConstructorListener extends MemberListener {

	public ConstructorListener(Deque<Member> stack) {
		super(stack);
	}

	@Override
	public void exitConstructorDeclaration(ConstructorDeclarationContext ctx) {
		MethodRepresentation member = new MethodRepresentation();
		member.setComment(readComment(ctx.comment));
		readMods(ctx.consMods, member);
		
		if (ctx.consParams.paramList.children!=null) {
			for (ParseTree pt:ctx.consParams.paramList.children) {
				if (!(pt instanceof FormalParamContext)) continue;
				FormalParamContext param = (FormalParamContext)pt;
				Member paramMember = new Member();
				paramMember.setName(param.paramName.getText());
				String paramType = link(param.paramType.getText());
				paramMember.setObjectType(new ObjectType(paramType));
				member.parameters.add(paramMember);
			}
		}
		Member peek = stack.peek();
		if (peek==null) return;
		member.setName("*constructor*");
		member.setObjectType(peek.getObjectType());
		if (peek instanceof ClassRepresentation) {
			ClassRepresentation rep = (ClassRepresentation) peek;
			rep.addConstructor(member);
		}
	}

}
