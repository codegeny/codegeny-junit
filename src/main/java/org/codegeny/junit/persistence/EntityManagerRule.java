package org.codegeny.junit.persistence;

import java.util.function.Supplier;

import javax.persistence.EntityManager;

import org.codegeny.junit.ThreadLocalRule;

public class EntityManagerRule extends ThreadLocalRule<EntityManager> {

	public EntityManagerRule(Supplier<? extends EntityManager> opener) {
		super(opener, EntityManager::close);
	}
}
