package de.unistuttgart.iaas.newbpmnprocess.model;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseFactory;
import org.drools.core.WorkingMemory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.unistuttgart.iaas.bpmn.model.ModelInstance;
import de.unistuttgart.iaas.bpmn.util.CompareUtils;
import de.unistuttgart.iaas.newbpmnprocess.database.DBConnection;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;


/**
 * 
 * @author skourama
 *
 */
//TODO: this model instance inheritance can be lifted? I believe it is only used for the process modeling
public class FragmentExt extends ModelInstance {

	
	private boolean hasStartEvent = false;
	private boolean hasEndEvent = false;
	private String id ; 
	private int numberOfFlowNodes =0;
	
	private List<ConnectionPoint> connectionPoints;
	private boolean isValidFragment;
	private int incomingConnections =0 ;
	private int outgoingConnections = 0;
	
	//TODO: generalize these types to superclasses?
	private Collection<EClass> activityTypes = Arrays.asList(
			Bpmn2Package.Literals.CALL_ACTIVITY, Bpmn2Package.Literals.SCRIPT_TASK, Bpmn2Package.Literals.SERVICE_TASK);
	
	//TODO: generalize these types to superclasses?
	private Collection<EClass> gatewayTypes = Arrays.asList(
			Bpmn2Package.Literals.EXCLUSIVE_GATEWAY, Bpmn2Package.Literals.PARALLEL_GATEWAY);
	private List<FragmentExt> failMatches = new ArrayList<FragmentExt>();
	

	/**
	 * Load fragment from file
	 * @param modelFileURI
	 */
	public FragmentExt(String modelFileURI)
	{ 
		super(modelFileURI);
		this.id = calculateId();
		if(id!= null)
		{
			hasStartEvent  = hasEventType(Arrays.asList(
					Bpmn2Package.Literals.START_EVENT), Constants.BpmnStartEventElement);
			hasEndEvent = hasEventType(Arrays.asList(
					Bpmn2Package.Literals.END_EVENT), Constants.BpmnEndEventElement);
			connectionPoints = new ArrayList<ConnectionPoint>();
			
			this.connectionPoints = calculateFragmentMetadata(id);
			fixFragmentsConnections();
			isValidFragment = checkIfValidFragment();
		}
		
	}
	
//
//	public FragmentExt(String fid, String modelFileURI, boolean hasStartEvent, boolean hasEndEvent, List<ConnectionPoint> connectionPoints)
//	{ 
//		super();	//FIXME: there should be a local folder of files with the fragments!!!
//		this.id = fid;
//		this.filePath = modelFileURI;
//		this.hasStartEvent = hasStartEvent;
//		this.hasEndEvent = hasEndEvent;
//		this.connectionPoints = connectionPoints;
//		fixFragmentsConnections();
//		isValidFragment = checkIfValidFragment();
//	}
		
	/**
	 * For loading a fragment back from the db
	 * @param fid
	 * @param modelFileURI
	 * @param hasStartEvent
	 * @param hasEndEvent
	 * @param connectionPoints
	 */
	public FragmentExt(String fid, String modelFileURI, boolean hasStartEvent, boolean hasEndEvent, int numberOfFlowNodes){
		//FIXME: there should be a local folder of files with the fragments!!!
		super(modelFileURI);	
		this.id = fid;
		this.filePath = modelFileURI;
		this.hasStartEvent = hasStartEvent;
		this.hasEndEvent = hasEndEvent;
		this.connectionPoints = discoverConnectionPointsFromDB(fid);
		this.numberOfFlowNodes = numberOfFlowNodes;
		fixFragmentsConnections();
		isValidFragment = checkIfValidFragment();
	//	fixFragmentsConnections();
	//	isValidFragment = checkIfValidFragment();
	}


	/**
	 * Check if it has Event type and in this case we should also implement the eligibility check
	 * @param event
	 * @param eventImplName
	 * @return
	 */
	private boolean hasEventType(Collection<EClass> event, String eventImplName) {
		//parsing fragment file and saving events information in the java object
		for (TreeIterator<EObject> iterator = CompareUtils
				.getAllContentsWithSpecificTypes(process, event, true); iterator
				.hasNext();) {
			
			EObject current = iterator.next();
			if (current instanceof FlowNode) {
				FlowNode node = (FlowNode) current;

				if(node.getClass().getSimpleName().matches(eventImplName))
				{
					 return true;
				}

			}
		}
		return false;
	}

	//TODO: remove the valid fragment concept
	//TODO: add a start-v
	/**
	 * A fragment is invalid if it has Start and incoming open connections or End Event and outgoing connections
	 * @param node
	 * @param eventImplName
	 * @return 
	 */
	private boolean checkIfValidFragment()
	{
		if(this.hasStartEvent && this.incomingConnections != 0)
		{
			return false;
		}
		return true;
	}

