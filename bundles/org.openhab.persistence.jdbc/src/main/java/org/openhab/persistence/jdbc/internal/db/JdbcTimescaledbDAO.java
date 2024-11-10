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

import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
import org.openhab.persistence.jdbc.internal.dto.ItemsVO;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcSQLException;
import org.openhab.persistence.jdbc.internal.utils.StringUtilsExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended Database Configuration class. Class represents the extended database-specific configuration. Overrides and
 * supplements the default settings from JdbcBaseDAO and JdbcPostgresqlDAO.
 *
 * @author Riccardo Nimser-Joseph - Initial contribution
 * @author Dan Cunningham - Fixes and refactoring
 */
@NonNullByDefault
public class JdbcTimescaledbDAO extends JdbcPostgresqlDAO {
    private final Logger logger = LoggerFactory.getLogger(JdbcTimescaledbDAO.class);

    private final String sqlCreateHypertable = "SELECT created FROM create_hypertable('#tableName#', 'time')";
    private final String sqlGetItemTables = "SELECT hypertable_name AS table_name FROM timescaledb_information.hypertables WHERE hypertable_name != '#itemsManageTable#'";

    @Override
    public Properties getConnectionProperties() {
        Properties properties = (Properties) this.databaseProps.clone();
        // Adjust the jdbc url since the service name 'timescaledb' is only used to differentiate the DAOs
        if (properties.containsKey("jdbcUrl")) {
            properties.put("jdbcUrl", properties.getProperty("jdbcUrl").replace("jdbc:timescaledb", "jdbc:postgresql"));
        }
        return properties;
    }

    /*************
     * ITEM DAOs *
     *************/

    @Override
    public void doCreateItemTable(ItemVO vo) throws JdbcSQLException {
        super.doCreateItemTable(vo);
        String sql = StringUtilsExt.replaceArrayMerge(this.sqlCreateHypertable, new String[] { "#tableName#" },
                new String[] { formattedIdentifier(vo.getTableName()) });
        this.logger.debug("JDBC::doCreateItemTable sql={}", sql);
        try {
            Yank.queryScalar(sql, Boolean.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    @Override
    public List<ItemsVO> doGetItemTables(ItemsVO vo) throws JdbcSQLException {
        String sql = StringUtilsExt.replaceArrayMerge(sqlGetItemTables, new String[] { "#itemsManageTable#" },
                new String[] { vo.getItemsManageTable() });
        this.logger.debug("JDBC::doGetItemTables sql={}", sql);
        try {
            return Yank.queryBeanList(sql, ItemsVO.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }
}
