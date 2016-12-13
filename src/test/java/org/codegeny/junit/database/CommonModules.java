package org.codegeny.junit.database;

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