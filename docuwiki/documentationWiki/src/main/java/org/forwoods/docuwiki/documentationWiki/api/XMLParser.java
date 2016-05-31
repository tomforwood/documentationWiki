package org.forwoods.docuwiki.documentationWiki.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XMLParser {
	String[] startTags= {"<summary","<param","<invariant","<remarks",
			"<returns","<paramref", "<code", "<example", "<para", "<c",
			"<see", "<typeparam", "<list", "<item", "<seealso"};
	String[] endTags = {"</summary>","</param>","</invariant>","</remarks>", 
			"</returns>","", "</code>", "</example>", "</para>", "</c>",
			"", "</typeparam>", "</list>", "</item>",""};
	
	List<Element> contents = new ArrayList<Element>();
	
	public void parse(String text) {
		//first section is <summary>
		int tagStart = Math.min(nextStart(text, 0), text.length());
		
		Element summary = new Element(0);
		String substring = text.substring(0,tagStart);
		substring = substring.trim();
		if (substring.length()>0) {
			summary.contents.add(new Characters(substring));
			contents.add(summary);
		}
		
		//parse nested tags and further content (that will get wrapped in <remarks>
		while (tagStart<text.length()) {
			int tagNum = tagAt(text,tagStart);
			Element el = new Element(tagNum);
			contents.add(el);
			int pos = el.parse(text, tagStart+startTags[tagNum].length());
			
			//find the next tag after the end of the above tags end
			tagStart = Math.min(nextStart(text, pos),text.length());
			
			//if there is text before the next tag wrap it in remarks
			if (tagStart!=pos) {
				Element remark = new Element(3);
				substring = text.substring(pos,tagStart);
				substring = substring.trim();
				if (substring.length()>0) {
					remark.contents.add(new Characters(substring));
					contents.add(remark);
				}
			}
		}
	}
	
	public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
		for (XML xml:contents) {
			xml.writeXML(writer);
		}
	}
	
	int nextStart(String text, int startPos) {
		int pos=Integer.MAX_VALUE;
		for (String s:startTags) {
			int indexOf = text.indexOf(s, startPos);
			if (indexOf>=0)  {
				char next = text.charAt(indexOf+s.length());
				if (next!='>' && next!=' ') {
					//">" and " " are the only valid chars after a tag name I think
					continue;
				}
				pos = Math.min(pos,  indexOf);
			}
		}
		return pos;
	}
	
	int tagAt(String text, int pos) {
		for (int i=0;i<startTags.length;i++) {
			if (text.startsWith(startTags[i], pos)) {
				//check the next char
				char next = text.charAt(pos+startTags[i].length());
				if (next=='>' || next==' ') {
					return i;
				}
			}
		}
		return -1;
	}
	
	abstract class XML {
		public abstract void writeXML(XMLStreamWriter writer) throws XMLStreamException;
	}
	
	class Characters extends XML {
		String text;
		public Characters(String substring) {
			text = substring;
		}
		@Override
		public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
			writer.writeCharacters(text);
		}
	}
	
	class Element extends XML {
		int enclosingTag;
		Map<String, String> attributes = new LinkedHashMap<>();
		List<XML> contents = new ArrayList<>();;
		
		public Element(int tag) {
			enclosingTag = tag;
		}

		public int parse(String text, int pos) {
			//find the close of the start tag
			int tagEnd = text.indexOf('>',pos);
			tagEnd = Math.min(tagEnd,  text.length());
			//read the attributes
			String attText = text.substring(pos, tagEnd);
			String[] atts = attText.split("[ \n]");
			for (String att:atts) {
				int eqpos = att.indexOf("=");
				if (eqpos<0) continue;
				String attName = att.substring(0,eqpos).trim();
				String attValue = att.substring(eqpos+1,att.length());
				attValue=attValue.replace("\"", "");//chop off quotes
				attValue=attValue.replace("/", "");//chop off tag end slash
				attributes.put(attName, attValue);
			}
			
			//read the contents
			//find the first possible end tag
			int endPos = text.indexOf(endTags[enclosingTag],tagEnd+1);
			//find the next nested tag
			int nested = nextStart(text, pos);
			if (nested<0) nested = text.length();
			
			String substring = text.substring(tagEnd+1, Math.min(endPos, nested));
			substring = substring.trim();
			if (substring.length()>0) {
				Characters textData = new Characters(substring);
				contents.add(textData);
			}
			
			if (nested<endPos) {//we have a nested tag
				int tagNum = tagAt(text,nested);
				Element el = new Element(tagNum);
				contents.add(el);
				pos=el.parse(text, nested+startTags[tagNum].length());
				
				endPos = text.indexOf(endTags[enclosingTag],pos);
				//find the next nested tag
				nested = nextStart(text, pos);
				if (nested<0) nested = text.length();
				
				substring = text.substring(pos, Math.min(endPos, nested));
				substring = substring.trim();
				if (substring.length()>0) {
					Characters textData = new Characters(substring);
					contents.add(textData);
				}
			}
			
			return endPos+endTags[enclosingTag].length();
		}

		@Override
		public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
			if (!contents.isEmpty()) {
				writer.writeStartElement(startTags[enclosingTag].substring(1));
			} else {
				writer.writeEmptyElement(startTags[enclosingTag].substring(1));
			}
			for (Map.Entry<String,String> attribute : attributes.entrySet()) {
				writer.writeAttribute(attribute.getKey(), attribute.getValue());
			}
			for (XML xml:contents) {
				xml.writeXML(writer);
			}
			if (!contents.isEmpty()) {
				writer.writeEndElement();
			}
		}
	}
}
