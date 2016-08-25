//If the fragments are compatible, new process will be made here
package de.unistuttgart.iaas.newbpmnprocess.composer;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import de.unistuttgart.iaas.newbpmnprocess.model.ConnectionPoint;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;

/**
 * Contains all functions for synthesizing a new process
 * @author skourama
 *
 */
public class NewProcessComposer {

	/**
	 * Drives the process model synthesizing
	 * @param selectedFragments - the fragments selected from the DB
	 * @return Filepath if the process was created or null if it was unsuccessfull
	 */
	public static String composeNewProcess(List<List<FragmentExt>> selectedFragments)
	{	
		String newProcessFileName = null;
		if(selectedFragments.size() < 2) { 
			System.err.println("Not enough fragments for synthesizing"); 
			return null;
		}
		//from the indeces to synthesize I am basically only interested in the memory
		//the memory contains the combination that can be linked
		LinkableFragmentsMemory linkableFragmentsMemory = getIndecesOfFragmentsToCompose(selectedFragments,new Indeces(), new LinkableFragmentsMemory(selectedFragments.size()));
	
		if(linkableFragmentsMemory == null || linkableFragmentsMemory.memoryIsEmpty()) {
			System.err.println("The fragments responding to these criteria could not be linked."
								+"\n Change the criteria and try again");
			return null;
		}
		else{	
			 List<FragmentExt> fragmentsToSynthesize = 
					getFragmentsToSynthesize(selectedFragments, linkableFragmentsMemory.getMemory());
			 newProcessFileName = createSyntheticProcess(fragmentsToSynthesize);
		}
		return newProcessFileName;
	}
	

	/**
	 * The memory only has the pointers to the fragments to syntehsize
	 * This function will find and return the appropriate fragments
	 * @param selectedFragments - the fragments selected from the DB
	 * @param indecesOfFragmentsToSynthesize - basically the returned memory
	 * @return
	 */
	private static List<FragmentExt> getFragmentsToSynthesize(
			List<List<FragmentExt>> selectedFragments,
			int[] indecesOfFragmentsToSynthesize) {
		List<FragmentExt> fragmentsToSynthesize = new ArrayList<FragmentExt>();
	
		for(int i=0; i < indecesOfFragmentsToSynthesize.length; i++)
		{
			int index = indecesOfFragmentsToSynthesize[i];
			List<FragmentExt> childList = selectedFragments.get(i);
			childList.get(index);
			
			FragmentExt fragment = selectedFragments.get(i).get(indecesOfFragmentsToSynthesize[i]);
			fragment.loadProcess(fragment.getFilePath());
			fragmentsToSynthesize.add(fragment);
		}
		return fragmentsToSynthesize;
	}

