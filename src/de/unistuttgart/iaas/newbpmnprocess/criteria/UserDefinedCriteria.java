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
	// TODO: remove the following - it is not needed any more
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
				// TODO: remove the following it is not needed anymore
				// //System.out.println(criteria.getActDetails()+","+criteria.getTotalTasks()+","+criteria.getTask()+","+criteria.getServiceTask()+","+criteria.getCallActivity()+","+criteria.getExclGateway()+","+criteria.getParalGateway()+";");
				//
				// int FragmentID=0;
				// FragmentID=getFragmentID(criterio);
				// if(FragmentID!=0)
				// {
				// FragmentsList.add(FragmentID);
				// }
				criteriaList.add(criterio);
			}
			//computeSize(doc);
//			processEngine = doc
//					.getElementsByTagName(Constants.CriteriaProcEngineElement)
//					.item(0).getTextContent();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	public List<List<FragmentExt>> getSelectedFragments(List<Integer> sizesCombination) {
		DBConnection connec = new DBConnection();
		int crCnt = 0;
		ResultSet resultFragments;
		List<FragmentExt> tmp;
		
		
		for (Criterio cr : criteriaList) {
			
			String selectFragmentSql = makeFragmentSelectionQuery(cr, crCnt,
					criteriaList.size(), sizesCombination.get(crCnt).intValue());
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
		
		FragmentExt fragment = new FragmentExt(fid, filepath, hasStartEvent,
				hasEndEvent, numberOfFlowNodes); 
		
		return fragment;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 
	 * SELECT distinct t0.fid FROM (SELECT fid, type, count(type) as counter
	 * FROM `connectingPoint` where 1 group by fid, type) as t0 inner join
	 * (SELECT fid, type, count(type) as counter FROM `connectingPoint` where 1
	 * group by fid, type) as t1 on (t0.fid = t1.fid) inner join (SELECT fid,
	 * type, count(type) as counter FROM `connectingPoint` where 1 group by fid,
	 * type) as t2 on (t1.fid = t2.fid) where t0.type = 'ExclusiveGatewayImpl'
	 * AND t0.counter = 1 AND t1.type = 'CallActivityImpl' AND t1.counter = 1
	 * AND t2.type = 'ScriptTaskImpl' AND t2.counter = "1"
	 * @param integer 
	 * 
	 * @param crCnt
	 * 
	 */
	// TODO: fix for the nonDetailed Query
	// TODO: this needs to be more flexible for other types
	private String makeFragmentSelectionQuery(Criterio cr, int fragmentPos,
			int totalSize, int size) {

		String query = 
				"select * from fragments f where f.fid in  ("
				+ " "
				+ "SELECT distinct t0.fid"
				+ " "
				+ "FROM ConnectionPointsStats as t0"
				+ " "
				+ "inner join ConnectionPointsStats as t1 on (t0.fid = t1.fid)"
				+ " "
				+ "inner join ConnectionPointsStats as t2 on (t1.fid = t2.fid)"
				+ " "
				+ "inner join ConnectionPointsStats as t3 on (t2.fid = t3.fid)"
				+ " "
				+ "inner join ConnectionPointsStats as t4 on (t3.fid = t4.fid)"		
				
//				"select * from fragments f where f.fid in  ("
//				+ "SELECT distinct t0.fid"
//				+ " "
//				+ "FROM (SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type) as t0"
//				+ " "
//				+ "inner join (SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type) as t1 on (t0.fid = t1.fid)"
//				+ " "
//				+ "inner join (SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type) as t2 on (t1.fid = t2.fid)"
//				+ " "
//				+ "inner join (SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type) as t3 on (t2.fid = t3.fid)"
//				+ " "
//				+ "inner join (SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type) as t4 on (t3.fid = t4.fid)"

				+ " " + "where" + " ";

		int tableCnt = 0;
		String queryEnd = "); ";
		String andStr = " AND ";

		String scriptTaskQuery = "\'ScriptTaskImpl\'";
		String serviceTaskQuery = "\'ServiceTaskImpl\'";
		String callActivityQuery = "\'CallActivityImpl\' ";

		String exclGatewayQuery = "\'ExclusiveGatewayImpl\' ";
		String parallelGatewayQuery = "\'ParallelGatewayImpl\'";

		// String parallelGatewayQuery =
		// "t4.type = \'ParallelGatewayImpl\'AND 	t4.counter = " +
		// cr.getParalGateway() ;

		if (cr.getTask() != 0) {
			query += "t" + tableCnt + ".type = " + scriptTaskQuery + andStr
					+ "t" + tableCnt + ".counter = " + cr.getTask();
			tableCnt++;
		}
		if (cr.getServiceTask() != 0) {
			if (tableCnt != 0)
				query += andStr;
			query += "t" + tableCnt + ".type = " + serviceTaskQuery + andStr
					+ "t" + tableCnt + ".counter = " + cr.getServiceTask();
			tableCnt++;
		}
		if (cr.getCallActivity() != 0) {

			if (tableCnt != 0)
				query += andStr;
			query += "t" + tableCnt + ".type = " + callActivityQuery + andStr
					+ "t" + tableCnt + ".counter = " + cr.getCallActivity();
			tableCnt++;
		}
		if (cr.getExclGateway() != 0) {

			if (tableCnt != 0)
				query += andStr;
			query += "t" + tableCnt + ".type = " + exclGatewayQuery + andStr
					+ "t" + tableCnt + ".counter = " + cr.getExclGateway();
			tableCnt++;
		}
		if (cr.getParalGateway() != 0) {

			if (tableCnt != 0)
				query += andStr;
			query += "t" + tableCnt + ".type = " + parallelGatewayQuery
					+ andStr + "t" + tableCnt + ".counter = "
					+ cr.getParalGateway();
			tableCnt++;
		}

		if (fragmentPos == 0) {
			query += andStr;
			query += "f.hasStartEvent = 1 ";
			query += andStr;
			query += " f.hasEndEvent = 0 ";
		} else if (fragmentPos == totalSize - 1) {
			query += andStr;
			query += " f.hasStartEvent = 0 ";
			// query += andStr; //this is omitted because we are not having many
			// elements with endEvent and the selection fails
			// query += " f.hasEndEvent = 1 ";
		} else {
			query += andStr;
			query += "f.hasStartEvent = 0 AND f.hasEndEvent = 0 ";

		}
		query += "AND f.numberOfFlowNodes = " + size;
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
