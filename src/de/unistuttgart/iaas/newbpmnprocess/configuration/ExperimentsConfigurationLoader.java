package de.unistuttgart.iaas.newbpmnprocess.configuration;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.unistuttgart.iaas.newbpmnprocess.processengines.ProcessEngine;


public class ExperimentsConfigurationLoader implements IConfigurationLoader{
	private Settings m_settings = new Settings();
	
	public Settings getSettings() {
		return m_settings;
	}

	//TODO better to inject this dependency. Create an interface and ...
	//ExperimentsConfigurationLoader(Settings settings){
	//	m_settings = settings;
	//}
	
	public boolean loadConfiguration(String uri, IErrorHandler handler) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uri);			
			if(!readNumberOfExecutions(doc, handler)){
				return false;
			}
			
			if(!readExperiments(doc, handler)){
				return false;
			}
			return true;
		} catch (ParserConfigurationException e) {
			handler.setError(
					new ExcperimentConfigurationError("Excpeption during the instatiation of the builder", e));
		} catch (IOException e) {
			handler.setError(
					new ExcperimentConfigurationError("Excpeption during the parsing of the file", e));
		} catch (Throwable e) {
			handler.setError(
					new ExcperimentConfigurationError("Unexpected exception", e));
		}
		return false;
	}
	
	private boolean readNumberOfExecutions(Document doc, IErrorHandler handler){
		boolean retVal = false;
		NodeList noOfExecutions = doc.getElementsByTagName("NumberOfExcetutions");
		
		if(noOfExecutions.getLength() == 0){
			handler.setError(new ExcperimentConfigurationError("There was not element NumberOfExcetutions"));
		}
		else if(noOfExecutions.getLength() > 1){
			handler.setError(new ExcperimentConfigurationError("Too many elements NumberOfExcetutions"));
		}
		else {
			int number = Integer.parseInt(((Element)noOfExecutions.item(0)).getTextContent());
			if(number == 0){
				handler.setError(new ExcperimentConfigurationError("NumberOfExcetutions is zero"));
			}
			else{
				retVal = true;
				m_settings.setNumberOfExecutions(number);
			}			
		}
		
		return retVal;
	}
	

	
	private boolean readExperiments(Document doc, IErrorHandler handler){
		NodeList experiments = doc.getElementsByTagName("Experiment");
		
		for(int i = 0; i < experiments.getLength(); ++i){
			Element experiment = (Element)experiments.item(i);
			//TODO no check for valid values ... exception or validate inside the experiment and then exception
			m_settings.add(new Experiment(
				Integer.parseInt(experiment.getElementsByTagName("NumberOfNodes").item(0).getTextContent()),
				Integer.parseInt(experiment.getElementsByTagName("NumberOfCriteria").item(0).getTextContent()),
				experiment.getElementsByTagName("URI").item(0).getTextContent(),
				ProcessEngine.valueOf(experiment.getElementsByTagName("ProcessEngine").item(0).getTextContent())));
		}
		
		if(m_settings.getExperiments().size() == 0){
			handler.setError(new ExcperimentConfigurationError("There were not criteria in configuration file"));
			return false;
		}
		return true;
	}
}
