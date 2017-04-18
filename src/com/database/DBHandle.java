package com.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBHandle {
	private static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://localhost:3306/";
	private static String dbName = "test";
	private static String character = "?useUnicode=true&characterEncodeing=UTF8&useSSL=false";
	private static String userName = "root";
	private static String passWord = "";

	private static java.sql.Connection connection;
	private static java.sql.Statement statement;

	private ResultSet resultSet = null;

	// ����һ��˽�еĹ��췽��
	private DBHandle() {
		// TODO Auto-generated constructor stub
		connectionDB();
	}

	// ����һ����̬˽�б���������ʼ������ʹ��final�ؼ��֣�ʹ��volatile��֤�˶��̷߳���ʱinstance�����Ŀɼ��ԣ�������instance��ʼ��ʱ�����������Ի�û��ֵ��ʱ���������̵߳��ã�
	private static volatile DBHandle dbInstance;

	// ����һ�����еľ�̬���������ظ�����ʵ��
	public static DBHandle getDBInstance() {
		// ����ʵ����ʱ����жϣ���ʹ��ͬ������飬instance������nullʱ��ֱ�ӷ��ض�������Ч�ʸߣ�
		if (dbInstance == null) {
			// ͬ������飨����δ��ʼ��ʱ��ʹ��ͬ������飬��֤���̷߳���ʱ�����ڵ�һ�δ����󣬲��ٱ��ظ�������
			synchronized (DBHandle.class) {
				// δ��ʼ��ʱ�����ʼ��instance����
				if (dbInstance == null) {
					dbInstance = new DBHandle();
				}
			}
		}
		return dbInstance;
	}

	private void connectionDB() {

		try {
			Class.forName(driver).newInstance();
			connection = DriverManager.getConnection(url + dbName + character,
					userName, passWord);
			if (connection != null) {
				System.out.println("Succeeded connection to MySql");
			}
			statement = connection.createStatement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isTableExist(String tableName) {
		try {
			resultSet = connection.getMetaData().getTables(null, null,
					tableName, null);
			if (resultSet.next()) {// ������ݱ��Ƿ����
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void createTable(String tableName) {

		if (!isTableExist(tableName)) {
			try {
				// System.out
				// .println("create table "
				// + tableName
				// +
				// " (num smallint, times timestamp not null, tem1 smallint, tem2 smallint, tem3 smallint, tem4 smallint, vis smallint, hum smallint, primary key(times, num))");
				statement
						.executeUpdate("create table "
								+ tableName
								+ " (num smallint, times timestamp not null, tem1 smallint, tem2 smallint, tem3 smallint, tem4 smallint, vis smallint, hum smallint, primary key(times, num))");
				// System.out.println("create table ok!");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void closeDB() {

		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
				System.out.println("Database connection terminated!");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ResultSet selectDB(String tableName, String startTime, String endTime) {
		try {
			System.out.println("select * from " + tableName
					+ " where times >= " + startTime + " and times <= "
					+ endTime + ";");
			resultSet = statement.executeQuery("select * from " + tableName
					+ " where times >= " + startTime + " and times <= "
					+ endTime + ";");
			// System.out.println("select ok!");
			return resultSet;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	public ResultSet selectDB(String tableName) {
		try {
			resultSet = statement
					.executeQuery("select * from (select * from "
							+ tableName
							+ " order by times desc, num desc limit 0,20) as temTable order by times asc, num asc"
							+ ";");
			// System.out.println("select ok!");
			return resultSet;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return null;
		}
	}

	public void insertDB(String tableName, int num, String times, int tem1,
			int tem2, int tem3, int tem4, int vis, int hum) {
		try {
			statement.executeUpdate("insert into " + tableName
					+ " (num, times, tem1, tem2, tem3, tem4, vis, hum) values("
					+ num + "," + times + "," + tem1 + "," + tem2 + "," + tem3
					+ "," + tem4 + "," + vis + "," + hum + ");");
			// System.out.println("insert ok!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
