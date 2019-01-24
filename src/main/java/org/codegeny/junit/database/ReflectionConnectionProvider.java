package org.codegeny.junit.database;

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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.IDatabaseConnection;

public class ReflectionConnectionProvider implements ConnectionProvider {
	
	private final Class<?> testClass;
	private final Object testInstance;

	public ReflectionConnectionProvider(Class<?> testClass) {
		this(testClass, null);
	}
	
	private ReflectionConnectionProvider(Class<?> testClass, Object testInstance) {
		this.testClass = testClass;
		this.testInstance = testInstance;
	}

	public ReflectionConnectionProvider(Object testInstance) {
		this(testInstance.getClass(), testInstance);
	}

	private Optional<IDatabaseConnection> byField(String name) {
		return extract(name, this::getField, testClass.getFields());
	}
	
	private Optional<IDatabaseConnection> byMethod(String name) {
		return extract(name, this::getMethod, testClass.getMethods());
	}
	
	private Predicate<AnnotatedElement> byName(String name) {
		return a -> a.isAnnotationPresent(DBUnitConnection.class) && a.getAnnotation(DBUnitConnection.class).name().equals(name);
	}
	
	@SafeVarargs
	private final <A extends AnnotatedElement> Optional<IDatabaseConnection> extract(String name, Function<A, IDatabaseConnection> extractor, A... values) {
		return Stream.of(values).filter(byName(name)).findFirst().map(extractor);
	}
	
	@Override
	public IDatabaseConnection getConnection(String name) throws Exception {
		return byField(name).orElseGet(() -> byMethod(name).orElseThrow(() -> new DatabaseUnitRuntimeException("Cannot find data source with name '" + name + "'")));
	}
	
	private IDatabaseConnection getField(Field field) {
		try {
			return toDatabaseConnection(field.get(this.testInstance));
		} catch (IllegalAccessException | SQLException | DatabaseUnitException exception) {
			throw new DatabaseUnitRuntimeException(exception);
		}
	}
	
	private IDatabaseConnection getMethod(Method method) {
		try {
			return toDatabaseConnection(method.invoke(this.testInstance));
		} catch (IllegalAccessException | InvocationTargetException | SQLException | DatabaseUnitException exception) {
			throw new DatabaseUnitRuntimeException(exception);
		}
	}
	
	private IDatabaseConnection toDatabaseConnection(Object object) throws SQLException, DatabaseUnitException {
		return ConnectionConverter.convert(object);
	}
}
