package org.forwoods.docuwiki.initial.parseListeners;

import java.util.Deque;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpBaseListener;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.DocCommentBlockContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ExtendedContext;

public class DocumentableListener extends BasicCSharpBaseListener {
	
	Deque<Documentable> stack;

	public DocumentableListener(Deque<Documentable> stack) {
		this.stack = stack;
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

}