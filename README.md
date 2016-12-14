[![Build Status](https://travis-ci.org/codegeny/codegeny-junit.png)](https://travis-ci.org/codegeny/codegeny-junit)
[![Code Coverage](https://codecov.io/gh/codegeny/codegeny-junit/branch/master/graph/badge.svg)](https://codecov.io/gh/codegeny/codegeny-junit/branch/master)
[![Code Analysis](https://api.codacy.com/project/badge/Grade/727060bc23ae406f836f76fac448a01f)](https://www.codacy.com/app/codegeny/codegeny-junit)

# codegeny-junit

A collection of JUnit rules (mainly for DBUnit and JPA) that I am using and wanted to share.

```xml
<dependency>
	<groupId>org.codegeny</groupId>
	<artifactId>codegeny-junit</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>

<repositories>
	<repository>
		<id>sonatype-nexus-snapshots</id>
		<url>https://oss.sonatype.org/content/repositories/snapshots/</url>
	</repository>
</repositories>
```

## DBUnitRule

This is a thin layer which drives DBUnit through a JUnit rule and some annotations.

Other projects exist which provide the same kind of functionality:

- [SpringTestDBUnit](https://github.com/springtestdbunit/spring-test-dbunit) for Spring
- [Unitils](http://www.unitils.org/tutorial-database.html)

This DBUnit integration tries to keep things simple and not tied to any other framework.

DBUnitRule only needs 2 things to work:

- A `ResourceLoader` to resolve (xml) data-sets
- A `ConnectionProvider` to provide the database connection(s) to work on

### Simple example

```java
@Rule
public final DBUnitRule dbUnit = new DBUnitRule(
    ResourceLoader.fromClass(this),
    name -> new DatabaseConnection(DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", ""))
);
    
@Test
@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
public void testSomething() { ... }    
```

### ResourceLoader

The `ResourceLoader` is given to the `DBUnitRule` so that it can load the data-set.

```java
public interface ResourceLoader {
	
    InputStream loadResource(String name) throws IOException;
}   
```

Various static methods provide ready-to-use implementations (for classpath, files...)

### ConnectionProvider

The `ConnectionProvider` must also be given to the `DBUnitRule` and provides (named) database connections.

```java
public interface ConnectionProvider {

    IDatabaseConnection getConnection(String name) throws Exception;
}
```

Notice that the return type is DBUnit `IDatabaseConnection`.

If your test only uses one connection, you can provide a `ConnectionProvider` like this:

```java
ConnectionProvider cp = name -> new DatabaseConnection(DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", ""));
```

If you have a `DataSource`:

```java
DataSource dataSource = ...;
ConnectionProvider cp = name -> new DatabaseDataSourceConnection(dataSource);
```

If you are using JNDI with multiple `DataSource`s:

```java
ConnectionProvider cp = name -> new DatabaseDataSourceConnection(new InitialContext(), "java:comp/env/jdbc/" + name));
```

When using multiple `DataSource`, you can specify the connection name via the `@DBUnit` annotation:

```java
@Test
@DBUnit(name = "customerDB" dataSets = "initial-customers.xml", expectedDataSets = "expected-customers.xml")
@DBUnit(name = "orderDB" dataSets = "initial-orders.xml", expectedDataSets = "expected-orders.xml")
public void testSomething() { ... }
```

### Mixing DBUnitRule with embedded containers like OpenEJB

If you are using an embedded container inside your integration tests, you can let that container manage the lifecycle of the `DataSource` and write
just enough code to fetch the `DataSource` from the container to give it to the `DBUnitRule`:

```java
@Properties({
	@Property(key = "defaultDataSource", value = "new://Resource?type=DataSource"),
	@Property(key = "defaultDataSource.JdbcUrl", value = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
	@Property(key = "defaultDataSource.JdbcDriver", value = "org.h2.Driver"),
})
public class MyDatabaseIT {

    @ClassRule
    public static final EJBContainerRule CONTAINER = new EJBContainerRule(); 
	
    @Rule
    public final TestRule ruleChain = RuleChain
        .outerRule(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(CONTAINER.resource(DataSource.class, "defaultDataSource"))))
        .around(new InjectRule(this, CONTAINER))
        .around(new TransactionRule()); // transaction must come after dbunit
        
    @Test
    @DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
    @Transaction
    public void testSomething() { ... }  
}
```

Alternatively, you could just `@Inject` the `DataSource` and use it in the `@DBUnitRule` (but make sure the injection rule is called before the dbunit one):

```java
@Properties({
	@Property(key = "defaultDataSource", value = "new://Resource?type=DataSource"),
	@Property(key = "defaultDataSource.JdbcUrl", value = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
	@Property(key = "defaultDataSource.JdbcDriver", value = "org.h2.Driver"),
})
public class MyDatabaseIT {

    @ClassRule
    public static final EJBContainerRule CONTAINER = new EJBContainerRule(); 

    @Inject
    private DataSource dataSource;
	
    @Rule
    public final TestRule ruleChain = RuleChain
        .outerRule(new InjectRule(this, CONTAINER)) // injection must come before dbunit
        .around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(this.dataSource))
        .around(new TransactionRule()); // transaction must come after dbunit
        
    @Test
    @DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
    @Transaction
    public void testSomething() { ... }  
}
```

If you are using some kind of transaction rule, make sure that it comes after the DBUnit rule (otherwise, changes made inside the test won't be visible by the `DBUnitRule`
and you will get an assertion error).

If you are using OpenEJB ApplicationComposer:

```java
@Resource
private DataSource dataSource;
	
@Rule
public final TestRule ruleChain = RuleChain
    .outerRule(new ApplicationComposerRule(this)) // injection must come before dbunit
    .around(new DBUnitRule(ResourceLoader.fromClass(this), name -> new DatabaseDataSourceConnection(this.dataSource)));
	
@Configuration
public Properties configuration() {
    Properties configuration = new Properties();
    configuration.setProperty("defaultDataSource", "new://Resource?type=DataSource");
    configuration.setProperty("defaultDataSource.JdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    configuration.setProperty("defaultDataSource.JdbcDriver", "org.h2.Driver");
    return configuration;
}

@Module
public Any applicationComposerModule() { ... }

@Test
@DBUnit(dataSets = "initial.xml", expectedDataSets = "expected.xml")
public void testSomething() { ... } 
```

TODO Arquillian

## EntityManagerRule

TODO