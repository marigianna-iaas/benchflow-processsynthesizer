package de.unistuttgart.iaas.newbpmnprocess.processengines;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;


/**
 * 
 * @author awahab
 *
 */
public class CamundaSpecificModification implements IEngineSpecificModification {

	
	public void modify(String filePath)
	{
		 try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath);
				
				addDefinitionsNamespace(doc);
				changeProcessName(doc);
				changeStartEventId(doc);
				changeEndEventId(doc);
				changeActivityId(doc);
				changeGatewayId(doc);
				addInputForXOR(doc);
				
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				doc.setXmlStandalone(true);
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(newProcessFile(filePath));
				//StreamResult result = new StreamResult(new File(Constants.NewProcessFullPath));
				transformer.transform(source, result);
		 
			   } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			   } catch (TransformerException tfe) {
					tfe.printStackTrace();
			   } catch (IOException ioe) {
				ioe.printStackTrace();
			   } catch (SAXException sae) {
				sae.printStackTrace();
			   }
	}
	
	private static File newProcessFile(String path){
		
		String newPath;
		int lastIndex = path.lastIndexOf(File.separatorChar);
		
		if(lastIndex == -1){
			newPath = "Camunda" + path;
			//there is no a path only a file name
		}
		else{
			//path + engine prefix + old file name
			newPath = path.substring(0, lastIndex+1) + "Camunda" + path.substring(lastIndex+1);
		}
		return new File(newPath);
	}
	
	private static void changeStartEventId(Document doc)
	{
		Node startevent = doc.getElementsByTagName(Constants.StartEventElement).item(0);
		
		NamedNodeMap attr = startevent.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		//Save the oldid of Start event
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.StartEventId);
		
		//Compare Oldid with all the source and target ref attribute in sequence flow and 
		//if found, replace by a new one
		for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
		{
			Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
			NamedNodeMap seqattr = sequenceflow.getAttributes();
			Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
			if(SourceAttr.getTextContent().matches(oldId))
			{
				SourceAttr.setTextContent(Constants.StartEventId);
			}
		}
	}
	
	private static void changeEndEventId(Document doc)
	{
		Node endevent = doc.getElementsByTagName(Constants.EndEventElement).item(0);
		if(endevent != null)
		{
			NamedNodeMap attr = endevent.getAttributes();
			Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
			
			// Save the oldid of End event
			String oldId=nodeAttr.getTextContent();
			nodeAttr.setTextContent(Constants.EndEventId);
			
			//Compare Oldid with all the source and target ref attribute in sequence flow and 
			//if found, replace by a new one
			for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
			{
				Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
				NamedNodeMap seqattr = sequenceflow.getAttributes();
				Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
				if(TargetAttr.getTextContent().matches(oldId))
				{
					TargetAttr.setTextContent(Constants.EndEventId);
				}
			}
		}
	}
	
	private static void changeActivityId(Document doc)
	{
		
		int countCallActivityElements = doc.getElementsByTagName(Constants.CallActivityElement).getLength();
		int i;
		
		//Iterate through all the Call Acivities in the process and change their Id's
		for (i=0;i<countCallActivityElements;i++)
		{
 
			Node callactivity = doc.getElementsByTagName(Constants.CallActivityElement).item(i);
	 
			NamedNodeMap attr = callactivity.getAttributes();
			Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
			
			//Save the oldid of the Call activity element
			String oldId=nodeAttr.getTextContent();
			nodeAttr.setTextContent(Constants.ActivityId+i);
			
			//Handle the Call activity element according to the camunda engine requirements
			addCalledElement(callactivity);
			
			//Compare Oldid with all the source and target ref attribute in sequence flow and 
			//if found, replace by a new one
			for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
			{
				Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
				NamedNodeMap seqattr = sequenceflow.getAttributes();
				Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
				if(SourceAttr.getTextContent().matches(oldId))
				{
					SourceAttr.setTextContent(Constants.ActivityId+i);
				}
				
				Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
				if(TargetAttr.getTextContent().matches(oldId))
				{
					TargetAttr.setTextContent(Constants.ActivityId+i);
				}
				
			}
		}
		
		int countServiceTasks = doc.getElementsByTagName(Constants.ServiceTaskElement).getLength();
		
		//Iterate through all the Service tasks in the Process
		for (int k=0;k<countServiceTasks;k++)
		{
 
		Node servicetask = doc.getElementsByTagName(Constants.ServiceTaskElement).item(k);
 
		NamedNodeMap attr = servicetask.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		//Save the oldid of the Service task element
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.ActivityId+i);
		
		//Make changes to the Service task element according the Camunda engine
		addRestServiceDetails(doc,servicetask);
		
		//Compare Oldid with all the source and target ref attribute in sequence flow and 
		//if found, replace by a new one
		for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
		{
			Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
			NamedNodeMap seqattr = sequenceflow.getAttributes();
			Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
			if(SourceAttr.getTextContent().matches(oldId))
			{
				SourceAttr.setTextContent(Constants.ActivityId+i);
			}
			
			Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
			if(TargetAttr.getTextContent().matches(oldId))
			{
				TargetAttr.setTextContent(Constants.ActivityId+i);
			}
			
		}
		i++;
		}
		
		int countScriptTasks = doc.getElementsByTagName(Constants.ScriptTaskElement).getLength();
		
		for (int k=0;k<countScriptTasks;k++)
		{
 
		// Get the staff element by tag name directly
		Node ScriptTask = doc.getElementsByTagName(Constants.ScriptTaskElement).item(k);
 
		// update staff attribute
		NamedNodeMap attr = ScriptTask.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.ActivityId+i);
		
		addScriptTaskDetails(doc,ScriptTask);
		
		for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
		{
			Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
			NamedNodeMap seqattr = sequenceflow.getAttributes();
			Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
			if(SourceAttr.getTextContent().matches(oldId))
			{
				SourceAttr.setTextContent(Constants.ActivityId+i);
			}
			
			Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
			if(TargetAttr.getTextContent().matches(oldId))
			{
				TargetAttr.setTextContent(Constants.ActivityId+i);
			}
			
		}
		i++;
		}
	}
	
	private static void addScriptTaskDetails(Document doc, Node ScriptTask)
	{
		Node script = doc.createElement(Constants.JbpmScriptElement);
		ScriptTask.appendChild(script);
		script.setTextContent(";");
		((Element)ScriptTask).setAttribute(Constants.JbpmScriptFormatAttribute, Constants.CamundaScriptFormatValue);		
	}
	
	private static void changeGatewayId(Document doc)
	{
		
		int countExclGatewayElements = doc.getElementsByTagName(Constants.ExclusiveGatewayElement).getLength();
		int i;
		
		//Iterate through all the Exclusive gateways
		for (i=0;i<countExclGatewayElements;i++)
		{
 
		Node ExclusiveGateway = doc.getElementsByTagName(Constants.ExclusiveGatewayElement).item(i);
 
		NamedNodeMap attr = ExclusiveGateway.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		//Save the oldid of the gateway
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.GatewayId+i);
		
		//Compare Oldid with all the source and target ref attribute in sequence flow and 
		//if found, replace by a new one
		for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
		{
			Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
			NamedNodeMap seqattr = sequenceflow.getAttributes();
			Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
			if(SourceAttr.getTextContent().matches(oldId))
			{
				SourceAttr.setTextContent(Constants.GatewayId+i);
			}
			
			Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
			if(TargetAttr.getTextContent().matches(oldId))
			{
				TargetAttr.setTextContent(Constants.GatewayId+i);
			}
			
		}
		}
		
		//Parallel Gateway
		
		int countParalGatewayElements = doc.getElementsByTagName(Constants.ParallelGatewayElement).getLength();
		
		//Iterate through all the Parallel Gateways elements in the new Process
		for (int k=0;k<countParalGatewayElements;k++)
		{
			
		Node ParallelGateway = doc.getElementsByTagName(Constants.ParallelGatewayElement).item(k);
 
		NamedNodeMap attr = ParallelGateway.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		//Save the oldid of the Parallel gateway
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.GatewayId+i);
		
		//Compare Oldid with all the source and target ref attribute in sequence flow and 
		//if found, replace by a new one
		for (int j=0;j<doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();j++)
		{
			Node sequenceflow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(j);
			NamedNodeMap seqattr = sequenceflow.getAttributes();
			Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
			if(SourceAttr.getTextContent().matches(oldId))
			{
				SourceAttr.setTextContent(Constants.GatewayId+i);
			}
			
			Node TargetAttr = seqattr.getNamedItem(Constants.TargetRefAttribute);
			if(TargetAttr.getTextContent().matches(oldId))
			{
				TargetAttr.setTextContent(Constants.GatewayId+i);
			}
			
		}
		i++;
		}
	}
	
	private static void changeProcessName (Document doc)
	{
		Node process = doc.getElementsByTagName(Constants.ProcessElement).item(0);
		String charlist =  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuffer randStr = new StringBuffer();
		for(int i=0; i<10; i++){
			int randomInt = 0;
	        Random randomGenerator = new Random();
	        randomInt = randomGenerator.nextInt(charlist.length()-1);
            char ch = charlist.charAt(randomInt);
            randStr.append(ch);
        }
		((Element)process).setAttribute(Constants.IdAttribute, randStr.toString());
		((Element)process).setAttribute(Constants.NameAtribute, randStr.toString());
		
		Element definitions = (Element) doc.getElementsByTagName(Constants.DefinitionsElement).item(0);
		boolean hasAttribute = definitions.hasAttribute(Constants.IdAttribute);
		if(hasAttribute)
			definitions.removeAttribute(Constants.IdAttribute);

	}
	
	private static void addCalledElement(Node callactivity)
	{
		((Element)callactivity).setAttribute(Constants.CalledElementAttribute, Constants.CamundaCalledElementValue);
	}
	
	private static void addRestServiceDetails(Document doc, Node servicetask)
	{
		Node extension = doc.createElement(Constants.ExtensionElement);
		servicetask.insertBefore(extension, servicetask.getFirstChild());
		
		Node connector = doc.createElement(Constants.CamundaConnectorElement);
		extension.appendChild(connector);
		
		Node connectorid = doc.createElement(Constants.CamundaConnectorIdElement);
		connector.appendChild(connectorid);
		connectorid.setTextContent(Constants.CamundaConnectorIdValue);
		
		Node inputoutput = doc.createElement(Constants.CamundaInputOutputElement);
		connector.appendChild(inputoutput);
		
		Node inputpar_url = doc.createElement(Constants.CamundaInputElement);
		inputoutput.appendChild(inputpar_url);
		inputpar_url.setTextContent(Constants.CamundaInputUrlValue);
		((Element)inputpar_url).setAttribute(Constants.NameAtribute, Constants.NameAttributeUrlValue);
		
		Node inputpar_method = doc.createElement(Constants.CamundaInputElement);
		inputoutput.appendChild(inputpar_method);
		inputpar_method.setTextContent(Constants.CamundaInputMethodValue);
		((Element)inputpar_method).setAttribute(Constants.NameAtribute, Constants.NameAttributeMethodValue);
		
		Node outputpar_val = doc.createElement(Constants.CamundaOutputElement);
		inputoutput.appendChild(outputpar_val);
		outputpar_val.setTextContent(Constants.CamundaOutputValue);
		((Element)outputpar_val).setAttribute(Constants.NameAtribute, Constants.NameOutputValue);
		
		//servicetask.appendChild(extension);
		
	}
	
	private static void addInputForXOR(Document doc)
	{
		if (doc.getElementsByTagName(Constants.ExclusiveGatewayElement).getLength()>0)
		{
			boolean ExtensionElementAdded=false;
			Node extension=null;
			
			for(int j=0;j<doc.getElementsByTagName(Constants.ExclusiveGatewayElement).getLength();j++)
			{
				Element exclGatewayel = (Element) doc.getElementsByTagName(Constants.ExclusiveGatewayElement).item(j);
				int count = exclGatewayel.getElementsByTagName(Constants.OutgoingElement).getLength();
				if(count>1)
				{
					if(ExtensionElementAdded==false)
					{
						Node start = doc.getElementsByTagName(Constants.StartEventElement).item(0);
						extension = doc.createElement(Constants.ExtensionElement);
						start.insertBefore(extension, start.getFirstChild());
						ExtensionElementAdded=true;
					}
			
					Node formproperty = doc.createElement(Constants.FormActivityElement);
					extension.appendChild(formproperty);
					((Element)formproperty).setAttribute(Constants.IdAttribute, Constants.BpmnVariable + (j+1));
					((Element)formproperty).setAttribute(Constants.NameAtribute, Constants.BpmnVariable + (j+1));
					((Element)formproperty).setAttribute(Constants.TypeAttribute, Constants.TypeAttributeValue);
					((Element)formproperty).setAttribute(Constants.VariableAttribute, Constants.VariableAttributeValue + (j+1));
					((Element)formproperty).setAttribute(Constants.DefaultAttribute, Constants.DefaultAttributeValue);
					((Element)formproperty).setAttribute(Constants.RequiredAttribute, Constants.RequiredAttributeValue);
				
					Node exclGateway = doc.getElementsByTagName(Constants.ExclusiveGatewayElement).item(j);
					int flowsCount = doc.getElementsByTagName(Constants.SequenceFlowElement).getLength();
					for(int i=0; i<flowsCount; i++)
					{
						Node SeqFlow = doc.getElementsByTagName(Constants.SequenceFlowElement).item(i);
						NamedNodeMap seqattr = SeqFlow.getAttributes();
						Node SourceAttr = seqattr.getNamedItem(Constants.SourceRefAttribute);
						if(SourceAttr.getTextContent().matches(exclGateway.getAttributes().getNamedItem(Constants.IdAttribute).getTextContent()))
						{
							Node CondExp = doc.createElement(Constants.ConditionExpressionElement);
							SeqFlow.appendChild(CondExp);
							((Element)CondExp).setAttribute(Constants.ConditionExpressionAttribute, Constants.ConditionExpressionAttributeValue);
					
							CondExp.appendChild(doc.createCDATASection("${input"+(j+1)+".equals(\""+(i+1)+"\")}"));
							//CondExp.setTextContent("<![CDATA[${input.equals(\""+(i+1)+"\")}]]>");
						}
					}
				}
			}

		}
	}
	
	private static void addDefinitionsNamespace(Document doc)
	{
		Node definitions = doc.getElementsByTagName(Constants.DefinitionsElement).item(0);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeXsi, Constants.DefinitionAttributeXsiValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeCamunda, Constants.DefinitionAttributeCamundaValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeDc, Constants.DefinitionAttributeDcValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeDi, Constants.DefinitionAttributeDiValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeSchemaLocation, Constants.DefinitionAttributeSchemaLocationValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeActiviti, Constants.DefinitionAttributeActivitiValue);
	}
	
}
