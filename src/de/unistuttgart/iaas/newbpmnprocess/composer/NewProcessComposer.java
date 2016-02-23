//If the fragments are compatible, new process will be made here
package de.unistuttgart.iaas.newbpmnprocess.composer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import de.unistuttgart.iaas.newbpmnprocess.model.ConnectionPoint;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;

public class NewProcessComposer {

	public static String composeNewProcess(List<List<FragmentExt>> selectedFragments,  int totalNumberOfFlowNodes)
	{	
		String newProcessFileName = null;
		if(selectedFragments.size() < 2) { 
			System.out.println("Not enough fragments for synthesizing"); 
			return null;
		}
		Indeces indecesToSynthesize = getIndecesOfFragmentsToCompose(selectedFragments,new Indeces(selectedFragments.size()), totalNumberOfFlowNodes);
		
		if(indecesToSynthesize.memoryIsEmpty()) {
			System.out.println("The fragments responding to these criteria could not be linked."
								+"\n Change the criteria and try again");
			return null;
		}
		else{	
			 List<FragmentExt> fragmentsToSynthesize = 
					getFragmentsToSynthesize(selectedFragments, indecesToSynthesize.getMemory());
				
			 newProcessFileName = linkFragments(fragmentsToSynthesize);	
			
		}
		return newProcessFileName;
	}
	

	/**
	 * Returns fragments to synthesize
	 * @param selectedFragments
	 * @param indecesToSynthesize
	 * @return
	 */
	private static List<FragmentExt> getFragmentsToSynthesize(
			List<List<FragmentExt>> selectedFragments,
			int[] indecesToSynthesize) {
		List<FragmentExt> fragmentsToSynthesize = new ArrayList<FragmentExt>();
	
		for(int i=0; i < indecesToSynthesize.length; i++)
		{
			FragmentExt fragment = selectedFragments.get(i).get(indecesToSynthesize[i]);
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
	private static Indeces getIndecesOfFragmentsToCompose(List<List<FragmentExt>> selectedFragments,  Indeces indeces, int totalNumberOfFlowNodes)
	{
			while(indeces.getNextFragmentlistsIndex() < selectedFragments.size() && indeces.getFragmentlistsIndex() != -1)	// size -1 because we are pairing i-1 with last row
			{
	 			List<FragmentExt> crFragmentList1 = selectedFragments.get(indeces.getFragmentlistsIndex());
	 			List<FragmentExt> crFragmentList2 = selectedFragments.get(indeces.getNextFragmentlistsIndex());
	 			
	 			outerLoop:
	 			while( indeces.getFragment1Index() < crFragmentList1.size())
	 			{
	 				FragmentExt fragment1 = crFragmentList1.get( indeces.getFragment1Index());
	 				while(indeces.getFragment2Index() < crFragmentList2.size())
	 				{
	 					FragmentExt fragment2 = crFragmentList2.get(indeces.getFragment2Index()); //gets from the position of fragment2Index 
	 					if(checkLinkingCompatibility(fragment1, fragment2)) //check middle fragments for start-end event here
	 					{
	 						indeces.addToMemory(indeces.getFragmentlistsIndex(), indeces.getFragment1Index());
	 						indeces.addToMemory(indeces.getNextFragmentlistsIndex(), indeces.getFragment2Index());
	 						indeces.setIndeces(indeces.getNextFragmentlistsIndex(), indeces.getFragment2Index(), 0);
	 						break outerLoop;
	 					}
	 					indeces.setFragment2Index(indeces.getNextFragment2Index());

	 				}	
	 					
	 				//finished list 2 without finding a matching
	
	 				if(indeces.getFragment2Index() == crFragmentList2.size())
	 				{
	 					//if the current list is the first just proceed by taking the next element with the beginning of the second list
	 					if(indeces.getFragmentlistsIndex()== 0)
	 					{
		 					indeces.setIndeces(indeces.getFragmentlistsIndex(), indeces.getNextFragment1Index(), 0);
		 					if(indeces.getFragment1Index() >= crFragmentList1.size()) //elements of list 0 finished unsuccessfully
		 					{
		 						return indeces;
		 					}

	 					}
	 					//else if it is not the first list. then you have to go one list back
	 					//and take the current index with the next element of the second list
	 					//otherwise it will lose connection
	 					else{
	 						indeces.setIndeces(indeces.getPrevFragmentlistsIndex(), indeces.getMemoryElement(indeces.getPrevFragmentlistsIndex()), indeces.getNextFragment2Index());
	 						indeces.rewindMemory(indeces.getNextFragmentlistsIndex());	//which is basically from the current list on. Erase all the entries
	 						getIndecesOfFragmentsToCompose(selectedFragments, indeces, totalNumberOfFlowNodes);
	 					}
	 					
	 				}
	 			}	
			}
			
			return indeces; 
	}
	

	private static boolean checkLinkingCompatibility(FragmentExt fragment1, FragmentExt fragment2) {
		
		if(fragment1.getOutgoingConnections() != 0 && fragment2.getIncomingConnections() !=0)
		return fragment1.getOutgoingConnections() == fragment2.getIncomingConnections();
		else
			return false;
	}


	private static String linkFragments(List<FragmentExt> fragmentsToSynthesize)
	{
	  	int index = 0;
	  
		ProcessXMLEditor processEditor = new ProcessXMLEditor();
		Document finalProcessXML = processEditor.getFinalProcessXML();
	
		finalProcessXML = processEditor.addFirstFragmentToProcess(finalProcessXML, fragmentsToSynthesize.get(0));
		while(index < fragmentsToSynthesize.size() -1 )
		{
		  	FragmentExt f1 = fragmentsToSynthesize.get(index);
		  	FragmentExt f2 = fragmentsToSynthesize.get(index + 1);
		  	
			finalProcessXML = processEditor.appendFragmentToFile(finalProcessXML, f2);
		  	for(ConnectionPoint cp1 : f1.getConnectionPoints())
		  	{
	  		  	for(ConnectionPoint cp2 : f2.getConnectionPoints())
			  	{
		  			while(cp1.getNeededOutgoing() > 0 && cp2.getNeededIncoming() > 0 )
			  		{  	
		  				
			  			processEditor.linkConnectionPoints(cp1, cp2);
			  			cp1 = processEditor.getBaseCp();
			  			cp2 = processEditor.getCompareCP();
			  			
			  		}
			  		if(cp1.getNeededIncoming() > 0  && cp2.getNeededOutgoing() > 0 )
			  		{
			  			while(cp2.getNeededOutgoing() > 0 )
			  			{
			  				//f2, f1, cp2, cp1, finalProcessXML
				  			
				  			processEditor.linkConnectionPoints(cp2, cp1);
				 			cp2 = processEditor.getBaseCp();
				  			cp1 = processEditor.getCompareCP();
			  			}
			  		}
			  		
			  	}
		  	}
	
			index++;

		}	
		FragmentExt f2 = fragmentsToSynthesize.get(fragmentsToSynthesize.size() -1 );
	  	for(ConnectionPoint cp2 : f2.getConnectionPoints())
	  	{
	  		while(cp2.getNeededOutgoing() > 0 )
	  		{
	  			processEditor.addEndEvent(cp2);
	  			cp2 = processEditor.getBaseCp();
	  		}
	  	}
				
		return processEditor.writeProcessToXML();
	}
}
