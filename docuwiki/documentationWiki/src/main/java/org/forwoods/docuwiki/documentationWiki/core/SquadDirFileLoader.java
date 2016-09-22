package org.forwoods.docuwiki.documentationWiki.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SquadDirFileLoader implements SquadFileLoader {
	
	public File squadDirectory;

	public SquadDirFileLoader(File squadDir) {
		squadDirectory = squadDir;
	}

	@Override
	public InputStream readFile(String filename) throws IOException {
		File f = new File(squadDirectory, filename);
		return new FileInputStream(f);
	}

}
