/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.jdbc.db;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.knowm.yank.Yank;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.dto.ItemsVO;
import org.openhab.persistence.jdbc.dto.JdbcHistoricItem;
import org.openhab.persistence.jdbc.utils.DbMetaData;
import org.openhab.persistence.jdbc.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Database Configuration class.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
@NonNullByDefault
public class JdbcBaseDAO {
    private final Logger logger = LoggerFactory.getLogger(JdbcBaseDAO.class);

    public final Properties databaseProps = new Properties();
    protected String urlSuffix = "";
    public final Map<String, String> sqlTypes = new HashMap<>();

    // Get Database Meta data
    protected @Nullable DbMetaData dbMeta;

    protected String sqlPingDB = "SELECT 1";
    protected String sqlGetDB = "SELECT DATABASE()";
    protected String sqlIfTableExists = "SHOW TABLES LIKE '#searchTable#'";
    protected String sqlCreateNewEntryInItemsTable = "INSERT INTO #itemsManageTable# (ItemName) VALUES ('#itemname#')";
    protected String sqlCreateItemsTableIfNot = "CREATE TABLE IF NOT EXISTS #itemsManageTable# (ItemId INT NOT NULL AUTO_INCREMENT,#colname# #coltype# NOT NULL,PRIMARY KEY (ItemId))";
    protected String sqlDeleteItemsEntry = "DELETE FROM items WHERE ItemName=#itemname#";
    protected String sqlGetItemIDTableNames = "SELECT itemid, itemname FROM #itemsManageTable#";
    protected String sqlGetItemTables = "SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema='#jdbcUriDatabaseName#' AND NOT table_name='#itemsManageTable#'";
    protected String sqlCreateItemTable = "CREATE TABLE IF NOT EXISTS #tableName# (time #tablePrimaryKey# NOT NULL, value #dbType#, PRIMARY KEY(time))";
    protected String sqlInsertItemValue = "INSERT INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, ? ) ON DUPLICATE KEY UPDATE VALUE= ?";

    /********
     * INIT *
     ********/
    public JdbcBaseDAO() {
        initSqlTypes();
        initDbProps();
    }

