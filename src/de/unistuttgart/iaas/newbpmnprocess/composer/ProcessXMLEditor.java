package de.unistuttgart.iaas.newbpmnprocess.composer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.unistuttgart.iaas.newbpmnprocess.model.ConnectionPoint;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

public class ProcessXMLEditor {
	private Document finalProcessXML;
	
	private ConnectionPoint baseCP;
	private ConnectionPoint compareCP; 
	
	
	private String newProcessFileName;


	public ProcessXMLEditor() {
	
		this.newProcessFileName = createNewBpmnFileName();
		this.finalProcessXML = initFinalProcessXML();
	}

	public String writeProcessToXML()
	{
			// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer;
				try {
					transformer = transformerFactory.newTransformer();
			
				DOMSource source = new DOMSource(finalProcessXML);
				StreamResult result = new StreamResult(new File(this.newProcessFileName));

				transformer.transform(source, result);
				return this.newProcessFileName;
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
					return null;
				} catch (TransformerException e) {
					e.printStackTrace();
					return null;
				}
	}
	
	public void writeDefinitionsToXML()
	{
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI
				.createFileURI(this.newProcessFileName ));

		DocumentRoot root = Bpmn2Factory.eINSTANCE.createDocumentRoot();
		Definitions definitions = Bpmn2Factory.eINSTANCE.createDefinitions(); 
		
