package initialPopulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpLexer;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser;
import org.forwoods.docuwiki.initial.parsers.BasicCSharpParser.CompilationUnitContext;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		URL resource = this.getClass().getClassLoader().getResource("Part.cs");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
		CharStream cstream = new ANTLRInputStream(reader);
		TokenStream tokens = new CommonTokenStream(new BasicCSharpLexer(cstream));
		BasicCSharpParser parser = new BasicCSharpParser(tokens);
		CompilationUnitContext file = parser.compilationUnit();
		
		
		System.out.println(file.toStringTree());
	}

}
