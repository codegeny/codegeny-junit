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

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.rules.ExternalResource;

public class EntityManagerRule extends ExternalResource implements Supplier<EntityManager> {
	
	private EntityManager entityManager;
	private final Supplier<? extends EntityManager> opener;
	
	public EntityManagerRule(Supplier<? extends EntityManager> opener) {
		this.opener = opener;
	}

	@Override
	protected void after() {
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
	}
	
	@Override
	protected void before() {
		this.entityManager = opener.get();
	}

	public EntityTransactionRule beginAndCommitTransaction() {
		return beginTransaction(transaction -> {
			if (transaction.isActive()) {
				transaction.commit();
			}
		});
	}

	public EntityTransactionRule beginAndRollbackTransaction() {
		return beginTransaction(EntityTransaction::rollback);
	}
	
	public EntityTransactionRule beginTransaction(Consumer<? super EntityTransaction> closer) {
		return new EntityTransactionRule(() -> {
			EntityTransaction transaction = this.entityManager.getTransaction();
			transaction.begin();
			return transaction;
		}, closer);
	}
	
	@Override
	public EntityManager get() {
		return this.entityManager;
	}
}
