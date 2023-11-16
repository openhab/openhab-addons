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

import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.knowm.yank.Yank;
import org.knowm.yank.exceptions.YankSQLException;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
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

    private final String sqlCreateHypertable = "SELECT created from create_hypertable('#tableName#', 'time')";

    @Override
    public Properties getConnectionProperties() {
        Properties properties = (Properties) this.databaseProps.clone();
        // Adjust the jdbc url since the service name 'timescaledb' is only used to differentiate the DAOs
        if (properties.containsKey("jdbcUrl")) {
            properties.put("jdbcUrl", properties.getProperty("jdbcUrl").replace("jdbc:timescaledb", "jdbc:postgresql"));
        }
        return properties;
    }

    @Override
    public void doCreateItemTable(ItemVO vo) throws JdbcSQLException {
        super.doCreateItemTable(vo);
        String sql = StringUtilsExt.replaceArrayMerge(this.sqlCreateHypertable, new String[] { "#tableName#" },
                new String[] { vo.getTableName() });
        this.logger.debug("JDBC::doCreateItemTable sql={}", sql);
        try {
            Yank.queryScalar(sql, Boolean.class, null);
        } catch (YankSQLException e) {
            throw new JdbcSQLException(e);
        }
    }
}
