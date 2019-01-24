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

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

@FunctionalInterface
public interface ReplacementFunction {

    Object replace(ITable table, int row, String column, Object value) throws DataSetException;
    
    default ReplacementFunction andThen(ReplacementFunction next) {
    	return (table, row, column, value) -> next.replace(table, row, column, replace(table, row, column, value));
    }
}
