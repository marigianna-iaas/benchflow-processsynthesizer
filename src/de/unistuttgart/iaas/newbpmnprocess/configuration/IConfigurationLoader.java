package de.unistuttgart.iaas.newbpmnprocess.configuration;

public interface IConfigurationLoader {
	
	/*
	 * uri: the path for the configuration. Not null and not Empty
	 * handler: in case of error holds info regarding the error
	 * return: true if the configuration was loaded successfully, false otherwise
	 */
	public boolean loadConfiguration(String uri, IErrorHandler handler);
}
