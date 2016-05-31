package org.forwoods.docuwiki.documentationWiki.resources;

import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.forwoods.docuwiki.documentable.ClassRepresentation.MethodRepresentation;
import org.forwoods.docuwiki.documentable.Documentable;
import org.forwoods.docuwiki.documentable.Member;
import org.forwoods.docuwiki.documentable.TopLevelDocumentable;
import org.forwoods.docuwiki.documentationWiki.api.MergedClass;
import org.forwoods.docuwiki.documentationWiki.api.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Path("/xml")
public class XMLDocResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLDocResource.class);

	private MongoCollection<Document> annotatedClasses;
	private File xmlFileLocation;
	private ClassResource classResource;
	
	public XMLDocResource(File xmlFileLocation, 
			MongoCollection<Document> annotatedClasses,
			ClassResource classResource) {
		this.xmlFileLocation = xmlFileLocation;
		this.annotatedClasses = annotatedClasses;
		this.classResource = classResource;
		LOGGER.info("Writing xml to "+xmlFileLocation.getAbsolutePath());
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("{file}")
	public Response getFile(@PathParam("file") String filename) throws IOException {
		if (!filename.matches("[-a-zA-z0-9]*.xml")) {
			LOGGER.info("File read not permitted of {} :",filename);
			return Response.status(Response.Status.FORBIDDEN).build();
		}
		File file = new File(xmlFileLocation, filename);
		StreamingOutput out = new StreamingOutput() {
			
			@Override
			public void write(OutputStream outStream) throws IOException, WebApplicationException {
				try {
					Files.copy(file.toPath(), outStream);
				}
				catch (FileNotFoundException ex) {
					LOGGER.info("File {} not found:",filename,ex);
					throw new WebApplicationException(Response.Status.NOT_FOUND);
				}
			}
		};
		Response response = Response.ok(out,MediaType.APPLICATION_OCTET_STREAM).build();
		return response;
	}
	
	
	//TODO catch and handle exceptions
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getList() throws XMLStreamException, IOException {
		//find the file
		long fileTimestamp = 0;
		if (!xmlFileLocation.exists()) {
			xmlFileLocation.mkdirs();
		}
		File xmlTimeStamp = new File(xmlFileLocation, "generationTime");
		if (xmlTimeStamp.exists()) {
			//get it's timestamp
			fileTimestamp = xmlTimeStamp.lastModified();
			xmlTimeStamp.setLastModified(System.currentTimeMillis());
		}
		else {
			xmlTimeStamp.createNewFile();
		}
		
		
		//find the most recent edit and it's timestamp
		Bson projection = include("modifyTime");
		Bson notnull = ne("modifyTime",null);
		Document latest = annotatedClasses.find(notnull).projection(projection).sort(descending("modifyTime")).first();
		Long latestModify = latest.getLong("modifyTime");
		
		
		if (latestModify>fileTimestamp) {
			//generate a new file
			Map<String, Triple<File, OutputStream,XMLStreamWriter>> writers = new HashMap<>();
			writeMembers(writers);
			
		
			for (Map.Entry<String,Triple<File, OutputStream, XMLStreamWriter>> assembly : writers.entrySet()) {
				String assemblyName = assembly.getKey();
				Triple<File, OutputStream, XMLStreamWriter> xmlFile = assembly.getValue();
				xmlFile.c.writeEndElement();//members
				xmlFile.c.writeEndElement();//doc
				xmlFile.c.writeEndDocument();
				xmlFile.c.flush();
				xmlFile.c.close();
				xmlFile.b.close();
				File destFile = new File(xmlFileLocation, assemblyName+".xml");
				Files.move(xmlFile.a.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		
		String joined = Files.list(xmlFileLocation.toPath())
			.filter(p->p.toString().endsWith(".xml"))
			.map(p->p.getFileName().toString())
			.collect(Collectors.joining("\"},{\"file\":\""));
		
		return "{\"files\":[{\"file\":\""+joined+"\"}]}";
	}

	private void writeMembers(Map<String, Triple<File, OutputStream, XMLStreamWriter>> writers) 
			throws XMLStreamException, IOException, FactoryConfigurationError {
		//find all the classes that have docs
		Bson projection = include("name");
		SortedSet<String> modifiedClasses = new TreeSet<>();
		try (MongoCursor<Document> cursor = annotatedClasses.find().projection(projection).iterator()) {
			while (cursor.hasNext()) {
				Document document = (Document) cursor.next();
				modifiedClasses.add(document.getString("name"));
			}
		}
		
		for (String className : modifiedClasses) {
			MergedClass<? extends TopLevelDocumentable> mc = classResource.getClass(className, null);
			writeClass(writers,mc);
		}
	}
	
	private void writeContent(XMLStreamWriter writer, Documentable doc) throws XMLStreamException {
		String comment = doc.getComment();
		if (comment==null) return;
		try {
			XMLParser parser = new XMLParser();
			parser.parse(comment);
			parser.writeXML(writer);
		}
		catch (Exception e) {
			writer.writeStartElement("summary");
			writer.writeCharacters("An error occured transforming this comment to XML - ");
			writer.writeCharacters(comment);
			writer.writeEndElement();
		}
	}


	private void writeClass(Map<String, Triple<File, OutputStream, XMLStreamWriter>> writers, MergedClass<? extends TopLevelDocumentable> mc) throws XMLStreamException, IOException, FactoryConfigurationError {
		XMLStreamWriter writer = getWriter(writers,mc.getAssemblyName());
		
		writer.writeStartElement("member");
		String classId = "T:"+mc.getName();
		writer.writeAttribute("name", classId);
		writeContent(writer, mc);
		writer.writeEndElement();//member
		
		writeFields(writer, mc.getInstanceFields(), mc,"F:");
		writeFields(writer,mc.getStaticFields(), mc,"F:");
		writeFields(writer, mc.getInstanceProperties(), mc,"P:");
		writeFields(writer, mc.getStaticProperties(), mc,"P:");
		writeMethods(writer, mc.getInstanceMethods(), mc, MethodRepresentation::getName);
		writeMethods(writer, mc.getStaticMethods(), mc, MethodRepresentation::getName);
		writeMethods(writer, mc.getConstructors(), mc, (mr)->"#ctor");
	}


	private XMLStreamWriter getWriter(Map<String, Triple<File,OutputStream, XMLStreamWriter>> writers, String assemblyName) throws IOException, XMLStreamException, FactoryConfigurationError {
		if (writers.containsKey(assemblyName)) {
			return writers.get(assemblyName).c;
		}
		else {
			File temp = File.createTempFile(assemblyName, "tmp", xmlFileLocation);
			OutputStream fout = new FileOutputStream(temp);
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fout);
			//writer = new IndentingXMLStreamWriter(writer);
			writer.writeStartDocument("UTF-8", "1.0");
			
			writer.writeStartElement("doc");
			writer.writeStartElement("assembly");
			writer.writeStartElement("name");
			writer.writeCharacters("XMLDocumentation");
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement("members");
			writers.put(assemblyName, new Triple<>(temp,fout,writer));
			return writer;
		}
	}


	private void writeMethods(XMLStreamWriter writer, 
			List<MethodRepresentation> methods,
			MergedClass<? extends TopLevelDocumentable> mc,
			Function<MethodRepresentation,String> methodName) throws XMLStreamException {
		if (methods==null) return;
		for (MethodRepresentation method : methods) {
			writer.writeStartElement("member");
			String memberId = "M:"+mc.getName()+"."+methodName.apply(method);
			if (!method.parameters.isEmpty()){
				memberId+=params(method.parameters);
			}
			writer.writeAttribute("name", memberId);
			writeContent(writer, method);
			writer.writeEndElement();//member
			
		}
	}
	
	private void writeFields(XMLStreamWriter writer, 
			List<? extends Member> members,
			MergedClass<? extends TopLevelDocumentable> mc, 
			String prefix) 
					throws XMLStreamException {
		if (members==null) return;
		for (Member member:members) {
			writer.writeStartElement("member");
			String memberId = prefix+mc.getName()+"."+member.getName();
			writer.writeAttribute("name", memberId);
			writeContent(writer, member);
			writer.writeEndElement();//member
		}
	}

	private final static Map<String,String> intrinsicTypes = new HashMap<>();
	static {
		intrinsicTypes.put("byte", "System.Byte");
		intrinsicTypes.put("char", "System.Char");
		intrinsicTypes.put("bool", "System.Boolean");
		intrinsicTypes.put("short", "System.Int16");
		intrinsicTypes.put("ushort", "System.UInt16");
		intrinsicTypes.put("int", "System.Int32");
		intrinsicTypes.put("uint", "System.UInt32");
		intrinsicTypes.put("float", "System.Single");
		intrinsicTypes.put("double", "System.Double");
		intrinsicTypes.put("decimal", "System.Decimal");
		intrinsicTypes.put("long", "System.Int64");
		intrinsicTypes.put("ulong", "System.UInt64");
	}
	
	private String params(List<Member> parameters) {
		StringBuilder b = new StringBuilder("(");
		for (Member member : parameters) {
			String tname = member.getObjectType().getTypeName();
			if (intrinsicTypes.containsKey(tname)) {
				tname =  intrinsicTypes.get(tname);
			}
			b.append(tname);
			b.append(",");
		}
		b.deleteCharAt(b.length()-1);
		b.append(")");
		return b.toString();
	}
	
	private class Triple<A,B,C> {
		A a;
		B b;
		C c;
		public Triple(A a, B b, C c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
	
	
}
