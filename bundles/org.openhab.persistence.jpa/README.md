# Java Persistence API (JPA) Persistence

This service allows you to persist state updates using an SQL or NoSQL database through the [Java Persistence API](https://en.wikipedia.org/wiki/Java_Persistence_API).
The service uses an abstraction layer that allows it to support many available SQL or NoSQL databases.

It creates a table named `historic_item` where all item states are stored.
The item state is stored in a string representation.

The service currently supports Apache Derby, MariaDB, MySQL, and PostgreSQL databases.
Only the embedded Apache Derby database driver is included.
Drivers for other databases must be installed manually.
See below for details.

## Configuration

This service can be configured in the file `services/jpa.cfg`.

| Property     | Default | Required  | Description                                                  |
| ------------ | ------- | :-------: | ------------------------------------------------------------ |
| url          |         |    Yes    | JDBC connection URL. Examples:<br/><br/>`jdbc:derby://hab.local:1527/openhab;create=true`<br/>`jdbc:mariadb://localhost:3306/openhab`<br/>`jdbc:mysql://localhost:3306/openhab`<br/>`jdbc:postgresql://hab.local:5432/openhab` |
| driver       |         |    Yes    | Database driver. Examples:<br/><br/>`com.mysql.jdbc.Driver`<br/>`org.apache.derby.jdbc.ClientDriver`<br/>`org.mariadb.jdbc.Driver`<br/>`org.postgresql.Driver`<br/><br/>Only the Apache Derby driver is included with the service. Drivers for other databases must be installed manually. Normally, JDBC database drivers are packaged as OSGi bundles and can be dropped into the `addons` folder. This has the advantage that users can update their drivers as needed. The following database drivers are known to work:<br/><br/>`postgresql-9.4-1203-jdbc41.jar`<br/>`postgresql-9.4-1206-jdbc41.jar` |
| user         |         | if needed | Database user name for connection                            |
| password     |         | if needed | Database user password for connection                        |
| syncmappings |         | if needed | The OpenJPA synchronize mappings configuration               |

## Adding support for other JPA-supported databases

All item- and event-related configuration is done in the file `persistence/jpa.persist`.

If a database driver is not provided as an OSGi bundle, the technique below can be used to extend the openHAB classpath.

Use the following classpath setup in `start.sh` or `start_debug.sh` of openHAB:

```bash
cp=$(echo lib/*.jar | tr ' ' ':'):$(find $eclipsehome -name "org.eclipse.equinox.launcher_*.jar" | sort | tail -1);
```

This will add all `.jar` files in a folder `lib` in the root of openHAB.

All databases that are supported by JPA can be added.

Define `driver` and `url` according to the database definitions.
