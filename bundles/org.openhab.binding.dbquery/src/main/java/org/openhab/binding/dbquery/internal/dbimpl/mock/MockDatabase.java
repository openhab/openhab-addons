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
package org.openhab.binding.dbquery.internal.dbimpl.mock;

import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryFactory;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.error.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of {@link Database}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class MockDatabase implements Database {
    private final Logger logger = LoggerFactory.getLogger(MockDatabase.class);
    private final MockQueryFactory mockQueryFactory = new MockQueryFactory();

    private boolean connected = false;

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        connected = true;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> disconnect() {
        connected = false;
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public QueryFactory queryFactory() throws DatabaseException {
        return mockQueryFactory;
    }

    @Override
    public CompletableFuture<QueryResult> executeQuery(Query query) {
        logger.debug("Executing query {}", query);
        var queryResult = QueryResult.ofSingleValue("column", "value");
        return CompletableFuture.completedFuture(queryResult);
    }
}

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
class MockQueryFactory implements QueryFactory {

    @Override
    public Query createQuery(String query, @Nullable QueryConfiguration queryConfiguration) {
        return Query.EMPTY;
    }

    @Override
    public Query createQuery(String query, QueryParameters parameters,
            @Nullable QueryConfiguration queryConfiguration) {
        return Query.EMPTY;
    }
}

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
class MockQuery implements Query {
    private final String query;

    public MockQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MockQuery.class.getSimpleName() + "[", "]").add("query='" + query + "'")
                .toString();
    }
}
