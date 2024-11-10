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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
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
public class JdbcH2DAO extends JdbcBaseDAO {
    private static final String DRIVER_CLASS_NAME = org.h2.Driver.class.getName();
    @SuppressWarnings("unused")
    private static final String DATA_SOURCE_CLASS_NAME = org.h2.jdbcx.JdbcDataSource.class.getName();

    private final Logger logger = LoggerFactory.getLogger(JdbcH2DAO.class);

    /********
     * INIT *
     ********/
    public JdbcH2DAO() {
        initSqlQueries();
        initSqlTypes();
        initDbProps();
    }

    private void initSqlQueries() {
        logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());
        sqlIfTableExists = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='#searchTable#'";
        sqlGetItemTables = "SELECT LOWER(table_name) AS table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema='PUBLIC' AND NOT table_name=UPPER('#itemsManageTable#')";
        sqlGetTableColumnTypes = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_schema='#jdbcUriDatabaseName#' AND table_name='#tableName#'";
        // SQL_INSERT_ITEM_VALUE = "INSERT INTO #tableName# (TIME, VALUE) VALUES( NOW(), CAST( ? as #dbType#) )";
        // http://stackoverflow.com/questions/19768051/h2-sql-database-insert-if-the-record-does-not-exist
        sqlInsertItemValue = "MERGE INTO #tableName# (TIME, VALUE) VALUES( #tablePrimaryValue#, CAST( ? as #dbType#) )";
    }

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     */
    private void initDbProps() {
        // Properties for HikariCP
        databaseProps.setProperty("driverClassName", DRIVER_CLASS_NAME);
    }

    /**************
     * ITEMS DAOs *
     **************/

    /*************
     * ITEM DAOs *
     *************/
    @Override
    public void doStoreItemValue(Item item, State itemState, ItemVO vo) throws JdbcSQLException {
        ItemVO storedVO = storeItemValueProvider(item, itemState, vo);
        String sql = StringUtilsExt.replaceArrayMerge(sqlInsertItemValue,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryValue#" },
                new String[] { formattedIdentifier(storedVO.getTableName()), storedVO.getDbType(),
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
                new String[] { formattedIdentifier(storedVO.getTableName()), storedVO.getDbType(), "?" });
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

    /*****************
     * H E L P E R S *
     *****************/

    /******************************
     * public Getters and Setters *
     ******************************/
}