    /**
     * ## Get high precision by fractal seconds, examples ##
     *
     * mysql > 5.5 + mariadb > 5.2:
     * DROP TABLE FractionalSeconds;
     * CREATE TABLE FractionalSeconds (time TIMESTAMP(3), value TIMESTAMP(3));
     * INSERT INTO FractionalSeconds (time, value) VALUES( NOW(3), '1999-01-09 20:11:11.126' );
     * SELECT time FROM FractionalSeconds ORDER BY time DESC LIMIT 1;
     *
     * mysql <= 5.5 + mariadb <= 5.2: !!! NO high precision and fractal seconds !!!
     * DROP TABLE FractionalSeconds;
     * CREATE TABLE FractionalSeconds (time TIMESTAMP, value TIMESTAMP);
     * INSERT INTO FractionalSeconds (time, value) VALUES( NOW(), '1999-01-09 20:11:11.126' );
     * SELECT time FROM FractionalSeconds ORDER BY time DESC LIMIT 1;
     *
     * derby:
     * DROP TABLE FractionalSeconds;
     * CREATE TABLE FractionalSeconds (time TIMESTAMP, value TIMESTAMP);
     * INSERT INTO FractionalSeconds (time, value) VALUES( CURRENT_TIMESTAMP, '1999-01-09 20:11:11.126' );
     * SELECT time, value FROM FractionalSeconds;
     *
     * H2 + postgreSQL + hsqldb:
     * DROP TABLE FractionalSeconds;
     * CREATE TABLE FractionalSeconds (time TIMESTAMP, value TIMESTAMP);
     * INSERT INTO FractionalSeconds (time, value) VALUES( NOW(), '1999-01-09 20:11:11.126' );
     * SELECT time, value FROM FractionalSeconds;
     *
     * Sqlite:
     * DROP TABLE FractionalSeconds;
     * CREATE TABLE FractionalSeconds (time TIMESTAMP, value TIMESTAMP);
     * INSERT INTO FractionalSeconds (time, value) VALUES( strftime('%Y-%m-%d %H:%M:%f' , 'now' , 'localtime'),
     * '1999-01-09 20:11:11.124' );
     * SELECT time FROM FractionalSeconds ORDER BY time DESC LIMIT 1;
     *
     */

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
        logger.debug("JDBC::initSqlTypes: Initialize the type array");
        sqlTypes.put("CALLITEM", "VARCHAR(200)");
        sqlTypes.put("COLORITEM", "VARCHAR(70)");
        sqlTypes.put("CONTACTITEM", "VARCHAR(6)");
        sqlTypes.put("DATETIMEITEM", "TIMESTAMP");
        sqlTypes.put("DIMMERITEM", "TINYINT");
        sqlTypes.put("IMAGEITEM", "VARCHAR(65500)");// jdbc max 21845
        sqlTypes.put("LOCATIONITEM", "VARCHAR(50)");
        sqlTypes.put("NUMBERITEM", "DOUBLE");
        sqlTypes.put("PLAYERITEM", "VARCHAR(20)");
        sqlTypes.put("ROLLERSHUTTERITEM", "TINYINT");
        sqlTypes.put("STRINGITEM", "VARCHAR(65500)");// jdbc max 21845
        sqlTypes.put("SWITCHITEM", "VARCHAR(6)");
        sqlTypes.put("tablePrimaryKey", "TIMESTAMP");
        sqlTypes.put("tablePrimaryValue", "NOW()");
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     *
     * driverClassName (used with jdbcUrl):
     * Derby: org.apache.derby.jdbc.EmbeddedDriver
     * H2: org.h2.Driver
     * HSQLDB: org.hsqldb.jdbcDriver
     * Jaybird: org.firebirdsql.jdbc.FBDriver
     * MariaDB: org.mariadb.jdbc.Driver
     * MySQL: com.mysql.jdbc.Driver
     * MaxDB: com.sap.dbtech.jdbc.DriverSapDB
     * PostgreSQL: org.postgresql.Driver
     * SyBase: com.sybase.jdbc3.jdbc.SybDriver
     * SqLite: org.sqlite.JDBC
     *
     * dataSourceClassName (for alternative Configuration):
     * Derby: org.apache.derby.jdbc.ClientDataSource
     * H2: org.h2.jdbcx.JdbcDataSource
     * HSQLDB: org.hsqldb.jdbc.JDBCDataSource
     * Jaybird: org.firebirdsql.pool.FBSimpleDataSource
     * MariaDB, MySQL: org.mariadb.jdbc.MySQLDataSource
     * MaxDB: com.sap.dbtech.jdbc.DriverSapDB
     * PostgreSQL: org.postgresql.ds.PGSimpleDataSource
     * SyBase: com.sybase.jdbc4.jdbc.SybDataSource
     * SqLite: org.sqlite.SQLiteDataSource
     *
     * HikariPool - configuration Example:
     * allowPoolSuspension.............false
     * autoCommit......................true
     * catalog.........................
     * connectionInitSql...............
     * connectionTestQuery.............
     * connectionTimeout...............30000
     * dataSource......................
     * dataSourceClassName.............
     * dataSourceJNDI..................
     * dataSourceProperties............{password=<masked>}
     * driverClassName.................
     * healthCheckProperties...........{}
     * healthCheckRegistry.............
     * idleTimeout.....................600000
     * initializationFailFast..........true
     * isolateInternalQueries..........false
     * jdbc4ConnectionTest.............false
     * jdbcUrl.........................jdbc:mysql://192.168.0.1:3306/test
     * leakDetectionThreshold..........0
     * maxLifetime.....................1800000
     * maximumPoolSize.................10
     * metricRegistry..................
     * metricsTrackerFactory...........
     * minimumIdle.....................10
     * password........................<masked>
     * poolName........................HikariPool-0
     * readOnly........................false
     * registerMbeans..................false
     * scheduledExecutorService........
     * threadFactory...................
     * transactionIsolation............
     * username........................xxxx
     * validationTimeout...............5000
     */
    private void initDbProps() {
        // databaseProps.setProperty("dataSource.url", "jdbc:mysql://192.168.0.1:3306/test");
        // databaseProps.setProperty("dataSource.user", "test");
        // databaseProps.setProperty("dataSource.password", "test");

        // Most relevant Performance values
        // maximumPoolSize to 20, minimumIdle to 5, and idleTimeout to 2 minutes.
        // databaseProps.setProperty("maximumPoolSize", ""+maximumPoolSize);
        // databaseProps.setProperty("minimumIdle", ""+minimumIdle);
        // databaseProps.setProperty("idleTimeout", ""+idleTimeout);
        // databaseProps.setProperty("connectionTimeout",""+connectionTimeout);
        // databaseProps.setProperty("idleTimeout", ""+idleTimeout);
        // databaseProps.setProperty("maxLifetime", ""+maxLifetime);
        // databaseProps.setProperty("validationTimeout",""+validationTimeout);
    }

