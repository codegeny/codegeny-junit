package org.codegeny.junit.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

public class StandardConnectionConverter implements ConnectionConverter {
	
	@Override
	public IDatabaseConnection toConnection(Object object) throws SQLException, DatabaseUnitException {
		return object instanceof Connection ? new DatabaseConnection((Connection) object) : null;
	}
}
