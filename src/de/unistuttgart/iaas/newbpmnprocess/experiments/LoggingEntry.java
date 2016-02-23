package de.unistuttgart.iaas.newbpmnprocess.experiments;

public class LoggingEntry {
	int m_execution = 0;
	int m_counter = 0;
	
	long m_startSelectFragments = 0;
	long m_elapsedTimeSelectFragments = 0;
	
	long m_startComposition = 0;
	long m_elapsedTimeComposition = 0;
	
	long m_startEngineSpecific = 0;
	long m_elapsedTimeEngineSpecific = 0;
	
	long m_startExperiment = 0;
	long m_elapsedExperiment = 0;
	private int m_cntOfCriteria = 0;
	
	private boolean m_modelFound = false;
	private int m_noOfTriedCombinations = 0;
	private String m_newModelFilePath = "";
	private String m_combinations = "";
	
	void setCounterOfNodes(int counter){
		m_counter = counter;
	}
	
	int getCounterOfNodes(){
		return m_counter;
	}
	
	void setExecutionNumber(int execution){
		m_execution = execution;
	}
	
	int getExecutionNumber(){
		return m_execution;
	}
	
	void startSelectFragments(){
		m_startSelectFragments = System.nanoTime();
	}
	
	void endSelectFragments(){
		if(m_startSelectFragments != 0){
			m_elapsedTimeSelectFragments = System.nanoTime() - m_startSelectFragments;
		}
	}
	
	long getElapsedTimeSelectFragmentsInNanosec(){
		return m_elapsedTimeSelectFragments;
	}
	
	double getElapsedTimeSelectFragmentsInSec(){
		return (double)m_elapsedTimeSelectFragments / 1000000000.0;
	}
	
	void startComposition(){
		m_startComposition = System.nanoTime();
	}
	
	void endComposition(){
		if(m_startComposition != 0 ){
			m_elapsedTimeComposition = System.nanoTime() - m_startComposition;
		}
	}
	
	long getElapsedTimeCompositionInNanosec(){
		return m_elapsedTimeComposition;
	}
	
	double getElapsedTimeCompositionInSec(){
		return (double)m_elapsedTimeComposition / 1000000000.0;
	}
	
	void startEngineSpecific(){
		m_startEngineSpecific = System.nanoTime();
	}
	
	void endEngineSpecific(){
		if(m_startEngineSpecific != 0){
			m_elapsedTimeEngineSpecific = System.nanoTime() - m_startEngineSpecific;
		}
	}
	
	long getElapsedTimeEngineSpecificInNanosec(){
		return m_elapsedTimeEngineSpecific;
	}
	
	double getElapsedTimeEngineSpecificInSec(){
		return (double)m_elapsedTimeEngineSpecific / 1000000000.0;
	}

	 void endExperimentTime() {
		if(m_startExperiment != 0){
			m_elapsedExperiment = System.nanoTime() - m_startExperiment;
		}
	}

	 void startExperimentTime() {
		m_startExperiment = System.nanoTime();
	}
	
	long getElapsedTimeExperimentInNanosec(){
			return m_elapsedExperiment;
		}
		
	double getElapsedTimeExperimentInSec(){
		return (double)m_elapsedExperiment / 1000000000.0;
	}
	
	public void setCounterOfCriteria(int cntOfCriteria)
	{
		this.m_cntOfCriteria = cntOfCriteria;
	}
	
	public int getCounterOfCriteria() {
		return m_cntOfCriteria;
	}
	
	public boolean isModelFound() {
		return m_modelFound;
	}

	public void setModelFound(boolean m_modelFound) {
		this.m_modelFound = m_modelFound;
	}

	public int getNoOfTriedCombinations() {
		return m_noOfTriedCombinations;
	}

	public void setNoOfTriedCombinations(int m_noOfTriedCombinations) {
		this.m_noOfTriedCombinations = m_noOfTriedCombinations;
	}

	public void setCombinations(String sizeCombinations) {
	
		this.m_combinations = sizeCombinations;
		this.m_combinations = this.m_combinations.replace(",", "&");
		this.m_combinations = this.m_combinations.replace("]", "");
		this.m_combinations = this.m_combinations.replace("[","");
		
	}

	public void setModelName(String newProcessFilePath) {
		this.m_newModelFilePath = newProcessFilePath;

	}
	
	public String getNewModelFilePath() {
		return m_newModelFilePath;
	}

	public String getCombinations() {
	
		return m_combinations;
	}
}
