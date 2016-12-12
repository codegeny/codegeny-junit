package org.codegeny.junit.database;

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
		
		public @Override void evaluate() throws Throwable {
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
	
	private final ConnectionProvider connectionProvider;
	private final Map<String, Object> properties = new HashMap<>();
	private final ResourceLoader resourceLoader;
	
	public DBUnitRule(ResourceLoader resourceLoader, ConnectionProvider connectionProvider) {
		this.resourceLoader = resourceLoader;
		this.connectionProvider = connectionProvider;
	}
	
	public @Override Statement apply(Statement base, Description description) {
		try {
			for (DBUnit dbUnit : description.getTestClass().getDeclaredMethod(description.getMethodName()).getAnnotationsByType(DBUnit.class)) {
				base = new DBUnitStatement(base, dbUnit);
			}
			return base;
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
			DataTypeFactoryResolver resolver = newDataTypeFactoryResolver();
			DatabaseConfig configuration = connection.getConfig();
			configuration.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, resolver.resolveDataTypeFactory(connection.getConnection().getMetaData()));
			this.properties.forEach(configuration::setProperty);
			return connection;
		} catch (DatabaseUnitException | SQLException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new DatabaseUnitException(exception);
		}
	}
	
	protected IDataSet newDataSet(InputStream inputStream) throws DatabaseUnitException, SQLException {
		return new DynamicReplacementDataSet(new FlatXmlDataSetBuilder().build(inputStream), toNull().andThen(resource(this.resourceLoader))); 
	}
	
	private IDataSet newDataSet(String[] dataSetNames) throws DatabaseUnitException, SQLException {
		IDataSet dataSet = new DefaultDataSet();
		for (String dataSetName : dataSetNames) {
			dataSet = new CompositeDataSet(dataSet, newDataSet(getResource(dataSetName)));
		}
		return dataSet;
	}
	
	protected DataTypeFactoryResolver newDataTypeFactoryResolver() {
		return new DataTypeFactoryResolver();
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
