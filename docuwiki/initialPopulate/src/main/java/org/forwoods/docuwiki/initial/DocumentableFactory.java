package org.forwoods.docuwiki.initial;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.initial.parseListeners.TopLevelListener;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpLexer;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser;

public class DocumentableFactory {
	
	public TopLevelDocumentable createTopLevel(Reader reader) throws IOException {
		BasicCSharpLexer tokenSource = new BasicCSharpLexer(new ANTLRInputStream(reader));
		BasicCSharpParser parser = new BasicCSharpParser(new CommonTokenStream(tokenSource));
		parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
            }
        });
		
		TopLevelListener listener = new TopLevelListener(parser);
		parser.addParseListener(listener);
		parser.compilationUnit();
		TopLevelDocumentable rep = listener.getRep();
		List<String> usings = listener.getUsings();
		//if (rep==null) return null;
		//TODO
		
		rep.setVersion(1);
		rep.setUserGenerated(true);
		
		link(rep, usings);
		
		return rep;
	}

	private void link(Member rep, List<String> usings) {
		// TODO look up with usings against full class list to work out full name
		
	}


}
