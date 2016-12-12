package org.codegeny.junit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ThreadLocalRule<T> implements TestRule, Supplier<T> {
	
	public static <T> ThreadLocalRule<T> threadLocalRule(Supplier<? extends T> opener, Consumer<? super T> closer) {
		return new ThreadLocalRule<>(opener, closer);
	}
	
	private final Consumer<? super T> closer;
	private final Supplier<? extends T> opener;
	private final ThreadLocal<T> threadLocal = new ThreadLocal<>();
	
	public ThreadLocalRule(Supplier<? extends T> opener, Consumer<? super T> closer) {
		this.opener = opener;
		this.closer = closer;
	}
	
	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				T value = opener.get();
				threadLocal.set(value);
				try {
					base.evaluate();
				} finally {
					threadLocal.remove();
					closer.accept(value);
				}
			}
		};
	}

	public <C> ThreadLocalRule<C> childRule(Function<? super T, ? extends C> opener, BiConsumer<? super T, ? super C> closer) {
		return childRule(opener, child -> closer.accept(get(), child));
	}
	
	public <C> ThreadLocalRule<C> childRule(Function<? super T, ? extends C> opener, Consumer<? super C> closer) {
		return threadLocalRule(() -> opener.apply(get()), closer);
	}
	
	@Override
	public T get() {
		return this.threadLocal.get();
	}
}
