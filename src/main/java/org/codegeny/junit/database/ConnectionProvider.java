package org.codegeny.junit.database;

import java.sql.Connection;
import java.sql.DriverManager;

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

import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;

@FunctionalInterface
public interface ConnectionProvider {
	
	IDatabaseConnection getConnection(String name) throws Exception;
	
	static ConnectionProvider forDataSource(DataSource dataSource) {
		return name -> new DatabaseDataSourceConnection(dataSource);
	}
	
	static ConnectionProvider forConnection(String url) {
		return name -> new DatabaseConnection(DriverManager.getConnection(url));
	}
	
	static ConnectionProvider forConnection(String url, String user, String pass) {
		return name -> new DatabaseConnection(DriverManager.getConnection(url, user, pass));
	}
	
	static ConnectionProvider forConnection(String url, Properties properties) {
		return name -> new DatabaseConnection(DriverManager.getConnection(url, properties));
	}
	
	static ConnectionProvider forConnection(Connection connection) {
		return name -> new DatabaseConnection(connection);
	}
	
	static ConnectionProvider forConnection(IDatabaseConnection connection) {
		return name -> connection;
	}
	
	static ConnectionProvider fromMap(Map<String, IDatabaseConnection> map) {
		return map::get;
	}
	
	static ConnectionProvider jndi() {
		return jndi(new Properties());
	}

	static ConnectionProvider jndi(Properties properties) {
		return name -> {
			try {
				Context context = new InitialContext(properties);
				try {
					return ConnectionConverter.convert(context.lookup(name));
				} finally {
					context.close();
				}
			} catch (NamingException namingException) {
				throw new RuntimeException(namingException);
			}
		};
	}
	
	default ConnectionProvider mapped(UnaryOperator<String> operator) {
		return name -> getConnection(operator.apply(name));
	}
}
