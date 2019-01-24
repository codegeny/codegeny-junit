package org.codegeny.junit.database;

/*-
 * #%L
 * A collection of JUnit rules
 * %%
 * Copyright (C) 2016 - 2019 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
