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
	protected void before() {
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
