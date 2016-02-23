package de.unistuttgart.iaas.newbpmnprocess.database;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBConnectionMiddleware {


	/**
	 * This will upload the fragment to the DB
	 * @param filepath
	 * @return
	 */
	//I think this is not used anymore
//	public int insertFragmentToDB(String filepath)
//	{
//		int id=0;
//		//here I will first evaluate the metadata
//		//hasstartEvent && hasEndEvent && incoming - outgoing connections
//		
//		
//				try
//				{
//					String sql = "Insert into fragments(filepath) values(?)";
//				
//					Object[] Values = {filepath};
//					DBConnection connec = new DBConnection();
//					ResultSet keys = connec.insertData_WithGeneratedKeys(sql, Values);
//				   
//					keys.next();  
//					id = keys.getInt(1);
//				}
//				
//				catch (Exception ex)
//				{
//					System.out.println(ex.getMessage());
//				}
//
//		return id;
//	}
//	
	/**
	 * It inserts the hasstart and hasend event metadata in the DB
	 * @param fid
	 * @param hasstart
	 * @param hasend
	 * @return the ID of the inserted fragment
	 */
	//TODO: all metadata can be inserted in one place
//	public int insertStartEndMetadata(String fid,boolean hasstart, boolean hasend)
//	{
//		int id=0;
//		
//		try
//		{
//			String sql="INSERT INTO metadata (fid,HasStartEvent,HasEndEvent) VALUES (?,?,?)";
//			
//			Object[] Values = {fid,hasstart,hasend};
//			DBConnection connec = new DBConnection();
//			ResultSet keys = connec.insertData_WithGeneratedKeys(sql, Values);
//        	   
//			keys.next();  
//			id = keys.getInt(1);
//		}
//		
//		catch (Exception ex)
//		{
//			System.out.println(ex.getMessage());
//		}
//		
//		return id;
//		
//	}
	
	
//FIXME: all functions using connreq are commented out because this field does not exist in the DB anymore
	public void insertTaskMetadata(int mid,String taskname,String componentid, int connrequired, String IncOut)
	{
		String sql="INSERT INTO taskmetadata (mid,taskname,componentid,connrequired,IncomingOutgoing) VALUES (?,?,?,?,?)";
			
		Object [] values = {mid,taskname,componentid,connrequired,IncOut};
		DBConnection connec= new DBConnection();
		connec.insertData(sql, values);
	}
	
	public void insertGatewayMetadata(int mid,String name,String componentid, int connrequired, String IncOut)
	{			
		String sql="INSERT INTO gatewaymetadata (mid,name,componentid,connrequired,IncomingOutgoing) VALUES (?,?,?,?,?)";
		
		Object [] values = {mid,name,componentid,connrequired,IncOut};
		DBConnection connec= new DBConnection();
		connec.insertData(sql, values);
	}


	public int computeTaskConnections(int f1,String IncOut)
	{
		int connreq =0;
		try
		{
			String sql="select sum(taskmetadata.connrequired) as num from taskmetadata join metadata on taskmetadata.mid=metadata.mid and metadata.fid='"+f1+"'" + "and taskmetadata.IncomingOutgoing='"+IncOut+"'";

			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(sql);
        	
            while (rs.next())
            {
				connreq = rs.getInt("num");
            }
		}		
	catch (Exception ex){
			System.out.println(ex.getMessage());
		}
		
		return connreq;
	}
	
	public int computeGatewayConnections(int f1,String IncOut)
	{
		int connreq =0;
		try
		{
			String sql="select sum(gatewaymetadata.connrequired) as num from gatewaymetadata join metadata on gatewaymetadata.mid=metadata.mid and metadata.fid='"+f1+"' "+ "and gatewaymetadata.IncomingOutgoing='"+IncOut+"'";
			
			DBConnection connec = new DBConnection();
			ResultSet rs = connec.selectData(sql);
        	
            while (rs.next())
            {
               connreq = rs.getInt("num");
            }
		}		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return connreq;
	}
	
	/**
	 * Queries the DB for a fragment that is compatible with the criteria given as an input
	 * @param criteria
	 * @return the ID of one fragment that is compatible with the given criteria
	 */
	//TODO: needs to be rechecked
/*	public int getFragmentID(CriteriaEntity criteria)
	{
		List<Integer> TotalTasksList = new ArrayList<Integer>();
		List<Integer> TaskList = new ArrayList<Integer>();
		List<Integer> ServiceTaskList = new ArrayList<Integer>();
		List<Integer> CallActivityList = new ArrayList<Integer>();
		List<Integer> ExclGatewayList = new ArrayList<Integer>();
		List<Integer> ParalGatewayList = new ArrayList<Integer>();
		
		if(criteria.getActDetails()==false)
		{
			try
			{
				String TotalTasksSql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.taskmetadata on FragmentsRepository.metadata.mid = FragmentsRepository.taskmetadata.mid AND FragmentsRepository.taskmetadata.taskname IN (\"scriptTaskImpl\",\"ServiceTaskImpl\",\"CallActivityImpl\") group by FragmentsRepository.metadata.mid having count(FragmentsRepository.taskmetadata.taskname)='"+criteria.getTotalTasks()+"'";
				
				DBConnection connec = new DBConnection();
	            ResultSet rs = connec.selectData(TotalTasksSql);
	        	
	            while (rs.next())
	            {
	               TotalTasksList.add(rs.getInt("ID"));
	            }
			}
			catch (Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}
		
		else
		{
		
		try
		{
			String TaskSql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.taskmetadata on FragmentsRepository.metadata.mid = FragmentsRepository.taskmetadata.mid AND FragmentsRepository.taskmetadata.taskname=\"scriptTaskImpl\" group by FragmentsRepository.metadata.mid having count(FragmentsRepository.taskmetadata.taskname)='"+criteria.getTask()+"'";
			
			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(TaskSql);
        	
            while (rs.next())
            {
               TaskList.add(rs.getInt("ID"));
            }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		try
		{
			String ServiceTaskSql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.taskmetadata on FragmentsRepository.metadata.mid = FragmentsRepository.taskmetadata.mid AND FragmentsRepository.taskmetadata.taskname=\"ServiceTaskImpl\" group by FragmentsRepository.metadata.mid having count(FragmentsRepository.taskmetadata.taskname)='"+criteria.getServiceTask()+"'";
			
			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(ServiceTaskSql);
        	
            while (rs.next())
            {
               ServiceTaskList.add(rs.getInt("ID"));
            }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		try
		{
			String CallActivitySql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.taskmetadata on FragmentsRepository.metadata.mid = FragmentsRepository.taskmetadata.mid AND FragmentsRepository.taskmetadata.taskname=\"CallActivityImpl\" group by FragmentsRepository.metadata.mid having count(FragmentsRepository.taskmetadata.taskname)='"+criteria.getCallActivity()+"'";
			
			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(CallActivitySql);
        	
            while (rs.next())
            {
               CallActivityList.add(rs.getInt("ID"));
            }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		}
		
		try
		{
			String ExclGatewaySql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.gatewaymetadata on FragmentsRepository.metadata.mid = FragmentsRepository.gatewaymetadata.mid AND FragmentsRepository.gatewaymetadata.name=\"ExclusiveGatewayImpl\" group by FragmentsRepository.metadata.mid having count(FragmentsRepository.gatewaymetadata.name)='"+criteria.getExclGateway()+"'";
			
			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(ExclGatewaySql);
        	
            while (rs.next())
            {
               ExclGatewayList.add(rs.getInt("ID"));
            }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		try
		{
			String ParalGatewaySql="select FragmentsRepository.metadata.fid as ID from FragmentsRepository.metadata LEFT JOIN FragmentsRepository.gatewaymetadata on FragmentsRepository.metadata.mid = FragmentsRepository.gatewaymetadata.mid AND FragmentsRepository.gatewaymetadata.name=\"ParallelGatewayImpl\" group by FragmentsRepository.metadata.mid having count(FragmentsRepository.gatewaymetadata.name)='"+criteria.getParalGateway()+"'";
			
			DBConnection connec = new DBConnection();
            ResultSet rs = connec.selectData(ParalGatewaySql);
        	
            while (rs.next())
            {
               ParalGatewayList.add(rs.getInt("ID"));
            }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		if(criteria.getActDetails()==false)
		{
			if(TotalTasksList.size()>0 && ExclGatewayList.size()>0 && ParalGatewayList.size()>0)
			{
				TotalTasksList.retainAll(ExclGatewayList);
				TotalTasksList.retainAll(ParalGatewayList);
				
				if(TotalTasksList.size()>0)
				{
					System.out.println(TotalTasksList.get(0));
					return TotalTasksList.get(0);
				}
				
				else
				{
					return 0;
				}
			}
			
			else
			{
				return 0;
			}
		}
		
		else
		{
		
		if(TaskList.size()>0 && ServiceTaskList.size()>0 && CallActivityList.size()>0 && ExclGatewayList.size()>0 && ParalGatewayList.size()>0)
		{
			TaskList.retainAll(ServiceTaskList);
			TaskList.retainAll(CallActivityList);
			TaskList.retainAll(ExclGatewayList);
			TaskList.retainAll(ParalGatewayList);

			if(TaskList.size()>0)
			{
				Random ran = new Random();
				int ind = ran.nextInt(TaskList.size());
				System.out.println(TaskList.get(ind));
				return TaskList.get(ind);
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return 0;
		}
		
		}

	}
*/
	//FIXME: this is how it loads the file again
	//FIXME: it reads back and forth from the 
//	public FragmentModel loadFragment(int f1)
//	{
//		File bpmnfile=null;
//		try
//		{
//			String sql="select file from fragments where fid='"+f1+"'";
//			
//			DBConnection connec = new DBConnection();
//		    ResultSet rs = connec.selectData(sql);
//		    while (rs.next()) {
//		      bpmnfile = new File("../temp.bpmn2");
//		      FileOutputStream fos = new FileOutputStream(bpmnfile);
//
//		      byte[] buffer = new byte[1];
//		      InputStream is = rs.getBinaryStream("file");
//		      while (is.read(buffer) > 0) {
//		        fos.write(buffer);
//		      }
//		      fos.close();
//		    }
//		}
//		catch (Exception ex)
//		{
//			System.out.println(ex.getMessage());
//		}
//		
//		FragmentModel model = new FragmentModel(bpmnfile.getAbsolutePath());
//		return model;
//	}
	
public List<String> getConnReqIds(int f1,String IncOut)
	{
		List<String> ids = new ArrayList<String>();
		try
		{
			String sql="select tm.componentid as id, tm.connrequired as connreq from metadata m join taskmetadata tm on m.mid=tm.mid and m.fid='"+f1+"'" + "and tm.IncomingOutgoing='"+IncOut+"'" + "UNION select gm.componentid as id, gm.connrequired as connreq from metadata m join gatewaymetadata gm on m.mid=gm.mid and m.fid='"+f1+"'" + "and gm.IncomingOutgoing='"+IncOut+"'";
		
			DBConnection connec = new DBConnection();
		    ResultSet rs = connec.selectData(sql);
		    while (rs.next()) {
		      
		    	for (int i=0;i<rs.getInt("connreq");i++)
		    	{
		    		ids.add(rs.getString("id"));
		    	}
	    	
		    }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return ids;
	}
	
	public boolean hasEvent(int fid,String EventName)
	{
		String sql=null;
		boolean ans=false;
		try
		{
			if(EventName.matches("start"))
				sql="select HasStartEvent as val from metadata where fid='"+fid+"'";
			else if(EventName.matches("end"))
				sql="select HasEndEvent as val from metadata where fid='"+fid+"'";
			
			DBConnection connec = new DBConnection();
		    ResultSet rs = connec.selectData(sql);
		    while (rs.next()) {
		      
		    	ans=rs.getBoolean("val");
		    	
		    }
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return ans;
	}	
	
	/**
	 * Will insert a newly calculated fragment to DB
	 * @author skourama
	 * 
	 */
	public void inserNewFragmentToDB()
	{
		//DBConnection connec.insertData(sql, values);
		//conn1.insertFragmentToDB(modelFilename.getAbsolutePath())
	}
	
}
