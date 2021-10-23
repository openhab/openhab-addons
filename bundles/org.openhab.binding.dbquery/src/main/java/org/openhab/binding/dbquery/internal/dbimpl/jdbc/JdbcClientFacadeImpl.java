/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.dbquery.internal.dbimpl.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jdbi.v3.core.Jdbi;
import org.openhab.binding.dbquery.internal.config.JdbcBridgeConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.jdbc.JdbcQueryFactory.JdbcQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Real implementation of {@link JdbcClientFacade}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class JdbcClientFacadeImpl implements JdbcClientFacade {
    private static final int CONNECTION_VALID_TIMEOUT_IN_SECONDS = 5;
    private static final Logger logger = LoggerFactory.getLogger(JdbcClientFacadeImpl.class);

    @Nullable
    private HikariDataSource dataSource;
    @Nullable
    private Jdbi jdbi;
    private final JdbcBridgeConfiguration configuration;
    private final CheckJdbcConnectionConfigurationHelper checkJdbcConnectionConfigurationHelper;

    public JdbcClientFacadeImpl(JdbcBridgeConfiguration configuration) {
        this.configuration = configuration;
        this.checkJdbcConnectionConfigurationHelper = new CheckJdbcConnectionConfigurationHelper(configuration);
    }

    @Override
    public boolean connect() {
        if (checkJdbcConnectionConfigurationHelper.checkSetupCorrectToConnect()) {
            return connectDatabase();
        } else {
            logger.warn("Database configuration/setup is incorrect: {}",
                    checkJdbcConnectionConfigurationHelper.getErrorMessage());
            return false;
        }
    }

    private boolean connectDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(configuration.getUrl());
            config.setUsername(configuration.getUser());
            config.setPassword(configuration.getPassword());
            config.setMaximumPoolSize(configuration.getMaxPoolSize());
            config.setMinimumIdle(configuration.getMinimumIdle());
            dataSource = new HikariDataSource(config);
            boolean connected = isConnected();
            if (connected) {
                jdbi = Jdbi.create(dataSource);
                logger.debug("Successfully connected to JDBC database");
                return true;
            } else {
                logger.warn("Cannot get a valid connection from datasource");
            }
        } catch (RuntimeException e) {
            logger.warn("Error connecting database using {}", configuration, e);
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        var dataSource = this.dataSource;
        if (dataSource != null) {
            try (var connection = dataSource.getConnection()) {
                return connection.isValid(CONNECTION_VALID_TIMEOUT_IN_SECONDS);
            } catch (SQLException e) {
                logger.warn("Error getting a connection and checking if it's valid", e);
            }
        }
        return false;
    }

    @Override
    public boolean disconnect() {
        var dataSource = this.dataSource;
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
        return true;
    }

    @Override
    public List<Map<String, @Nullable Object>> query(JdbcQuery query) {
        var jdbi = this.jdbi;
        if (jdbi != null) {
            return jdbi.withHandle(handle -> {
                var jdbiQuery = handle.createQuery(query.getQuery()).bindMap(query.getParams());
                return jdbiQuery.mapToMap().list();
            });
        } else {
            logger.warn("Can run query because database is currently disconnected");
            // TODO: Better throw
            return Collections.emptyList();
        }
    }
}
