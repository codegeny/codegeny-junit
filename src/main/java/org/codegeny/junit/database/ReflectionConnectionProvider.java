package org.codegeny.junit.database;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
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
		return a -> a.isAnnotationPresent(DBUnitDataSource.class) && a.getAnnotation(DBUnitDataSource.class).name().equals(name);
	}
	
	@SafeVarargs
	private final <A extends AnnotatedElement> Optional<IDatabaseConnection> extract(String name, Function<A, IDatabaseConnection> extractor, A... values) {
		return Stream.of(values).filter(byName(name)).findFirst().map(extractor);
	}
	
	@Override
	public IDatabaseConnection getConnection(String name) throws Exception {
		return byField(name).orElseGet(() -> byMethod(name).orElseThrow(() -> new RuntimeException("Cannot find " + name)));
	}
	
	private IDatabaseConnection getField(Field field) {
		try {
			return toDatabaseConnection(field.get(this.testInstance));
		} catch (IllegalAccessException | SQLException | DatabaseUnitException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private IDatabaseConnection getMethod(Method method) {
		try {
			return toDatabaseConnection(method.invoke(this.testInstance));
		} catch (IllegalAccessException | InvocationTargetException | SQLException | DatabaseUnitException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private IDatabaseConnection toDatabaseConnection(Object object) throws SQLException, DatabaseUnitException {
		if (object instanceof IDatabaseConnection) {
			return (IDatabaseConnection) object;
		}
		if (object instanceof DataSource) {
			return new DatabaseDataSourceConnection((DataSource) object);
		}
		if (object instanceof Connection) {
			return new DatabaseConnection((Connection) object);
		}
		throw new RuntimeException("Cannot convert " + object + " to IDatabaseConnection");
	}
}
