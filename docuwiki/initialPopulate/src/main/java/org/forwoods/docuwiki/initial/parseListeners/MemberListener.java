package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private final static Map<String, String> replacements = new HashMap<>();
	
	static {
		//list of pointless tags to strip out
		replacements.put("<summary>", "");
		replacements.put("</summary>", "");
		replacements.put("<remarks>", "");
		replacements.put("</remarks>", "");
		replacements.put("<description>", "");
		replacements.put("</description>", "");
	}

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
		String line=commentLine.substring(3);//remove the ///
		for (Map.Entry<String, String> replace:replacements.entrySet()) {
			line = line.replace(replace.getKey(), replace.getValue());
		}
		return line;
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