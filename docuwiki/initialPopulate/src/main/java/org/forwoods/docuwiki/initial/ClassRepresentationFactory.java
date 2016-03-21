package org.forwoods.docuwiki.initial;

import java.io.IOException;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.forwoods.docuwiki.documentable.ClassRepresentation;
import org.forwoods.docuwiki.documentable.Modifier;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpBaseListener;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpLexer;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ClassBodyContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.ClassDeclarationContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.DocCommentBlockContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.GenericMethodContext;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.PropertyDeclarationContext;

public class ClassRepresentationFactory {
	
	public ClassRepresentation createClassRep(Reader reader) throws IOException {
		BasicCSharpLexer tokenSource = new BasicCSharpLexer(new ANTLRInputStream(reader));
		BasicCSharpParser parser = new BasicCSharpParser(new CommonTokenStream(tokenSource));
		parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
		
		ClassRepresentation rep = new ClassRepresentation();
		rep.setVersion(1);
		parser.addParseListener(new ClassListener(rep));
		parser.compilationUnit();
		if (rep.getName()==null) return null;//this wasn't a class (it was probably an enum
		//TODO cope with enums
		return rep;
	}
	private final class ClassListener extends BasicCSharpBaseListener {
		private ClassRepresentation rep;

		public ClassListener(ClassRepresentation rep) {
			this.rep = rep;
		}

		@Override
		public void enterClassDeclaration(ClassDeclarationContext ctx) {
			
		}

		@Override
		public void exitClassDeclaration(ClassDeclarationContext ctx) {
			rep.setName(ctx.name.getText());
			rep.getClassModifiers().clear();
			if (ctx.classmods!=null) {
				for (ParseTree pt:ctx.classmods.children) {
					rep.addClassModifier(Modifier.lookup(pt.getText()));
				}
			}
			
			rep.setComment(readComment(ctx.comment));
		}

		private String readComment(DocCommentBlockContext comment) {
			System.out.println(comment);
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
		public void exitClassBody(ClassBodyContext ctx) {
			
		}
	}


}
