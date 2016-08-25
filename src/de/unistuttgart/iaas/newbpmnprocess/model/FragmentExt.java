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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseFactory;
import org.drools.core.WorkingMemory;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.ChoreographyActivity;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
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
// TODO: this model instance inheritance can be lifted? I believe it is only
// used for the process modeling
//TODO: this should most probably be two classes of FragmentExt and FragmentExtMetadata
public class FragmentExt extends ModelInstance {

	public static Set<String> nonCoreElements = new HashSet<String>();
	public static Set<String> coreElements = new HashSet<String>();
	
	private boolean hasStartEvent = false;
	private boolean hasEndEvent = false;
	private String id;
	private int numberOfFlowNodes = 0;

	private List<ConnectionPoint> connectionPoints;
	private boolean isValidFragment;
	private int incomingConnections = 0;
	private int outgoingConnections = 0;

	// TODO: generalize these types to superclasses?
	private Collection<EClass> activityTypes = Arrays.asList(
			Bpmn2Package.Literals.ACTIVITY,
			Bpmn2Package.Literals.TASK);

	// TODO: generalize these types to superclasses?
	private Collection<EClass> gatewayTypes = Arrays.asList(
			Bpmn2Package.Literals.EXCLUSIVE_GATEWAY,
			Bpmn2Package.Literals.PARALLEL_GATEWAY);
	private List<FragmentExt> failMatches = new ArrayList<FragmentExt>();
	private int numberOfParallelGateways = 0;
	private int numberOfScriptTasks = 0;
	private int numberOfCallActivities = 0;
	private int numberOfExclusiveGateways = 0;
	private int numberOfServiceTasks = 0;
	private boolean hasNonCoreElements;

	/**
	 * Load fragment from file
	 * 
	 * @param modelFileURI
	 */
	public FragmentExt(String modelFileURI) {
		super(modelFileURI);
		this.filePath = modelFileURI;

		this.id = calculateId();
		if (id != null) {
			
			connectionPoints = new ArrayList<ConnectionPoint>();
			fixFragmentExtMetadata();

			this.connectionPoints = fixFragmentExtConnectionPoints(id);
			isValidFragment = checkIfValidFragment();
		}

	}

	/**
	 * For loading a fragment back from the db
	 * 
	 * @param fid
	 * @param modelFileURI
	 * @param hasStartEvent
	 * @param hasEndEvent
	 * @param connectionPoints
	 */
	public FragmentExt(String fid, String modelFileURI, boolean hasStartEvent,
			boolean hasEndEvent, int numberOfFlowNodes, int numberOfCallActivities, 
			int numberOfScriptTasks, int numberOfServiceTasks, int numberOfParallelGateways,int numberOfExclusiveGateways, boolean hasNonCoreElements ) {
		// FIXME: there should be a local folder of files with the fragments!!!
		super(modelFileURI);
		this.id = fid;
		this.filePath = modelFileURI;
		this.hasStartEvent = hasStartEvent;
		this.hasEndEvent = hasEndEvent;
		this.connectionPoints = discoverConnectionPointsFromDB(fid);
		this.numberOfFlowNodes = numberOfFlowNodes;
		this.numberOfCallActivities = numberOfCallActivities;
		this.numberOfScriptTasks = numberOfScriptTasks;
		this.numberOfServiceTasks = numberOfServiceTasks;
		this.numberOfParallelGateways = numberOfParallelGateways;
		this.numberOfExclusiveGateways = numberOfExclusiveGateways;
		this.hasNonCoreElements = hasNonCoreElements;
		isValidFragment = checkIfValidFragment();

	}

	// TODO: remove the valid fragment concept
	// TODO: add a start-v
	/**
	 * A fragment is invalid if it has Start and incoming open connections or
	 * End Event and outgoing connections
	 * 
	 * @param node
	 * @param eventImplName
	 * @return
	 */
	private boolean checkIfValidFragment() {
		if (this.hasStartEvent && this.incomingConnections != 0) {
			return false;
		}
		return true;
	}

	public boolean hasStartEvent() {
		return hasStartEvent;
	}

	public boolean hasEndEvent() {
		return hasEndEvent;
	}

	public List<ConnectionPoint> getConnectionPoints() {
		return connectionPoints;
	}

	public void addNewConnectionPointsToList(List<ConnectionPoint> connectionPoints) {
		this.connectionPoints.addAll(connectionPoints) ;
	}

	public boolean isValidFragment() {
		return isValidFragment;
	}

