
import java.sql.*;

class Railway_MySQL {
	//private static String url="jdbc:mysql://127.0.0.1:3306/railway?useSSL=true"; 
	private static String url="jdbc:mysql://localhost:3306?useSSL=true"; //localhost 192.168.1.106 115.155.98.96 202.201.0.133
	private static String driver="com.mysql.jdbc.Driver";
	private static Connection conn;
	private static Statement comm;
	private static ResultSet rs;
	
	public static void regDriver() {
		try {
			Class.forName(driver).newInstance();
		} catch(Exception e) {
			System.out.println("Can't create driver...");
		}
	}
	
	public static void conBuild() {
		try {
			regDriver();
			conn=DriverManager.getConnection(url,"root","");//initialization
			conn.setAutoCommit(true);
			comm=conn.createStatement();
		} catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Can't create mysql Connection...");
		}
	}
	
	public static boolean ifexistDatabase(String database_name) throws SQLException {
		ResultSet rsDatabases =  Railway_MySQL.execQuery("SELECT * FROM information_schema.SCHEMATA where SCHEMA_NAME='"+database_name+"'");
		if (rsDatabases.next())
			return true;
		else
			return false;
	}
	
	public static boolean ifexistTable(String table_name) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rsTables = meta.getTables(null,null,table_name,null);
		if(rsTables.next())
		   return true;
		else
		   return false;
	}
	
	public static ResultSet execQuery(String stmt) {
		try {
			comm=conn.createStatement();
			rs=comm.executeQuery(stmt);
			return rs;
		} catch(Exception e) {
			System.out.println("Can't create statement");
			return null;
		}
	}
	
	public static void execUpdate(String UpdateString) {
		try {
			comm=conn.createStatement();
			comm.executeUpdate(UpdateString);
		} catch(Exception e) {
			e.getMessage();
		}
	}
	
	public static void changeUrl() {
		url=new String("jdbc:mysql://127.0.0.1:3306/railway?useSSL=true");
	}
	
	public static void closeDB() {
		try {
			comm.close();
			conn.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void createTableRailway() {
		String str=""+
				"CREATE TABLE STATION("+
				"station_id		INT			NOT NULL,"+
				"visit			INT			DEFAULT 0,"+
				"id				int			NOT NULL auto_increment,"+
				"PRIMARY KEY (id)"+
				") auto_increment=1";
		System.out.println(str);
		Railway_MySQL.execUpdate(str);
		
	}
}