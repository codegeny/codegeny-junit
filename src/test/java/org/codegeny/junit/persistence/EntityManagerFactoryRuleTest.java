package org.codegeny.junit.persistence;

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
