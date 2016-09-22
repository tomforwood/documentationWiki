package org.forwoods.docuwiki.documentationWiki.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SquadZipFileLoader implements SquadFileLoader {


	private final URL squadZipFile;
	
	public SquadZipFileLoader(URL docSaveLocation) {
		squadZipFile = docSaveLocation;
	}

	@Override
	public InputStream readFile(String filename) throws IOException {
		
		ZipInputStream zin = new ZipInputStream(squadZipFile.openStream());
		ZipEntry zen=null;
		while ((zen=zin.getNextEntry())!=null) {
			if (zen.getName().equals(filename)) {
				return new XMLAmpFixerInputStream(zin);
			}
		}
		return null;
	}
}
