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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcSQLException;
import org.openhab.persistence.jdbc.internal.utils.DbMetaData;
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
public class JdbcMariadbDAO extends JdbcBaseDAO {
    private static final String DRIVER_CLASS_NAME = org.mariadb.jdbc.Driver.class.getName();
    @SuppressWarnings("unused")
    private static final String DATA_SOURCE_CLASS_NAME = org.mariadb.jdbc.MariaDbDataSource.class.getName();

    private final Logger logger = LoggerFactory.getLogger(JdbcMariadbDAO.class);

    /********
     * INIT *
     ********/
    public JdbcMariadbDAO() {
        initSqlTypes();
        initDbProps();
        initSqlQueries();
    }

    private void initSqlQueries() {
        logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());
    }

    /**
     * INFO: http://www.java2s.com/Code/Java/Database-SQL-JDBC/StandardSQLDataTypeswithTheirJavaEquivalents.htm
     */
    private void initSqlTypes() {
        logger.debug("JDBC::initSqlTypes: Initialize the type array");

        // MariaDB using utf-8 max = 16383, using 16383-128 = 16255
        sqlTypes.put("IMAGEITEM", "VARCHAR(16255)");
        sqlTypes.put("STRINGITEM", "VARCHAR(16255)");
    }

    /**
     * INFO: https://github.com/brettwooldridge/HikariCP
     */
    private void initDbProps() {
        // Performancetuning
        databaseProps.setProperty("dataSource.cachePrepStmts", "true");
        databaseProps.setProperty("dataSource.prepStmtCacheSize", "250");
        databaseProps.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        databaseProps.setProperty("dataSource.jdbcCompliantTruncation", "false");// jdbc standard max varchar max length
        // of 21845

        // Properties for HikariCP
        // Use driverClassName
        databaseProps.setProperty("driverClassName", DRIVER_CLASS_NAME);
        // driverClassName OR BETTER USE dataSourceClassName
        // databaseProps.setProperty("dataSourceClassName", DATA_SOURCE_CLASS_NAME);
        databaseProps.setProperty("maximumPoolSize", "3");
        databaseProps.setProperty("minimumIdle", "2");
    }

    @Override
    public void initAfterFirstDbConnection() {
        logger.debug("JDBC::initAfterFirstDbConnection: Initializing step, after db is connected.");
        DbMetaData dbMeta = new DbMetaData();
        this.dbMeta = dbMeta;
        // Initialize sqlTypes, depending on DB version for example
        if (dbMeta.isDbVersionGreater(5, 1)) {
            sqlTypes.put("DATETIMEITEM", "TIMESTAMP(3)");
            sqlTypes.put("tablePrimaryKey", "TIMESTAMP(3)");
            sqlTypes.put("tablePrimaryValue", "NOW(3)");
        }
    }

    /**************
     * ITEMS DAOs *
     **************/
    @Override
    public @Nullable Integer doPingDB() throws JdbcSQLException {
        try {
            final @Nullable Long result = Yank.queryScalar(sqlPingDB, Long.class, null);
            return Objects.nonNull(result) ? result.intValue() : null;
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    /*************
     * ITEM DAOs *
     *************/

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
