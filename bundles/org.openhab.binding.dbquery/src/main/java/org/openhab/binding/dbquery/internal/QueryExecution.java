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
package org.openhab.binding.dbquery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mantains information of a query that is currently executing
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryExecution {
    private final Logger logger = LoggerFactory.getLogger(QueryExecution.class);
    private final Database database;
    private final String queryString;
    private final QueryConfiguration queryConfiguration;

    private QueryParameters queryParameters;
    private @Nullable QueryResultListener queryResultListener;

    public QueryExecution(Database database, QueryConfiguration queryConfiguration,
            QueryResultListener queryResultListener) {
        this.database = database;
        this.queryString = queryConfiguration.getQuery();
        this.queryConfiguration = queryConfiguration;
        this.queryResultListener = queryResultListener;
        this.queryParameters = QueryParameters.EMPTY;
    }

    public void setQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void execute() {
        Query query;
        if (queryConfiguration.isHasParameters()) {
            query = database.queryFactory().createQuery(queryString, queryParameters, queryConfiguration);
        } else {
            query = database.queryFactory().createQuery(queryString, queryConfiguration);
        }

        logger.trace("Execute query {}", query);
        database.executeQuery(query).thenAccept(this::notifyQueryResult).exceptionally(error -> {
            logger.warn("Error executing query", error);
            notifyQueryResult(QueryResult.ofIncorrectResult("Error executing query"));
            return null;
        });
    }

    private void notifyQueryResult(QueryResult queryResult) {
        var currentQueryResultListener = queryResultListener;
        if (currentQueryResultListener != null) {
            currentQueryResultListener.queryResultReceived(queryResult);
        }
    }

    public void cancel() {
        queryResultListener = null;
    }

    public QueryParameters getQueryParameters() {
        return queryParameters;
    }

    public interface QueryResultListener {
        void queryResultReceived(QueryResult queryResult);
    }

    @Override
    public String toString() {
        return "QueryExecution{" + "queryString='" + queryString + '\'' + ", queryParameters=" + queryParameters + '}';
    }
}