	public boolean getHasStartEvent() {
		return hasStartEvent;
	}


	public boolean getHasEndEvent() {
		return hasEndEvent;
	}

	public List<ConnectionPoint> getConnectionPoints() {
		return connectionPoints;
	}

	public void setConnectionPoints(List<ConnectionPoint> connectionPoints) {
		this.connectionPoints = connectionPoints;
	}

	public boolean getIsValidFragment()
	{
		return isValidFragment;
	}
	
	/**
	 * This function bases on the assumption that the fragment files are named after 
	 * path/fragment+UUID+.bpmn.
	 * by removing the path the 8 the position of the "fragment" and 5 the position of ".bpmn2". we take the string in the middle 
	 * @return  uuid of the fragment
	 */
	private String calculateId() {
		if(this.filePath.contains("fragment"))
		{
			String[] splittedFilepath = this.filePath.split("/");
			String filename = splittedFilepath[splittedFilepath.length -1];
	 		String uuid = filename.substring(8, filename.length() - 6);	
			return uuid;
		}
		return null;
	}
	

	public String getId()
	{
		return id;
	}

	/**
	 * Returns the list of activities/tasks after scanning them per type to find out the open connections
	 * @param Process
	 * @return 
	 * @throws IOException 
	 * @throws DroolsParserException 
	 */
	protected List<ConnectionPoint> scanActivities (Process prcs, String fid) throws DroolsParserException, IOException
	{
		for (TreeIterator<EObject> iterator = CompareUtils
				.getAllContentsWithSpecificTypes(process, activityTypes, true); iterator
				.hasNext();) {
			
			EObject curr = iterator.next();
			if (curr instanceof FlowNode) {
				FlowNode node = (FlowNode) curr;
				ConnectionPoint connP = new ConnectionPoint(fid, node.getClass().getSimpleName().toString(),  node);
				executeActivityRules(node, connP);
				if (connP.isValidConnectionPoint()) 
				{
					connectionPoints.add(connP);
				}
			}
		}
		return connectionPoints;
	}
	
	
	protected void executeActivityRules( FlowNode node, ConnectionPoint connP) throws DroolsParserException, IOException {
		 
		PackageBuilder packageBuilder = new PackageBuilder();
		
		//Convert rule file to InputStream
		InputStream resourceAsStream = new FileInputStream(Constants.activitiesRulebookPath);
		
		Reader reader = new InputStreamReader(resourceAsStream);
		packageBuilder.addPackageFromDrl(reader);
		org.drools.core.rule.Package rulesPackage = packageBuilder
				.getPackage();
		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(rulesPackage);
		//Create new WorkingMemory session for this RuleBase. By default the RuleBase retains a weak reference to returned WorkingMemory
		WorkingMemory workingMemory = ruleBase.newStatefulSession();

		//Insert and fire all rules until its empty
		workingMemory.insert(node);
		workingMemory.insert(connP);
		workingMemory.fireAllRules();
	}
	
	/**
	 * Return the list of gateways after scanning gateways etc in a way to find out if they require any additional connection
	 * @param Process
	 * @return 
	 * @throws IOException 
	 * @throws DroolsParserException 
	 */
	protected List<ConnectionPoint> scanGateways(Process prcs, String fid) throws DroolsParserException, IOException
	{
		for (TreeIterator<EObject> iterator = CompareUtils
				.getAllContentsWithSpecificTypes(process, gatewayTypes, true); iterator
				.hasNext();) {
			

			EObject current = iterator.next();
			if (current instanceof FlowNode) {
				FlowNode node = (FlowNode) current;
				ConnectionPoint connP  = new ConnectionPoint(fid, node.getClass().getSimpleName().toString(), node);
				executeGatewaysRules(node, connP);

				if (connP.isValidConnectionPoint()) 
				{
					connectionPoints.add(connP);
				}
			}
		}
		return connectionPoints;
	}
	
