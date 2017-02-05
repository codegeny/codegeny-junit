package org.codegeny.junit.persistence;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.EntityTransaction;

import org.junit.rules.ExternalResource;

public class EntityTransactionRule extends ExternalResource implements Supplier<EntityTransaction> {
	
	private final Consumer<? super EntityTransaction> closer;
	private EntityTransaction entityTransaction;
	private final Supplier<? extends EntityTransaction> opener;
	
	public EntityTransactionRule(Supplier<? extends EntityTransaction> opener, Consumer<? super EntityTransaction> closer) {
		this.opener = opener;
		this.closer = closer;
	}
	
	@Override
	protected void after() {
		if (this.entityTransaction != null && this.entityTransaction.isActive()) {
			closer.accept(entityTransaction);
		}
	}
	
	@Override
	protected void before() throws Throwable {
		this.entityTransaction = opener.get();
	}
	
	@Override
	public EntityTransaction get() {
		return this.entityTransaction;
	}
}
