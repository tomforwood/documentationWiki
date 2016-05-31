package org.forwoods.docuwiki.documentationWiki.resources;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.forwoods.docuwiki.documentationWiki.api.XMLParser;
import org.junit.Before;
import org.junit.Test;

public class XMLDocTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testXMLStructure() throws XMLStreamException, FactoryConfigurationError {
		String structured = "This is a complicated "
				+ "<param name=\"test\" fish=\"cod\">xml containing<param>nested stuff "
				+ "and <</param></param>a<param>another param</param>thing";
		String expected = "<summary>This is a complicated </summary>"
				+ "<param name=\"test\" fish=\"cod\">xml containing<param>nested "
				+ "stuff and &lt;</param></param><remarks>a</remarks><param>another param</param>"
				+ "<remarks>thing</remarks>";
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(bout);
		
		XMLParser parser = new XMLParser();
		parser.parse(structured);
		parser.writeXML(writer);
		String result = new String(bout.toByteArray());
		assertEquals(expected, result);
	}

	@Test
	public void testXMLStructure2() throws XMLStreamException, FactoryConfigurationError {
		String structured = "This is a complicated "
				+ "<param name=\"test\" fish=\"cod\">xml containing<paramref name=signal/>"
				+ "and <</param>a<param>another param</param>thing";
		String expected = "<summary>This is a complicated </summary>"
				+ "<param name=\"test\" fish=\"cod\">xml containing<paramref name=\"signal\"/>"
				+ "and &lt;</param><remarks>a</remarks><param>another param</param>"
				+ "<remarks>thing</remarks>";
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(bout);
		
		XMLParser parser = new XMLParser();
		parser.parse(structured);
		parser.writeXML(writer);
		String result = new String(bout.toByteArray());
		assertEquals(expected, result);
	}
	
	
	@Test
	public void testXMLStructure3() throws XMLStreamException, FactoryConfigurationError {
		String structured = "Tracking station wrapper for IDiscoverable.RevealAltitude()  "
				+ "Requires DiscoveryLevels.StateVectors";
		String expected = "<summary>Tracking station wrapper for IDiscoverable.RevealAltitude()  "
				+ "Requires DiscoveryLevels.StateVectors</summary>";
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(bout);
		
		XMLParser parser = new XMLParser();
		parser.parse(structured);
		parser.writeXML(writer);
		String result = new String(bout.toByteArray());
		assertEquals(expected, result);
	}
}
