package de.unistuttgart.iaas.newbpmnprocess.configuration;

public final class ExcperimentConfigurationError implements IError {
	Throwable m_exception = null;
	String m_description;
	
	ExcperimentConfigurationError(String des){
		m_description = des;
	}
	ExcperimentConfigurationError(String des, Throwable e){
		m_description = des;
		m_exception = e;
	}
	
	public void setException(Throwable e) {
		m_exception = e;
	}

	public boolean hasException() {
		return m_exception != null;
	}

	public Throwable getException() {
		return m_exception;
	}
	
	public void setDescription(String str) {
		m_description = str;
	}

	public String getDescription() {
		return m_description;
	}

}
