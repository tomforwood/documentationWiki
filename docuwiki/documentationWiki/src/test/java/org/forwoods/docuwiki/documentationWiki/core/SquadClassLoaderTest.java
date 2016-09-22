package org.forwoods.docuwiki.documentationWiki.core;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.assertj.core.api.Condition;
import org.assertj.core.api.iterable.Extractor;
import org.forwoods.docuwiki.documentable.ClassRepresentation.FieldRepresentation;
import org.forwoods.docuwiki.documentationWiki.api.SquadClass;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class SquadClassLoaderTest {
	
	SquadClassLoader loader;
	private SquadFileLoader fileLoader; 
	
	SetMultimap<String, String> comments = HashMultimap.create();

	@Before
	public void setUp() throws Exception {
		//TODO move to a fixtures dir
		File squadDir = new File("E:/downloads/xml/");
		fileLoader = new SquadDirFileLoader(squadDir);
		//URL zipIn = getClass().getClassLoader().getResource("xml.zip");
		//fileLoader = new SquadZipFileLoader(zipIn);
		loader = new SquadClassLoader(fileLoader);
		
	}
	
	@Test
	public void findAllComments() throws IOException, XMLStreamException, XPathExpressionException {
		/*loader.readIndex();
		XPathFactory xpathFac = XPathFactory.newInstance();
		String[] keys =  new String[]{"Contracts.Contract","GameEvents", "ModuleReactionWheel"};
		
		for (String fileName:loader.index.values()) {
		//for (String className:keys) {
			//String fileName = loader.xmlFileName(className);
			System.out.println(fileName);
			InputStream fin = fileLoader.readFile("xml/"+fileName+".xml");
			XPath xpath = xpathFac.newXPath();
			InputSource source = new InputSource(new XMLAmpFixerInputStream(fin));
			String path = "//briefdescription/*";
			findNodes(xpath, source, path, fileName);
			

			fin = fileLoader.readFile("xml/"+fileName+".xml");
			source = new InputSource(fin);
			path = "//para";
			findNodes(xpath, source, path, fileName);
			
			
		}
		for (String s:comments.keySet()) {
			Set<String> containingFiles = comments.get(s);
			boolean covered=false;
			for (String k:keys) {
				String kname = loader.xmlFileName(k);
				if (containingFiles.contains(kname)) 
				{
					System.out.println(s +" is covered by "+k);
					covered = true;
					break;
				}
			}
			if (!covered) {
				System.out.print(s);
				System.out.println(" "+comments.get(s).iterator().next());
			}
		}*/
	}

	private void findNodes(XPath xpath, InputSource source, String path, String filename) throws XPathExpressionException {
		NodeList nodes = (NodeList)xpath.evaluate(path, source, XPathConstants.NODESET);
		for (int i=0;i<nodes.getLength();i++) {
			Node n = nodes.item(i);
			StringBuilder pathString = new StringBuilder();
			printPath(n,pathString);

			if (n.getTextContent().length()>0) {
				if (pathString.toString().equals("/null/doxygen/compounddef/sectiondef/memberdef/detaileddescription/para/simplesect/para") &&
						filename.equals("class_contracts_1_1_contract"))
				{
					System.out.println(n.getTextContent());
				}
				comments.put(pathString.toString(), filename);
			}
		}
	}
	
	private void printPath(Node n, StringBuilder builder) {
		if (n.getParentNode()!=null) {
			printPath(n.getParentNode(), builder);
		}
		builder.append("/");
		builder.append(n.getLocalName());
	}

	@Test
	public void testReadIndex() throws IOException {
		String s = loader.xmlFileName("Contracts.Contract");
		assertThat(s).isEqualTo("class_contracts_1_1_contract");
	}
	
	@Test
	public void testReadContract() throws IOException {
		SquadClass sqClass = loader.loadClass("Contracts.Contract");
		
		assertThat(sqClass.getName()).isEqualTo("Contracts.Contract");
		
		assertThat(sqClass.getFields())
			.filteredOn(f->f.getName().equals("AutoAccept"))
			.extracting(f->f.getSquadComment())
			.areExactly(1, new Condition<>(s-> s.startsWith("<para>Whether this contract"),"com"));
		
		assertThat(sqClass.getProperties())
		.filteredOn(f->f.getName().equals("Agent"))
		.extracting(f->f.getSquadComment())
		.areExactly(1, new Condition<>(s-> s.startsWith("<para>Space agency"),"com"));
		
		assertThat(sqClass.getMethods())
		.filteredOn(m->m.getName().equals("MessageCancellationPenalties"))
		.allMatch(m->m.getSquadComment().startsWith("<para>Appended to the text"))
		.flatExtracting(m->m.parameters)
		.extracting(param->param.getObjectType().getTypeName())
		.contains("double","float");
		
		assertThat(sqClass.getMethods())
		.filteredOn(m->m.getName().equals("SetupSeed"))
		.allMatch(m->{
			return m.getSquadComment().contains("<para>< initializes unity");
			})
		.flatExtracting(m->m.parameters)
		.extracting(param->param.getObjectType().getTypeName())
		.contains("int");
		
		//TODO nested enums with commments
		assertThat(sqClass.getNested())
		.filteredOn(nest->nest.getName().equals("ContractPrestige"))
		.flatExtracting(n->n.getEnums())
		.extracting(ev->ev.getName())
		.contains("Trivial","Significant","Exceptional");
	}
	
	@Test
	public void testReadGameEvents() throws IOException {
		SquadClass sqClass = loader.loadClass("GameEvents");
		
		assertThat(sqClass.getName()).isEqualTo("GameEvents");
		
		assertThat(sqClass.getSquadComment()).startsWith("<para>Static manager");
		
		
		Extractor<SquadClass, Collection<FieldRepresentation>> nestedFieldExtractor = 
				sc-> sc.getFields();
		assertThat(sqClass.getNested())
		.filteredOn(e->e.getName().equals("GameEvents.Contract"))
		.flatExtracting(nestedFieldExtractor)
		.filteredOn(f-> f.getName().equals("onAccepted"))
		.areExactly(1, new Condition<>(
					field-> field.getSquadComment().contains("Fired when a contract"), 
					"commentCondition"));
		
		assertThat(sqClass.getNested())
		.filteredOn(e->e.getName().equals("GameEvents.ExplosionReaction"))
		.flatExtracting(nestedFieldExtractor)
		.filteredOn(f-> f.getName().equals("distance"))
		.areExactly(1, new Condition<>(
					field->true,
					//field-> field.getSquadComment().contains("Fired when a contract"), 
					"commentCondition"));
	}
	
	@Test
	public void testReadModuleReactionWheel() throws IOException {
		SquadClass sqClass = loader.loadClass("ModuleReactionWheel");
		
		assertThat(sqClass.getName()).isEqualTo("ModuleReactionWheel");
		
		assertThat(sqClass.getMethods())
			.filteredOn(m->m.getName().equals("OnSave"))
			.allMatch(m->m.getSquadComment().contains("Node"));
	}
	
	@Test
	public void testReadInterface() throws IOException {
		SquadClass sqClass = loader.loadClass("IAnalyticPreview");
		assertThat(sqClass.getName()).isEqualTo("IAnalyticPreview");
		assertThat(sqClass.getSquadComment()).startsWith("<para>This interface");
		assertThat(sqClass.getMethods())
		.filteredOn(m->m.getName().equals("InternalFluxAdjust"))
		.allMatch(m->m.getSquadComment().contains("This method is called"));
		
	}
}
