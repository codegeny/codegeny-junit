package org.codegeny.junit.database;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.junit.jee.transaction.Transaction;
import org.apache.openejb.junit.jee.transaction.TransactionRule;
import org.apache.openejb.testing.Classes;
import org.codegeny.junit.President;
import org.codegeny.junit.PresidentRepository;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@Classes(cdi = true, value = PresidentRepository.class)
public class ServiceInApplicationComposerDBUnitRule {
	
	@Resource
	private DataSource dataSource;
	
	@Inject
	private PresidentRepository presidentRepository;

	@Rule
	public final TestRule ruleChain = RuleChain
		.outerRule(new ApplicationComposerRule(this, CommonModules.INSTANCE)) // injection must come before dbunit
		.around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(this.dataSource)))
		.around(new TransactionRule()); // transaction must come after dbunit
		
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	@Transaction
	public void testSomething() {
		this.presidentRepository.addPresident(new President(3L, "Abraham Lincoln"));
		this.presidentRepository.findPresident(2L).ifPresent(this.presidentRepository::removePresident);
	}
}
