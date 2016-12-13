package org.codegeny.junit.persistence;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.codegeny.junit.ThreadLocalRule;

public class EntityManagerRule extends ThreadLocalRule<EntityManager> {

	public EntityManagerRule(Supplier<? extends EntityManager> opener) {
		super(opener, EntityManager::close);
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
			EntityTransaction transaction = get().getTransaction();
			transaction.begin();
			return transaction;
		}, closer);
	}
}
