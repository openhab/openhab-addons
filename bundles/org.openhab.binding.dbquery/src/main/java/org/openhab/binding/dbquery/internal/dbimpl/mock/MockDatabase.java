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

@NonNullByDefault
public class MockDatabase implements Database {
    private final Logger logger = LoggerFactory.getLogger(MockDatabase.class);
    private MockQueryFactory mockQueryFactory = new MockQueryFactory();

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
