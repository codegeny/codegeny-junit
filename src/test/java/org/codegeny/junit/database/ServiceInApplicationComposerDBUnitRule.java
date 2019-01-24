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
