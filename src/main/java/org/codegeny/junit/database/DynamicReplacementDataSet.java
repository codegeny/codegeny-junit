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

import org.dbunit.dataset.AbstractDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ITableMetaData;

public class DynamicReplacementDataSet extends AbstractDataSet {

	private class ReplacementTableIterator implements ITableIterator {

		private final ITableIterator tableIterator;

		public ReplacementTableIterator(ITableIterator tableIterator) {
			this.tableIterator = tableIterator;
		}

		@Override
		public ITable getTable() throws DataSetException {
			return new ReplacementTable(tableIterator.getTable());
		}

		@Override
		public ITableMetaData getTableMetaData() throws DataSetException {
			return tableIterator.getTableMetaData();
		}

		@Override
		public boolean next() throws DataSetException {
			return tableIterator.next();
		}
	}

	private class ReplacementTable implements ITable {

		private final ITable table;

		public ReplacementTable(ITable table) {
			this.table = table;
		}

		@Override
		public int getRowCount() {
			return table.getRowCount();
		}

		@Override
		public ITableMetaData getTableMetaData() {
			return table.getTableMetaData();
		}

		@Override
		public Object getValue(int row, String column) throws DataSetException {
			return function.replace(table, row, column, table.getValue(row, column));
		}
	}

	private final IDataSet dataSet;
	private final ReplacementFunction function;

	public DynamicReplacementDataSet(IDataSet dataSet, ReplacementFunction function) {
		this.dataSet = dataSet;
		this.function = function;
	}

	@Override
	protected ITableIterator createIterator(boolean reversed) throws DataSetException {
		return new ReplacementTableIterator(reversed ? dataSet.reverseIterator() : dataSet.iterator());
	}

	@Override
	public ITable getTable(String tableName) throws DataSetException {
		return new ReplacementTable(dataSet.getTable(tableName));
	}

	@Override
	public String[] getTableNames() throws DataSetException {
		return dataSet.getTableNames();
	}

	@Override
	public ITableMetaData getTableMetaData(String tableName) throws DataSetException {
		return dataSet.getTableMetaData(tableName);
	}
}
