package de.unistuttgart.iaas.newbpmnprocess.criteria;

public class Criterio {
	
	private boolean ActDetails;
	private int TotalTasks;
	private int Task;
	private int ServiceTask;
	private int CallActivity;
	private int ExclGateway;
	private int ParalGateway;
	
	public Criterio()
	{
		ActDetails=false;
		TotalTasks=0;
		Task=0;
		ServiceTask=0;
		CallActivity=0;
		ExclGateway=0;
		ParalGateway=0;
	}
	
	public boolean getActDetails()
	{
		return ActDetails;
	}
	
	public void setActDetails(boolean actdetails)
	{
		ActDetails=actdetails;
	}
	
	public int getTotalTasks()
	{
		return TotalTasks;
	}
	
	public void setTotalTasks(int totaltasks)
	{
		TotalTasks=totaltasks;
	}

	public int getTask() {
		return Task;
	}

	public void setTask(int task) {
		Task = task;
	}

	public int getServiceTask() {
		return ServiceTask;
	}

	public void setServiceTask(int serviceTask) {
		ServiceTask = serviceTask;
	}

	public int getCallActivity() {
		return CallActivity;
	}

	public void setCallActivity(int callActivity) {
		CallActivity = callActivity;
	}

	public int getExclGateway() {
		return ExclGateway;
	}

	public void setExclGateway(int exclGateway) {
		ExclGateway = exclGateway;
	}

	public int getParalGateway() {
		return ParalGateway;
	}

	public void setParalGateway(int paralGateway) {
		ParalGateway = paralGateway;
	}

}
