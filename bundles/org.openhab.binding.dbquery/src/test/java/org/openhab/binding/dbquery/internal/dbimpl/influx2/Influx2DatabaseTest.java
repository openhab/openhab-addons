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
package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dbquery.internal.config.InfluxDB2BridgeConfiguration;
import org.openhab.binding.dbquery.internal.domain.Query;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault(value = { DefaultLocation.PARAMETER })
class Influx2DatabaseTest {
    private Influx2Database instance;

    @BeforeEach
    public void setup() {
        instance = new Influx2Database(new InfluxDB2BridgeConfiguration(), new InfluxDBClientFacadeMock());
    }

    @AfterEach
    public void clearDown() {
        instance = null;
    }

    @Test
    public void givenQueryThatReturnsScalarResultGetValidScalarResult() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(InfluxDBClientFacadeMock.SCALAR_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), is(true));
        assertThat(queryResult.getData(), hasSize(1));
        assertThat(queryResult.getData().get(0).getColumnsSize(), is(1));
    }

    @Test
    public void givenQueryThatReturnsMultipleRowsGetValidQueryResult() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(InfluxDBClientFacadeMock.MULTIPLE_ROWS_QUERY,
                QueryParameters.EMPTY, null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), is(true));
        assertThat(queryResult.getData(), hasSize(InfluxDBClientFacadeMock.MULTIPLE_ROWS_SIZE));
        assertThat("contains expected result data", queryResult.getData().stream().allMatch(row -> {
            var value = (String) row.getValue(InfluxDBClientFacadeMock.VALUE_COLUMN);
            var time = row.getValue(InfluxDBClientFacadeMock.TIME_COLUMN);
            return value != null && value.contains(InfluxDBClientFacadeMock.MULTIPLE_ROWS_VALUE_PREFIX) && time != null;
        }));
    }

    @Test
    public void givenQueryThatReturnsErrorGetErroneusResult() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(InfluxDBClientFacadeMock.INVALID_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), equalTo(false));
        assertThat(queryResult.getData(), is(empty()));
    }

    @Test
    public void givenQueryThatReturnsNoRowsGetEmptyResult() throws Exception {
        instance.connect().get();
        Query query = instance.queryFactory().createQuery(InfluxDBClientFacadeMock.EMPTY_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        var queryResult = future.get();

        assertThat(queryResult, notNullValue());
        assertThat(queryResult.isCorrect(), equalTo(true));
        assertThat(queryResult.getData(), is(empty()));
    }

    @Test
    public void givenNotConnectedClientShouldGetIncorrectQuery() {
        Query query = instance.queryFactory().createQuery(InfluxDBClientFacadeMock.SCALAR_QUERY, QueryParameters.EMPTY,
                null);
        var future = instance.executeQuery(query);
        assertThat(future.isCompletedExceptionally(), is(Boolean.TRUE));
    }
}
