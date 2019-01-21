package org.codegeny.junit.database;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.IDatabaseConnection;

public interface ConnectionConverter {
	
	IDatabaseConnection toConnection(Object object) throws SQLException, DatabaseUnitException;
	
	static IDatabaseConnection convert(Object object) throws SQLException, DatabaseUnitException {
		Iterator<ConnectionConverter> iterator = ServiceLoader.load(ConnectionConverter.class).iterator();
		while (iterator.hasNext()) {
			IDatabaseConnection result = iterator.next().toConnection(object);
			if (result != null) {
				return result;
			}
		}		
		throw new DatabaseUnitRuntimeException("Cannot convert " + object + " to IDatabaseConnection");
	}
}
