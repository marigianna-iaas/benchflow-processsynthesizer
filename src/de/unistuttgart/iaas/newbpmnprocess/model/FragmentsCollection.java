package de.unistuttgart.iaas.newbpmnprocess.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.bpmn2.FlowNode;
import de.unistuttgart.iaas.bpmn.collection.BpmnCollectionSingleton;
import de.unistuttgart.iaas.newbpmnprocess.database.DBConnection;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

public class FragmentsCollection extends BpmnCollectionSingleton{

	
	protected List<FragmentExt> fragmentsCollection = new ArrayList<FragmentExt>();
	private List<File> bpmnFilePaths= new ArrayList<File>();
	Constants constObj = new Constants();

	/**
	 * This class is responsible for loading all the fragments that exist in the path "../thesis/fragments"
	 * It will load all these fragments on the database
	 */
	public void loadFromFilesToDB() {
		try {
			
			File collectionFolder = new File(Constants.FragmentsDirectoryPath);
			String[] listOfBpmnFiles = collectionFolder.list();

			for (String bpmnFileName : listOfBpmnFiles) {
				File bpmnFile = new File(collectionFolder.getAbsolutePath()
						.toString() + File.separator+ bpmnFileName);
				if (bpmnFile.isFile()) {
					this.bpmnFilePaths.add(bpmnFile);
					FragmentExt fragment = new FragmentExt(bpmnFile.getCanonicalPath());
					if(fragment.getId() != null)
					{
						insertFragmentsToDB(fragment);	
						insertConnectionPointsToDB(fragment);
					}
				}

			}


		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception: "+ e.getMessage(), e);

		}
	}

	
	public List<FragmentExt> getAllFragments()
	{
		return this.fragmentsCollection;
	}

	private int numberOfFlowNodes(FragmentExt fragment){
		int number = 0;
		for(org.eclipse.bpmn2.FlowElement f : fragment.getProcess().getFlowElements())
		{
			if(f instanceof FlowNode)
			{
				++number;
			}
		}
		return number;
	}
	
	public void insertFragmentsToDB(FragmentExt fragment) {
		
		
		
		String sql = "Insert into fragments(fid, filepath, hasStartEvent, hasEndEvent, numberOfFlowNodes) values(?,?,?,?,?)";

			if(fragment.getIsValidFragment())
			{	
				//FIXME: SOS! make filePath relevant
				Object[] values = {fragment.getId(),fragment.getFilePath(),fragment.getHasStartEvent(), fragment.getHasEndEvent(), numberOfFlowNodes(fragment)};
	
				DBConnection connec= new DBConnection();
				connec.insertData(sql, values);
			}
			else{
				System.out.println("Invalid Fragment skipped from DB:" + fragment.getId());
			}
	}
	
	public void insertConnectionPointsToDB(FragmentExt fragment) {
		//TODO: assert fragments not null
		String sql = "Insert into connectionPoints(fid, incoming,outgoing,type, nodeId) values(?,?,?,?, ?)";
		for(ConnectionPoint c : fragment.getConnectionPoints())
		{
			Object[] values = {fragment.getId(), c.getNeededIncoming(), c.getNeededOutgoing(), c.getType(), c.getFlownode().getId()};
	
			DBConnection connec= new DBConnection();
			connec.insertData(sql, values);
		}
	
	}
	
	public void addFragmentToCollection(FragmentExt newFragment)
	{
		fragmentsCollection.add(newFragment);
	}
	
	
	
}
