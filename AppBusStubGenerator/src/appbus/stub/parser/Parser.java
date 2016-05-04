package appbus.stub.parser;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import appbus.stub.model.ApplicationInterfaces;
import appbus.stub.model.ApplicationInterfacesProperties;
import appbus.stub.model.TInterface;

/**
 * 
 * Class for parsing tosca xml files. Collects information needed for the
 * generation of the stub classes for using the OpenTOSCA JSON/HTTP Application Bus
 * API.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class Parser {

	final static String NAMESPACE = "http://www.uni-stuttgart.de/opentosca";
	final static String INTERFACES_NAME = "ApplicationInterfaces";
	final static String INTERFACES_PROPERTIES_NAME = "ApplicationInterfacesProperties";

	static DocumentBuilderFactory factory;
	static DocumentBuilder builder;
	static JAXBContext jc;
	static Unmarshaller unmarshaller;

	static Map<String, List<TInterface>> nodeTemplateInterfacesMap;
	static Map<String, QName> nodeTemplateNodeTypeMap;
	static Map<QName, List<TInterface>> nodeTypeInterfacesMap;
	static File toscaFolder;

	/**
	 * 
	 * Parses the specified tosca xml files.
	 * 
	 * @param toscaFile
	 *            (can be a single file or a directory)
	 * @return HashMap containing NodeTemplate-Name as key and a List containing the
	 *         associated TInterface objects as value
	 * @throws ParserConfigurationException
	 * @throws JAXBException
	 */
	public static Map<String, List<TInterface>> run(File toscaFile) throws ParserConfigurationException, JAXBException {

		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		builder = factory.newDocumentBuilder();

		jc = JAXBContext.newInstance(ApplicationInterfaces.class,
				ApplicationInterfacesProperties.class);
		unmarshaller = jc.createUnmarshaller();

		nodeTemplateInterfacesMap = new HashMap<String, List<TInterface>>();
		nodeTemplateNodeTypeMap = new HashMap<String, QName>();
		nodeTypeInterfacesMap = new HashMap<QName, List<TInterface>>();

		if (!toscaFile.isDirectory()) {
			toscaFolder = toscaFile.getParentFile();
		} else {
			toscaFolder = toscaFile;
		}

		File[] files = { toscaFile };
		if (toscaFolder.exists() && toscaFolder.isDirectory()) {
			files = getXMLFiles(toscaFolder);
		}

		Map<String, String> namespaceMap;

		for (File file : files) {

			System.out.println("Parsing :" + file.getAbsolutePath());

			Document document = null;
			try {
				document = builder.parse(file);
			} catch (SAXException | IOException e1) {
				System.out.println("WARNING: " + file + " isn't a  proper xml file.");
			}

			if (document != null) {

				// get ApplicationInterfaces in current document
				NodeList appInterfacesNodeList = document.getElementsByTagNameNS(NAMESPACE, INTERFACES_NAME);

				int appInterfacesNodeListLength = appInterfacesNodeList.getLength();

				System.out.println("Found " + appInterfacesNodeListLength
						+ " ApplicationInterfaces elements in " + file.getName() + ".");

				// for every ApplicationInterfaces element
				for (int i = 0; i < appInterfacesNodeListLength; i++) {

					Node appInterfacesNode = appInterfacesNodeList.item(i);

					// Get NodeType QName
					QName nodeType = null;
					if (appInterfacesNode.getParentNode().getNodeName().equals("NodeType")) {

						nodeType = new QName(document.getDocumentElement().getAttribute("targetNamespace"),
								appInterfacesNode.getParentNode().getAttributes().item(0).getNodeValue());

						ApplicationInterfaces appInterfaces = (ApplicationInterfaces) unmarshaller
								.unmarshal(appInterfacesNode);

						// get Interfaces elements
						List<TInterface> tInterfaceList = appInterfaces.getInterface();

						System.out.println(
								"The " + (i + 1) + ". ApplicationInterfaces element belongs to the NodeType: "
										+ nodeType + " and contains " + tInterfaceList.size() + " Interface elements:");

						nodeTypeInterfacesMap.put(nodeType, tInterfaceList);
					}
				}

				namespaceMap = getNamespaces(document);

				// get all NodeTemplates
				NodeList nodeTemplates = document.getElementsByTagNameNS("http://docs.oasis-open.org/tosca/ns/2011/12",
						"NodeTemplate");
				System.out.println("Found " + nodeTemplates.getLength() + " NodeTemplates in " + file.getName() + " :");

				// get for every NodeTemplate its ID and its Type
				for (int i = 0; i < nodeTemplates.getLength(); i++) {
					Node nodeTemplate = nodeTemplates.item(i);

					String nodeTemplateID = nodeTemplate.getAttributes().getNamedItem("id").getNodeValue();
					String nodeTemplateType = nodeTemplate.getAttributes().getNamedItem("type").getNodeValue();

					String[] nodeType = nodeTemplateType.split(":");

					if (nodeType.length == 2) {

						QName nodeTypeQName = new QName(namespaceMap.get(nodeType[0]), nodeType[1]);

						System.out.println(" - NodeTemplateId: " + nodeTemplateID + " of Type: " + nodeTypeQName);

						nodeTemplateNodeTypeMap.put(nodeTemplateID, nodeTypeQName);
					}

				}

			}
		}

		for (Entry<String, QName> nodeTemplateNodeTypMapping : nodeTemplateNodeTypeMap.entrySet()) {

			String nodeTemplatID = nodeTemplateNodeTypMapping.getKey();
			QName nodeType = nodeTemplateNodeTypMapping.getValue();

			if (nodeTypeInterfacesMap.containsKey(nodeType)) {
				nodeTemplateInterfacesMap.put(nodeTemplatID, nodeTypeInterfacesMap.get(nodeType));
			}
		}

		return nodeTemplateInterfacesMap;

	}

	/**
	 * 
	 * Gets all xml files in the specified folder.
	 * 
	 * @param toscafolder
	 * @return all containing files ending with <tt>.xml</tt>.
	 */
	private static File[] getXMLFiles(File toscafolder) {

		// Create a FileFilter that matches ".xml" files
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xml");
			}
		};

		// Get pathnames of matching files.
		File[] toscaFiles = toscafolder.listFiles(filter);

		return toscaFiles;
	}

	/**
	 * @param document
	 *            containing the definition file
	 * @return defined prefixes and namespaces in the specified definitions file
	 */
	private static HashMap<String, String> getNamespaces(Document document) {

		HashMap<String, String> namespaceMap = new HashMap<String, String>();

		NamedNodeMap definitionAttributes = document.getDocumentElement().getAttributes();

		for (int i = 0; i < definitionAttributes.getLength(); i++) {
			Node node = definitionAttributes.item(i);

			if (node.getNamespaceURI() != null) {
				String prefix = node.getLocalName();
				String namespace = node.getNodeValue();

				namespaceMap.put(prefix, namespace);

				System.out.println("Prefix: " + prefix + " Namespace: " + namespace);
			}

		}
		return namespaceMap;
	}

	// For tests
	private static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			te.printStackTrace();
		}
		return sw.toString();
	}
}
