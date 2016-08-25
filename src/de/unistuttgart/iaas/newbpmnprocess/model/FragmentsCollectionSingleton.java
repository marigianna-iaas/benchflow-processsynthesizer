package de.unistuttgart.iaas.newbpmnprocess.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.unistuttgart.iaas.newbpmnprocess.database.DBConnection;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

//FIXME: Collection in general should be an interface where the BpmnCollection and the FragmentsCollection inherit
public class FragmentsCollectionSingleton implements IBpmnFileTypesCollection<FragmentExt> {

	//this method logs the info..
	protected final static Logger LOGGER = Logger.getLogger(FragmentsCollectionSingleton.class.getName());
	static{
		LOGGER.setLevel(Level.ALL);
	}
	
	protected static FragmentsCollectionSingleton fragmentsCollectionInstance  = new FragmentsCollectionSingleton();
	protected List<FragmentExt> fragmentsCollection = new ArrayList<FragmentExt>();
	private List<File> bpmnFilePaths= new ArrayList<File>();
	Constants constObj = new Constants();

	protected FragmentsCollectionSingleton() {
		// Exists only to defeat instantiation.
	}

	//we call this method to get the singleton class instance
	public static FragmentsCollectionSingleton getInstance() {
		
		return fragmentsCollectionInstance;
	}	
	
	
	
	/**
	 * This class is responsible for loading all the fragments that exist in the path "../thesis/fragments"
	 * It will load all these fragments on the database
	 */
	public void loadFromCollectionToDB() {
		int problematicFilesCnt = 0;
		try {		
			for(FragmentExt fragment : fragmentsCollection)		
			{
				if(fragment.getId() != null && !fragment.getHasNonCoreElements())
				{
					insertFragmentsToDB(fragment);	
					insertConnectionPointsToDB(fragment);
				}
			}
		}
		catch(Exception e)
		{
			problematicFilesCnt++;
			LOGGER.log(Level.SEVERE, "Exception: "+ e.getMessage(), e);
		}
		System.out.println("DB Load Finished");
		if(problematicFilesCnt > 0 ) System.err.println("Problematic: "+ problematicFilesCnt);
		System.out.println("Core elemets: " + FragmentExt.coreElements.toString());
		System.out.println("Non core elemets: " + FragmentExt.nonCoreElements.toString());
	}

	
	public List<FragmentExt> getAllFiles()
	{
		return this.fragmentsCollection;
	}


	public void insertFragmentsToDB(FragmentExt fragment) {
		if(fragment.isValidFragment())
		{
			String sql = "Insert into fragments(fid, filepath, hasStartEvent, hasEndEvent, numberOfFlowNodes, numberOfExclusiveGateways, numberOfParallelGateways, numberOfScriptTasks,numberOfCallActivity, numberOfServiceTasks , hasNonCoreElemenets) values(?,?,?,?,?,?,?,?,?,?,?)";
	
			//FIXME: SOS! make filePath relevant
				Object[] values = {fragment.getId(),fragment.getFilePath(),fragment.hasStartEvent(), fragment.hasEndEvent(), fragment.getNumberOfFlowNodes(), fragment.getNumberOfExclusiveGateways(),
						fragment.getNumberOfParallelGateway(), fragment.getNumberOfScriptTasks(), fragment.getNumberOfCallActivity(), fragment.getNumberOfServiceTasks(), fragment.getHasNonCoreElements()};
		
				DBConnection connec= new DBConnection();
				connec.insertData(sql, values);
		}
		else{
			System.err.println("Fragment: " + fragment.getId() + "invalid. Incoming connections:" + fragment.getIncomingConnections());
		}
	}
	
	public void insertConnectionPointsToDB(FragmentExt fragment) {
		//TODO: assert fragments not null
		String sql = "Insert into connectionPoints(fid, incoming,outgoing,type, nodeId, isFlexible) values(?,?,?,?,?,?)";
		for(ConnectionPoint c : fragment.getConnectionPoints())
		{
			Object[] values = {fragment.getId(), c.getNeededIncoming(), c.getNeededOutgoing(), c.getType(), c.getFlownode().getId(), c.isFlexible()};
	
			DBConnection connec= new DBConnection();
			connec.insertData(sql, values);
		}
	
	}


	public void loadCollectionFromFiles() {
		try {
			
			File collectionFolder = new File(Constants.FragmentsDirectoryPath);
			String[] listOfBpmnFiles = collectionFolder.list();
			for (String bpmnFileName : listOfBpmnFiles) {
				File bpmnFile = new File(collectionFolder.getAbsolutePath()
						.toString() + File.separator+ bpmnFileName);
				if (bpmnFile.isFile()) {
					this.bpmnFilePaths.add(bpmnFile);
					FragmentExt newFragment = new FragmentExt(bpmnFile.getCanonicalPath());
					fragmentsCollection.add(newFragment);
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception: "+ e.getMessage(), e);

		}
	}

	public void addFileToCollection(FragmentExt newFragment) {
		fragmentsCollection.add(newFragment);
		
	}

	
	
	
}
