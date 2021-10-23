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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.config.JdbcBridgeConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.jdbc.JdbcQueryFactory.JdbcQuery;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.error.DatabaseException;

/**
 * JDBC implementation of {@link Database}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class JdbcDatabase implements Database {
    private final JdbcBridgeConfiguration config;
    private final JdbcClientFacade client;
    private final ExecutorService executors;
    private final JdbcQueryFactory queryFactory;

    public JdbcDatabase(JdbcBridgeConfiguration config, JdbcClientFacade jdbcClient) {
        this.config = config;
        this.client = jdbcClient;
        executors = Executors.newSingleThreadScheduledExecutor();
        queryFactory = new JdbcQueryFactory();
    }

    @Override
    public CompletableFuture<Boolean> isConnected() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (JdbcDatabase.this) {
                return client.isConnected();
            }
        }, executors);
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (JdbcDatabase.this) {
                return client.connect();
            }
        }, executors);
    }

    @Override
    public CompletableFuture<Boolean> disconnect() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (JdbcDatabase.this) {
                return client.disconnect();
            }
        }, executors);
    }

    @Override
    public QueryFactory queryFactory() throws DatabaseException {
        return queryFactory;
    }

    @Override
    public CompletableFuture<QueryResult> executeQuery(Query query) {
        try {
            if (query instanceof JdbcQuery) {
                JdbcQuery jdbcQuery = (JdbcQuery) query;
                var results = client.query(jdbcQuery);
                return CompletableFuture.completedFuture(new Jdbc2QueryResultExtractor().apply(results));
            } else {
                return CompletableFuture
                        .completedFuture(QueryResult.ofIncorrectResult("Unnexpected query type " + query));
            }
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
