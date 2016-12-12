package org.codegeny.junit.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface ResourceLoader {
	
	static ResourceLoader fromClass(Object object) {
		return fromClass(object.getClass());
	}
	
	static ResourceLoader fromClass(Class<?> klass) {
		return klass::getResourceAsStream;
	}
	
	static ResourceLoader fromClasspath() {
		return fromClasspath(Thread.currentThread().getContextClassLoader());
	}
	
	static ResourceLoader fromClasspath(ClassLoader classLoader) {
		return classLoader::getResourceAsStream;
	}
	
	static ResourceLoader fromFile() {
		return FileInputStream::new;
	}
	
	static ResourceLoader fromFolder(File folder) {
		return name -> new FileInputStream(new File(folder, name));
	}
	
	static ResourceLoader fromFolder(String folder) {
		return fromFolder(new File(folder));
	}
	
	InputStream loadResource(String name) throws IOException;
}