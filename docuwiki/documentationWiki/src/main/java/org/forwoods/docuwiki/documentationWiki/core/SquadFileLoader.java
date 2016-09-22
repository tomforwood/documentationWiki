package org.forwoods.docuwiki.documentationWiki.core;

import java.io.IOException;
import java.io.InputStream;

public interface SquadFileLoader {

	InputStream readFile(String filename) throws IOException;

}