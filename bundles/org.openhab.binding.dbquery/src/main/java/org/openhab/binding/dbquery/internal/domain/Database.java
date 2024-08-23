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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.error.DatabaseException;

/**
 * Abstracts database operations needed for query execution
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface Database {
    boolean isConnected();

    CompletableFuture<Boolean> connect();

    CompletableFuture<Boolean> disconnect();

    QueryFactory queryFactory() throws DatabaseException;

    CompletableFuture<QueryResult> executeQuery(Query query);

    Database EMPTY = new Database() {
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
