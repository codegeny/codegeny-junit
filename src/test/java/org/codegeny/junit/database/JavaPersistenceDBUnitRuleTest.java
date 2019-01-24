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

import java.sql.DriverManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.codegeny.junit.President;
import org.dbunit.database.DatabaseConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class JavaPersistenceDBUnitRuleTest {
	
	private static EntityManagerFactory ENTITY_MANAGER_FACTORY;
	
	@AfterClass
	public static void closeEntityManagerFactory() {
		ENTITY_MANAGER_FACTORY.close();
	}
	
	@BeforeClass
	public static void createEntityManagerFactory() {
		ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("unmanaged", null);
	}
	
	@Rule
	public final TestRule dbUnit = new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseConnection(DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")));
	
	private EntityManager entityManager;
	
	@After
	public void closeEntityManager() {
		this.entityManager.close();
	}
	
	@Before
	public void createEntityManager() {
		this.entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
	}
	
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	public void simpleTest() {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(new President(3L, "Abraham Lincoln"));
		this.entityManager.remove(this.entityManager.getReference(President.class, 2L));
		this.entityManager.getTransaction().commit();
	}
}
