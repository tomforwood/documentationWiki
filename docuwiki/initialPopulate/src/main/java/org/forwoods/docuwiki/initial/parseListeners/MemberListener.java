package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.initial.InitialPopulate;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpBaseListener;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.CmodsContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.DocCommentBlockContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ExtendedContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ModifiersContext;

public class MemberListener extends BasicCSharpBaseListener {
	
	Deque<Member> stack;
	List<String> usings;

	public MemberListener(Deque<Member> stack, List<String> usings) {
		this.stack = stack;
		this.usings = usings;
	}

	protected String readComment(DocCommentBlockContext comment) {
		StringBuilder result = new StringBuilder();
		for (TerminalNode node:comment.DocComment()) {
			result.append(trimCommentLine(node.getText()));
		}
		return result.toString().trim();
	}

	private String trimCommentLine(String commentLine) {
		return commentLine.substring(3).replace("<summary>", "").replace("</summary>", "");
	}

	@Override
	public void exitExtended(ExtendedContext ctx) {
		TopLevelDocumentable rep = (TopLevelDocumentable)stack.peek();
		rep.addExtension(ctx.extName.getText());
		//TODO this is run by every listerer and duplicates
	}

	protected void readMods(ModifiersContext mods, Member member) {
		if (mods!=null && mods.children!=null) {
			for (ParseTree pt:mods.children) {
				member.addModifier(Modifier.lookup(pt.getText()));
			}
		}
	}


	protected void readMods(CmodsContext mods, Member member) {
		if (mods!=null && mods.children!=null) {
			for (ParseTree pt:mods.children) {
				member.addModifier(Modifier.lookup(pt.getText()));
			}
		}
	}

	protected String link(String typeName) {
		if (InitialPopulate.readClasses.contains(typeName)) {
			return typeName;
		}

		for (String using:usings) {
			//try each using as a prefix
			String resolved = using+"."+typeName;
			if (InitialPopulate.readClasses.contains(resolved)) {
				return resolved;
			}
		}
		return typeName;
	}

}