    public void initAfterFirstDbConnection() {
        logger.debug("JDBC::initAfterFirstDbConnection: Initializing step, after db is connected.");
        // Initialize sqlTypes, depending on DB version for example
        dbMeta = new DbMetaData();// get DB information
    }

    public Properties getConnectionProperties() {
        return new Properties(this.databaseProps);
    }

    /**************
     * ITEMS DAOs *
     **************/
    public @Nullable Integer doPingDB() {
        return Yank.queryScalar(sqlPingDB, (Class<@Nullable Integer>) Integer.class, null);
    }

    public @Nullable String doGetDB() {
        return Yank.queryScalar(sqlGetDB, (Class<@Nullable String>) String.class, null);
    }

    public boolean doIfTableExists(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlIfTableExists, new String[] { "#searchTable#" },
                new String[] { vo.getItemsManageTable() });
        logger.debug("JDBC::doIfTableExists sql={}", sql);
        return Yank.queryScalar(sql, (Class<@Nullable String>) String.class, null) != null;
    }

    public Long doCreateNewEntryInItemsTable(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateNewEntryInItemsTable,
                new String[] { "#itemsManageTable#", "#itemname#" },
                new String[] { vo.getItemsManageTable(), vo.getItemname() });
        logger.debug("JDBC::doCreateNewEntryInItemsTable sql={}", sql);
        return Yank.insert(sql, null);
    }

    public ItemsVO doCreateItemsTableIfNot(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemsTableIfNot,
                new String[] { "#itemsManageTable#", "#colname#", "#coltype#" },
                new String[] { vo.getItemsManageTable(), vo.getColname(), vo.getColtype() });
        logger.debug("JDBC::doCreateItemsTableIfNot sql={}", sql);
        Yank.execute(sql, null);
        return vo;
    }

    public void doDeleteItemsEntry(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlDeleteItemsEntry, new String[] { "#itemname#" },
                new String[] { vo.getItemname() });
        logger.debug("JDBC::doDeleteItemsEntry sql={}", sql);
        Yank.execute(sql, null);
    }

    public List<ItemsVO> doGetItemIDTableNames(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlGetItemIDTableNames, new String[] { "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable() });
        logger.debug("JDBC::doGetItemIDTableNames sql={}", sql);
        return Yank.queryBeanList(sql, ItemsVO.class, null);
    }

    public List<ItemsVO> doGetItemTables(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlGetItemTables,
                new String[] { "#jdbcUriDatabaseName#", "#itemsManageTable#" },
                new String[] { vo.getJdbcUriDatabaseName(), vo.getItemsManageTable() });
        logger.debug("JDBC::doGetItemTables sql={}", sql);
        return Yank.queryBeanList(sql, ItemsVO.class, null);
    }

    /*************
     * ITEM DAOs *
     *************/
    public void doUpdateItemTableNames(List<ItemVO> vol) {
        if (!vol.isEmpty()) {
            String sql = updateItemTableNamesProvider(vol);
            Yank.execute(sql, null);
        }
    }

    public void doCreateItemTable(ItemVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemTable,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryKey#" },
                new String[] { vo.getTableName(), vo.getDbType(), sqlTypes.get("tablePrimaryKey") });
        logger.debug("JDBC::doCreateItemTable sql={}", sql);
        Yank.execute(sql, null);
    }

    public void doStoreItemValue(Item item, State itemState, ItemVO vo) {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName(), sqlTypes.get("tablePrimaryValue") });
        Object[] params = new Object[] { storedVO.getValue(), storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} value='{}'", sql, storedVO.getValue());
        Yank.execute(sql, params);
    }

    public void doStoreItemValue(Item item, State itemState, ItemVO vo, ZonedDateTime date) {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#tablePrimaryValue#" }, new String[] { storedVO.getTableName(), "?" });
        java.sql.Timestamp timestamp = new java.sql.Timestamp(date.toInstant().toEpochMilli());
        Object[] params = new Object[] { timestamp, storedVO.getValue(), storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} timestamp={} value='{}'", sql, timestamp, storedVO.getValue());
        Yank.execute(sql, params);
    }

    public List<HistoricItem> doGetHistItemFilterQuery(Item item, FilterCriteria filter, int numberDecimalcount,
            String table, String name, ZoneId timeZone) {
        String sql = histItemFilterQueryProvider(filter, numberDecimalcount, table, name, timeZone);
        logger.debug("JDBC::doGetHistItemFilterQuery sql={}", sql);
        List<Object[]> m = Yank.queryObjectArrays(sql, null);
        if (m == null) {
            logger.debug("JDBC::doGetHistItemFilterQuery Query failed. Returning an empty list.");
            return List.of();
        }
        // we already retrieve the unit here once as it is a very costly operation
        String itemName = item.getName();
        Unit<? extends Quantity<?>> unit = item instanceof NumberItem ? ((NumberItem) item).getUnit() : null;
        return m.stream().map(o -> new JdbcHistoricItem(itemName, objectAsState(item, unit, o[1]), objectAsDate(o[0])))
                .collect(Collectors.<HistoricItem> toList());
    }

    public void doDeleteItemValues(FilterCriteria filter, String table, ZoneId timeZone) {
        String sql = histItemFilterDeleteProvider(filter, table, timeZone);
        logger.debug("JDBC::doDeleteItemValues sql={}", sql);
        Yank.execute(sql, null);
    }

    /*************
     * Providers *
     *************/
    static final DateTimeFormatter JDBC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected String histItemFilterQueryProvider(FilterCriteria filter, int numberDecimalcount, String table,
            String simpleName, ZoneId timeZone) {
        logger.debug(
                "JDBC::getHistItemFilterQueryProvider filter = {}, numberDecimalcount = {}, table = {}, simpleName = {}",
                filter, numberDecimalcount, table, simpleName);

        String filterString = resolveTimeFilter(filter, timeZone);
        filterString += (filter.getOrdering() == Ordering.ASCENDING) ? " ORDER BY time ASC" : " ORDER BY time DESC";
        if (filter.getPageSize() != Integer.MAX_VALUE) {
            filterString += " LIMIT " + filter.getPageNumber() * filter.getPageSize() + "," + filter.getPageSize();
        }
        // SELECT time, ROUND(value,3) FROM number_item_0114 ORDER BY time DESC LIMIT 0,1
        // rounding HALF UP
        String queryString = "NUMBERITEM".equalsIgnoreCase(simpleName) && numberDecimalcount > -1
                ? "SELECT time, ROUND(value," + numberDecimalcount + ") FROM " + table
                : "SELECT time, value FROM " + table;
        if (!filterString.isEmpty()) {
            queryString += filterString;
        }
        logger.debug("JDBC::query queryString = {}", queryString);
        return queryString;
    }

    protected String histItemFilterDeleteProvider(FilterCriteria filter, String table, ZoneId timeZone) {
        logger.debug("JDBC::histItemFilterDeleteProvider filter = {}, table = {}", filter, table);

        String filterString = resolveTimeFilter(filter, timeZone);
        String deleteString = filterString.isEmpty() ? "TRUNCATE TABLE " + table
                : "DELETE FROM " + table + filterString;
        logger.debug("JDBC::delete deleteString = {}", deleteString);
        return deleteString;
    }

    protected String resolveTimeFilter(FilterCriteria filter, ZoneId timeZone) {
        String filterString = "";
        if (filter.getBeginDate() != null) {
            filterString += filterString.isEmpty() ? " WHERE" : " AND";
            filterString += " TIME>'" + JDBC_DATE_FORMAT.format(filter.getBeginDate().withZoneSameInstant(timeZone))
                    + "'";
        }
        if (filter.getEndDate() != null) {
            filterString += filterString.isEmpty() ? " WHERE" : " AND";
            filterString += " TIME<'" + JDBC_DATE_FORMAT.format(filter.getEndDate().withZoneSameInstant(timeZone))
                    + "'";
        }
        return filterString;
    }

    private String updateItemTableNamesProvider(List<ItemVO> namesList) {
        logger.debug("JDBC::updateItemTableNamesProvider namesList.size = {}", namesList.size());
        String queryString = "";
        for (int i = 0; i < namesList.size(); i++) {
            ItemVO it = namesList.get(i);
            queryString += "ALTER TABLE " + it.getTableName() + " RENAME TO " + it.getNewTableName() + ";";
        }
        logger.debug("JDBC::query queryString = {}", queryString);
        return queryString;
    }

    protected ItemVO storeItemValueProvider(Item item, State itemState, ItemVO vo) {
        String itemType = getItemType(item);

        logger.debug("JDBC::storeItemValueProvider: item '{}' as Type '{}' in '{}' with state '{}'", item.getName(),
                itemType, vo.getTableName(), itemState);

        // insertItemValue
        logger.debug("JDBC::storeItemValueProvider: itemState: '{}'", itemState);
        /*
         * !!ATTENTION!!
         *
         * 1. DimmerItem.getStateAs(PercentType.class).toString() always
         * returns 0
         * RollershutterItem.getStateAs(PercentType.class).toString() works
         * as expected
         *
         * 2. (item instanceof ColorItem) == (item instanceof DimmerItem) =
         * true Therefore for instance tests ColorItem always has to be
         * tested before DimmerItem
         *
         * !!ATTENTION!!
         */
        switch (itemType) {
            case "COLORITEM":
                vo.setValueTypes(getSqlTypes().get(itemType), java.lang.String.class);
                vo.setValue(itemState.toString());
                break;
            case "NUMBERITEM":
                State convertedState = itemState;
                if (item instanceof NumberItem && itemState instanceof QuantityType) {
                    Unit<? extends Quantity<?>> unit = ((NumberItem) item).getUnit();
                    if (unit != null && !Units.ONE.equals(unit)) {
                        convertedState = ((QuantityType<?>) itemState).toUnit(unit);
                        if (convertedState == null) {
                            logger.warn(
                                    "JDBC::storeItemValueProvider: Failed to convert state '{}' to unit '{}'. Please check your item definition for correctness.",
                                    itemState, unit);
                            convertedState = itemState;
                        }
                    }
                }
                String it = getSqlTypes().get(itemType);
                if (it == null) {
                    logger.warn("JDBC::storeItemValueProvider: No SQL type defined for item type {}", itemType);
                } else if (it.toUpperCase().contains("DOUBLE")) {
                    vo.setValueTypes(it, java.lang.Double.class);
                    double value = ((Number) convertedState).doubleValue();
                    logger.debug("JDBC::storeItemValueProvider: newVal.doubleValue: '{}'", value);
                    vo.setValue(value);
                } else if (it.toUpperCase().contains("DECIMAL") || it.toUpperCase().contains("NUMERIC")) {
                    vo.setValueTypes(it, java.math.BigDecimal.class);
                    BigDecimal value = BigDecimal.valueOf(((Number) convertedState).doubleValue());
                    logger.debug("JDBC::storeItemValueProvider: newVal.toBigDecimal: '{}'", value);
                    vo.setValue(value);
                } else if (it.toUpperCase().contains("INT")) {
                    vo.setValueTypes(it, java.lang.Integer.class);
                    int value = ((Number) convertedState).intValue();
                    logger.debug("JDBC::storeItemValueProvider: newVal.intValue: '{}'", value);
                    vo.setValue(value);
                } else {// fall back to String
                    vo.setValueTypes(it, java.lang.String.class);
                    logger.warn("JDBC::storeItemValueProvider: itemState: '{}'", convertedState);
                    vo.setValue(convertedState.toString());
                }
                break;
            case "ROLLERSHUTTERITEM":
            case "DIMMERITEM":
                vo.setValueTypes(getSqlTypes().get(itemType), java.lang.Integer.class);
                int value = ((DecimalType) itemState).intValue();
                logger.debug("JDBC::storeItemValueProvider: newVal.intValue: '{}'", value);
                vo.setValue(value);
                break;
            case "DATETIMEITEM":
                vo.setValueTypes(getSqlTypes().get(itemType), java.sql.Timestamp.class);
                java.sql.Timestamp d = new java.sql.Timestamp(
                        ((DateTimeType) itemState).getZonedDateTime().toInstant().toEpochMilli());
                logger.debug("JDBC::storeItemValueProvider: DateTimeItem: '{}'", d);
                vo.setValue(d);
                break;
            case "IMAGEITEM":
                vo.setValueTypes(getSqlTypes().get(itemType), java.lang.String.class);
                String encodedString = item.getState().toFullString();
                logger.debug("JDBC::storeItemValueProvider: ImageItem: '{}'", encodedString);
                vo.setValue(encodedString);
                break;
            default:
                // All other items should return the best format by default
                vo.setValueTypes(getSqlTypes().get(itemType), java.lang.String.class);
                logger.debug("JDBC::storeItemValueProvider: other: itemState: '{}'", itemState);
                vo.setValue(itemState.toString());
                break;
        }
        return vo;
    }

    /*****************
     * H E L P E R S *
     *****************/
    protected State objectAsState(Item item, @Nullable Unit<? extends Quantity<?>> unit, Object v) {
        logger.debug(
                "JDBC::ItemResultHandler::handleResult getState value = '{}', unit = '{}', getClass = '{}', clazz = '{}'",
                v, unit, v.getClass(), v.getClass().getSimpleName());
        if (item instanceof NumberItem) {
            String it = getSqlTypes().get("NUMBERITEM");
            if (it == null) {
                throw new UnsupportedOperationException("No SQL type defined for item type NUMBERITEM");
            }
            if (it.toUpperCase().contains("DOUBLE")) {
                return unit == null ? new DecimalType(((Number) v).doubleValue())
                        : QuantityType.valueOf(((Number) v).doubleValue(), unit);
            } else if (it.toUpperCase().contains("DECIMAL") || it.toUpperCase().contains("NUMERIC")) {
                return unit == null ? new DecimalType((BigDecimal) v)
                        : QuantityType.valueOf(((BigDecimal) v).doubleValue(), unit);
            } else if (it.toUpperCase().contains("INT")) {
                return unit == null ? new DecimalType(objectAsInteger(v))
                        : QuantityType.valueOf(((Integer) v).doubleValue(), unit);
            }
            return unit == null ? DecimalType.valueOf(objectAsString(v)) : QuantityType.valueOf(objectAsString(v));
        } else if (item instanceof DateTimeItem) {
            return new DateTimeType(
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(objectAsLong(v)), ZoneId.systemDefault()));
        } else if (item instanceof ColorItem) {
            return HSBType.valueOf(objectAsString(v));
        } else if (item instanceof DimmerItem || item instanceof RollershutterItem) {
            return new PercentType(objectAsInteger(v));
        } else if (item instanceof ImageItem) {
            return RawType.valueOf(objectAsString(v));
        } else if (item instanceof ContactItem || item instanceof PlayerItem || item instanceof SwitchItem) {
            State state = TypeParser.parseState(item.getAcceptedDataTypes(), ((String) v).toString().trim());
            if (state == null) {
                throw new UnsupportedOperationException("Unable to parse state for item " + item.toString());
            }
            return state;
        } else {
            State state = TypeParser.parseState(item.getAcceptedDataTypes(), ((String) v).toString());
            if (state == null) {
                throw new UnsupportedOperationException("Unable to parse state for item " + item.toString());
            }
            return state;
        }
    }

    protected ZonedDateTime objectAsDate(Object v) {
        if (v instanceof LocalDateTime) {
            return ZonedDateTime.of((LocalDateTime) v, ZoneId.systemDefault());
        } else if (v instanceof java.sql.Timestamp) {
            return ZonedDateTime.ofInstant(((java.sql.Timestamp) v).toInstant(), ZoneId.systemDefault());
        } else if (v instanceof Instant) {
            return ZonedDateTime.ofInstant((Instant) v, ZoneId.systemDefault());
        } else if (v instanceof java.lang.String) {
            return ZonedDateTime.ofInstant(java.sql.Timestamp.valueOf(v.toString()).toInstant(),
                    ZoneId.systemDefault());
        }
        throw new UnsupportedOperationException("Date of type " + v.getClass().getName() + " is not supported");
    }

    protected Long objectAsLong(Object v) {
        if (v instanceof Long) {
            return ((Number) v).longValue();
        } else if (v instanceof java.sql.Date) {
            return ((java.sql.Date) v).getTime();
        } else if (v instanceof LocalDateTime) {
            return ((LocalDateTime) v).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else if (v instanceof Instant) {
            return ((Instant) v).toEpochMilli();
        } else if (v instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) v).getTime();
        }
        throw new UnsupportedOperationException("Date of type " + v.getClass().getName() + " is not supported");
    }

    protected Integer objectAsInteger(Object v) {
        if (v instanceof Byte) {
            return ((Byte) v).intValue();
        }
        return ((Integer) v).intValue();
    }

    protected String objectAsString(Object v) {
        if (v instanceof byte[]) {
            return new String((byte[]) v);
        }
        return ((String) v).toString();
    }

    public String getItemType(Item i) {
        Item item = i;
        String def = "STRINGITEM";
        if (i instanceof GroupItem) {
            item = ((GroupItem) i).getBaseItem();
            if (item == null) {
                // if GroupItem:<ItemType> is not defined in *.items using StringType
                logger.debug(
                        "JDBC::getItemType: Cannot detect ItemType for {} because the GroupItems' base type isn't set in *.items File.",
                        i.getName());
                Iterator<Item> iterator = ((GroupItem) i).getMembers().iterator();
                if (!iterator.hasNext()) {
                    logger.debug(
                            "JDBC::getItemType: No Child-Members of GroupItem {}, use ItemType for STRINGITEM as Fallback",
                            i.getName());
                    return def;
                }
                item = iterator.next();
            }
        }
        String itemType = item.getClass().getSimpleName().toUpperCase();
        logger.debug("JDBC::getItemType: Try to use ItemType {} for Item {}", itemType, i.getName());
        if (sqlTypes.get(itemType) == null) {
            logger.warn(
                    "JDBC::getItemType: No sqlType found for ItemType {}, use ItemType for STRINGITEM as Fallback for {}",
                    itemType, i.getName());
            return def;
        }
        return itemType;
    }

    /******************************
     * public Getters and Setters *
     ******************************/
    public Map<String, String> getSqlTypes() {
        return sqlTypes;
    }

    public String getDataType(Item item) {
        String dataType = sqlTypes.get(getItemType(item));
        if (dataType == null) {
            throw new UnsupportedOperationException("No data type found for " + getItemType(item));
        }
        return dataType;
    }
}