	/**
	 * Tests different combinations of fragments to derive a linked process by using backtracking to 
	 * exclude combinations that did not work
	 * @param selectedFragments
	 * @param indeces an object containing that are searched and matched
	 * @return a list of indexes that correspond to linkable fragments. The indeces give the position of fragments in the selectedFragments
	 */
	//TODO: this method can be improved a lot concerning the constraints of smartly selecting fragments
	//TODO: would a rule engine be in use?
	private static LinkableFragmentsMemory getIndecesOfFragmentsToCompose(List<List<FragmentExt>> selectedFragments,  Indeces indeces, LinkableFragmentsMemory successfullFragmentsMemory)
	{
			if(indeces.getNextParentRow() < selectedFragments.size() && indeces.getParentRow() != -1)	// size -1 because we are pairing i-1 with last row
			{
	 			List<FragmentExt> parentList = selectedFragments.get(indeces.getParentRow());
	 			List<FragmentExt> childList = selectedFragments.get(indeces.getNextParentRow());
	 			
	 			while(indeces.getParentColumn() < parentList.size())
	 			{
	 				FragmentExt parentFragment = parentList.get( indeces.getParentColumn());
	 				while(indeces.getChildColumn() < childList.size())
	 				{
	 					FragmentExt childFragment = childList.get(indeces.getChildColumn()); //gets from the position of fragment2Index 
	 					if(checkLinkingCompatibility(parentFragment, childFragment)) //check middle fragments for start-end event here
	 					{
	 						successfullFragmentsMemory.addToMemory(indeces.getParentRow(), indeces.getParentColumn());
	 						successfullFragmentsMemory.addToMemory(indeces.getNextParentRow(), indeces.getChildColumn());
	 						indeces.setIndeces(indeces.getNextParentRow(), indeces.getChildColumn(), 0);
	 						if(indeces.getNextParentRow() == selectedFragments.size())
	 						{
	 							
	 							return successfullFragmentsMemory;
	 						}
	 						
	 			 			getIndecesOfFragmentsToCompose(selectedFragments, indeces, successfullFragmentsMemory);
	 					}
	 					indeces.setChildColumn(indeces.getNextChildColumn());

	 				}	
	 					
	 				//finished list 2 without finding a matching
	
	 				//if the current list is the first just proceed by taking the next element with the beginning of the second list
	 					if(indeces.getParentRow()== 0)
	 					{
		 					indeces.setIndeces(indeces.getParentRow(), indeces.getNextParentColumn(), 0);
		 					if(indeces.getParentColumn() >= parentList.size()) //elements of list 0 finished unsuccessfully
		 					{
		 						return null;
		 					}

	 					}
	 					//else if it is not the first list. then you have to go one list back
	 					//and take the current index with the next element of the second list
	 					//otherwise it will lose connection
	 					else{
	 						indeces.setIndeces(indeces.getPrevParentRow(), successfullFragmentsMemory.getMemoryElement(indeces.getPrevParentRow()), indeces.getNextChildColumn());
	 						successfullFragmentsMemory.rewindMemory(indeces.getNextParentRow());	//which is basically from the current list on. Erase all the entries
	 						getIndecesOfFragmentsToCompose(selectedFragments, indeces, successfullFragmentsMemory);
	 					}
	 					
	 			}	
			}
			return successfullFragmentsMemory; 
	}
	

	/**
	 * Checks compatibility between two fragments
	 * if they have the same amound of connections 
	 * TODO: see if this compatibility can be extended to avoid deadlocks?
	 * @param rowLevel 
	 */
	private static boolean checkLinkingCompatibility(FragmentExt parentFragment, FragmentExt childFragment) {
		
		if(parentFragment.getOutgoingConnections() != 0 && childFragment.getIncomingConnections() !=0)
		{
			return( parentFragment.getOutgoingConnections() >= childFragment.getIncomingConnections());
		}
			return false;
	}


