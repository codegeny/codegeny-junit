package org.codegeny.junit.database;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

@FunctionalInterface
public interface ReplacementFunction {

    Object replace(ITable table, int row, String column, Object value) throws DataSetException;
    
    default ReplacementFunction andThen(ReplacementFunction next) {
    	return (table, row, column, value) -> next.replace(table, row, column, replace(table, row, column, value));
    }
}
