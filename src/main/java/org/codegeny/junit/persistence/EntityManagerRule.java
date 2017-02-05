package org.codegeny.junit.persistence;

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
	protected void before() throws Throwable {
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
