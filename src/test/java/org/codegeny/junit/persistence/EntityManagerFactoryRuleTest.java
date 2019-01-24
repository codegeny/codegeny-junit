package org.codegeny.junit.persistence;

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

import static org.junit.Assert.assertEquals;

import org.codegeny.junit.President;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class EntityManagerFactoryRuleTest {
	
	@ClassRule
	public static final EntityManagerFactoryRule ENTITY_MANAGER_FACTORY = new EntityManagerFactoryRule("unmanaged");
	
	@Rule
	public final EntityManagerRule entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
	
	@Test
	public void simpleTest() {
		this.entityManager.get().getTransaction().begin();
		this.entityManager.get().persist(new President(3L, "Abraham Lincoln"));
		this.entityManager.get().getTransaction().commit();
		
		this.entityManager.get().clear();
		
		assertEquals("Abraham Lincoln", this.entityManager.get().find(President.class, 3L).getName());
	}
}
