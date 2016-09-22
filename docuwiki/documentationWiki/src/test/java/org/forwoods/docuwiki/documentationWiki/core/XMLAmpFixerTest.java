package org.forwoods.docuwiki.documentationWiki.core;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class XMLAmpFixerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReadValidEntity() throws IOException {
		String dataS = "this is valid containing &amp;";
		byte[] data = dataS.getBytes();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		
		byte[] output = new byte[data.length];
		
		XMLAmpFixerInputStream fixer = new XMLAmpFixerInputStream(bis);
		
		fixer.read(output);
		
		assertEquals(dataS, new String(output));
		
		fixer.close();
		
	}

	@Test
	public void testReadInvalid() throws IOException {
		String fixedS = "this is invalid containing &amp;";
		String dataS = "this is invalid containing &";
		byte[] data = dataS.getBytes();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		
		byte[] output = new byte[fixedS.length()];
		
		
		XMLAmpFixerInputStream fixer = new XMLAmpFixerInputStream(bis);
		
		fixer.read(output);
		
		assertEquals(fixedS, new String(output));
		
		fixer.close();
	}

}
