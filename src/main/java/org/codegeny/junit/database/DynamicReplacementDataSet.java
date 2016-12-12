package org.codegeny.junit.database;

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

		public @Override ITable getTable() throws DataSetException {
			return new ReplacementTable(tableIterator.getTable());
		}

		public @Override ITableMetaData getTableMetaData() throws DataSetException {
			return tableIterator.getTableMetaData();
		}

		public @Override boolean next() throws DataSetException {
			return tableIterator.next();
		}
	}

	private class ReplacementTable implements ITable {

		private final ITable table;

		public ReplacementTable(ITable table) {
			this.table = table;
		}

		public @Override int getRowCount() {
			return table.getRowCount();
		}

		public @Override ITableMetaData getTableMetaData() {
			return table.getTableMetaData();
		}

		public @Override Object getValue(int row, String column) throws DataSetException {
			return function.replace(table, row, column, table.getValue(row, column));
		}
	}

	private final IDataSet dataSet;
	private final ReplacementFunction function;

	public DynamicReplacementDataSet(IDataSet dataSet, ReplacementFunction function) {
		this.dataSet = dataSet;
		this.function = function;
	}

	protected @Override ITableIterator createIterator(boolean reversed) throws DataSetException {
		return new ReplacementTableIterator(reversed ? dataSet.reverseIterator() : dataSet.iterator());
	}

	public @Override ITable getTable(String tableName) throws DataSetException {
		return new ReplacementTable(dataSet.getTable(tableName));
	}

	public @Override String[] getTableNames() throws DataSetException {
		return dataSet.getTableNames();
	}

	public @Override ITableMetaData getTableMetaData(String tableName) throws DataSetException {
		return dataSet.getTableMetaData(tableName);
	}
}
