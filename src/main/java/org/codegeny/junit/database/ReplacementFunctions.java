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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.DataSetException;

public class ReplacementFunctions {
	
	public static final String DEFAULT_NULL_STRING = "null";
	public static final String DEFAULT_RESOURCE_PREFIX = "resource:";

	public static final ReplacementFunction noOp() {
		return (table, row, column, value) -> value;
	}
	
	private static Object resource(Object value, ResourceLoader loader, String prefix) throws DataSetException {
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
		return (table, row, column, value) -> resource(value, loader, prefix);
	}

	public static final ReplacementFunction toNull() {
		return toNull(DEFAULT_NULL_STRING);
	}
	
	public static final ReplacementFunction toNull(String nullString) {
		return (table, row, column, value) -> nullString.equals(value) ? null : value;
	}
}
