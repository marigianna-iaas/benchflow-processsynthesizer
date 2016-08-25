package de.unistuttgart.iaas.newbpmnprocess.composer;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
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
	
	private ConnectionPoint leftConnectionPoint;
	private ConnectionPoint rightConnectionPoint; 
	
	
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
		SecureRandom random = new SecureRandom();
		String randomId =  new BigInteger(130, random).toString(32);
		String bpmnFileName = Constants.BpmnFilesPath + File.separator + "newProcess"
		+ randomId+ ".bpmn2";
		return bpmnFileName;
	}
	

	private Document initFinalProcessXML()
	{
		try {
			writeDefinitionsToXML();
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			
	
			Document xmlOfNewProcess = xmlFactory.newDocumentBuilder().parse(new File(this.newProcessFileName));
			return xmlOfNewProcess;

		} catch (SAXException | IOException | ParserConfigurationException e) {
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
//delete never called 
//	public void setFinalProcessXML(Document processXML) {
//		this.finalProcessXML = processXML;
//	}
	
	public String getRandomId()
	{
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
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
			return nodes.item(0);
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	


	public ConnectionPoint getLeftConnectionPoint() {
		return leftConnectionPoint;
	}

	public ConnectionPoint getRightConncetionPoint() {
		return rightConnectionPoint;
	}

	public void addEndEventToConnectionPoint(ConnectionPoint connectionPointOfCurrFragment) {
		//create the new IDs
		String targetRefId = getRandomId();
		String sourceRefId = connectionPointOfCurrFragment.getFlownode().getId();
		String sequenceFlowId = getRandomId();

		//get the existentNodeToConnect
		Node leftConnectionPointNode = getNodeFromId(finalProcessXML, connectionPointOfCurrFragment);

		//create the new end event node
		Element endEventNode = finalProcessXML.createElement("bpmn2:endEvent");
		endEventNode.setAttribute("id", targetRefId);
		finalProcessXML = appendElementToProcess(finalProcessXML, endEventNode);

		finalProcessXML  = addTwoNodes(leftConnectionPointNode , endEventNode, sourceRefId, targetRefId, sequenceFlowId);
		//finalProcessXML = appendNodeToProcess(finalProcessXML, endEventNode);
	}
	
	public void addStartEventToConnectionPoint(ConnectionPoint connectionPointOfCurrFragment) {
		//create the new IDs
		String sourceRefId = getRandomId();
		String targetRefId = connectionPointOfCurrFragment.getFlownode().getId();
		String sequenceFlowId = getRandomId();

		//get the existentNodeToConnect
		Node rightConnectionPointNode = getNodeFromId(finalProcessXML, connectionPointOfCurrFragment);

		//create the new end event node
		Element startEventNode = finalProcessXML.createElement("bpmn2:startEvent");
		startEventNode.setAttribute("id", sourceRefId);
		finalProcessXML = appendElementToProcess(finalProcessXML, startEventNode);

		finalProcessXML = addTwoNodes(startEventNode , rightConnectionPointNode, sourceRefId, targetRefId, sequenceFlowId);


		//finalProcessXML = appendNodeToProcess(finalProcessXML, startEventNode);
	}

	private Document addTwoNodes(Node sourceNode, Node targetNode, String sourceNodeId, String targetNodeId, String sequenceFlowId) {
		finalProcessXML= createSequenceFlowOnDocument(sourceNodeId , targetNodeId ,sequenceFlowId );
		//for the outgoing
		addElementToNode(finalProcessXML, sourceNode, sequenceFlowId, "bpmn2:outgoing");
		
		//for the incoming
		addElementToNode(finalProcessXML, targetNode, sequenceFlowId, "bpmn2:incoming");
		return finalProcessXML;
	}
	
	/**
	 * Will connect 2 edges 
	 * @param leftConnectionPoint
	 * @param rightConnectionPoint
	 */
	public void linkSequenceFlowsOfConnectionPoints(ConnectionPoint leftConnectionPoint, ConnectionPoint rightConnectionPoint)
	{
			if(leftConnectionPoint.getNeededOutgoing() >0 )	
			{	
				//create the SequenceFlow
				String targetRefId = rightConnectionPoint.getFlownode().getId();
				String sourceRefId  = leftConnectionPoint.getFlownode().getId();
				String sequenceFlowId = getRandomId();
				//get the left node from the left connection point
				Node leftConnectionPointNode = getNodeFromId(finalProcessXML, leftConnectionPoint);
				
				//get the right node from the right connection point
				Node rightConnectionPointNode = getNodeFromId(finalProcessXML, rightConnectionPoint);
				finalProcessXML = addTwoNodes(leftConnectionPointNode , rightConnectionPointNode, sourceRefId, targetRefId, sequenceFlowId);
			}
	}
	
	
	private void addElementToNode(Document finalProcessXML,Node nodeToAdd, String sequenceFlowId, String elementTag) {		
		org.w3c.dom.Element newEmptyElement = finalProcessXML.createElement(elementTag);
		newEmptyElement.appendChild(finalProcessXML.createTextNode(sequenceFlowId));

		nodeToAdd.appendChild(newEmptyElement);
	}

	private Document createSequenceFlowOnDocument(String sourceRefId,String targetRefId, String sequenceFlowId)
	{
		org.w3c.dom.Element sequenceFlow = finalProcessXML.createElement("bpmn2:sequenceFlow");
		sequenceFlow.setAttribute("id", sequenceFlowId);
		sequenceFlow.setAttribute("sourceRef", sourceRefId);
		sequenceFlow.setAttribute("targetRef", targetRefId);
		finalProcessXML = appendElementToProcess(finalProcessXML, sequenceFlow);
		return finalProcessXML;
	}

	
	public void addRelaxPointAndEndEvent(ConnectionPoint connectionPointOfCurrFragment) {		
		
		//get the appropriate IDs
		String targetRefId = getRandomId();
		String sourceRefId = connectionPointOfCurrFragment.getFlownode().getId();
		String sequenceFlowId = getRandomId();
		
		//get the current node from the connection point on the left
		Node currentConnectionPointNode = getNodeFromId(finalProcessXML, connectionPointOfCurrFragment);
		//create the relax node
		Element taskNode = finalProcessXML.createElement("bpmn2:task");
		taskNode.setAttribute("id", targetRefId);
		taskNode.setAttribute("name", "Relax Node");
		finalProcessXML = appendElementToProcess(finalProcessXML, taskNode);

		
		finalProcessXML  = addTwoNodes( currentConnectionPointNode , taskNode, sourceRefId, targetRefId, sequenceFlowId);
		//finalProcessXML = appendNodeToProcess(finalProcessXML, taskNode);
		
		//make the new IDs
		String endEventTargetRefId = getRandomId();
		String endEventSourceRefId = targetRefId;
		sequenceFlowId = getRandomId();
		
		//create the new end event
		Element endEventNode = finalProcessXML.createElement("bpmn2:endEvent");
		endEventNode.setAttribute("id", endEventTargetRefId);
		finalProcessXML = appendElementToProcess(finalProcessXML, endEventNode);

		
		finalProcessXML  =  addTwoNodes( taskNode , endEventNode, endEventSourceRefId, endEventTargetRefId, sequenceFlowId);

		//finalProcessXML = appendNodeToProcess(finalProcessXML, endEventNode);
	}	
}
