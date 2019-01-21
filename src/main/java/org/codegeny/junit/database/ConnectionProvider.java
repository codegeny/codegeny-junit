package org.codegeny.junit.database;

import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dbunit.database.IDatabaseConnection;

@FunctionalInterface
public interface ConnectionProvider {
	
	IDatabaseConnection getConnection(String name) throws Exception;
	
	static ConnectionProvider fromMap(Map<String, IDatabaseConnection> map) {
		return map::get;
	}
	
	static ConnectionProvider always(Object connection) {
		return name -> ConnectionConverter.convert(connection);
	}
	
	static ConnectionProvider jndi() {
		return jndi(new Properties());
	}

	static ConnectionProvider jndi(Properties properties) {
		return name -> {
			try {
				Context context = new InitialContext(properties);
				try {
					return ConnectionConverter.convert(context.lookup(name));
				} finally {
					context.close();
				}
			} catch (NamingException namingException) {
				throw new RuntimeException(namingException);
			}
		};
	}
	
	default ConnectionProvider mapped(UnaryOperator<String> operator) {
		return name -> getConnection(operator.apply(name));
	}
}
