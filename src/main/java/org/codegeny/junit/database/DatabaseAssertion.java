package org.codegeny.junit.database;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

public interface DatabaseAssertion {

	void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException;
	
	void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException;
}
