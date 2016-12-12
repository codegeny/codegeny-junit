package org.codegeny.junit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.database.DatabaseConnection;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class SimpleConnectionDBUnitRuleTest {
	
	@BeforeClass
	public static void createTables() throws SQLException {
		try (Connection connection = getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("drop table if exists president");
				statement.execute("create table president (id int, name varchar2)");
			}
		}
	}
	
	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
	}
	
	@Rule
	public final TestRule dbUnit = new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseConnection(getConnection()));
	
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	public void doSomething() throws SQLException {
		try (Connection connection = getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("insert into president (id, name) values (3, 'Abraham Lincoln')");
				statement.execute("delete from president where id = 2");
			}
		}
	}
}