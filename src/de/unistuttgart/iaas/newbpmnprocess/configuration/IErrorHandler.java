package de.unistuttgart.iaas.newbpmnprocess.configuration;

public interface IErrorHandler {
	//Notify method ...
	void setError(IError error);
	IError getError();
}
