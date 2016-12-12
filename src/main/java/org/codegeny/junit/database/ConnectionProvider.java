package org.codegeny.junit.database;

import org.dbunit.database.IDatabaseConnection;

public interface ConnectionProvider {
	
	IDatabaseConnection getConnection(String name) throws Exception;
}
