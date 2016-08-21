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
			int cnt =0;
			for (String bpmnFileName : listOfBpmnFiles) {
				File bpmnFile = new File(collectionFolder.getAbsolutePath()
						.toString() + File.separator+ bpmnFileName);
				if (bpmnFile.isFile()) {
					this.bpmnFilePaths.add(bpmnFile);
					try{
						FragmentExt fragment = new FragmentExt(bpmnFile.getCanonicalPath());
						if(fragment.getId() != null)
						{
							insertFragmentsToDB(fragment);	
							insertConnectionPointsToDB(fragment);
						}
					}
					catch(Exception e)
					{
						cnt++;
					}
				}

			}

			//todo: at the end implement an execution for this:  
			//insert into `ConnectionPointsStats` (fid, type, counter)
			//SELECT fid, type, count(type) as 'Counter' FROM `connectionPoints` group by type, fid order by fid
			System.out.println(cnt + "problematic");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception: "+ e.getMessage(), e);

		}
	}

	
	public List<FragmentExt> getAllFragments()
	{
		return this.fragmentsCollection;
	}


	public void insertFragmentsToDB(FragmentExt fragment) {
		
		
		
		String sql = "Insert into fragments(fid, filepath, hasStartEvent, hasEndEvent, numberOfFlowNodes, numberOfExclusiveGateways, numberOfParallelGateways, numberOfScriptTasks,numberOfCallActivity, numberOfServiceTasks ) values(?,?,?,?,?,?,?,?,?,?)";

			if(fragment.getIsValidFragment())
			{	
				//FIXME: SOS! make filePath relevant
				Object[] values = {fragment.getId(),fragment.getFilePath(),fragment.getHasStartEvent(), fragment.getHasEndEvent(), fragment.getNumberOfFlowNodes(), fragment.getNumberOfExclusiveGateways(),
						fragment.getNumberOfParallelGateway(), fragment.getNumberOfScriptTasks(), fragment.getNumberOfCallActivity(), fragment.getNumberOfServiceTasks()};
	
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
