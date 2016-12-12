package org.codegeny.junit.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.IDataTypeFactory;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;

public class DataTypeFactoryResolver {

	// TODO add all others
	public IDataTypeFactory resolveDataTypeFactory(DatabaseMetaData databaseMetaData) throws DatabaseUnitException, SQLException {
		switch (databaseMetaData.getDatabaseProductName()) {
		case "HSQL Database Engine":
			return new HsqldbDataTypeFactory();
		case "H2":
			return new H2DataTypeFactory();
		default:
			return resolveDefaultDataTypeFactory(databaseMetaData);
		}
	}
	
	protected IDataTypeFactory resolveDefaultDataTypeFactory(DatabaseMetaData databaseMetaData) throws DatabaseUnitException, SQLException {
		return new DefaultDataTypeFactory();
	}
}
