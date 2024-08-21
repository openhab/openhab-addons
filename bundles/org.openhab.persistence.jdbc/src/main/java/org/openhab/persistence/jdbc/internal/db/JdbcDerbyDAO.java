/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
import org.openhab.persistence.jdbc.internal.dto.ItemsVO;
import org.openhab.persistence.jdbc.internal.dto.JdbcHistoricItem;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcSQLException;
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
public class JdbcDerbyDAO extends JdbcBaseDAO {
    private static final String DRIVER_CLASS_NAME = org.apache.derby.jdbc.EmbeddedDriver.class.getName();
    @SuppressWarnings("unused")
    private static final String DATA_SOURCE_CLASS_NAME = org.apache.derby.jdbc.EmbeddedDataSource.class.getName();

    private final Logger logger = LoggerFactory.getLogger(JdbcDerbyDAO.class);

    /********
     * INIT *
     ********/
    public JdbcDerbyDAO() {
        initSqlTypes();
        initDbProps();
        initSqlQueries();
    }

    private void initSqlQueries() {
        logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());
        sqlPingDB = "values 1";
        sqlGetDB = "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY( 'DataDictionaryVersion' )"; // returns version
        sqlIfTableExists = "SELECT * FROM SYS.SYSTABLES WHERE TABLENAME='#searchTable#'";
        sqlCreateItemsTableIfNot = "CREATE TABLE #itemsManageTable# ( ItemId INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), #colname# #coltype# NOT NULL)";
        sqlCreateItemTable = "CREATE TABLE #tableName# (time #tablePrimaryKey# NOT NULL, value #dbType#, PRIMARY KEY(time))";
        // Prevent error against duplicate time value (seldom): No powerful Merge found:
        // http://www.codeproject.com/Questions/162627/how-to-insert-new-record-in-my-table-if-not-exists
        sqlInsertItemValue = "INSERT INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )";
        sqlAlterTableColumn = "ALTER TABLE #tableName# ALTER COLUMN #columnName# SET DATA TYPE #columnType#";
    }

    private void initSqlTypes() {
        sqlTypes.put("DATETIMEITEM", "TIMESTAMP");
        sqlTypes.put("DIMMERITEM", "SMALLINT");
        sqlTypes.put("IMAGEITEM", "VARCHAR(32000)");
        sqlTypes.put("ROLLERSHUTTERITEM", "SMALLINT");
        sqlTypes.put("STRINGITEM", "VARCHAR(32000)");
        sqlTypes.put("tablePrimaryValue", "CURRENT_TIMESTAMP");
        logger.debug("JDBC::initSqlTypes: Initialized the type array sqlTypes={}", sqlTypes.values());
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     */
    private void initDbProps() {
        // Properties for HikariCP
        // Use driverClassName
        databaseProps.setProperty("driverClassName", DRIVER_CLASS_NAME);
        // OR dataSourceClassName
        // databaseProps.setProperty("dataSourceClassName", DATA_SOURCE_CLASS_NAME);
        databaseProps.setProperty("maximumPoolSize", "1");
        databaseProps.setProperty("minimumIdle", "1");
    }

    @Override
    public void initAfterFirstDbConnection() {
        logger.debug("JDBC::initAfterFirstDbConnection: Initializing step, after db is connected.");
        // Initialize sqlTypes, depending on DB version for example
        // derby does not like this... dbMeta = new DbMetaData();// get DB information
    }

    /**************
     * ITEMS DAOs *
     **************/
    @Override
    public @Nullable Integer doPingDB() throws JdbcSQLException {
        try {
            return Yank.queryScalar(sqlPingDB, Integer.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public boolean doIfTableExists(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlIfTableExists, new String[] { "#searchTable#" },
                new String[] { vo.getItemsManageTable().toUpperCase() });
        logger.debug("JDBC::doIfTableExists sql={}", sql);
        try {
            final @Nullable String result = Yank.queryScalar(sql, String.class, null);
            return Objects.nonNull(result);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public Long doCreateNewEntryInItemsTable(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateNewEntryInItemsTable,
                new String[] { "#itemsManageTable#", "#itemname#" },
                new String[] { vo.getItemsManageTable().toUpperCase(), vo.getItemName() });
        logger.debug("JDBC::doCreateNewEntryInItemsTable sql={}", sql);
        try {
            return Yank.insert(sql, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public ItemsVO doCreateItemsTableIfNot(ItemsVO vo) throws JdbcSQLException {
        boolean tableExists = doIfTableExists(vo);
        if (!tableExists) {
            String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemsTableIfNot,
                    new String[] { "#itemsManageTable#", "#colname#", "#coltype#" },
                    new String[] { vo.getItemsManageTable().toUpperCase(), vo.getColname(), vo.getColtype() });
            logger.debug("JDBC::doCreateItemsTableIfNot tableExists={} therefore sql={}", tableExists, sql);
            try {
                Yank.execute(sql, null);
            } catch (YankSQLException e) {
                throw new JdbcSQLException(e);
            }
        } else {
            logger.debug("JDBC::doCreateItemsTableIfNot tableExists={}, did not CREATE TABLE", tableExists);
        }
        return vo;
    }

    /*************
     * ITEM DAOs *
     *************/
    @Override
    public void doCreateItemTable(ItemVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlCreateItemTable,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryKey#" },
                new String[] { vo.getTableName(), vo.getDbType(), sqlTypes.get("tablePrimaryKey") });
        try {
            Yank.execute(sql, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo) throws JdbcSQLException {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { storedVO.getTableName().toUpperCase(), storedVO.getDbType(),
                        sqlTypes.get("tablePrimaryValue") });
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
                new String[] { storedVO.getTableName().toUpperCase(), storedVO.getDbType(), "?" });
        java.sql.Timestamp timestamp = new java.sql.Timestamp(date.toInstant().toEpochMilli());
        Object[] params = { timestamp, storedVO.getValue() };
        logger.debug("JDBC::doStoreItemValue sql={} timestamp={} value='{}'", sql, timestamp, storedVO.getValue());
        try {
            Yank.execute(sql, params);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public List<HistoricItem> doGetHistItemFilterQuery(Item item, FilterCriteria filter, int numberDecimalcount,
            String table, String name, ZoneId timeZone) throws JdbcSQLException {
        String sql = histItemFilterQueryProvider(filter, numberDecimalcount, table, name, timeZone);
        List<Object[]> m;
        try {
            m = Yank.queryObjectArrays(sql, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
        logger.debug("JDBC::doGetHistItemFilterQuery got Array length={}", m.size());
        // we already retrieve the unit here once as it is a very costly operation
        String itemName = item.getName();
        Unit<? extends Quantity<?>> unit = item instanceof NumberItem ni ? ni.getUnit() : null;
        return m.stream().map(o -> {
            logger.debug("JDBC::doGetHistItemFilterQuery 0='{}' 1='{}'", o[0], o[1]);
            return new JdbcHistoricItem(itemName, objectAsState(item, unit, o[1]), objectAsZonedDateTime(o[0]));
        }).collect(Collectors.<HistoricItem> toList());
    }

    /****************************
     * SQL generation Providers *
     ****************************/

    @Override
    protected String histItemFilterQueryProvider(FilterCriteria filter, int numberDecimalcount, String table,
            String simpleName, ZoneId timeZone) {
        logger.debug(
                "JDBC::getHistItemFilterQueryProvider filter = {}, numberDecimalcount = {}, table = {}, simpleName = {}",
                StringUtilsExt.filterToString(filter), numberDecimalcount, table, simpleName);

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
            // TODO: TESTING!!!
            // filterString += " LIMIT " + filter.getPageNumber() *
            // filter.getPageSize() + "," + filter.getPageSize();
            // SELECT time, value FROM ohscriptfiles_sw_ace_paths_0001 ORDER BY
            // time DESC OFFSET 1 ROWS FETCH NEXT 0 ROWS ONLY
            // filterString += " OFFSET " + filter.getPageSize() +" ROWS FETCH
            // FIRST||NEXT " + filter.getPageNumber() * filter.getPageSize() + "
            // ROWS ONLY";
            filterString += " OFFSET " + filter.getPageSize() + " ROWS FETCH FIRST "
                    + (filter.getPageNumber() * filter.getPageSize() + 1) + " ROWS ONLY";
        }

        // http://www.seemoredata.com/en/showthread.php?132-Round-function-in-Apache-Derby
        // simulated round function in Derby: CAST(value 0.0005 AS DECIMAL(15,3))
        // simulated round function in Derby: "CAST(value 0.0005 AS DECIMAL(15,"+numberDecimalcount+"))"

        String queryString = "SELECT time,";
        if ("NUMBERITEM".equalsIgnoreCase(simpleName) && numberDecimalcount > -1) {
            // rounding HALF UP
            queryString += "CAST(value 0.";
            for (int i = 0; i < numberDecimalcount; i++) {
                queryString += "0";
            }
            queryString += "5 AS DECIMAL(31," + numberDecimalcount + "))"; // 31 is DECIMAL max precision
                                                                           // https://db.apache.org/derby/docs/10.0/manuals/develop/develop151.html
        } else {
            queryString += " value FROM " + table.toUpperCase();
        }

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
