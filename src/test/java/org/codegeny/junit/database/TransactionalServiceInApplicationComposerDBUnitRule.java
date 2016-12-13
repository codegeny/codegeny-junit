package org.codegeny.junit.database;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.openejb.junit.ApplicationComposerRule;
import org.apache.openejb.testing.Classes;
import org.codegeny.junit.PresidentService;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@Classes(cdi = true, value = PresidentService.class)
public class TransactionalServiceInApplicationComposerDBUnitRule {
	
	@Resource
	private DataSource dataSource;
	
	@Inject
	private PresidentService presidentService;

	@Rule
	public final TestRule ruleChain = RuleChain
		.outerRule(new ApplicationComposerRule(this, CommonModules.INSTANCE)) // injection must come before dbunit
		.around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(this.dataSource)));
		
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	public void testSomething() {
		this.presidentService.doSomething();
	}
}
