package org.openhab.binding.dbquery.internal.domain;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.error.DatabaseException;

@NonNullByDefault
public interface Database {
    boolean isConnected();

    CompletableFuture<Boolean> connect();

    CompletableFuture<Boolean> disconnect();

    QueryFactory queryFactory() throws DatabaseException;

    CompletableFuture<QueryResult> executeQuery(Query query);

    public static final Database EMPTY = new Database() {
        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public CompletableFuture<Boolean> connect() {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Boolean> disconnect() {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public QueryFactory queryFactory() {
            return QueryFactory.EMPTY;
        }

        @Override
        public CompletableFuture<QueryResult> executeQuery(Query query) {
            return CompletableFuture.completedFuture(QueryResult.ofIncorrectResult("Empty database"));
        }
    };
}
