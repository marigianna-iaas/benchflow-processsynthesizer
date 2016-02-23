package de.unistuttgart.iaas.newbpmnprocess.model;

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ConnectionPoint{

	private int neededIncoming;
	private int neededOutgoing;
	private String type;
	private FlowNode flownode;
	
	public ConnectionPoint(String fid, String type,  FlowNode node)
	{
		this.fid = fid;
		this.type = type;
		this.neededIncoming = 0;
		this.neededOutgoing = 0;
		this.flownode = EcoreUtil.copy(node);
	}
	public ConnectionPoint(String fid, String type, int incoming, int outgoing, FlowNode node)
	{
		this.fid = fid;
		this.type = type;
		this.neededIncoming = incoming;
		this.neededOutgoing = outgoing;
		this.flownode = EcoreUtil.copy(node);
	}

	
	public boolean isConnected()
	{
		return (this.neededIncoming ==0 && this.neededOutgoing == 0 );
	}
	
	
	public String getFid()
	{
		return this.fid;
	}
	
	public void  setNeededIncoming(int incoming)
	{
		this.neededIncoming = incoming;
	}
	
	public void setNeededOutgoing(int outgoing)
	{
		this.neededOutgoing = outgoing;
	}
	
	public int getNeededIncoming()
	{
		return this.neededIncoming;
	}
	
	public int getNeededOutgoing()
	{
		return this.neededOutgoing;
	}

	
	public boolean isValidConnectionPoint()
	{
		//needs one or more connections
		return (neededIncoming !=0 || neededOutgoing !=0);
	}
	
	public String getType()
	{
		return this.type;
	}
	

	public FlowNode getFlownode() {
		return flownode;
	}

	public void setFlownode(FlowNode flownode) {
		this.flownode = flownode;
	}


	public void setFid(String fid) {
		this.fid = fid;
	}

	private String fid;

	
}
