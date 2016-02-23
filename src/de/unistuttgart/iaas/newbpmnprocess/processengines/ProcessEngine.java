package de.unistuttgart.iaas.newbpmnprocess.processengines;

public enum ProcessEngine {
	Camunda("Camunda", new CamundaSpecificModification());
	
	private final String m_name;
	private final IEngineSpecificModification m_modification;
	
	private ProcessEngine(String name, IEngineSpecificModification modification) {
		m_name = name;
		m_modification = modification;
	}
	
	public void modify(String filePath){
		m_modification.modify(filePath);
	}
	
	@Override
	public String toString(){
		return m_name;
	}
}
