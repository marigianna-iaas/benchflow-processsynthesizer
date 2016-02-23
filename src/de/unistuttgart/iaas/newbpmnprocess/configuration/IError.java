package de.unistuttgart.iaas.newbpmnprocess.configuration;

public interface IError {

	void setException(Throwable e);
	boolean hasException();
	Throwable getException();
	
	
	void setDescription(String str);
	String getDescription();
}
