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
	protected void before() {
		this.entityTransaction = opener.get();
	}
	
	@Override
	public EntityTransaction get() {
		return this.entityTransaction;
	}
}
