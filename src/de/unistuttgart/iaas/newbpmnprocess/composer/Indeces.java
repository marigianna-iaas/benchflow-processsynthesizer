package de.unistuttgart.iaas.newbpmnprocess.composer;

public class Indeces {
	private int parentRow =0;
	private int parentColumn =0 ;
	private int childColumn =0 ;

	
	public void setIndeces(int parentRow, int parentColumn, int childColumn)
	{
		this.parentRow = parentRow;
		this.parentColumn = parentColumn ; 
		this.childColumn = childColumn ;
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

	
	
}
