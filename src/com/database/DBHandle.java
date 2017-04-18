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

	// 定义一个私有的构造方法
	private DBHandle() {
		// TODO Auto-generated constructor stub
		connectionDB();
	}

	// 定义一个静态私有变量（不初始化，不使用final关键字，使用volatile保证了多线程访问时instance变量的可见性，避免了instance初始化时其他变量属性还没赋值完时，被另外线程调用）
	private static volatile DBHandle dbInstance;

	// 定义一个公有的静态方法，返回该类型实例
	public static DBHandle getDBInstance() {
		// 对象实例化时与否判断（不使用同步代码块，instance不等于null时，直接返回对象，运行效率高）
		if (dbInstance == null) {
			// 同步代码块（对象未初始化时，使用同步代码块，保证多线程访问时对象在第一次创建后，不再被重复创建）
			synchronized (DBHandle.class) {
				// 未初始化时，则初始化instance变量
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
			if (resultSet.next()) {// 检查数据表是否存在
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
