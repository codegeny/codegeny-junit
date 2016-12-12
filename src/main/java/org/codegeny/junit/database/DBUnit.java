package org.codegeny.junit.database;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.SortedTable;
import org.dbunit.operation.DatabaseOperation;

public @Retention(RUNTIME) @Target(METHOD) @Repeatable(DBUnits.class) @interface DBUnit {
	
	enum AssertionMode implements DatabaseAssertion {
		NON_STRICT {

			public @Override void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				for (String tableName : expectedDataSet.getTableNames()) {
					ITable expectedTable = expectedDataSet.getTable(tableName);
					ITable actualTable = actualDataSet.getTable(tableName);
					assertEquals(expectedTable, actualTable);
				}
			}

			public @Override void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
				ITableMetaData expectedMetaData = expectedTable.getTableMetaData();
				ITableMetaData actualMetaData = actualTable.getTableMetaData();
				String[] ignoredColumns = Stream.of(Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual()).map(Column::getColumnName).toArray(i -> new String[i]);
				Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoredColumns);
			}
		},
		NON_STRICT_UNORDERED {

			public @Override void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				NON_STRICT.assertEquals(expectedDataSet, actualDataSet);
			}
			
			public @Override void assertEquals(ITable expectedSortedTable, ITable actualSortedTable) throws DatabaseUnitException {
				Column[] expectedColumns = expectedSortedTable.getTableMetaData().getColumns();
				expectedSortedTable = new SortedTable(expectedSortedTable, expectedColumns);
				actualSortedTable = new SortedTable(actualSortedTable, expectedColumns);
				NON_STRICT.assertEquals(expectedSortedTable, actualSortedTable);
			}
		},
		STRICT {

			public @Override void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				Assertion.assertEquals(expectedDataSet, actualDataSet);
			}

			public @Override void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
				Assertion.assertEquals(expectedTable, actualTable);
			}
		};
	}

	enum Operation {

		CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT),
		DELETE(DatabaseOperation.DELETE),
		DELETE_ALL(DatabaseOperation.DELETE_ALL),
		INSERT(DatabaseOperation.INSERT),
		NONE(DatabaseOperation.NONE),
		REFRESH(DatabaseOperation.REFRESH),
		TRUNCATE_TABLE(DatabaseOperation.TRUNCATE_TABLE),
		UPDATE(DatabaseOperation.UPDATE);

		private final DatabaseOperation databaseOperation;

		private Operation(DatabaseOperation databaseOperation) {
			this.databaseOperation = databaseOperation;
		}

		public DatabaseOperation asDatabaseOperation() {
			return this.databaseOperation;
		}
	}

	String DEFAULT_NAME = "default";

	AssertionMode assertionMode() default AssertionMode.NON_STRICT;

	String[] dataSets() default {};

	String[] expectedDataSets() default {};

	String name() default DEFAULT_NAME;

	Operation setUpOperation() default Operation.CLEAN_INSERT;

	Operation tearDownOperation() default Operation.NONE;
}