	/**
	 * Evaluates the open connections of each gateway 
	 * @param node 
	 * @param node
	 * @param obj
	 * @throws DroolsParserException
	 * @throws IOException
	 */
	protected void executeGatewaysRules(FlowNode node, ConnectionPoint connP) throws DroolsParserException, IOException {
		if(node.getIncoming().size()==1 && node.getOutgoing().size()>=2)
		{
			connP.setNeededIncoming(0);
			connP.setNeededOutgoing(0);
		}
		else if(node.getIncoming().size()>=2 && node.getOutgoing().size()==1)
		{
			connP.setNeededIncoming(0);
			connP.setNeededOutgoing(0);
		}
		else if(node.getIncoming().size()<1 && node.getOutgoing().size()>=2)
		{
			connP.setNeededIncoming(1);
			connP.setNeededOutgoing(0);
		}
		else if(node.getIncoming().size()>=2 && node.getOutgoing().size()<1)
		{
			connP.setNeededOutgoing(1);
			connP.setNeededIncoming(0);
		}
		else if(node.getIncoming().size()==1 && node.getOutgoing().size()<1)
		{
			connP.setNeededOutgoing(2);
			connP.setNeededIncoming(0);
		}
		else if(node.getIncoming().size()<1 && node.getOutgoing().size()==1)
		{
			connP.setNeededIncoming(2);
			connP.setNeededOutgoing(0);
		}
		else if(node.getIncoming().size()==1 && node.getOutgoing().size()==1)
		{
			//here we are reducing the probability to create an invalid fragment
			//and most probably increase connectivity
			if(this.hasStartEvent && !this.hasEndEvent)
			{
				connP.setNeededOutgoing(1);
				connP.setNeededIncoming(0);
			}
			else if(this.hasEndEvent && !this.hasStartEvent)
			{
				connP.setNeededIncoming(1);
				connP.setNeededOutgoing(0);

			}
			else 
			{
				//creates incoming or outgoing connection randomly
				//TODO: can it get smarter?
				Random random = new Random();
				if(random.nextBoolean()) connP.setNeededIncoming(1);
				else 
				{
					connP.setNeededOutgoing(1); 
					connP.setNeededIncoming(0);
				}
				
			}
		}
	}
	
	
	/**
	 * It reads each model in the collection and applies the rules to create the metadata
	 * @param fid 
	 * @return 
	 * @throws DroolsParserException
	 * @throws IOException
	 */
	protected List<ConnectionPoint> calculateFragmentMetadata(String fid) 
	{
		try{	
			//will do the procedures if the fragment is valid for the DB otherwise it is skipped
			this.connectionPoints = scanActivities(this.discoverProcesses().get(0), fid);
			this.connectionPoints = scanGateways(this.discoverProcesses().get(0), fid);
		
		}
		catch (DroolsParserException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return connectionPoints;
	}
	
	
	public int getIncomingConnections() {
		return incomingConnections;
	}

	public void fixFragmentsConnections() {
		for(ConnectionPoint connectionPoint : connectionPoints)
		{
			this.incomingConnections += connectionPoint.getNeededIncoming();
			this.outgoingConnections += connectionPoint.getNeededOutgoing();
		}
	}

	public int getOutgoingConnections() {
		return outgoingConnections;
	}


	public boolean hasNoFailMatched(FragmentExt fragment) {
		return this.failMatches.contains(fragment);
	
	}
	
	public void addCombinationFailMatches(List<FragmentExt> combination)
	{
		 this.failMatches.addAll(combination);
	}

	public List<ConnectionPoint> discoverConnectionPointsFromDB(String fid) {
		try {
			DBConnection connec = new DBConnection();
			String selectConnectionPoints = "Select * from connectionPoints where fid = \""
					+ fid + "\"";
			ResultSet resultConnectionPoints = connec
					.selectData(selectConnectionPoints);
			List<ConnectionPoint> connectionPoints = new ArrayList<ConnectionPoint>();

			while (resultConnectionPoints.next()) {
				String type = resultConnectionPoints.getString("type");
				int incoming = resultConnectionPoints.getInt("incoming");
				int outgoing = resultConnectionPoints.getInt("outgoing");
				String nodeId = resultConnectionPoints.getString("nodeId");
				FlowNode node = findFlowNodeInFragment(nodeId);
				if(node!= null)
				{
					ConnectionPoint cp = new ConnectionPoint(fid, type, incoming,
							outgoing, node);
					connectionPoints.add(cp);
				}
			}
			connec.closeConnection();
			return connectionPoints;
		} catch (SQLException e) {
			System.out
					.println("There was an error in Fragment - Checkpoint creation from DB");
			e.printStackTrace();
			return null;
		}
	}
	
	//FIXME: implement this
	public FlowNode findFlowNodeInFragment(String nodeId) {

		List<FlowElement> flowElements = this.getProcess().getFlowElements();
		for(FlowElement f : flowElements)
		{
			if(f instanceof FlowNode)
			{
				if(f.getId().equals(nodeId)) return (FlowNode) f;
			}
		}
		return null;
	}
	
	public Document getXMLDocumentOfFragment()
	{
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		try {
			return fact.newDocumentBuilder().parse(new File(this.getFilePath()));
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getNumberOfFlowNodes() {
		return numberOfFlowNodes;
	}

}
