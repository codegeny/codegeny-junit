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

import java.util.Properties;

import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.codegeny.junit.President;

public enum CommonModules { // can be reused by multiple tests

	INSTANCE;

	@Configuration
	public Properties configuration() {
		Properties configuration = new Properties();
		configuration.setProperty("defaultDataSource", "new://Resource?type=DataSource");
		configuration.setProperty("defaultDataSource.JdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
		configuration.setProperty("defaultDataSource.JdbcDriver", "org.h2.Driver");
		return configuration;
	}

	@Module
	public PersistenceUnit persistence() {
		PersistenceUnit persistenceUnit = new PersistenceUnit("managed");
		persistenceUnit.addClass(President.class);
		persistenceUnit.setProvider("org.hibernate.jpa.HibernatePersistenceProvider");
		persistenceUnit.setProperty("javax.persistence.schema-generation.database.action", "drop-and-create");
		return persistenceUnit;
	}
}
