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
