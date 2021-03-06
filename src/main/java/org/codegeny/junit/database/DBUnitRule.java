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

import static org.codegeny.junit.database.ReplacementFunctions.resource;
import static org.codegeny.junit.database.ReplacementFunctions.toNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DBUnitRule implements TestRule {
	
	private class DBUnitStatement extends Statement {
		
		private final Statement base;
		private final DBUnit dbUnit;

		public DBUnitStatement(Statement base, DBUnit dbUnit) {
			this.base = base;
			this.dbUnit = dbUnit;
		}
		
		@Override
		public void evaluate() throws Throwable {
			IDatabaseConnection connection = newConnection(this.dbUnit.name());
			try {
				IDatabaseTester tester = newTester(connection);
				tester.setDataSet(newDataSet(this.dbUnit.dataSets()));
				tester.setSetUpOperation(this.dbUnit.setUpOperation().asDatabaseOperation());
				tester.onSetup();
				try {
					this.base.evaluate();
					compareTables(this.dbUnit.assertionMode(), this.dbUnit.expectedDataSets(), connection);
				} finally {
					tester.setTearDownOperation(this.dbUnit.tearDownOperation().asDatabaseOperation());
					tester.onTearDown();
				}
			} finally {
				connection.close();
			}
		}
	}
	
	public static DBUnitRule defaultSettings(Class<?> testClass) {
		return new DBUnitRule(ResourceLoader.fromClass(testClass), new ReflectionConnectionProvider(testClass));
	}
	
	public static DBUnitRule defaultSettings(Object testInstance) {
		return new DBUnitRule(ResourceLoader.fromClass(testInstance), new ReflectionConnectionProvider(testInstance));
	}
	
	private final ConnectionProvider connectionProvider;	
	private final Map<String, Object> properties = new HashMap<>();
	private final ResourceLoader resourceLoader;
	
	public DBUnitRule(ResourceLoader resourceLoader, ConnectionProvider connectionProvider) {
		this.resourceLoader = resourceLoader;
		this.connectionProvider = connectionProvider;
	}
	
	@Override
	public Statement apply(Statement base, Description description) {
		try {
			Statement result = base;
			for (DBUnit dbUnit : description.getTestClass().getDeclaredMethod(description.getMethodName()).getAnnotationsByType(DBUnit.class)) {
				result = new DBUnitStatement(result, dbUnit);
			}
			return result;
		} catch (NoSuchMethodException noSuchMethodException) {
			throw new DatabaseUnitRuntimeException(noSuchMethodException);
		}
	}
	
	protected void compareTables(DatabaseAssertion assertion, String[] expectedDataSets, IDatabaseConnection connection) throws DatabaseUnitException, SQLException {
		IDataSet expectedDataSet = newDataSet(expectedDataSets);
		IDataSet actualDataSet = connection.createDataSet();
		assertion.assertEquals(expectedDataSet, actualDataSet);
	}
	
	private InputStream getResource(String resourceName) throws DatabaseUnitException {
		try {
			return this.resourceLoader.loadResource(resourceName);
		} catch (IOException ioException) {
			throw new DatabaseUnitException(ioException);
		}
	}
	
	protected IDatabaseConnection newConnection(String connectionName) throws DatabaseUnitException, SQLException {
		try {
			IDatabaseConnection connection = connectionProvider.getConnection(connectionName);
			DatabaseConfig configuration = connection.getConfig();
			configuration.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, DataTypeFactorySupplier.resolveDataTypeFactory(connection.getConnection().getMetaData()));
			this.properties.forEach(configuration::setProperty);
			return connection;
		} catch (DatabaseUnitException | SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DatabaseUnitException(exception);
		}
	}
	
	protected IDataSet newDataSet(InputStream inputStream) throws DatabaseUnitException, SQLException {
		return new DynamicReplacementDataSet(newDataSetBuilder().build(inputStream), toNull().andThen(resource(this.resourceLoader))); 
	}
	
	private IDataSet newDataSet(String[] dataSetNames) throws DatabaseUnitException, SQLException {
		IDataSet dataSet = new DefaultDataSet();
		for (String dataSetName : dataSetNames) {
			dataSet = new CompositeDataSet(dataSet, newDataSet(getResource(dataSetName)));
		}
		return dataSet;
	}
	
	protected FlatXmlDataSetBuilder newDataSetBuilder() {
		return new FlatXmlDataSetBuilder().setColumnSensing(true);
	}
	
	protected IDatabaseTester newTester(IDatabaseConnection connection) throws DatabaseUnitException, SQLException {
		IDatabaseTester tester = new DefaultDatabaseTester(connection);
		tester.setOperationListener(IOperationListener.NO_OP_OPERATION_LISTENER); // prevent connection to be closed
		return tester;
	}
	
	public DBUnitRule withProperty(String key, Object value) {
		this.properties.put(key, value);
		return this;
	}
}
