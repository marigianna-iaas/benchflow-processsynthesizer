package de.unistuttgart.iaas.newbpmnprocess.composer;


public class Indeces {
	private int fragmentlistsIndex =0;
	private int fragment1Index =0 ;
	private int fragment2Index =0 ;
	private int [] memory;
	
	public Indeces(int memoryArraySize)
	{
		memory = new int[memoryArraySize];
		for(int i=0; i< memory.length; i++) memory[i] = -1;

	}

	
	public void setIndeces(int fragmentlistsIndex, int fragment1Index, int fragment2Index)
	{
		this.fragmentlistsIndex = fragmentlistsIndex;
		this.fragment1Index = fragment1Index ; 
		this.fragment2Index = fragment2Index ;
	}

	public void rewindMemory(int key) {
		for(int i=key; i< memory.length; i++)
			memory[i] = -1;
	}
	
	
	public int getFragmentlistsIndex() {
		return fragmentlistsIndex;
	}
	
	public int getNextFragmentlistsIndex() {
		return fragmentlistsIndex + 1;
	}
	public int getPrevFragmentlistsIndex() {
		return fragmentlistsIndex -1;
	}

	public void setFragmentlistsIndex(int fragmentlistsIndex) {
		this.fragmentlistsIndex = fragmentlistsIndex;
	}

	public int getFragment1Index() {
		return fragment1Index;
	}
	
	public int getNextFragment1Index() {
		return fragment1Index + 1;
	}

	public void setFragment1Index(int fragment1Index) {
		this.fragment1Index = fragment1Index;
	}

	public int getFragment2Index() {
		return fragment2Index;
	}

	
	public int getNextFragment2Index() {
		return fragment2Index + 1;
	}
	public void setFragment2Index(int fragment2Index) {
		this.fragment2Index = fragment2Index;
	}

	public  int[] getMemory() {
		return memory;
	}

	public void setMemory( int[] memory) {
		this.memory = memory;
	}
	
	public int getMemoryElement(int index)
	{
	
		return  memory[index];
	}
	
	public void addToMemory(int index, int value)
	{
		memory[index] = value;
	}
	
	public boolean memoryIsEmpty()
	{
		for(int i=0; i< memory.length; i++)
		{
			if(memory[i] == -1)
				return true;
		}
			return false;
	}
	
}
