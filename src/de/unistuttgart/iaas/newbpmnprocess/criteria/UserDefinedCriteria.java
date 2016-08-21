package de.unistuttgart.iaas.newbpmnprocess.criteria;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.unistuttgart.iaas.newbpmnprocess.database.DBConnection;
import de.unistuttgart.iaas.newbpmnprocess.model.FragmentExt;
import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

public class UserDefinedCriteria {
	private final String m_path;
	private List<List<FragmentExt>> selectedFragments;
	private List<Criterio> criteriaList;
	

	public UserDefinedCriteria(String path) {
		m_path = path;
		criteriaList = new ArrayList<Criterio>();
		selectedFragments = new ArrayList<List<FragmentExt>>();
	}

	/**
	 * Takes the criteria file and reads all the criteria. It creates Criterio
	 * Objects with respect to the input
	 */
	// FIXME: the way criteria are read seems extremely static. Improve the
	// criteria input
	public void readUserDefinedCriteria() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.parse(m_path);
			for (int i = 0; i < doc.getElementsByTagName(
					Constants.CriteriaFragmentElement).getLength(); i++) {
				Node fragment = doc.getElementsByTagName(
						Constants.CriteriaFragmentElement).item(i);
				Criterio criterio = new Criterio();
				Element eElement = (Element) fragment;
				if (eElement.getElementsByTagName(
						Constants.CriteriaActivitiesElement).getLength() < 1) {
					criterio.setActDetails(true);
					criterio.setTask(Integer.parseInt(eElement
							.getElementsByTagName(
									Constants.CriteriaScrTaskElement).item(0)
							.getTextContent()));
					criterio.setServiceTask(Integer.parseInt(eElement
							.getElementsByTagName(
									Constants.CriteriaSerTaskElement).item(0)
							.getTextContent()));
					criterio.setCallActivity(Integer.parseInt(eElement
							.getElementsByTagName(
									Constants.CriteriaCallActElement).item(0)
							.getTextContent()));

				} else {
					criterio.setActDetails(false);
					criterio.setTotalTasks(Integer.parseInt(eElement
							.getElementsByTagName(
									Constants.CriteriaActivitiesElement)
							.item(0).getTextContent()));
				}

				criterio.setExclGateway(Integer.parseInt(eElement
						.getElementsByTagName(
								Constants.CriteriaExclGatewayElement).item(0)
						.getTextContent()));
				criterio.setParalGateway(Integer.parseInt(eElement
						.getElementsByTagName(
								Constants.CriteriaParalGatewayElement).item(0)
						.getTextContent()));
				criteriaList.add(criterio);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The following will return me how many connection points per type in a
	 * fragment
	 * 
	 * SELECT fid, count(fid) as totalElementsPerType FROM `connectingPoint`
	 * WHERE type="CallActivityImpl" group by (fid) ================= The
	 * following will return me the fids of the fragments that have the specific
	 * number of elements of the query type
	 * 
	 * SELECT fid from (SELECT fid, count(fid) as totalElementsPerType FROM
	 * `connectingPoint` WHERE type="CallActivityImpl" group by (fid)) as a
	 * where a.totalElementsPerType = 2
	 * 
	 * ==== I have to dynamically create this query for each criterio
	 * @param sizesCombination 
	 */

	public List<List<FragmentExt>> getSelectedFragments() {
		DBConnection connec = new DBConnection();
		int crCnt = 0;
		ResultSet resultFragments;
		List<FragmentExt> tmp;
		
		
		for (Criterio cr : criteriaList) {
			
			String selectFragmentSql = makeFragmentSelectionQuery(cr, crCnt,
					criteriaList.size());
			// selects all fragments that comply with specific criteria
			resultFragments = connec.selectData(selectFragmentSql);
			tmp = new ArrayList<FragmentExt>();
			try {
				// if(resultFragments != null) resultFragments.beforeFirst();
				while (resultFragments.next()) {
					FragmentExt selectedFragment = createFragment(resultFragments);
					tmp.add(selectedFragment);
				}
				if (!tmp.isEmpty())
					selectedFragments.add(tmp);

			} catch (SQLException e) {
				e.printStackTrace();
			}

			crCnt++;
		}
		connec.closeConnection();

		return selectedFragments;

	}

	public FragmentExt createFragment(ResultSet resultFragment) {

		String fid;
		try {
			fid = resultFragment.getString("fid");
		

		String filepath = resultFragment.getString("filepath");
		boolean hasStartEvent = resultFragment.getBoolean("hasStartEvent");
		boolean hasEndEvent = resultFragment.getBoolean("hasEndEvent");
		int numberOfFlowNodes = resultFragment.getInt("numberOfFlowNodes");
		int numberOfCallActivities = resultFragment.getInt("numberOfCallActivity");
		int numberOfScriptTasks = resultFragment.getInt("numberOfScriptTasks");
		int numberOfServiceTasks = resultFragment.getInt("numberOfServiceTasks");
		int numberOfParallelGateways = resultFragment.getInt("numberOfParallelGateways");
		int numberOfExclusiveGateways = resultFragment.getInt("numberOfExclusiveGateways");

		FragmentExt fragment = new FragmentExt(fid, filepath, hasStartEvent,
				hasEndEvent, numberOfFlowNodes,   numberOfCallActivities, 
				 numberOfScriptTasks,  numberOfServiceTasks,  numberOfParallelGateways, numberOfExclusiveGateways ); 
		
		return fragment;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	private String makeFragmentSelectionQuery(Criterio cr, int fragmentPos,
			int totalSize) {

		String query = 
				"select * from fragments where" + " ";

		int tableCnt = 0;
		String andStr = " AND ";
		String queryEnd = "; ";

		String scriptTaskQuery = "numberOfScriptTasks";
		String serviceTaskQuery = "numberOfServiceTasks";
		String callActivityQuery = "numberOfCallActivity";

		String exclGatewayQuery = "numberOfExclusiveGateways";
		String parallelGatewayQuery = "numberOfParallelGateways";
		//FIXME: make the following a function!!
		
	//	if (cr.getTask() != 0) {
			query +=  scriptTaskQuery + " = " + cr.getTask();
			tableCnt++;
	//	}
	//	if (cr.getServiceTask() != 0) {
			if (tableCnt != 0)
				query += andStr;
			query +=  serviceTaskQuery + " = " + cr.getServiceTask();
			tableCnt++;
	//	}
	//	if (cr.getCallActivity() != 0) {

			if (tableCnt != 0)
				query += andStr;
			query +=  callActivityQuery + " = " + cr.getCallActivity();
		
			tableCnt++;
	//	}
	//	if (cr.getExclGateway() != 0) {

			if (tableCnt != 0)
				query += andStr;
			query +=  exclGatewayQuery + " = " + cr.getExclGateway();
			tableCnt++;
		//}
	//	if (cr.getParalGateway() != 0) {

			if (tableCnt != 0)
				query += andStr;
				query +=  parallelGatewayQuery + " = " + cr.getParalGateway();
				tableCnt++;
	//	}
//
		if (fragmentPos == 0) {
			query += andStr;
			query += "hasStartEvent = 1 "; //fixme: will have to add a start event if no existent
			query += andStr;
			query += " hasEndEvent = 0 ";
		} else if (fragmentPos == totalSize - 1) {
			query += andStr;
			query += "hasStartEvent = 0 ";
			// query += andStr; //this is omitted because we are not having many
			// elements with endEvent and the selection fails
			// query += " f.hasEndEvent = 1 ";
		} else {
			query += andStr;
			query += "hasStartEvent = 0 AND hasEndEvent = 0 ";
		}
	//	query += "AND numberOfFlowNodes >= " + size;
		query += queryEnd;
		return query;
	}

	/**
	 * Sets the DB connection to query a FragmentID with respect to criteria
	 * 
	 * @param criteria
	 * @return
	 */
	// private int getFragmentID(CriteriaEntity criteria)
	// {
	// DBConnectionMiddleware DBMidObj = new DBConnectionMiddleware();
	// return DBMidObj.getFragmentID(criteria);
	// }

	// TODO: remove this is not necessary
	// public List<Integer> getFragmentsList()
	// {
	// return this.FragmentsList;
	// }

//	private void computeSize(Document doc) {
//		Element size = (Element) doc.getElementsByTagName(
//				Constants.CriteriaCfcElement).item(0);
//		sizeMin = Integer.parseInt(size
//				.getAttribute(Constants.CriteriaCfcMinAttribute));
//		sizeMax = Integer.parseInt(size
//				.getAttribute(Constants.CriteriaCfcMaxAttribute));
//	}

//	public String getProcessEngine() {
//		return processEngine;
//	}
	
	
	public int getNoOfCriteria()
	{
		return criteriaList.size();
	}

	public void dumpSelectedFragments ()
	{
		this.selectedFragments= new ArrayList<List<FragmentExt>>();
	}
	
	public void dumpCriteriaList ()
	{
		this.criteriaList= new ArrayList<Criterio>();
	}
	
	
}
