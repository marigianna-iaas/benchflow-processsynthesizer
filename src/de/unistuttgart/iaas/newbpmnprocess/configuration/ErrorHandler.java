package de.unistuttgart.iaas.newbpmnprocess.configuration;

public class ErrorHandler implements IErrorHandler {
	IError m_error = null;
	
	public void setError(IError error) {
		m_error = error;		
	}

	public IError getError() {
		return m_error;
	}
	
}
