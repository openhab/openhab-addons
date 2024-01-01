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
package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.config.InfluxDB2BridgeConfiguration;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.error.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.query.FluxRecord;

/**
 * Influx2 implementation of {@link Database}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class Influx2Database implements Database {
    private final Logger logger = LoggerFactory.getLogger(Influx2Database.class);
    private final ExecutorService executors;
    private final InfluxDB2BridgeConfiguration config;
    private final InfluxDBClientFacade client;
    private final QueryFactory queryFactory;

    public Influx2Database(InfluxDB2BridgeConfiguration config, InfluxDBClientFacade influxDBClientFacade) {
        this.config = config;
        this.client = influxDBClientFacade;
        executors = Executors.newSingleThreadScheduledExecutor();
        queryFactory = new Influx2QueryFactory();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (Influx2Database.this) {
                return client.connect();
            }
        }, executors);
    }

    @Override
    public CompletableFuture<Boolean> disconnect() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (Influx2Database.this) {
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
            if (query instanceof Influx2QueryFactory.Influx2Query influxQuery) {
                CompletableFuture<QueryResult> asyncResult = new CompletableFuture<>();
                List<FluxRecord> records = new ArrayList<>();
                client.query(influxQuery.getQuery(), (cancellable, record) -> { // onNext
                    records.add(record);
                }, error -> { // onError
                    logger.warn("Error executing query {}", query, error);
                    asyncResult.complete(QueryResult.ofIncorrectResult("Error executing query"));
                }, () -> { // onComplete
                    asyncResult.complete(new Influx2QueryResultExtractor().apply(records));
                });
                return asyncResult;
            } else {
                return CompletableFuture
                        .completedFuture(QueryResult.ofIncorrectResult("Unnexpected query type " + query));
            }
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public String toString() {
        return "Influx2Database{config=" + config + '}';
    }
}
