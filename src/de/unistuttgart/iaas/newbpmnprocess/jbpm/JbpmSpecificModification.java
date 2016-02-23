package de.unistuttgart.iaas.newbpmnprocess.jbpm;

import java.io.File;
import java.io.IOException;
import java.util.Random;

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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

public class JbpmSpecificModification {

	Constants ConstObj = new Constants();
	
	public void start()
	{
		try 
		{
			String filepath = Constants.NewProcessesDirectoryFullPath;
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filepath);
			
			AddDefinitionsNamespace(doc);
			ChangeProcessName(doc);
			ChangeStartEventId(doc);
			ChangeEndEventId(doc);
			ChangeActivityId(doc);
			ChangeGatewayId(doc);
			AddInputForXOR(doc);
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			doc.setXmlStandalone(true);
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(Constants.NewProcessFullPath));
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
	
	public void ChangeActivityId(Document doc)
	{
		
		int countCallActivityElements = doc.getElementsByTagName(Constants.CallActivityElement).getLength();
		int i;
		for (i=0;i<countCallActivityElements;i++)
		{
 
		// Get the staff element by tag name directly
		Node callactivity = doc.getElementsByTagName(Constants.CallActivityElement).item(i);
 
		// update staff attribute
		NamedNodeMap attr = callactivity.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.ActivityId+i);
		
		AddCalledElement(callactivity);
		
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
		
		AddScriptTaskDetails(doc,ScriptTask);
		
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
		
		//Edit Service Task
		int countServiceTasks = doc.getElementsByTagName(Constants.ServiceTaskElement).getLength();
		
		for (int k=0;k<countServiceTasks;k++)
		{
 
		// Get the staff element by tag name directly
		Node ServiceTask = doc.getElementsByTagName(Constants.ServiceTaskElement).item(k);
 
		// update staff attribute
		NamedNodeMap attr = ServiceTask.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.ActivityId+i);
		
		AddServiceTaskDetails(doc,ServiceTask,k);
		
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
	
	public void ChangeGatewayId(Document doc)
	{
		
		int countExclGatewayElements = doc.getElementsByTagName(Constants.ExclusiveGatewayElement).getLength();
		int i;
		for (i=0;i<countExclGatewayElements;i++)
		{
 
		// Get the staff element by tag name directly
		Node ExclusiveGateway = doc.getElementsByTagName(Constants.ExclusiveGatewayElement).item(i);
		
		//Add Gatewaydirection Attribute
		AddGatewayDirectionAttribute(ExclusiveGateway);
 
		// update staff attribute
		NamedNodeMap attr = ExclusiveGateway.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.GatewayId+i);
		
		
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
		
		for (int k=0;k<countParalGatewayElements;k++)
		{
 
		// Get the staff element by tag name directly
		Node ParallelGateway = doc.getElementsByTagName(Constants.ParallelGatewayElement).item(k);
 
		//Add Gatewaydirection Attribute
		AddGatewayDirectionAttribute(ParallelGateway);
		
		// update staff attribute
		NamedNodeMap attr = ParallelGateway.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
		
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.GatewayId+i);
		
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
	
	public void AddGatewayDirectionAttribute(Node ExclusiveGateway)
	{
		Element ExclGateway = (Element)ExclusiveGateway;
		int CountOutgoing = ExclGateway.getElementsByTagName(Constants.OutgoingElement).getLength();
		
		if(CountOutgoing>1)
		{
			((Element)ExclusiveGateway).setAttribute(Constants.JbpmGatewayDirectionAtribute, Constants.JbpmGatewayDirectionDivergeValue);
		}
		else
		{
			((Element)ExclusiveGateway).setAttribute(Constants.JbpmGatewayDirectionAtribute, Constants.JbpmGatewayDirectionConvergeValue);
		}
	}
	
	public void AddInputForXOR(Document doc)
	{		
		Node Def = doc.getElementsByTagName(Constants.DefinitionsElement).item(0);
		Node Process = doc.getElementsByTagName(Constants.ProcessElement).item(0);
		Boolean ItemDefAdded = false;
		
		for(int j=0;j<doc.getElementsByTagName(Constants.ExclusiveGatewayElement).getLength();j++)
		{
			Element exclGatewayel = (Element) doc.getElementsByTagName(Constants.ExclusiveGatewayElement).item(j);
			int count = exclGatewayel.getElementsByTagName(Constants.OutgoingElement).getLength();
			if(count>1)
			{
				if(ItemDefAdded==false)
				{
					Node ItemDef = doc.createElement(Constants.JbpmItemDefinitionElement);
					((Element)ItemDef).setAttribute(Constants.IdAttribute,Constants.JbpmItemDefIdAttributeValue2);
					((Element)ItemDef).setAttribute(Constants.JbpmItemDefStrucRefAttribute,Constants.JbpmItemDefStrucRefAttributeValue2);
					
					Def.insertBefore(ItemDef, Def.getFirstChild());
					
					ItemDefAdded=true;
				}
				
				Node Property = doc.createElement(Constants.JbpmPropertyElement);
				((Element)Property).setAttribute(Constants.IdAttribute,Constants.BpmnVariable + (j+1));
				((Element)Property).setAttribute(Constants.NameAtribute,Constants.BpmnVariable + (j+1));
				((Element)Property).setAttribute(Constants.JbpmPropItemSubRefAttribute,Constants.JbpmItemDefIdAttributeValue2);
				
				Process.insertBefore(Property, Process.getFirstChild());
				
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
						((Element)CondExp).setAttribute(Constants.JbpmCondExpLanguageAttribute, Constants.JbpmCondExpLanguageAttributeValue);
				
						CondExp.setTextContent("return input"+(j+1)+" == "+(i+1)+";");
					}
				}
			}
		}
	}
	
