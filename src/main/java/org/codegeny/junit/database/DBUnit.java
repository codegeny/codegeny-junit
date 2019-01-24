package org.codegeny.junit.database;

/*-
 * #%L
 * A collection of JUnit rules
 * %%
 * Copyright (C) 2016 - 2019 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(DBUnits.class)
public @interface DBUnit {
	
	enum AssertionMode implements DatabaseAssertion {
		NON_STRICT {

			@Override
			public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				for (String tableName : expectedDataSet.getTableNames()) {
					ITable expectedTable = expectedDataSet.getTable(tableName);
					ITable actualTable = actualDataSet.getTable(tableName);
					assertEquals(expectedTable, actualTable);
				}
			}

			@Override
			public void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
				ITableMetaData expectedMetaData = expectedTable.getTableMetaData();
				ITableMetaData actualMetaData = actualTable.getTableMetaData();
				String[] ignoredColumns = Stream.of(Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual()).map(Column::getColumnName).toArray(i -> new String[i]);
				Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoredColumns);
			}
		},
		NON_STRICT_UNORDERED {

			@Override
			public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				for (String tableName : expectedDataSet.getTableNames()) {
					ITable expectedTable = expectedDataSet.getTable(tableName);
					ITable actualTable = actualDataSet.getTable(tableName);
					assertEquals(expectedTable, actualTable);
				}
			}
			
			@Override
			public void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
				Column[] expectedColumns = expectedTable.getTableMetaData().getColumns();
				ITable expectedSortedTable = new SortedTable(expectedTable, expectedColumns);
				ITable actualSortedTable = new SortedTable(actualTable, expectedColumns);
				NON_STRICT.assertEquals(expectedSortedTable, actualSortedTable);
			}
		},
		STRICT {

			@Override
			public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
				Assertion.assertEquals(expectedDataSet, actualDataSet);
			}

			@Override
			public void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
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
