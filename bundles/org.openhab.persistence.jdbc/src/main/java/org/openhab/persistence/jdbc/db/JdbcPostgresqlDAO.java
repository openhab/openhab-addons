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

import java.time.ZoneId;
import java.util.List;

import org.knowm.yank.Yank;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.types.State;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.dto.ItemsVO;
import org.openhab.persistence.jdbc.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended Database Configuration class. Class represents
 * the extended database-specific configuration. Overrides and supplements the
 * default settings from JdbcBaseDAO. Enter only the differences to JdbcBaseDAO here.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
public class JdbcPostgresqlDAO extends JdbcBaseDAO {
    private final Logger logger = LoggerFactory.getLogger(JdbcPostgresqlDAO.class);

    /********
     * INIT *
     ********/
    public JdbcPostgresqlDAO() {
        super();
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
        sqlGetItemTables = "SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema=(SELECT table_schema "
                + "FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_name='#itemsManageTable#') AND NOT table_name='#itemsManageTable#'";
        // http://stackoverflow.com/questions/17267417/how-do-i-do-an-upsert-merge-insert-on-duplicate-update-in-postgresql
        // for later use, PostgreSql > 9.5 to prevent PRIMARY key violation use:
        // SQL_INSERT_ITEM_VALUE = "INSERT INTO #tableName# (TIME, VALUE) VALUES( NOW(), CAST( ? as #dbType#) ) ON
        // CONFLICT DO NOTHING";
        sqlInsertItemValue = "INSERT INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )";
    }

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
        // Initialize the type array
        sqlTypes.put("CALLITEM", "VARCHAR");
        sqlTypes.put("COLORITEM", "VARCHAR");
        sqlTypes.put("CONTACTITEM", "VARCHAR");
        sqlTypes.put("DATETIMEITEM", "TIMESTAMP");
        sqlTypes.put("DIMMERITEM", "SMALLINT");
        sqlTypes.put("IMAGEITEM", "VARCHAR");
        sqlTypes.put("LOCATIONITEM", "VARCHAR");
        sqlTypes.put("NUMBERITEM", "DOUBLE PRECISION");
        sqlTypes.put("PLAYERITEM", "VARCHAR");
        sqlTypes.put("ROLLERSHUTTERITEM", "SMALLINT");
        sqlTypes.put("STRINGITEM", "VARCHAR");
        sqlTypes.put("SWITCHITEM", "VARCHAR");
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
        databaseProps.setProperty("driverClassName", "org.postgresql.Driver");
        // driverClassName OR BETTER USE dataSourceClassName
        // databaseProps.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        // databaseProps.setProperty("maximumPoolSize", "3");
        // databaseProps.setProperty("minimumIdle", "2");
    }

    /**************
     * ITEMS DAOs *
     **************/
    @Override
    public ItemsVO doCreateItemsTableIfNot(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemsTableIfNot,
                new String[] { "#itemsManageTable#", "#colname#", "#coltype#", "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable(), vo.getColname(), vo.getColtype(), vo.getItemsManageTable() });
        logger.debug("JDBC::doCreateItemsTableIfNot sql={}", sql);
        Yank.execute(sql, null);
        return vo;
    }

    @Override
    public Long doCreateNewEntryInItemsTable(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateNewEntryInItemsTable,
                new String[] { "#itemsManageTable#", "#itemname#" },
                new String[] { vo.getItemsManageTable(), vo.getItemname() });
        logger.debug("JDBC::doCreateNewEntryInItemsTable sql={}", sql);
        return Yank.insert(sql, null);
    }

    @Override
    public List<ItemsVO> doGetItemTables(ItemsVO vo) {
        String sql = StringUtilsExt.replaceArrayMerge(this.sqlGetItemTables,
                new String[] { "#itemsManageTable#", "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable(), vo.getItemsManageTable() });
        this.logger.debug("JDBC::doGetItemTables sql={}", sql);
        return Yank.queryBeanList(sql, ItemsVO.class, null);
    }

    /*************
     * ITEM DAOs *
     *************/
    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo) {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName(), storedVO.getDbType(), sqlTypes.get("tablePrimaryValue") });
        Object[] params = new Object[] { storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} value='{}'", sql, storedVO.getValue());
        Yank.execute(sql, params);
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
