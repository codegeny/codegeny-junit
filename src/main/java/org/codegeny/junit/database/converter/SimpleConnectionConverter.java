package org.codegeny.junit.database.converter;

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

import java.sql.SQLException;

import org.codegeny.junit.database.ConnectionConverter;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;

public class SimpleConnectionConverter implements ConnectionConverter {

	@Override
	public IDatabaseConnection toConnection(Object object) throws SQLException, DatabaseUnitException {
		return object instanceof IDatabaseConnection ? (IDatabaseConnection) object : null;
	}
}
