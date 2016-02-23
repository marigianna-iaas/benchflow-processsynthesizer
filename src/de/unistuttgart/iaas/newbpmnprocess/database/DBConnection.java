package de.unistuttgart.iaas.newbpmnprocess.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.unistuttgart.iaas.newbpmnprocess.utils.Constants;

public class DBConnection {
	
	Constants ConstObj = new Constants();
	Connection conn = null;

	public Connection setupConnection()
	{
		
		try
		{
			Class.forName(Constants.JdbcDriverName).newInstance();
			conn = DriverManager.getConnection(Constants.ConnectionUrl, Constants.ConnectionUser, Constants.ConnectionPassword);
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			try {
				wait(300000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return conn;
	}
	
	public void insertData(String sql, Object[] values)
	{

		try
		{
			conn = setupConnection();
			PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			
			for (int i=0;i<values.length;i++)
			{
				statement.setObject(i+1, values[i]);
			}
			
			statement.executeUpdate();
			conn.close();
		}
		
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}

	}

	public ResultSet selectData(String sql)
	{
		ResultSet rst = null;
		try
		{
			Connection conn = setupConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			rst = stmt.executeQuery();
		}
		
		catch (Throwable ex)
		{
			System.out.println(ex.getMessage());
		}
		
		return rst;
	}

//using executeUpdate for delete inserts etc
//	public ResultSet selectData2(String sql)
//	{
//		ResultSet rst = null;
//		try
//		{
//			//Connection conn = setupConnection();
//			//PreparedStatement stmt = conn.prepareStatement(sql);
//			//rst = stmt.executeQuery();
//			
//			Connection conn = setupConnection();
//			Statement stmt = conn.createStatement();
//			stmt.executeUpdate("DELETE FROM ConnectionPointsStats WHERE 1");
//			stmt.executeUpdate(
//					"INSERT INTO ConnectionPointsStats (fid, type, counter) SELECT fid, type, count(type) as counter FROM `connectionPoints` where 1 group by fid, type ");		
//			rst = stmt.executeQuery(sql);
//		}
//		
//		catch (Throwable ex)
//		{
//			ex.printStackTrace();
//			System.err.println(ex.getMessage());
//		}
//		
//		return rst;
//	}
	
	
	public void closeConnection()
	{
		if(conn != null )
		{
			try {
				if(!conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				System.out.println("problem in closing");
				
				e.printStackTrace();
			}
		}
	}
	
}
