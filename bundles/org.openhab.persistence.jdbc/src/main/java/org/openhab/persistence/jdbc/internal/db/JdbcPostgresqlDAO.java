/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.persistence.jdbc.internal.db;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.types.State;
import org.openhab.persistence.jdbc.internal.dto.Column;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
import org.openhab.persistence.jdbc.internal.dto.ItemsVO;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcSQLException;
import org.openhab.persistence.jdbc.internal.utils.DbMetaData;
import org.openhab.persistence.jdbc.internal.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended Database Configuration class. Class represents
 * the extended database-specific configuration. Overrides and supplements the
 * default settings from JdbcBaseDAO. Enter only the differences to JdbcBaseDAO here.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
@NonNullByDefault
public class JdbcPostgresqlDAO extends JdbcBaseDAO {
    private static final String DRIVER_CLASS_NAME = org.postgresql.Driver.class.getName();
    @SuppressWarnings("unused")
    private static final String DATA_SOURCE_CLASS_NAME = org.postgresql.ds.PGSimpleDataSource.class.getName();

    private final Logger logger = LoggerFactory.getLogger(JdbcPostgresqlDAO.class);

    /********
     * INIT *
     ********/
    public JdbcPostgresqlDAO() {
        initSqlQueries();
        initSqlTypes();
        initDbProps();
    }

