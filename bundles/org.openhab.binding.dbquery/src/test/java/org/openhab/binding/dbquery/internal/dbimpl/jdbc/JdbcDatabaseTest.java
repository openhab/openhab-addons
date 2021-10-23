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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import java.util.List;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dbquery.internal.config.JdbcBridgeConfiguration;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault(value = { DefaultLocation.PARAMETER })
class JdbcDatabaseTest {
    private JdbcDatabase instance;

    @BeforeEach
    public void setup() {
        instance = new JdbcDatabase(new JdbcBridgeConfiguration(), new JdbcDBClientFacadeMock());
    }

    @AfterEach
    public void clearDown() {
        instance = null;
    }

    @Test
    public void given_query_that_returns_scalar_result_get_valid_scalar_result() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(JdbcDBClientFacadeMock.SCALAR_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), is(true));
        assertThat(queryResult.getData(), hasSize(1));
        assertThat(queryResult.getData().get(0).getColumnsSize(), is(1));
    }

    @Test
    public void given_query_that_returns_multiple_rows_get_valid_query_result() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(JdbcDBClientFacadeMock.MULTIPLE_ROWS_QUERY,
                QueryParameters.EMPTY, null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), is(true));
        assertThat(queryResult.getData(), hasSize(JdbcDBClientFacadeMock.MULTIPLE_ROWS_SIZE));
        assertThat("contains expected result data",
                queryResult.getData().stream()
                        .allMatch(row -> row.getColumnNames().containsAll(List.of("column1", "column2"))
                                && row.getValue("column1").toString().startsWith("value1")
                                && row.getValue("column2").toString().startsWith("value2")));
    }

    @Test
    public void given_query_that_returns_error_get_erroneus_result() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(JdbcDBClientFacadeMock.INVALID_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);

        assertThat(future.isCompletedExceptionally(), Matchers.is(Boolean.TRUE));
    }

    @Test
    public void given_query_that_returns_no_rows_get_empty_result() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(JdbcDBClientFacadeMock.EMPTY_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), equalTo(true));
        assertThat(queryResult.getData(), is(empty()));
    }

    @Test
    public void given_not_connected_client_should_get_incorrect_query() {
        Query query = instance.queryFactory().createQuery(JdbcDBClientFacadeMock.SCALAR_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        assertThat(future.isCompletedExceptionally(), is(Boolean.TRUE));
    }
}
