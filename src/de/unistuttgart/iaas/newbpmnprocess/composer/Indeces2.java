package de.unistuttgart.iaas.newbpmnprocess.composer;

public class Indeces2 {
	private int parentRow =0;
	private int parentColumn =0 ;
	private int childColumn =0 ;
	private int [] memory;
	
	public Indeces2(int memoryArraySize)
	{
		memory = new int[memoryArraySize];
		for(int i=0; i< memory.length; i++) memory[i] = -1;

	}

	
	public void setIndeces(int parentRow, int parentColumn, int childColumn)
	{
		this.parentRow = parentRow;
		this.parentColumn = parentColumn ; 
		this.childColumn = childColumn ;
	}

	public void rewindMemory(int position) {
		for(int i=position; i< memory.length; i++)
			memory[i] = -1;
	}
	
	
	public int getParentRow() {
		return parentRow;
	}
	
	public int getNextParentRow() {
		return parentRow + 1;
	}
	public int getPrevParentRow() {
		return parentRow -1;
	}

	public void setPrentRow(int parentRow) {
		this.parentRow = parentRow;
	}

	public int getParentColumn() {
		return parentColumn;
	}
	
	public int getNextParentColumn() {
		return parentColumn + 1;
	}

	public void setParentColumn(int parentColumn) {
		this.parentColumn = parentColumn;
	}

	public int getChildColumn() {
		return childColumn;
	}

	
	public int getNextChildColumn() {
		return childColumn + 1;
	}
	public void setChildColumn(int childColumn) {
		this.childColumn = childColumn;
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
