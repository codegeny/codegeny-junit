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

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.codegeny.junit.President;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * In this example, a single DataSource is created and passed to both the DBUnitRule and the EntityManagerFactory (this must be done via a PersistenceUnitInfo).
 */
public class JavaPersistenceSharingDataSourceDBUnitRuleTest {
	
	private static final DataSource DATA_SOURCE = new SimpleDataSource() {
		
		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
		}
	};
	
	private static EntityManagerFactory ENTITY_MANAGER_FACTORY;
	
	@AfterClass
	public static void closeEntityManagerFactory() {
		ENTITY_MANAGER_FACTORY.close();
	}
	
	@BeforeClass
	public static void createEntityManagerFactory() {
		ENTITY_MANAGER_FACTORY = PersistenceProviderResolverHolder
			.getPersistenceProviderResolver()
			.getPersistenceProviders()
			.stream()
			.findAny()
			.map(provider -> provider.createContainerEntityManagerFactory(createPersistenceUnitInfo(), null))
			.orElseThrow(() -> new RuntimeException("Cannot create entity manager"));
	}
	
	private static PersistenceUnitInfo createPersistenceUnitInfo() {
		return new PersistenceUnitInfo() {
			
			@Override
			public void addTransformer(ClassTransformer transformer) {
				return;
			}
			
			@Override
			public boolean excludeUnlistedClasses() {
				return true;
			}
			
			@Override
			public ClassLoader getClassLoader() {
				return Thread.currentThread().getContextClassLoader();
			}
			
			@Override
			public List<URL> getJarFileUrls() {
				return Collections.emptyList();
			}
			
			@Override
			public DataSource getJtaDataSource() {
				return null;
			}
			
			@Override
			public List<String> getManagedClassNames() {
				return Collections.singletonList(President.class.getName());
			}
			
			@Override
			public List<String> getMappingFileNames() {
				return Collections.emptyList();
			}
			
			@Override
			public ClassLoader getNewTempClassLoader() {
				return null;
			}
			
			@Override
			public DataSource getNonJtaDataSource() {
				return DATA_SOURCE;
			}
			
			@Override
			public String getPersistenceProviderClassName() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public String getPersistenceUnitName() {
				return "test";
			}
			
			@Override
			public URL getPersistenceUnitRootUrl() {
				return null;
			}
			
			@Override
			public String getPersistenceXMLSchemaVersion() {
				return "2.1";
			}
			
			@Override
			public Properties getProperties() {
				Properties properties = new Properties();
				properties.setProperty("javax.persistence.schema-generation.database.action", "drop-and-create");
				return properties;
			}
			
			@Override
			public SharedCacheMode getSharedCacheMode() {
				return SharedCacheMode.UNSPECIFIED;
			}
			
			@Override
			public PersistenceUnitTransactionType getTransactionType() {
				return PersistenceUnitTransactionType.RESOURCE_LOCAL;
			}
			
			@Override
			public ValidationMode getValidationMode() {
				return ValidationMode.AUTO;
			}
		};
	}
	
	@Rule
	public final TestRule dbUnit = new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(DATA_SOURCE));
	
	private EntityManager entityManager;
	
	@After
	public void closeEntityManager() {
		this.entityManager.close();
	}
	
	@Before
	public void createEntityManager() {
		this.entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
	}
	
	@Test
	@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
	public void simpleTest() {
		this.entityManager.getTransaction().begin();
		this.entityManager.persist(new President(3L, "Abraham Lincoln"));
		this.entityManager.remove(this.entityManager.getReference(President.class, 2L));
		this.entityManager.getTransaction().commit();
	}
}
