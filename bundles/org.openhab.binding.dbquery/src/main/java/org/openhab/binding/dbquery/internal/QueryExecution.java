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

@NonNullByDefault
public class QueryExecution {
    private final Logger logger = LoggerFactory.getLogger(QueryExecution.class);
    private final Database database;
    private final String queryString;
    private final QueryConfiguration queryConfiguration;

    private QueryParameters queryParameters = new QueryParameters();
    private @Nullable QueryResultListener queryResultListener;

    public QueryExecution(Database database, QueryConfiguration queryConfiguration,
            QueryResultListener queryResultListener) {
        this.database = database;
        this.queryString = queryConfiguration.getQuery();
        this.queryConfiguration = queryConfiguration;
        this.queryResultListener = queryResultListener;
        this.queryParameters = new QueryParameters();
    }

    public void setQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void execute() {
        Query query;
        if (queryConfiguration.isHasParameters())
            query = database.queryFactory().createQuery(queryString, queryParameters, queryConfiguration);
        else
            query = database.queryFactory().createQuery(queryString, queryConfiguration);

        database.executeQuery(query).thenAccept(this::notifyQueryResult).exceptionally(error -> {
            logger.warn("Error executing query", error);
            notifyQueryResult(QueryResult.ofIncorrectResult("Error executing query"));
            return null;
        });
    }

    private void notifyQueryResult(QueryResult queryResult) {
        if (queryResultListener != null) {
            queryResultListener.queryResultReceived(queryResult);
        }
    }

    public void cancel() {
        queryResultListener = null;
    }

    public interface QueryResultListener {
        void queryResultReceived(QueryResult queryResult);
    }
}