	/**
	 * This function bases on the assumption that the fragment files are named
	 * after path/fragment+UUID+.bpmn. by removing the path the 8 the position
	 * of the "fragment" and 5 the position of ".bpmn2". we take the string in
	 * the middle
	 * 
	 * @return uuid of the fragment
	 */
	private String calculateId() {
		if (this.filePath.contains("fragment")) {
			String[] splittedFilepath = this.filePath.split("/");
			String filename = splittedFilepath[splittedFilepath.length - 1];
			String uuid = filename.substring(8, filename.length() - 6);
			return uuid;
		}
		return null;
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns the list of activities/tasks after scanning them per type to find
	 * out the open connections
	 * 
	 * @param Process
	 * @return
	 * @throws IOException
	 * @throws DroolsParserException
	 */
	protected List<ConnectionPoint> scanActivities(Process prcs)
			throws DroolsParserException, IOException {
		List <ConnectionPoint> discoveredConnectionPoints = new ArrayList<ConnectionPoint>();
		
		for (TreeIterator<EObject> iterator = CompareUtils
				.getAllContentsWithSpecificTypes(process, activityTypes, true); iterator
				.hasNext();) {

			EObject curr = iterator.next();
			if (curr instanceof FlowNode) {
				FlowNode node = (FlowNode) curr;
				ConnectionPoint connP = new ConnectionPoint(id, node
						.getClass().getSimpleName().toString(), node);
				executeActivityRules(node, connP);
				if (connP.isValidConnectionPoint()) {
					discoveredConnectionPoints.add(connP);
				}
			}
		}
		return discoveredConnectionPoints;
	}

	protected void executeActivityRules(FlowNode node, ConnectionPoint connP)
			throws DroolsParserException, IOException {

		PackageBuilder packageBuilder = new PackageBuilder();

		// Convert rule file to InputStream
		InputStream resourceAsStream = new FileInputStream(
				Constants.activitiesRulebookPath);

		Reader reader = new InputStreamReader(resourceAsStream);
		packageBuilder.addPackageFromDrl(reader);
		org.drools.core.rule.Package rulesPackage = packageBuilder.getPackage();
		RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(rulesPackage);
		// Create new WorkingMemory session for this RuleBase. By default the
		// RuleBase retains a weak reference to returned WorkingMemory
		WorkingMemory workingMemory = ruleBase.newStatefulSession();

		// Insert and fire all rules until its empty
		workingMemory.insert(node);
		workingMemory.insert(connP);
		workingMemory.fireAllRules();
	}

	/**
	 * Return the list of gateways after scanning gateways etc in a way to find
	 * out if they require any additional connection
	 * 
	 * @param Process
	 * @return
	 * @throws IOException
	 * @throws DroolsParserException
	 */
	protected List<ConnectionPoint> scanGateways(Process prcs)
			throws DroolsParserException, IOException {
		List <ConnectionPoint> discoveredConnectionPoints = new ArrayList<ConnectionPoint>();

		for (TreeIterator<EObject> iterator = CompareUtils
				.getAllContentsWithSpecificTypes(process, gatewayTypes, true); iterator
				.hasNext();) {

			EObject current = iterator.next();
			if (current instanceof FlowNode) {
				FlowNode node = (FlowNode) current;
				ConnectionPoint connP = new ConnectionPoint(id, node
						.getClass().getSimpleName().toString(), node);
				executeGatewaysRules(node, connP);

				if (connP.isValidConnectionPoint()) {
					discoveredConnectionPoints.add(connP);
				}
			}
		}
		return discoveredConnectionPoints;
	}

	
	/**
	 * Evaluates the open connections of each gateway
	 * 
	 * @param node
	 * @param node
	 * @param obj
	 * @throws DroolsParserException
	 * @throws IOException
	 */
	protected void executeGatewaysRules(FlowNode node, ConnectionPoint connP)
			throws DroolsParserException, IOException {		
	
		//stable cases
		if (node.getIncoming().size() == 1 && node.getOutgoing().size() >= 2) {
			connP.setNeededConnections(0, 0, false);
		} else if (node.getIncoming().size() >= 2
				&& node.getOutgoing().size() == 1) {
			connP.setNeededConnections(0, 0, false);
		} else if (node.getIncoming().size() < 1
				&& node.getOutgoing().size() >= 2) {
			connP.setNeededConnections(1, 0, false);
		} else if (node.getIncoming().size() >= 2
				&& node.getOutgoing().size() == 0) {
			connP.setNeededConnections(0, 1, false);
		//Special and flexible cases
		//The flexible connection points will have always 2 incoming and 2 outgoing
		//The real number will be evaluated during the linking process
		} else if (node.getIncoming().size() == 1 && node.getOutgoing().size() == 0) {
				if(this.hasStartEvent)
				{
					connP.setNeededConnections(0, 2, false);				
				}
				else if(this.hasEndEvent)
				{
					connP.setNeededConnections(1, 1, false);		
				}
				else //then it can be used as flexi. Flexis will have in total 2 and 2 and they will be adjusted accordingly
				{
					connP.setNeededConnections(1, 2, false);
				}
		} else if (node.getIncoming().size() == 0 && node.getOutgoing().size() == 1) {
				if(this.hasStartEvent)
				{
					connP.setNeededConnections(1, 1, false);				
				}
				else if(this.hasEndEvent)
				{
					connP.setNeededConnections(2, 0, false);		
				}
				else //then it can be used as flexi
				{
					connP.setNeededConnections(2, 1, true);
				}
		} else if (node.getIncoming().size() == 1 && node.getOutgoing().size() == 1) {
				if(this.hasStartEvent)
				{
					connP.setNeededConnections(0, 1, false);				
				}
				else if(this.hasEndEvent)
				{
					connP.setNeededConnections(1, 0, false);		
				}
				else //then it can be used as flexi
				{
					connP.setNeededConnections(1, 1, true);
				}
		}
	}
	

	/**
	 * It reads each model in the collection and applies the rules to create the
	 * metadata
	 * 
	 * @param fid
	 * @return
	 * @throws DroolsParserException
	 * @throws IOException
	 */
	protected List<ConnectionPoint> fixFragmentExtConnectionPoints(String fid) {
		try {
			// will do the procedures if the fragment is valid for the DB
			// otherwise it is skipped
			List <ConnectionPoint> discoveredConnectionPoints = null;
			discoveredConnectionPoints = scanActivities(this.discoverProcesses().get(0));
			this.addNewConnectionPointsToList(discoveredConnectionPoints);
			discoveredConnectionPoints = scanGateways(this.discoverProcesses().get(0));
			this.addNewConnectionPointsToList(discoveredConnectionPoints);

		} catch (DroolsParserException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connectionPoints;
	}

	//INVESTIGATE: might not be the safest but at least it will be consistent?
	public int getIncomingConnections() {
		this.incomingConnections = 0;
		for (ConnectionPoint connectionPoint : connectionPoints) {
			this.incomingConnections += connectionPoint.getNeededIncoming();
		}
		return this.incomingConnections;
	}

	

	public int getOutgoingConnections() {
		this.outgoingConnections = 0;
		for (ConnectionPoint connectionPoint : connectionPoints) {
			this.outgoingConnections += connectionPoint.getNeededOutgoing();
		}
		return this.outgoingConnections;
	}

	public boolean hasNoFailMatched(FragmentExt fragment) {
		return this.failMatches.contains(fragment);

	}

	public void addCombinationFailMatches(List<FragmentExt> combination) {
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
				boolean isFlexible = resultConnectionPoints.getBoolean("isFlexible");
				FlowNode node = findFlowNodeInFragment(nodeId);
				if (node != null) {
					ConnectionPoint cp = new ConnectionPoint(fid, type,
							incoming, outgoing, node, isFlexible);
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

	public FlowNode findFlowNodeInFragment(String nodeId) {

		List<FlowElement> flowElements = this.getProcess().getFlowElements();
		for (FlowElement f : flowElements) {
			if (f instanceof FlowNode) {
				if (f.getId().equals(nodeId))
					return (FlowNode) f;
			}
		}
		return null;
	}

	public Document getXMLDocumentOfFragment() {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		try {
			return fact.newDocumentBuilder()
					.parse(new File(this.getFilePath()));
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

	public int getNumberOfParallelGateway() {
		return numberOfParallelGateways;
	}

	public int getNumberOfScriptTasks() {
		return numberOfScriptTasks;
	}

	public int getNumberOfCallActivity() {
		return numberOfCallActivities;
	}

	public int getNumberOfExclusiveGateways() {
		return numberOfExclusiveGateways;
	}

	public Object getNumberOfServiceTasks() {
		return numberOfServiceTasks;
	}
	

	private void fixFragmentExtMetadata() {
		for (org.eclipse.bpmn2.FlowElement f : this.getProcess()
				.getFlowElements()) {
			if (f instanceof FlowNode) {

				++numberOfFlowNodes;
				if (f instanceof SubProcess){
					hasNonCoreElements = true;
					nonCoreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if(f instanceof ExclusiveGateway){
					numberOfExclusiveGateways++;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if (f instanceof ParallelGateway){
					numberOfParallelGateways++;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if (f instanceof Task){
					numberOfScriptTasks++;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if (f instanceof Activity || f instanceof ChoreographyActivity){
					numberOfCallActivities++;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if (f instanceof StartEvent){
					hasStartEvent = true;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else if (f instanceof EndEvent){
					hasEndEvent = true;
					coreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				} else {
					hasNonCoreElements = true;
					nonCoreElements.add(((FlowNode)f).getClass().getSimpleName().toString());
				}
			}
		}
	}
	
	public boolean getHasNonCoreElements()
	{
		return this.hasNonCoreElements;
	}
	
	public List<ConnectionPoint> getConnectionPointsByType(String type)
	{
		List<ConnectionPoint> connectionPointByTypeList = new ArrayList<ConnectionPoint>();
		for(ConnectionPoint connP : this.connectionPoints)
		{
			if(connP.getType().equals(type))
			{
				connectionPointByTypeList.add(connP);
			}
		}
		return connectionPointByTypeList;
	}
}
