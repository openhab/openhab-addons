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
package org.openhab.binding.dbquery.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.QueryResultExtractor;
import org.openhab.binding.dbquery.internal.domain.ResultRow;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
class Influx2QueryResultExtractorTest {
    public static final QueryResult ONE_ROW_ONE_COLUMN_RESULT = QueryResult.ofSingleValue("AnyValueName", "value");
    public static final QueryResult SEVERAL_ROWS_COLUMNS_RESULT = QueryResult.of(
            new ResultRow(Map.of("valueName", "value1", "column2", "value2")),
            new ResultRow(Map.of("valueName", "value1", "column2", "value2")));
    public static final QueryResult ONE_ROW_SEVERAL_COLUMNS_RESULT = QueryResult
            .of(new ResultRow(Map.of("valueName", "value1", "column2", "value2")));
    public static final QueryResult INCORRECT_RESULT = QueryResult.ofIncorrectResult("Incorrect result");

    private static final QueryConfiguration SCALAR_VALUE_CONFIG = new QueryConfiguration("query", 10, 10, true, null,
            false);
    private static final QueryConfiguration NON_SCALAR_VALUE_CONFIG = new QueryConfiguration("query", 10, 10, false,
            null, false);
    private static final QueryConfiguration SCALAR_VALUE_CONFIG_WITH_SCALAR_COLUMN = new QueryConfiguration("query", 10,
            10, true, "valueName", false);

    @Test
    void givenAResultWithOneRowAndOneColumnAndScalarConfigurationScalarValueIsReturned() {
        var extracted = new QueryResultExtractor(SCALAR_VALUE_CONFIG).extractResult(ONE_ROW_ONE_COLUMN_RESULT);

        assertThat(extracted.isCorrect(), is(Boolean.TRUE));
        assertThat(extracted.getResult(), is("value"));
    }

    @Test
    void givenAResultWithSeveralRowsAndScalarConfigurationIncorrectValueIsReturned() {
        var extracted = new QueryResultExtractor(SCALAR_VALUE_CONFIG).extractResult(SEVERAL_ROWS_COLUMNS_RESULT);

        assertThat(extracted.isCorrect(), is(false));
        assertThat(extracted.getResult(), nullValue());
    }

    @Test
    void givenAResultWithSeveralColumnsAndScalarConfigurationIncorrectValueIsReturned() {
        var extracted = new QueryResultExtractor(SCALAR_VALUE_CONFIG).extractResult(ONE_ROW_SEVERAL_COLUMNS_RESULT);

        assertThat(extracted.isCorrect(), is(false));
        assertThat(extracted.getResult(), nullValue());
    }

    @Test
    void givenAResultWithSeveralColumnsAndScalarConfigurationAndScalarColumnDefinedValueIsReturned() {
        var extracted = new QueryResultExtractor(SCALAR_VALUE_CONFIG_WITH_SCALAR_COLUMN)
                .extractResult(ONE_ROW_SEVERAL_COLUMNS_RESULT);

        assertThat(extracted.isCorrect(), is(true));
        assertThat(extracted.getResult(), is("value1"));
    }

    @Test
    void givenAResultWithSeveralRowsAndNonScalarConfigQueryResultIsReturned() {
        var extracted = new QueryResultExtractor(NON_SCALAR_VALUE_CONFIG).extractResult(SEVERAL_ROWS_COLUMNS_RESULT);

        assertThat(extracted.isCorrect(), is(true));
        assertThat(extracted.getResult(), is(SEVERAL_ROWS_COLUMNS_RESULT));
    }

    @Test
    void givenAIncorrectResultIncorrectValueIsReturned() {
        var extracted = new QueryResultExtractor(NON_SCALAR_VALUE_CONFIG).extractResult(INCORRECT_RESULT);

        assertThat(extracted.isCorrect(), is(false));
        assertThat(extracted.getResult(), nullValue());
    }
}
