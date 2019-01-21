package org.codegeny.junit.database;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;

public class SimpleConnectionConverter implements ConnectionConverter {

	@Override
	public IDatabaseConnection toConnection(Object object) throws SQLException, DatabaseUnitException {
		return object instanceof IDatabaseConnection ? (IDatabaseConnection) object : null;
	}
}