	/**
	 * Links the fragments together
	 * TODO: write clearer?
	 * @param fragmentsToSynthesize
	 * @return
	 */
	private static String createSyntheticProcess(List<FragmentExt> fragmentsToSynthesize)
	{
	  	int index = 0;
		ProcessXMLEditor processEditor = new ProcessXMLEditor();
		Document finalProcessXML = processEditor.getFinalProcessXML();
	
		finalProcessXML = processEditor.addFirstFragmentToProcess(finalProcessXML, fragmentsToSynthesize.get(0));
		while(index < fragmentsToSynthesize.size() -1 )
		{
		  	FragmentExt fragmentLeft = fragmentsToSynthesize.get(index); //TODO: in an optimal scenario would choose fragments with 1 incoming or start event
		  	FragmentExt fragmentRight = fragmentsToSynthesize.get(index + 1); //TODO: in an optimal scenario this would choose fragments with 1 outgoing or end event
		  	
			finalProcessXML = processEditor.appendFragmentToFile(finalProcessXML, fragmentRight);
		

			processEditor = linkFragments(processEditor, fragmentLeft.getConnectionPointsByType("ParallelGatewayImpl" ), fragmentRight.getConnectionPoints());
			processEditor = linkFragments(processEditor, fragmentLeft.getConnectionPointsByType("ExclusiveGatewayImpl" ), fragmentRight.getConnectionPoints());
			processEditor = linkFragments(processEditor, fragmentLeft.getConnectionPointsByType("CallActivityImpl" ), fragmentRight.getConnectionPoints());
			processEditor = linkFragments(processEditor, fragmentLeft.getConnectionPointsByType("TaskImpl" ), fragmentRight.getConnectionPoints());
			index ++;
		}
		processEditor = fixStartEvent(processEditor, fragmentsToSynthesize.get(0));
		processEditor = fixEndEvents(processEditor , fragmentsToSynthesize);
		
		
		return processEditor.writeProcessToXML();
	}

	
private static ProcessXMLEditor fixStartEvent(ProcessXMLEditor processEditor, FragmentExt firstFragment) {
	
  	for(ConnectionPoint connectionPointLeft : firstFragment.getConnectionPoints())
  	{
  		if(connectionPointLeft.getNeededIncoming() > 0)
  		{
  			processEditor.addStartEventToConnectionPoint(connectionPointLeft);
  			connectionPointLeft.fixConnectedIncoming();
  			connectionPointLeft.setNeededIncoming(0);
  			break;
  		}
  	}
  	return processEditor;

}


private static ProcessXMLEditor linkFragments(ProcessXMLEditor processEditor, List<ConnectionPoint> connectionPointsLeft, List<ConnectionPoint> connectionPointsRight )
{
	
  	for(ConnectionPoint connectionPointLeft : connectionPointsLeft)
  	{
		for(ConnectionPoint connectionPointRight : connectionPointsRight)
	  	{
		  		//FIXME: optimization - it will take the connection point irrelevant to their expected incoming or outgoing edges
		  		while(connectionPointLeft.getNeededOutgoing() > 0 && connectionPointRight.getNeededIncoming() > 0 )
		  		{
		  			processEditor.linkSequenceFlowsOfConnectionPoints(connectionPointLeft, connectionPointRight);
		  			connectionPointLeft.fixConnectedOutgoing();
		  			connectionPointRight.fixConnectedIncoming();
		  		}
	  	}
  	}
	return processEditor;

}
	
	
	private static ProcessXMLEditor fixEndEvents(ProcessXMLEditor processEditor, 
			List<FragmentExt> fragmentsToSynthesize
			) {
					
		for(FragmentExt currFragment: fragmentsToSynthesize)
		{
			System.out.println("Fragment:"+ currFragment.getId());
		  	for(ConnectionPoint connectionPointOfCurrFragment : currFragment.getConnectionPoints())
		  	{
		  		while(connectionPointOfCurrFragment.getNeededOutgoing() > 0 )
		  		{
		  			if(connectionPointOfCurrFragment.getType().equals("CallActivityImpl") || connectionPointOfCurrFragment.getType().equals("TaskImpl"))
		  			{
		  				processEditor.addEndEventToConnectionPoint(connectionPointOfCurrFragment);
		  				connectionPointOfCurrFragment.fixConnectedOutgoing();
		  				connectionPointOfCurrFragment.setNeededOutgoing(0);	//even if more were needed it should be enough
		  			}
		  			else if (connectionPointOfCurrFragment.getType().equals("ExclusiveGatewayImpl") || connectionPointOfCurrFragment.getType().equals("ParallelGatewayImpl")) 
		  			{
		  				if(connectionPointOfCurrFragment.getNeededOutgoing() == 1)
		  				{
			  				processEditor.addEndEventToConnectionPoint(connectionPointOfCurrFragment);
			  				connectionPointOfCurrFragment.fixConnectedOutgoing();
			  				connectionPointOfCurrFragment.setNeededOutgoing(0);
		  				}
		  				else if (connectionPointOfCurrFragment.getNeededOutgoing() >= 1)
		  				{
			  				processEditor.addRelaxPointAndEndEvent(connectionPointOfCurrFragment);
			  				connectionPointOfCurrFragment.fixConnectedOutgoing();
			  				System.out.println("Relax point (extra task) should be added to node: " + currFragment.getId() + "for consistency");
			  				connectionPointOfCurrFragment.setNeededOutgoing(1);	//here one more will be left which should be one
		  				}
		  				
		  			}
		  		}
		  	}
		}
		return processEditor;
	}
	
}