    private void initSqlQueries() {
        logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());
        // System Information Functions: https://www.postgresql.org/docs/9.2/static/functions-info.html
        sqlGetDB = "SELECT CURRENT_DATABASE()";
        sqlIfTableExists = "SELECT * FROM PG_TABLES WHERE TABLENAME='#searchTable#'";
        sqlCreateItemsTableIfNot = "CREATE TABLE IF NOT EXISTS #itemsManageTable# (itemid SERIAL NOT NULL, #colname# #coltype# NOT NULL, CONSTRAINT #itemsManageTable#_pkey PRIMARY KEY (itemid))";
        sqlCreateNewEntryInItemsTable = "INSERT INTO items (itemname) SELECT itemname FROM #itemsManageTable# UNION VALUES ('#itemname#') EXCEPT SELECT itemname FROM items";
        sqlGetItemTables = """
                SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema=(SELECT table_schema \
                FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_name='#itemsManageTable#') AND NOT table_name='#itemsManageTable#'\
                """;
        // The PostgreSQL equivalent to MySQL columns.column_type is data_type (e.g. "timestamp with time zone") and
        // udt_name which contains a shorter alias (e.g. "timestamptz"). We alias data_type as "column_type" and
        // udt_name as "column_type_alias" to be compatible with the 'Column' class used in Yank.queryBeanList
        sqlGetTableColumnTypes = """
                SELECT column_name, data_type as column_type, udt_name as column_type_alias, is_nullable FROM information_schema.columns \
                WHERE table_name='#tableName#' AND table_catalog='#jdbcUriDatabaseName#' AND table_schema=(SELECT table_schema FROM information_schema.tables WHERE table_type='BASE TABLE' \
                AND table_name='#itemsManageTable#')\
                """;
        // NOTICE: on PostgreSql >= 9.5, sqlInsertItemValue query template is modified to do an "upsert" (overwrite
        // existing value). The version check and query change is performed at initAfterFirstDbConnection()
        sqlInsertItemValue = "INSERT INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )";
        sqlAlterTableColumn = "ALTER TABLE #tableName# ALTER COLUMN #columnName# TYPE #columnType#";
    }

    @Override
    public void initAfterFirstDbConnection() {
        logger.debug("JDBC::initAfterFirstDbConnection: Initializing step, after db is connected.");
        DbMetaData dbMeta = new DbMetaData();
        this.dbMeta = dbMeta;
        // Perform "upsert" (on PostgreSql >= 9.5): Overwrite previous VALUE if same TIME (Primary Key) is provided
        // This is the default at JdbcBaseDAO and is equivalent to MySQL: ON DUPLICATE KEY UPDATE VALUE
        // see: https://www.postgresql.org/docs/9.5/sql-insert.html
        if (dbMeta.isDbVersionGreater(9, 4)) {
            logger.debug("JDBC::initAfterFirstDbConnection: Values with the same time will be upserted (Pg >= 9.5)");
            sqlInsertItemValue = """
                    INSERT INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )\
                     ON CONFLICT (TIME) DO UPDATE SET VALUE=EXCLUDED.VALUE\
                    """;
        }
    }

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
        // Initialize the type array
        sqlTypes.put("CALLITEM", "VARCHAR");
        sqlTypes.put("COLORITEM", "VARCHAR");
        sqlTypes.put("CONTACTITEM", "VARCHAR");
        sqlTypes.put("DATETIMEITEM", "TIMESTAMPTZ");
        sqlTypes.put("DIMMERITEM", "SMALLINT");
        sqlTypes.put("IMAGEITEM", "VARCHAR");
        sqlTypes.put("LOCATIONITEM", "VARCHAR");
        sqlTypes.put("NUMBERITEM", "DOUBLE PRECISION");
        sqlTypes.put("PLAYERITEM", "VARCHAR");
        sqlTypes.put("ROLLERSHUTTERITEM", "SMALLINT");
        sqlTypes.put("STRINGITEM", "VARCHAR");
        sqlTypes.put("SWITCHITEM", "VARCHAR");
        sqlTypes.put("tablePrimaryKey", "TIMESTAMPTZ");
        logger.debug("JDBC::initSqlTypes: Initialized the type array sqlTypes={}", sqlTypes.values());
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     */
    private void initDbProps() {
        // Performance:
        // databaseProps.setProperty("dataSource.cachePrepStmts", "true");
        // databaseProps.setProperty("dataSource.prepStmtCacheSize", "250");
        // databaseProps.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");

        // Properties for HikariCP
        databaseProps.setProperty("driverClassName", DRIVER_CLASS_NAME);
        // driverClassName OR BETTER USE dataSourceClassName
        // databaseProps.setProperty("dataSourceClassName", DATA_SOURCE_CLASS_NAME);
        // databaseProps.setProperty("maximumPoolSize", "3");
        // databaseProps.setProperty("minimumIdle", "2");
    }

    /**************
     * ITEMS DAOs *
     **************/
    @Override
    public ItemsVO doCreateItemsTableIfNot(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemsTableIfNot,
                new String[] { "#itemsManageTable#", "#colname#", "#coltype#", "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable(), vo.getColname(), vo.getColtype(), vo.getItemsManageTable() });
        logger.debug("JDBC::doCreateItemsTableIfNot sql={}", sql);
        try {
            Yank.execute(sql, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
        return vo;
    }

    @Override
    public Long doCreateNewEntryInItemsTable(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateNewEntryInItemsTable,
                new String[] { "#itemsManageTable#", "#itemname#" },
                new String[] { vo.getItemsManageTable(), vo.getItemName() });
        logger.debug("JDBC::doCreateNewEntryInItemsTable sql={}", sql);
        try {
            return Yank.insert(sql, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public List<ItemsVO> doGetItemTables(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(this.sqlGetItemTables,
                new String[] { "#itemsManageTable#", "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable(), vo.getItemsManageTable() });
        this.logger.debug("JDBC::doGetItemTables sql={}", sql);
        try {
            return Yank.queryBeanList(sql, ItemsVO.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    /*
     * Override because for PostgreSQL a different query is required with a 3rd argument (itemsManageTable)
     */
    @Override
    public List<Column> doGetTableColumns(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlGetTableColumnTypes,
                new String[] { "#jdbcUriDatabaseName#", "#tableName#", "#itemsManageTable#" },
                new String[] { vo.getJdbcUriDatabaseName(), vo.getTableName(), vo.getItemsManageTable() });
        logger.debug("JDBC::doGetTableColumns sql={}", sql);
        try {
            return Yank.queryBeanList(sql, Column.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    /*************
     * ITEM DAOs *
     *************/

    /*
     * Override since PostgreSQL does not support setting NOT NULL in the same clause as ALTER COLUMN .. TYPE
     */
    @Override
    public void doAlterTableColumn(String tableName, String columnName, String columnType, boolean nullable)
            throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlAlterTableColumn,
                new String[] { "#tableName#", "#columnName#", "#columnType#" },
                new String[] { tableName, columnName, columnType });
        logger.info("JDBC::doAlterTableColumn sql={}", sql);
        try {
            Yank.execute(sql, null);
            if (!nullable) {
                String sql2 = StringUtilsExt.replaceArrayMerge(
                        "ALTER TABLE #tableName# ALTER COLUMN #columnName# SET NOT NULL",
                        new String[] { "#tableName#", "#columnName#" }, new String[] { tableName, columnName });
                logger.info("JDBC::doAlterTableColumn sql={}", sql2);
                Yank.execute(sql2, null);
            }
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo) throws JdbcSQLException {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName(), storedVO.getDbType(), sqlTypes.get("tablePrimaryValue") });
        Object[] params = { storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} value='{}'", sql, storedVO.getValue());
        try {
            Yank.execute(sql, params);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo, ZonedDateTime date) throws JdbcSQLException {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName(), storedVO.getDbType(), "?" });
        java.sql.Timestamp timestamp = new java.sql.Timestamp(date.toInstant().toEpochMilli());
        Object[] params = { timestamp, storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} timestamp={} value='{}'", sql, timestamp, storedVO.getValue());
        try {
            Yank.execute(sql, params);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    /****************************
     * SQL generation Providers *
     ****************************/

    @Override
    protected String histItemFilterQueryProvider(FilterCriteria filter, int numberDecimalcount, String table,
            String simpleName, ZoneId timeZone) {
        logger.debug(
                "JDBC::getHistItemFilterQueryProvider filter = {}, numberDecimalcount = {}, table = {}, simpleName = {}",
                filter.toString(), numberDecimalcount, table, simpleName);

        String filterString = "";
        ZonedDateTime beginDate = filter.getBeginDate();
        if (beginDate != null) {
            filterString += filterString.isEmpty() ? " WHERE" : " AND";
            filterString += " TIME>='" + JDBC_DATE_FORMAT.format(beginDate.withZoneSameInstant(timeZone)) + "'";
        }
        ZonedDateTime endDate = filter.getEndDate();
        if (endDate != null) {
            filterString += filterString.isEmpty() ? " WHERE" : " AND";
            filterString += " TIME<='" + JDBC_DATE_FORMAT.format(endDate.withZoneSameInstant(timeZone)) + "'";
        }
        filterString += (filter.getOrdering() == Ordering.ASCENDING) ? " ORDER BY time ASC" : " ORDER BY time DESC";
        if (filter.getPageSize() != 0x7fffffff) {
            // see:
            // http://www.jooq.org/doc/3.5/manual/sql-building/sql-statements/select-statement/limit-clause/
            filterString += " OFFSET " + filter.getPageNumber() * filter.getPageSize() + " LIMIT "
                    + filter.getPageSize();
        }
        String queryString = "NUMBERITEM".equalsIgnoreCase(simpleName) && numberDecimalcount > -1
                ? "SELECT time, ROUND(CAST (value AS numeric)," + numberDecimalcount + ") FROM " + table
                : "SELECT time, value FROM " + table;
        if (!filterString.isEmpty()) {
            queryString += filterString;
        }
        logger.debug("JDBC::query queryString = {}", queryString);
        return queryString;
    }

    /*****************
     * H E L P E R S *
     *****************/

    /******************************
     * public Getters and Setters *
     ******************************/
}
