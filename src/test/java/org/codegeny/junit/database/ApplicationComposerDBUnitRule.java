package org.codegeny.junit.database;

import java.util.Properties;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.junit.jee.transaction.Transaction;
import org.apache.openejb.junit.jee.transaction.TransactionRule;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ApplicationComposerDBUnitRule {
	
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
	
	@Resource
	private DataSource dataSource;
	
	@PersistenceContext(unitName = "managed")
	private EntityManager entityManager;

	@Rule
	public final TestRule ruleChain = RuleChain
		.outerRule(new ApplicationComposerRule(this, CommonModules.INSTANCE)) // injection must come before dbunit
		.around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(this.dataSource)))
		.around(new TransactionRule()); // transaction must come after dbunit
		
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	@Transaction
	public void testSomething() {
		this.entityManager.persist(new President(3L, "Abraham Lincoln"));
		this.entityManager.remove(this.entityManager.getReference(President.class, 2L));
	}
}
