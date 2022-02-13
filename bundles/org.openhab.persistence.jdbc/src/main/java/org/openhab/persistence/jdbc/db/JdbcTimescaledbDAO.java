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

import java.util.Properties;

import org.knowm.yank.Yank;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended Database Configuration class. Class represents the extended database-specific configuration. Overrides and
 * supplements the default settings from JdbcBaseDAO and JdbcPostgresqlDAO.
 *
 * @author Riccardo Nimser-Joseph - Initial contribution
 */
public class JdbcTimescaledbDAO extends JdbcPostgresqlDAO {
    private final Logger logger = LoggerFactory.getLogger(JdbcTimescaledbDAO.class);

    protected String sqlCreateHypertable;

    public JdbcTimescaledbDAO() {
        super();

        initSqlQueries();
    }

    public Properties getDatabaseProperties() {
        Properties properties = new Properties(this.databaseProps);

        // Adjust the jdbc url since the service name 'timescaledb' is only used to differentiate the DAOs
        if (properties.containsKey("jdbcUrl")) {
            properties.put("jdbcUrl", properties.getProperty("jdbcUrl").replace("timescaledb", "postgresql"));
        }

        return properties;
    }

    public void doCreateItemTable(ItemVO vo) {
        String sql;

        sql = StringUtilsExt.replaceArrayMerge(this.sqlCreateItemTable,
                new String[] { "#tableName#", "#dbType#", "#tablePrimaryKey#" },
                new String[] { vo.getTableName(), vo.getDbType(), sqlTypes.get("tablePrimaryKey") });
        this.logger.debug("JDBC::doCreateItemTable sql={}", sql);
        Yank.execute(sql, null);

        sql = StringUtilsExt.replaceArrayMerge(this.sqlCreateHypertable, new String[] { "#tableName#" },
                new String[] { vo.getTableName() });
        this.logger.debug("JDBC::doCreateItemTable sql={}", sql);
        Yank.execute(sql, null);
    }

    private void initSqlQueries() {
        this.logger.debug("JDBC::initSqlQueries: '{}'", this.getClass().getSimpleName());

        this.sqlCreateHypertable = "SELECT create_hypertable('#tableName#', 'time')";
    }
}
