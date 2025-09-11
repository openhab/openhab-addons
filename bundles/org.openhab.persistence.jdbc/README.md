# JDBC Persistence

This service writes and reads item states to and from a number of relational database systems that support [Java Database Connectivity (JDBC)](https://en.wikipedia.org/wiki/Java_Database_Connectivity).
This service allows you to persist state updates using one of several different underlying database services.
It is designed for a maximum of scalability, to store very large amounts of data and still over the years not lose its speed.

You can install JDBC persistence for many supported databases, but **only one JDBC persistence service for a single database type** should be installed and can be configured at any point in time.

The generic design makes it relatively easy for developers to integrate other databases that have JDBC drivers.
The following databases are currently supported and tested:

| Database                                     | Tested Driver / Version                                                                                                                     |
| -------------------------------------------- |---------------------------------------------------------------------------------------------------------------------------------------------|
| [Apache Derby](https://db.apache.org/derby/) | [derby-10.17.1.0.jar](https://mvnrepository.com/artifact/org.apache.derby/derby)                                                            |
| [H2](https://www.h2database.com/)            | [h2-2.3.232.jar](https://mvnrepository.com/artifact/com.h2database/h2)                                                                      |
| [HSQLDB](http://hsqldb.org/)                 | [hsqldb-2.7.4.jar](https://mvnrepository.com/artifact/org.hsqldb/hsqldb)                                                                    |
| [MariaDB](https://mariadb.org/)              | [mariadb-java-client-3.5.5.jar](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)                                    |
| [MySQL](https://www.mysql.com/)              | [mysql-connector-j-9.4.0.jar](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j)                                               |
| [PostgreSQL](https://www.postgresql.org/)    | [postgresql-42.7.7.jar](https://mvnrepository.com/artifact/org.postgresql/postgresql)                                                       |
| [SQLite](https://www.sqlite.org/)            | [sqlite-jdbc-3.50.3.0.jar](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)                                                       |
| [TimescaleDB](https://www.timescale.com/)    | [postgresql-42.7.7.jar](https://mvnrepository.com/artifact/org.postgresql/postgresql)                                                       |
| [OracleDB](https://www.oracle.com/database/) | [com.oracle.database.jdbc.ojdbc11-23.5.0.2407.jar](https://mvnrepository.com/artifact/org.openhab.osgiify/com.oracle.database.jdbc.ojdbc11) |

## Table of Contents

<!-- MarkdownTOC -->

- [Configuration](#configuration)
  - [Minimal Configuration](#minimal-configuration)
  - [Migration from MySQL to JDBC Persistence Services](#migration-from-mysql-to-jdbc-persistence-services)
- [Technical Notes](#technical-notes)
  - [Database Table Schema](#database-table-schema)
  - [Number Precision](#number-precision)
  - [Rounding results](#rounding-results)
  - [Maintenance](#maintenance)
  - [For Developers](#for-developers)
  - [Performance Tests](#performance-tests)

<!-- /MarkdownTOC -->

## Configuration

This service can be configured in the file `services/jdbc.cfg` or through mainUI under the settings of the specific JDBC DB Add-on.
Note that the relevance of the parameters and default values may be different for specific database types.
The listed defaults are used when not overriden by the specific database Add-on.

| Property                    | Default                                                      | Required  | Description                                                  |
| --------------------------- | ------------------------------------------------------------ | :-------: | ------------------------------------------------------------ |
| url                         |                                                              |    Yes    | JDBC URL to establish a connection to your database.  Examples:<br/><br/>`jdbc:derby:./testDerby;create=true`<br/>`jdbc:h2:./testH2`<br/>`jdbc:hsqldb:./testHsqlDb`<br/>`jdbc:mariadb://192.168.0.1:3306/testMariadb`<br/>`jdbc:mysql://192.168.0.1:3306/testMysql?serverTimezone=UTC`<br/>`jdbc:postgresql://192.168.0.1:5432/testPostgresql`<br/>`jdbc:timescaledb://192.168.0.1:5432/testPostgresql`<br/>`jdbc:sqlite:./testSqlite.db`<br/>`jdbc:oracle:thin:@dbname?TNS_ADMIN=./dbname_tns_admin_folder`.<br/><br/>If no database is available it will be created; for example the url `jdbc:h2:./testH2` creates a new H2 database in openHAB folder. Example to create your own MySQL database directly:<br/><br/>`CREATE DATABASE 'yourDB' CHARACTER SET utf8 COLLATE utf8_general_ci;` |
| user                        |                                                              | if needed | database user name                                           |
| password                    |                                                              | if needed | database user password                                       |
| errReconnectThreshold       | 0                                                            |    No     | when the service is deactivated (0 means ignore)             |
| sqltype.CALL                | `VARCHAR(200)`                                               |    No     | All `sqlType` options allow you to change the SQL data type used to store values for different openHAB item states.  See the following links for further information: [mybatis](https://mybatis.github.io/mybatis-3/apidocs/reference/org/apache/ibatis/type/JdbcType.html) [H2](https://www.h2database.com/html/datatypes.html) [PostgresSQL](https://www.postgresql.org/docs/9.3/static/datatype.html) |
| sqltype.COLOR               | `VARCHAR(70)`                                                |    No     | see above                                                    |
| sqltype.CONTACT             | `VARCHAR(6)`                                                 |    No     | see above                                                    |
| sqltype.DATETIME            | `DATETIME`                                                   |    No     | see above                                                    |
| sqltype.DIMMER              | `TINYINT`                                                    |    No     | see above                                                    |
| sqltype.IMAGE               | `VARCHAR(65500)`                                             |    No     | see above                                                    |
| sqltype.LOCATION            | `VARCHAR(50)`                                                |    No     | see above                                                    |
| sqltype.NUMBER              | `DOUBLE`                                                     |    No     | see above                                                    |
| sqltype.PLAYER              | `VARCHAR(20)`                                                |    No     | see above                                                    |
| sqltype.ROLLERSHUTTER       | `TINYINT`                                                    |    No     | see above                                                    |
| sqltype.STRING              | `VARCHAR(65500)`                                             |    No     | see above                                                    |
| sqltype.SWITCH              | `VARCHAR(6)`                                                 |    No     | see above                                                    |
| sqltype.tablePrimaryKey     | `TIMESTAMP`                                                  |    No     | type of `time` column for newly created item tables          |
| sqltype.tablePrimaryValue   | `NOW()`                                                      |    No     | value of `time` column for newly inserted rows               |
| numberDecimalcount          | 3                                                            |    No     | for Itemtype "Number" default decimal digit count            |
| itemsManageTable            | `items`                                                      |    No     | items manage table. For Migration from MySQL Persistence, set to `Items`. |
| tableNamePrefix             | `item`                                                       |    No     | table name prefix. For Migration from MySQL Persistence, set to `Item`. |
| tableUseRealItemNames       | `false`                                                      |    No     | table name prefix generation.  When set to `true`, real item names are used for table names and `tableNamePrefix` is ignored.  When set to `false`, the `tableNamePrefix` is used to generate table names with sequential numbers. |
| tableCaseSensitiveItemNames | `false`                                                      |    No     | table name case. This setting is only applicable when `tableUseRealItemNames` is `true`. When set to `true`, item name case is preserved in table names and no prefix or suffix is added. When set to `false`, table names are lower cased and a numeric suffix is added. Please read [this](#case-sensitive-item-names) before enabling. |
| tableIdDigitCount           | 4                                                            |    No     | when `tableUseRealItemNames` is `false` and thus table names are generated sequentially, this controls how many zero-padded digits are used in the table name.  With the default of 4, the first table name will end with `0001`. For migration from the MySQL persistence service, set this to 0. |
| rebuildTableNames           | false                                                        |    No     | rename existing tables using `tableUseRealItemNames` and `tableIdDigitCount`. USE WITH CARE! Deactivate after Renaming is done! |
| jdbc.maximumPoolSize        | configured per database in package `org.openhab.persistence.jdbc.db.*` |    No     | Some embedded databases can handle only one connection. See [this link](https://github.com/brettwooldridge/HikariCP/issues/256) for more information |
| jdbc.minimumIdle            | see above                                                    |    No     | see above                                                    |
| enableLogTime               | `false`                                                      |    No     | timekeeping                                                  |

All item- and event-related configuration is done in the file `persistence/jdbc.persist`.

To configure this service as the default persistence service for openHAB, add or change the line

```ini
org.openhab.core.persistence:default=jdbc
```

in the file `services/runtime.cfg`.

### Minimal Configuration

services/jdbc.cfg

```ini
url=jdbc:postgresql://192.168.0.1:5432/testPostgresql
```

### Oracle DB Specific Configuration

Oracle connectivity has been tested on an Oracle Always Free Tier Autonomous DB 19c.

You need to configure your database connection to not use an Oracle Wallet, but use the Java Key Store (JKS).
To connect to an Oracle Autonomous Database, use the instructions at <https://www.oracle.com/database/technologies/java-connectivity-to-atp.html#pre-requisites-tab>, under Java Key Stores (JKS).

Your services/jdbc.cfg should contain the following minimal configuration for connecting to an Oracle Autonomous Database:

```ini
url=jdbc:oracle:thin:@dbname?TNS_ADMIN=./dbname_tns_admin_folder
user=openhab
password=openhab_password
```

The `TNS_ADMIN` parameter points to the directory where the the `tnsnames.ora`file, `ojdbc.properties` file and key files (from the ADB wallet download) are located.
Other Oracle DB setups may require different connection parameters.

It is advised to create a specific user with sufficient permissions and space for openHAB persistence.
This is the user that should be in `jdbc.cfg`.
The user default schema will be used.

Default data types for an Oracle DB are different from the general defaults:

| Type                        | Oracle DB Type         |
|-----------------------------|------------------------|
| sqltype.COLOR               | `VARCHAR2(70)`         |
| sqltype.CONTACT             | `VARCHAR2(6)`          |
| sqltype.DATETIME            | `TIMESTAMP`            |
| sqltype.DIMMER              | `NUMBER(3)`            |
| sqltype.IMAGE               | `CLOB`                 |
| sqltype.LOCATION            | `VARCHAR2(50)`         |
| sqltype.NUMBER              | `FLOAT`                |
| sqltype.PLAYER              | `VARCHAR2(20)`         |
| sqltype.ROLLERSHUTTER       | `NUMBER(3)`            |
| sqltype.STRING              | `VARCHAR2(16000 CHAR)` |
| sqltype.SWITCH              | `VARCHAR2(6)`          |
| sqltype.tablePrimaryKey     | `TIMESTAMP`            |
| sqltype.tablePrimaryValue   | `CURRENT_TIME`         |

### Case Sensitive Item Names

To avoid numbered suffixes entirely, `tableUseRealItemNames` and `tableCaseSensitiveItemNames` must both be enabled.
With this configuration, tables are named exactly like their corresponding items.
In order for this to work correctly, the underlying operating system, database server and configuration must support case sensitive table names.
For MySQL, see [MySQL: Identifier Case Sensitivity](https://dev.mysql.com/doc/refman/8.0/en/identifier-case-sensitivity.html) for more information.

Please make sure to have a dedicated schema when using this option, since otherwise table name collisions are more likely to happen.

### Migration from MySQL to JDBC Persistence Services

The JDBC Persistence service can act as a replacement for the MySQL Persistence service.
Here is an example of a configuration for a MySQL database named `testMysql` with user `test` and password `test`:

services/jdbc.cfg

```ini
url=jdbc:mysql://192.168.0.1:3306/testMysql
user=test
password=test
itemsManageTable=Items
tableNamePrefix=Item
tableUseRealItemNames=false
tableIdDigitCount=0
```

Remember to install and uninstall the services you want, and rename `persistence/mysql.persist` to `persistence/jdbc.persist`.

## Technical Notes

### Database Table Schema

The table name schema can be reconfigured after creation, if needed.

The service will create a mapping table to link each item to a table, and a separate table is generated for each item.
The item data tables include time and data values.
The SQL data type used depends on the openHAB item type, and allows the item state to be recovered back into openHAB in the same way it was stored.

With this _per-item_ layout, the scalability and easy maintenance of the database is ensured, even if large amounts of data must be managed.
To rename existing tables, use the parameters `tableNamePrefix`, `tableUseRealItemNames`, `tableIdDigitCount` and `tableCaseSensitiveItemNames` in the configuration.

Please be aware that changing the name of `itemsManageTable` is not supported by the migration.
If this is changed, the table must be renamed manually according to new configured name.

### Number Precision

Default openHAB number items are persisted with SQL datatype `double`.
Internally openHAB uses `BigDecimal`.
If better numerical precision is needed, for example set `sqltype.NUMBER = DECIMAL(max digits, max decimals)`, then on the Java side, the service works with `BigDecimal` without type conversion.
If more come decimals as `max decimals` provides, this persisted value is rounded mathematically correctly.
The SQL types `DECIMAL` or  `NUMERIC` are precise, but to work with `DOUBLE` is faster.

### Rounding results

The results of database queries of number items are rounded to three decimal places by default.
With `numberDecimalcount` decimals can be changed.
Especially if sql types `DECIMAL` or  `NUMERIC` are used for `sqltype.NUMBER`, rounding can be disabled by setting `numberDecimalcount=-1`.

### Maintenance

Some maintenance tools are provided as console commands.

#### List Tables

Tables and corresponding items can be listed with the command `jdbc tables list`.
Per default only tables with some kind of problem are listed.
To list all tables, use the command `jdbc tables list all`.

The list contains table name, item name, row count and status, which can be one of:

- **Valid:** Table is consistent.
- **Item missing:** Table has no corresponding item.
- **Table missing:** Referenced table does not exist.
- **Item and table missing:** Referenced table does not exist nor has corresponding item.
- **Orphan table:** Mapping for table does not exist in index.

#### Clean Inconsistent Items

Some issues can be fixed automatically using the command `jdbc tables clean` (all items having issues) or `jdbc tables clean <itemName>` (single item).
This cleanup operation will remove items from the index (table `Items`) if the referenced table does not exist.

If the item does not exist, the table will be physically deleted, but only if it's empty.
This precaution is taken because items may have existed previously, and the data might still be valuable.
For example, an item for a lost or repurposed sensor could have been deleted from the system while preserving persisted data.
To skip this check for a single item, use `jdbc tables clean <itemName> force` with care.

Prior to performing a `jdbc tables clean` operation, it's recommended to review the result of `jdbc tables list`.

Fixing integrity issues can be useful before performing a migration to another naming scheme.
For example, when migrating to `tableCaseSensitiveItemNames`, an index will no longer exist after the migration:

**Before migration:**

| Table             | Row count | Item   | Status        |
|-------------------|---------: |--------|---------------|
| ActualItem        |         0 |        | Orphan table  |
| TableNotBelonging |         0 |        | Orphan table  |
| item0077          |         0 | MyItem | Table missing |

**After migration:**

| Table             | Row count | Item              | Status        |
|-------------------|---------: |-------------------|---------------|
| ActualItem        |         0 | ActualItem        | Valid         |
| TableNotBelonging |         0 | TableNotBelonging | Item missing  |

This happened:

- `ActualItem` was missing in the index and became valid because it was left untouched, not being a part of the migration. After the migration, it happened to match the name of an existing item, thus it became valid.
- `TableNotBelonging` was also not part of the migration, but since now assumed to match an item, status changed since no item with that name exists.
- `item0077`, being the only correct table name according to previous naming scheme, disappeared from the list since it didn't have a corresponding table, and is now no longer part of any index.

In other words, extracting this information from the index before removing it, can be beneficial in order to understand the issues and possible causes.

#### Reload Index/Schema

Manual changes in the index table, `Items`, will not be picked up automatically for performance reasons.
The same is true when manually adding new item tables or deleting existing ones.
After making such changes, the command `jdbc reload` can be used to reload the index.

#### Check/fix Schema

Use the command `jdbc schema check` to perform an integrity check of the schema.

Identified issues can be fixed automatically using the command `jdbc schema fix` (all items having issues) or `jdbc schema fix <itemName>` (single item).

Issues than can be identified and possibly fixed:

- Wrong column name case (`time` and `name`).
- Wrong column type. Before fixing this, make sure that time-zone is correctly configured.
- Unexpected column (identify only).

### For Developers

- Clearly separated source files for the database-specific part of openHAB logic.
- Code duplication by similar services is prevented.
- Integrating a new SQL and JDBC enabled database is fairly simple.

### Performance Tests

Not necessarily representative of the performance you may experience.

| DATABASE   | FIRST RUN | AVERAGE | FASTEST | SIZE AFTER | COMMENT        |
| ---------- | --------: | ------: | ------: | ---------: | -------------- |
| Derby      |     7.829 |   6.892 |   5.381 |    5.36 MB | local embedded |
| H2         |     1.797 |   2.080 |   1.580 |    0.96 MB | local embedded |
| hsqldb     |     3.474 |   2.104 |   1.310 |    1.23 MB | local embedded |
| mysql      |    11.873 |  11.524 |  10.971 |          - | ext. Server VM |
| postgresql |     8.147 |   7.072 |   6.895 |          - | ext. Server VM |
| sqlite     |     2.406 |   1.249 |   1.137 |    0.28 MB | local embedded |

- Each test ran about 20 Times every 30 seconds.
- openHAB 1.x has ready started for about a Minute.
- the data in seconds for the evaluation are from the console output.

Used a script like this:

```java
var count = 0;
rule "DB STRESS TEST"
when
    Time cron "30 * * * * ?"
then
    if( count = 24) count = 0
    count = count+1
    if( count > 3 && count < 23){
        for( var i=500; i>1; i=i-1){
            postUpdate( NUMBERITEM, i)
            SWITCHITEM.previousState().state
            postUpdate( DIMMERITEM, OFF)
            NUMBERITEM.changedSince( now().minusMinutes(1))
            postUpdate( DIMMERITEM, ON)
        }
    }
end
```
