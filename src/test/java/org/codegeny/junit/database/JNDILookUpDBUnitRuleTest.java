package org.codegeny.junit.database;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.openejb.junit.jee.EJBContainerRule;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.apache.openejb.junit.jee.transaction.Transaction;
import org.apache.openejb.junit.jee.transaction.TransactionRule;
import org.codegeny.junit.President;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@Properties({
	@Property(key = "defaultDataSource", value = "new://Resource?type=DataSource"),
	@Property(key = "defaultDataSource.JdbcUrl", value = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
	@Property(key = "defaultDataSource.JdbcDriver", value = "org.h2.Driver"),
	@Property(key = "javax.persistence.provider", value="org.hibernate.jpa.HibernatePersistenceProvider")
})
public class JNDILookUpDBUnitRuleTest {

	@PersistenceContext(unitName = "managed")
	private EntityManager entityManager;
	
	@Rule
	public final TestRule ruleChain = RuleChain
		.outerRule(new EJBContainerRule(this))
		.around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(new InitialContext(), "java:openejb/Resource/defaultDataSource")))
		.around(new TransactionRule());

	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	@Transaction
	public void testSomething() {
		this.entityManager.persist(new President(3L, "Abraham Lincoln"));
		this.entityManager.remove(this.entityManager.getReference(President.class, 2L));
	}
}
