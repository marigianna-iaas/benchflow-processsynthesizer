package de.unistuttgart.iaas.newbpmnprocess.configuration;

import de.unistuttgart.iaas.newbpmnprocess.processengines.ProcessEngine;

/**
 * An experiment consists of 
 * a) counter of nodes
 * b) path to criteria
 * c) process engine
 * 
 * @author skourama
 *
 */
public class Experiment {
	private final int m_cntOfNodes;
	private final int m_cntOfCriteria;
	private final String m_path;
	private final ProcessEngine m_processEngine;

	
	Experiment(int cntOfNodes, int cntOfCriteria, String path, ProcessEngine processEngineName){
		m_cntOfNodes = cntOfNodes;
		m_cntOfCriteria = cntOfCriteria;
		m_path = path;
		m_processEngine = processEngineName;
	}
	
	public int getCounterOfNodes() {
		return m_cntOfNodes;
	}

	public String getPath() {
		return m_path;
	}

	
	public ProcessEngine getProcessEngine() {
		return m_processEngine;
	}

	
	public int getCounterOfCriteria() {
		return m_cntOfCriteria;
	}
	
	
	@Override
	public String toString() {
		return "Experiment [m_cntOfNodes=" + m_cntOfNodes + ", m_path="
				+ m_path + ", m_processEngine=" + m_processEngine + "]";
	}



}