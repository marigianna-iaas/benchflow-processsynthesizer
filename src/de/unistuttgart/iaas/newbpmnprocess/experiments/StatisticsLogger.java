package de.unistuttgart.iaas.newbpmnprocess.experiments;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class StatisticsLogger {
	private boolean m_isHeaderAdded = false;
	private List<LoggingEntry> m_logEntries = new ArrayList<LoggingEntry>();

	public boolean add(LoggingEntry e) {
		return m_logEntries.add(e);
	}
	
	public void write(){
		try {
			FileWriter writer = new FileWriter("experimentResults.csv", true);
			
			if (!m_isHeaderAdded)
			{
				m_isHeaderAdded = true;
				
				writer.append("#Criteria");
				writer.append(',');
				
				writer.append("#Nodes");
				writer.append(',');
				writer.append("Number of execution");
				writer.append(',');
				
				writer.append("SelectFragments Seconds");
				writer.append(',');
				writer.append("SelectFragments Nano seconds");
				writer.append(',');
				
				writer.append("Composition Seconds");
				writer.append(',');
				writer.append("Composition Nano seconds");
				writer.append(',');
				
				writer.append("EngineSpecific Seconds");
				writer.append(',');
				writer.append("EngineSpecific Nano seconds");
				writer.append(',');
				
				
				writer.append("Experiment Seconds");
				writer.append(',');
				writer.append("Experiment Nano seconds");
				writer.append(',');
				
				writer.append("Model Found");
				writer.append(',');
				writer.append("Model Path");
				writer.append(',');
				writer.append("#Tried Combinations");
				writer.append(',');
				writer.append("Success Combination");
				writer.append('\n');
				
				
			}

			
			for(LoggingEntry entry : m_logEntries){
				writer.append(Integer.toString(entry.getCounterOfCriteria()));
				writer.append(',');

				writer.append(Integer.toString(entry.getCounterOfNodes()));
				writer.append(',');

				writer.append(Integer.toString(entry.getExecutionNumber()));
				writer.append(',');
				
				writer.append(Double.toString(entry.getElapsedTimeSelectFragmentsInSec()));
				writer.append(',');
				
				writer.append(Long.toString(entry.getElapsedTimeSelectFragmentsInNanosec()));
				writer.append(',');
				
				writer.append(Double.toString(entry.getElapsedTimeCompositionInSec()));
				writer.append(',');
				
				writer.append(Long.toString(entry.getElapsedTimeCompositionInNanosec()));
				writer.append(',');
				
				writer.append(Double.toString(entry.getElapsedTimeEngineSpecificInSec()));
				writer.append(',');
				
				writer.append(Long.toString(entry.getElapsedTimeEngineSpecificInNanosec()));
				writer.append(',');
				
				writer.append(Double.toString(entry.getElapsedTimeExperimentInSec()));
				writer.append(',');
				
				writer.append(Long.toString(entry.getElapsedTimeExperimentInNanosec()));
				writer.append(',');
				
				writer.append(Boolean.toString(entry.isModelFound()));
				writer.append(',');
				writer.append(entry.getNewModelFilePath());
				writer.append(',');
				writer.append(Integer.toString(entry.getNoOfTriedCombinations()));
				writer.append(',');
				writer.append(entry.getCombinations());
				writer.append('\n');
				

			}
			writer.flush();
			writer.close();
			
			m_logEntries.clear();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
