package org.codegeny.junit.database;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public @Retention(RUNTIME) @Target(METHOD) @interface DBUnits {
	
	DBUnit[] value();
}