package org.codegeny.junit.persistence;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.SynchronizationType;

import org.junit.rules.ExternalResource;

public class EntityManagerFactoryRule extends ExternalResource implements Supplier<EntityManagerFactory> {
	
	private EntityManagerFactory entityManagerFactory;
	private final Supplier<? extends EntityManagerFactory> opener;
	
	public EntityManagerFactoryRule(String persistenceUnitName) {
		this(() -> Persistence.createEntityManagerFactory(persistenceUnitName));
	}
	
	public EntityManagerFactoryRule(String persistenceUnitName, Map<?, ?> properties) {
		this(() -> Persistence.createEntityManagerFactory(persistenceUnitName, properties));
	}
	
	public EntityManagerFactoryRule(Supplier<? extends EntityManagerFactory> opener) {
		this.opener = opener;
	}
	
	@Override
	protected void after() {
		if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
			entityManagerFactory.close();
		}
	}
	
	@Override
	public EntityManagerFactory get() {
		return this.entityManagerFactory;
	}
	
	@Override
	protected void before() throws Throwable {
		this.entityManagerFactory = opener.get();
	}
	
	public EntityManagerRule createEntityManager() {
		return createEntityManager(EntityManagerFactory::createEntityManager);
	}
	
	public EntityManagerRule createEntityManager(Function<EntityManagerFactory, EntityManager> opener) {
		return new EntityManagerRule(() -> opener.apply(this.entityManagerFactory));
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
