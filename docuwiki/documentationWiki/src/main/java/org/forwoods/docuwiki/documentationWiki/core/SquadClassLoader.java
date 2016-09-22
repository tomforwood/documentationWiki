package org.forwoods.docuwiki.documentationWiki.core;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.ClassRepresentation.PropertyRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.EnumRepresentation.EnumConstant;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.ObjectType;
import org.forwoods.docuwiki.documentationWiki.api.SquadClass;


public class SquadClassLoader {
	private static final QName kindName = new QName("kind");
	
	private final SquadFileLoader fileLoader;
	
	private Map<String, SquadClass> loaded = new ConcurrentHashMap<>();
	
	protected Map<String, String> index = new HashMap<>();

	private XMLInputFactory fac = XMLInputFactory.newFactory();
	
	public SquadClassLoader(SquadFileLoader fileLoader) {
		this.fileLoader = fileLoader;
		fac.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "false");
	}
	
	public SquadClass loadClass(String className) throws IOException{
		if (loaded.containsKey(className)) {
			return loaded.get(className);
		}
		String filename = xmlFileName(className);
		SquadClass squadClass = loadClassFromFile(filename );
		if (squadClass!=null) {
			loaded.put(className, squadClass);
		}
		return squadClass;
	}

	protected SquadClass loadClassFromFile(String filename) throws IOException {

		SquadClass squadClass = new SquadClass();
		squadClass.setFileName(filename);
		InputStream classIn = fileLoader.readFile("xml/"+filename+".xml");
		XMLEventReader reader;
		try {
			reader = fac.createXMLEventReader(classIn);
			readToElement(reader, "compoundname");
			readCompound(reader, squadClass);
			reader.close();
			return squadClass;
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	private void readCompound(XMLEventReader reader, SquadClass squadClass) throws XMLStreamException, IOException {
		String className= reader.nextEvent().asCharacters().getData().replace("::", ".");
		squadClass.setName(className);
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().toString().equals("compounddef")) {
					return;
				}
				break;
			case XMLEvent.START_ELEMENT:
				StartElement start = event.asStartElement();
				if (start.getName().toString().equals("memberdef")) {
					readMember(reader, squadClass, start);
				}
				else if (start.getName().toString().equals("innerclass")) {
					SquadClass nested = readInner(reader);
					squadClass.getNested().add(nested);
				}
				else if (isDecription(start)) {
					readComment(reader, squadClass, start);
				}
				break;
			}
			
		}
		
	}

	private boolean isDecription(StartElement start) {
		return start.getName().toString().equals("briefdescription") ||
				start.getName().toString().equals("inbodydescription") ||
				start.getName().toString().equals("detaileddescription");
	}

	private SquadClass readInner(XMLEventReader reader) throws IOException, XMLStreamException {
		Characters asCharacters = reader.nextEvent().asCharacters();
		String name = asCharacters.getData();
		SquadClass sc = loadClass(name);
		return sc;
	}

	private void readMember(XMLEventReader reader, SquadClass squadClass, StartElement start) throws XMLStreamException {
		String kind = start.getAttributeByName(kindName).getValue();
		if (kind.equals("variable")) {
			FieldRepresentation field = new FieldRepresentation();
			readVar(reader, squadClass, field);
			squadClass.getFields().add(field);
		}
		else if (kind.equals("function")) {
			readMethod(reader, squadClass);
		}
		else if (kind.equals("property")) {
			PropertyRepresentation property = new PropertyRepresentation();
			readVar(reader, squadClass, property);
			squadClass.getProperties().add(property);
		}
		else if (kind.equals("enum")) {
			SquadClass enumRep = readEnum(reader, start.getName());
			squadClass.getNested().add(enumRep);
		}
	}
	
	private SquadClass readEnum(XMLEventReader reader, QName endTag) throws XMLStreamException {
		SquadClass enumDef = new SquadClass();
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().equals(endTag)) {
					return enumDef;
				}
				break;
			case XMLEvent.START_ELEMENT:
				StartElement start = event.asStartElement();
				if (start.getName().toString().equals("name")) {
					String name = reader.nextEvent().asCharacters().getData();
					enumDef.setName(name);
				}
				else if (start.getName().toString().equals("enumvalue")) {
					readEnumValue(reader, enumDef, start);
				}
				else if (isDecription(start)) {
					readComment(reader, enumDef, start);
				}
				break;
			}
			
		}
		return enumDef;
	}

	private void readEnumValue(XMLEventReader reader, SquadClass enumDef, StartElement start) throws XMLStreamException {
		EnumConstant cons = new EnumConstant();
		enumDef.getEnums().add(cons);
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().equals(start.getName())) return;
				break;
			case XMLEvent.START_ELEMENT:
				StartElement elStart = event.asStartElement();
				String elName = elStart.getName().toString();
				if (elName.equals("name")) {
					String name = reader.nextEvent().asCharacters().getData();
					cons.setName(name);
				}
				else if (isDecription(elStart)) {
					readComment(reader, cons, elStart);
				}
				break;
			}
			
		}
	}

	private void readMethod(XMLEventReader reader, SquadClass squadClass) throws XMLStreamException {
		MethodRepresentation method = new MethodRepresentation();
		squadClass.getMethods().add(method);
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().toString().equals("memberdef")) return;
				break;
			case XMLEvent.START_ELEMENT:
				StartElement elStart = event.asStartElement();
				String elName = elStart.getName().toString();
				if (elName.equals("name")) {
					String name = reader.nextEvent().asCharacters().getData();
					method.setName(name);
				}
				else if (elName.equals("param")) {
					readParam(reader, method);
				}
				else if (isDecription(elStart)) {
					readComment(reader, method, elStart);
				}
				break;
			}
			
		}
	}

	private void readParam(XMLEventReader reader, MethodRepresentation method) throws XMLStreamException {
		readToElement(reader, "type");
		String typeName = readTypeName(reader);
		Member param = new Member();
		ObjectType type = new ObjectType(typeName);
		param.setObjectType(type);
		method.parameters.add(param);
		readToEndElement(reader, "param");
	}

	private String readTypeName(XMLEventReader reader) throws XMLStreamException {
		String typeName=null;
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().toString().equals("type"))
					return typeName;
				break;
			case XMLEvent.CHARACTERS:
				typeName = event.asCharacters().getData();
				break;
			}
		}
		return typeName;
	}

	private void readVar(XMLEventReader reader, SquadClass squadClass, Member field) throws XMLStreamException {
		
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.END_ELEMENT:
				EndElement end = event.asEndElement();
				if (end.getName().toString().equals("memberdef")) return;
				break;
			case XMLEvent.START_ELEMENT:
				StartElement elStart = event.asStartElement();
				if (elStart.getName().toString().equals("name")) {
					String name = reader.nextEvent().asCharacters().getData();
					field.setName(name);
				}
				if (isDecription(elStart)) {
					readComment(reader, field, elStart);
				}
				break;
			}
		}
	}

	private void readComment(XMLEventReader reader, Documentable field, StartElement endTag) throws XMLStreamException {
		String comment = readAsText(reader, endTag);
		comment = comment.trim();
		if (comment.length()>0) {
			if (field.getSquadComment()==null) {
				field.setSquadComment(comment);
			}
			else {
				field.setSquadComment(field.getSquadComment()+"\n"+comment);
			}
		}
	}

	private String readAsText(XMLEventReader reader, StartElement endTag) throws XMLStreamException {
		StringBuilder builder = new StringBuilder();
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isEndElement() && event.asEndElement().getName().equals(endTag.getName())) {
				return builder.toString();
			}
			builder.append(event.toString());
		}
		return builder.toString();
	}

	private StartElement readToElement(XMLEventReader reader, String elementName) throws XMLStreamException {
		while (reader.hasNext()) {
			//read to the class def
			XMLEvent next = reader.nextEvent();
			if (!next.isStartElement()) continue;
			StartElement start = next.asStartElement();
			if (start.getName().toString().equals(elementName)) return start;
		}
		return null;
	}
	
	private EndElement readToEndElement(XMLEventReader reader, String elementName) throws XMLStreamException {
		while (reader.hasNext()) {
			//read to the class def
			XMLEvent next = reader.nextEvent();
			if (!next.isEndElement()) continue;
			EndElement end = next.asEndElement();
			if (end.getName().toString().equals(elementName)) return end;
		}
		return null;
	}

	public String xmlFileName(String classname) throws IOException {
		if (index.isEmpty()) {
			try {
				readIndex();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String dottedClassName = classname.replace("+",".");
		String colonClassName = dottedClassName.replace(".", "::");
		String xmlFile = index.get(colonClassName);
		if (xmlFile==null) {
			System.out.println("K");
		}
		return xmlFile;
	}
	
	protected synchronized void readIndex() throws IOException, XMLStreamException {
		if (!index.isEmpty()) return;
		InputStream indexIn = fileLoader.readFile("xml/index.xml");
		XMLEventReader reader = fac.createXMLEventReader(indexIn);
		QName refid = new QName("refid");
		QName kind = new QName("kind");
		StartElement start=null;
		while ((start=readToElement(reader, "compound"))!=null) {
			Attribute attributeByName = start.getAttributeByName(refid);
			if (attributeByName==null) {
				continue;
			}
			String kindAt = start.getAttributeByName(kind).getValue();
			if (kindAt.equals("file")) continue;
			String fileref = attributeByName.getValue();
			StartElement nameEvent = reader.nextEvent().asStartElement();
			if (!nameEvent.getName().toString().equals("name")) throw new UnexpectedException("error reading XML");
			Characters asCharacters = reader.nextEvent().asCharacters();
			String className = asCharacters.getData();
			index.put(className, fileref);
		}
		reader.close();
		System.out.println(index.size());
		
	}

	public void clearCachedClasses() {
		loaded.clear();
		index.clear();
	}
}