		// Add the definitions to the document root
		root.setDefinitions(definitions);
		// Add the document root to the resource
		resource.getContents().add(root);
		// Try to save the resource
	
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			System.out.println("File could not be saved");
			e.printStackTrace();
		}
	}
	
	private String createNewBpmnFileName()
	{
	  
		String bpmnFileName = Constants.BpmnFilesPath + File.separator + "newProcess"
		+ getRandomId()+ ".bpmn2";
		return bpmnFileName;
	}
	
	public String getRandomId()
	{
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
	
	private Document initFinalProcessXML()
	{
		try {
			writeDefinitionsToXML();
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			
	
			Document xmlOfNewProcess = xmlFactory.newDocumentBuilder().parse(new File(this.newProcessFileName));
			return xmlOfNewProcess;

		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public Document createProcessXMLFile()
	{
		try {
		 DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		 org.w3c.dom.Document xmlOfNewProcess = 
				 xmlFactory.newDocumentBuilder().parse(new File(this.newProcessFileName));
		 
		 return xmlOfNewProcess;
		
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public Node getProcessNode(Document doc)
	{
		return doc.getDocumentElement().getElementsByTagName("bpmn2:process").item(0);
	}
	
	public Document addFirstFragmentToProcess(Document doc, FragmentExt f)
	{
		Document fragmentXML = f.getXMLDocumentOfFragment();
		Node xmlF1ProcessNode = getProcessNode(fragmentXML);
		Node newNode = doc.importNode(xmlF1ProcessNode, true);
		doc.getDocumentElement().appendChild(newNode);
		return doc;
	}
	
	public Document getFinalProcessXML() {
		return finalProcessXML;
	}

	public void setFinalProcessXML(Document processXML) {
		this.finalProcessXML = processXML;
	}
	
	public Document appendFragmentToFile(Document finalProcessXML, FragmentExt f)
	{
		Document newProcessXML = f.getXMLDocumentOfFragment();
		
		Node processToReplace = getProcessNode(finalProcessXML);
		Node processToExtend = getProcessNode(finalProcessXML);
	
		Node newProcessProcessNode = getProcessNode(newProcessXML);
		
		NodeList processElements = newProcessProcessNode.getChildNodes();
			
		for (int i = 0; i < processElements.getLength(); i++) {
				
			Node childNode = processElements.item(i);
			Node nodeToAppend = finalProcessXML.importNode(childNode, true);
			processToExtend.appendChild(nodeToAppend);
			finalProcessXML.getDocumentElement().replaceChild(processToExtend, processToReplace);
		}
		return finalProcessXML;
	}
	
	
	public void linkConnectionPoints(ConnectionPoint baseCP, ConnectionPoint compareCP)
	{
			this.baseCP = baseCP;
			this.compareCP = compareCP;
			if(baseCP.getNeededOutgoing() >0 )	
			{	
//				Node processNode = 
//						finalProcessXML.getDocumentElement().
//						getElementsByTagName("bpmn2:process").item(0);
				String outgoingFNId = baseCP.getFlownode().getId();
				String incomingFNId = compareCP.getFlownode().getId();
				String newSequenceFlowId = getRandomId();

				finalProcessXML= createSequenceFlow(newSequenceFlowId, outgoingFNId, incomingFNId );

				//for the outgoing
				Node setOutgoingNode = getNodeFromId(finalProcessXML, baseCP);
				
				org.w3c.dom.Element outgoingNode = finalProcessXML.createElement("bpmn2:outgoing");
				outgoingNode.setNodeValue(newSequenceFlowId); 
				Node newOutgoingNode = setOutgoingNode.appendChild(outgoingNode);
				
				finalProcessXML = appendNodeToProcess(finalProcessXML, newOutgoingNode);
				baseCP.setNeededOutgoing(baseCP.getNeededOutgoing() -1 );
				
				//for the incoming
				Node setIncomingNode = getNodeFromId(finalProcessXML, compareCP);
				
				org.w3c.dom.Element incomingNode = finalProcessXML.createElement("bpmn2:incoming");
				incomingNode.setNodeValue(newSequenceFlowId); 
				Node newIncomingNode = setIncomingNode.appendChild(incomingNode);
				
				finalProcessXML = appendNodeToProcess(finalProcessXML, newIncomingNode);
				compareCP.setNeededIncoming(compareCP.getNeededIncoming() -1 );
			}
	}
	
	public Document appendElementToProcess(Document finalProcessXML, Element el)
	{
		
		Node processToReplace = getProcessNode(finalProcessXML);
		Node processToExtend = getProcessNode(finalProcessXML);
	
		// newProcessProcessNode  ==> el
		Node nodeToAppend = finalProcessXML.importNode(el, true);
		processToExtend.appendChild(nodeToAppend);
		finalProcessXML.getDocumentElement().replaceChild(processToExtend, processToReplace);
	
		return finalProcessXML;
	}
	
	public Document appendNodeToProcess(Document finalProcessXML, Node newNode)
	{
		
		Node processToReplace = getProcessNode(finalProcessXML);
		Node processToExtend = getProcessNode(finalProcessXML);
	
		// newProcessProcessNode  ==> el
		Node nodeToAppend = finalProcessXML.importNode(newNode, true);
		processToExtend.appendChild(nodeToAppend);
		finalProcessXML.getDocumentElement().replaceChild(processToExtend, processToReplace);
	
		return finalProcessXML;
	}
	
	public Document createSequenceFlow(String sequenceFlowId, String outgoingFNId, String incomingFNId)
	{
		org.w3c.dom.Element sequenceFlow = finalProcessXML.createElement("bpmn2:sequenceFlow");
		sequenceFlow.setAttribute("id", sequenceFlowId);
		sequenceFlow.setAttribute("sourceRef", outgoingFNId);
		sequenceFlow.setAttribute("targetRef", incomingFNId);
		finalProcessXML = appendElementToProcess(finalProcessXML, sequenceFlow);
		//finalProcessXML.getDocumentElement().appendChild(sequenceFlow);
		return finalProcessXML;
	}
	
	private Node getNodeFromId(Document finalProcessXML, ConnectionPoint cp)
	{
		String typeImpl = cp.getFlownode().getClass().getSimpleName().replace("Impl", "");
		String type = typeImpl.substring(0, 1).toLowerCase() + typeImpl.substring(1, typeImpl.length());
		String id = cp.getFlownode().getId();
		try {
			//Important without the namespace bpmn2::
			String xPatern = "/definitions/process/"+type+"[@id='"+id+"']";
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(xPatern);
			NodeList nodes = (NodeList) expr.evaluate(finalProcessXML, XPathConstants.NODESET);
//			for (int i = 0; i < nodes.getLength(); ++i) {
//			   nodes.item(i);
//			}
			return nodes.item(0);
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	


	public ConnectionPoint getBaseCp() {
		return baseCP;
	}

//	public void setBaseCp(ConnectionPoint cp) {
//		this.baseCP = cp;
//	}

	public ConnectionPoint getCompareCP() {
		return compareCP;
	}

	public void addEndEvent(ConnectionPoint cp) {
		baseCP = cp;
		String incomingFNId = getRandomId();
		String outgoingFNId = cp.getFlownode().getId();
		String newSequenceFlowId = getRandomId();

		finalProcessXML= createSequenceFlow(newSequenceFlowId, outgoingFNId, incomingFNId );

		//for the outgoing
		Node setOutgoingNode = getNodeFromId(finalProcessXML, baseCP);
		
		org.w3c.dom.Element outgoingNode = finalProcessXML.createElement("bpmn2:outgoing");
		outgoingNode.setNodeValue(newSequenceFlowId); 
		Node newOutgoingNode = setOutgoingNode.appendChild(outgoingNode);
		
		finalProcessXML = appendNodeToProcess(finalProcessXML, newOutgoingNode);
		baseCP.setNeededOutgoing(baseCP.getNeededOutgoing() -1 );
		
		//for the incoming
		Element endEventNode = finalProcessXML.createElement("bpmn2:endEvent");
		endEventNode.setAttribute("id", incomingFNId);
		
		org.w3c.dom.Element incomingNode = finalProcessXML.createElement("bpmn2:incoming");
		incomingNode.setNodeValue(newSequenceFlowId); 
		Node newIncomingNode = endEventNode.appendChild(incomingNode);
		
		finalProcessXML = appendNodeToProcess(finalProcessXML, newIncomingNode);
		finalProcessXML = appendNodeToProcess(finalProcessXML, endEventNode);
	}	
}
