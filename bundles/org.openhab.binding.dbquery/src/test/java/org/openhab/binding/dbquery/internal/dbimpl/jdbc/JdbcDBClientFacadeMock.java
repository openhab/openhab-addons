/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.openhab.binding.dbquery.internal.error.DatabaseException;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class JdbcDBClientFacadeMock implements JdbcClientFacade {
    public static final String INVALID_QUERY = "invalid";
    public static final String EMPTY_QUERY = "empty";
    public static final String SCALAR_QUERY = "scalar";
    public static final String MULTIPLE_ROWS_QUERY = "multiple";

    public static final int MULTIPLE_ROWS_SIZE = 3;

    private boolean connected;

    @Override
    public boolean connect() {
        connected = true;
        return true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean disconnect() {
        connected = false;
        return true;
    }

    @Override
    public List<Map<String, @Nullable Object>> query(JdbcQueryFactory.JdbcQuery query) {
        if (!connected) {
            throw new DatabaseException("Client not connected");
        }

        if (INVALID_QUERY.equals(query.getQuery())) {
            throw new UnableToCreateStatementException("Unable");
        } else if (EMPTY_QUERY.equals(query.getQuery())) {
            return List.of();
        } else if (SCALAR_QUERY.equals(query.getQuery())) {
            return List.of(Map.of("column1", 1));
        } else if (MULTIPLE_ROWS_QUERY.equals(query.getQuery())) {
            return List.of(Map.of("column1", "value1a", "column2", "value2"),
                    Map.of("column1", "value1b", "column2", "value2b"),
                    Map.of("column1", "value1c", "column2", "value2c"));
        } else {
            throw new UnsupportedOperationException("Query " + query + "not expected");
        }
    }
}