	public void AddServiceTaskDetails(Document doc,Node ServiceTask,int k)
	{
		((Element)ServiceTask).setAttribute(Constants.JbpmServiceTaskImplAttribute, Constants.JbpmServiceTaskImplAttributeValue);
		((Element)ServiceTask).setAttribute(Constants.JbpmServiceTaskOperRefAttribute, Constants.JbpmServiceTaskOperRefAttributeValue);
		
		//For the first iteration, add the details about class and operation which 
		//should be invoke when service task node is executed in the process.
		
		if(k==0)
		{
			//Adding ItemDefinition Element with attributes
			Node ItemDef = doc.createElement(Constants.JbpmItemDefinitionElement);
			Node Def = doc.getElementsByTagName(Constants.DefinitionsElement).item(0);
			((Element)ItemDef).setAttribute(Constants.IdAttribute,Constants.JbpmItemDefIdAttributeValue);
			((Element)ItemDef).setAttribute(Constants.JbpmItemDefStrucRefAttribute,Constants.JbpmItemDefStrucRefAttributeValue);
			
			//Adding message type element with attributes
			Node Message = doc.createElement(Constants.JbpmMessageElement);
			((Element)Message).setAttribute(Constants.IdAttribute,Constants.JbpmMsgIdAttributeValue);
			((Element)Message).setAttribute(Constants.JbpmMsgItemRefAttribute,Constants.JbpmItemDefIdAttributeValue);
			
			//Adding Interface type element with attributes
			Node Interface = doc.createElement(Constants.JbpmInterfaceElement);
			Def.insertBefore(Interface, Def.getFirstChild());
			Def.insertBefore(Message,Def.getFirstChild());
			Def.insertBefore(ItemDef, Def.getFirstChild());
			((Element)Interface).setAttribute(Constants.IdAttribute,Constants.JbpmIntfIdAttributeValue);
			((Element)Interface).setAttribute(Constants.NameAtribute,Constants.JbpmIntfNameAttributeValue);
			((Element)Interface).setAttribute(Constants.JbpmIntfImplRefAttribute,Constants.JbpmIntfNameAttributeValue);
			
			//Adding Operation type element with attributes
			Node Operation = doc.createElement(Constants.JbpmOperationElement);
			((Element)Operation).setAttribute(Constants.IdAttribute,Constants.JbpmServiceTaskOperRefAttributeValue);
			((Element)Operation).setAttribute(Constants.NameAtribute,Constants.JbpmOperNameAttributeValue);
			
			Interface.insertBefore(Operation, Interface.getFirstChild());
			
			//Adding Operation parameter element with attributes
			Node InMessage = doc.createElement(Constants.JbpmInMessageElement);
			InMessage.setTextContent(Constants.JbpmMsgIdAttributeValue);
			
			Operation.insertBefore(InMessage, Operation.getFirstChild());
		}
	}
	
	public void AddScriptTaskDetails(Document doc, Node ScriptTask)
	{
		Node script = doc.createElement(Constants.JbpmScriptElement);
		ScriptTask.insertBefore(script, ScriptTask.getFirstChild());
		script.setTextContent(";");
		((Element)ScriptTask).setAttribute(Constants.JbpmScriptFormatAttribute, Constants.JbpmScriptFormatAttributeValue);		
	}
	
	public void AddCalledElement(Node callactivity)
	{
		((Element)callactivity).setAttribute(Constants.CalledElementAttribute, Constants.JbpmCalledElementValue);
	}
	
	public void ChangeProcessName (Document doc)
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
		((Element)process).setAttribute(Constants.ProcessTypeAttribute, Constants.ProcessTypeAttributeValue);
		((Element)process).setAttribute(Constants.IdAttribute, randStr.toString());
		((Element)process).setAttribute(Constants.NameAtribute, randStr.toString());

	}
	
	public void ChangeStartEventId(Document doc)
	{
		Node startevent = doc.getElementsByTagName(Constants.StartEventElement).item(0);

		NamedNodeMap attr = startevent.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
				
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.StartEventId);
		
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
	
	public void ChangeEndEventId(Document doc)
	{
		Node endevent = doc.getElementsByTagName(Constants.EndEventElement).item(0);

		NamedNodeMap attr = endevent.getAttributes();
		Node nodeAttr = attr.getNamedItem(Constants.IdAttribute);
				
		String oldId=nodeAttr.getTextContent();
		nodeAttr.setTextContent(Constants.EndEventId);
		
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
		
		//Adding name Attribute
		((Element)endevent).setAttribute(Constants.NameAtribute, Constants.EndEventId);
		
	}
	
	public void AddDefinitionsNamespace(Document doc)
	{
		Node definitions = doc.getElementsByTagName(Constants.DefinitionsElement).item(0);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeTypeLanguage, Constants.DefinitionAttributeTypeLanguageValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeExpressionLanguage, Constants.DefinitionAttributeExpressionLanguageValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeXmlns, Constants.DefinitionAttributeXmlnsValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeXmlnsTns, Constants.DefinitionAttributeXmlnsTnsValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeXsi, Constants.DefinitionAttributeXsiValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeDc, Constants.DefinitionAttributeDcValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeDi, Constants.DefinitionAttributeDiValue);
		((Element)definitions).setAttribute(Constants.DefinitionAttributeSchemaLocation, Constants.DefinitionAttributeSchemaLocationValue);
	}

}
