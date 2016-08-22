package de.unistuttgart.iaas.newbpmnprocess.composer;


/**
 * Has the memory for the logic to define which fragments to synthesize
 * @variable: successfullFragmentsMemory - the memory that holds the indeces of the fragments. It is a vertical array that shows the indeces on the columns of each horizontal list
 */
public class LinkableFragmentsMemory {
	private int [] successfullFragmentsMemory;
	
	public LinkableFragmentsMemory(int memoryArraySize) {
		successfullFragmentsMemory = new int[memoryArraySize];
		for(int i=0; i< successfullFragmentsMemory.length; i++) 
			successfullFragmentsMemory[i] = -1;

	}

	public void setMemory( int[] memory) {
		this.successfullFragmentsMemory = memory;
	}
	
	public int getMemoryElement(int index)
	{
	
		return  successfullFragmentsMemory[index];
	}
	
	public void addToMemory(int index, int value)
	{
		successfullFragmentsMemory[index] = value;
	}
	
	public boolean memoryIsEmpty()
	{
		for(int i=0; i< successfullFragmentsMemory.length; i++)
		{
			if(successfullFragmentsMemory[i] == -1)
				return true;
		}
		return false;
	}
	
	public void rewindMemory(int position) {
		for(int i=position; i< successfullFragmentsMemory.length; i++)
			successfullFragmentsMemory[i] = -1;
	}

	public int[] getMemory()
	{
		return successfullFragmentsMemory;
	}
	
}
