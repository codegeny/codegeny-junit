package org.codegeny.junit.persistence;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.SynchronizationType;

import org.codegeny.junit.ThreadLocalRule;

public class EntityManagerFactoryRule extends ThreadLocalRule<EntityManagerFactory> {
	
	public EntityManagerFactoryRule(String persistenceUnitName) {
		this(() -> Persistence.createEntityManagerFactory(persistenceUnitName));
	}
	
	public EntityManagerFactoryRule(String persistenceUnitName, Map<?, ?> properties) {
		this(() -> Persistence.createEntityManagerFactory(persistenceUnitName, properties));
	}
	
	public EntityManagerFactoryRule(Supplier<? extends EntityManagerFactory> opener) {
		super(opener, EntityManagerFactory::close);
	}
	
	public EntityManagerRule createEntityManager() {
		return createEntityManager(EntityManagerFactory::createEntityManager);
	}
	
	public EntityManagerRule createEntityManager(Function<EntityManagerFactory, EntityManager> opener) {
		return new EntityManagerRule(() -> opener.apply(get()));
	}
	
	public EntityManagerRule createEntityManager(Map<?, ?> map) {
		return createEntityManager(entityManagerFactory -> entityManagerFactory.createEntityManager(map));
	}
	
	public EntityManagerRule createEntityManager(SynchronizationType synchronizationType) {
		return createEntityManager(entityManagerFactory -> entityManagerFactory.createEntityManager(synchronizationType));
	}
	
	public EntityManagerRule createEntityManager(SynchronizationType synchronizationType, Map<?, ?> map) {
		return createEntityManager(entityManagerFactory -> entityManagerFactory.createEntityManager(synchronizationType, map));
	}
}
