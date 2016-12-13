package org.codegeny.junit.database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

public class ReplacementFunctions {
	
	public static final String DEFAULT_NULL_STRING = "null";
	public static final String DEFAULT_RESOURCE_PREFIX = "resource:";

	public static final ReplacementFunction noOp() {
		return (table, row, column, value) -> value;
	}
	
	private static Object resource(ITable table, String column, Object value, ResourceLoader loader, String prefix) throws DataSetException {
		try {
			if (value instanceof String) {
				String string = (String) value;
				if (string.startsWith(prefix)) {
					try (InputStream in = loader.loadResource(string.substring(prefix.length())); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						byte[] buffer = new byte[8192];
						for (int n = in.read(buffer); n > 0; n = in.read(buffer)) {
						    out.write(buffer, 0, n);
						}
						return out.toByteArray();
					}
				}
			}
			return value;
		} catch (IOException ioException) {
			throw new DataSetException(ioException);
		}
	}

	public static final ReplacementFunction resource(ResourceLoader loader) {
		return resource(loader, DEFAULT_RESOURCE_PREFIX);
	}
	
	public static final ReplacementFunction resource(ResourceLoader loader, String prefix) {
		return (table, row, column, value) -> resource(table, column, value, loader, prefix);
	}

	public static final ReplacementFunction toNull() {
		return toNull(DEFAULT_NULL_STRING);
	}
	
	public static final ReplacementFunction toNull(String nullString) {
		return (table, row, column, value) -> nullString.equals(value) ? null : value;
	}
}
