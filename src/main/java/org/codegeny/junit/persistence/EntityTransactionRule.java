package org.codegeny.junit.persistence;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.persistence.EntityTransaction;

import org.codegeny.junit.ThreadLocalRule;

public class EntityTransactionRule extends ThreadLocalRule<EntityTransaction> {
	
	public EntityTransactionRule(Supplier<? extends EntityTransaction> opener, Consumer<? super EntityTransaction> closer) {
		super(opener, closer);
	}
}
