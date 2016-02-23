package de.unistuttgart.iaas.newbpmnprocess.configuration;

import java.util.ArrayList;
import java.util.List;

public class Settings {
	int m_noOfExecutions;
	List<Experiment>  m_experiments = new ArrayList<Experiment>();
	int m_noOfminAllowedNodes; 
	
	public int getNnumberOfExecutions() {
		return m_noOfExecutions;
	}

	public void setNumberOfExecutions(int noOfExecutions) {
		m_noOfExecutions = noOfExecutions;
	}

	public boolean add(Experiment e) {
		return m_experiments.add(e);
	}
	
	public List<Experiment> getExperiments() {
		return m_experiments;
	}
	
	public void setMinAllowedNumberOfNodes(int noOfMinAllowedNodes)
	{
		m_noOfminAllowedNodes = noOfMinAllowedNodes;
	}
	
	public int getMinAllowedNumberOfNodes()
	{
		return m_noOfminAllowedNodes;
	}
	
	
}
