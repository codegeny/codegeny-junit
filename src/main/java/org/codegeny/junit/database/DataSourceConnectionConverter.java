package org.codegeny.junit.database;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;

public class DataSourceConnectionConverter implements ConnectionConverter {
	
	@Override
	public IDatabaseConnection toConnection(Object object) throws SQLException, DatabaseUnitException {
		return object instanceof DataSource ? new DatabaseDataSourceConnection((DataSource) object) : null;
	}
}
