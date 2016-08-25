package de.unistuttgart.iaas.newbpmnprocess.model;

import java.util.Collection;

import de.unistuttgart.iaas.bpmn.model.ModelInstance;

public interface IBpmnFileTypesCollection<T>{

	public Collection<? extends ModelInstance> getAllFiles();
	public void addFileToCollection(T file);
	public void loadCollectionFromFiles(); 	//this should be less coupled from FilesManager concerning the path from which we are reading 
	public void loadFromCollectionToDB();
}
