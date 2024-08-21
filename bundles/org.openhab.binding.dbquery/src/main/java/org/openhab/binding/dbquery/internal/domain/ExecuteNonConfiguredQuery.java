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
package org.openhab.binding.dbquery.internal.domain;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a non defined query in given database
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class ExecuteNonConfiguredQuery {
    private final Logger logger = LoggerFactory.getLogger(ExecuteNonConfiguredQuery.class);
    private final Database database;

    public ExecuteNonConfiguredQuery(Database database) {
        this.database = database;
    }

    public CompletableFuture<QueryResult> execute(String queryString, Map<String, @Nullable Object> parameters,
            Duration timeout) {
        if (!database.isConnected()) {
            return CompletableFuture.completedFuture(QueryResult.ofIncorrectResult("Database not connected"));
        }

        Query query = database.queryFactory().createQuery(queryString, new QueryParameters(parameters),
                createConfiguration(queryString, timeout));
        return database.executeQuery(query);
    }

    public QueryResult executeSynchronously(String queryString, Map<String, @Nullable Object> parameters,
            Duration timeout) {
        var completableFuture = execute(queryString, parameters, timeout);
        try {
            if (timeout.isZero()) {
                return completableFuture.get();
            } else {
                return completableFuture.get(timeout.getSeconds(), TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            logger.debug("Query was interrupted", e);
            Thread.currentThread().interrupt();
            return QueryResult.ofIncorrectResult("Query execution was interrupted");
        } catch (ExecutionException e) {
            logger.warn("Error executing query", e);
            return QueryResult.ofIncorrectResult("Error executing query " + e.getMessage());
        } catch (TimeoutException e) {
            logger.debug("Timeout executing query", e);
            return QueryResult.ofIncorrectResult("Timeout");
        }
    }

    private QueryConfiguration createConfiguration(String query, Duration timeout) {
        return new QueryConfiguration(query, QueryConfiguration.NO_INTERVAL, (int) timeout.getSeconds(), false, null,
                true);
    }
}
