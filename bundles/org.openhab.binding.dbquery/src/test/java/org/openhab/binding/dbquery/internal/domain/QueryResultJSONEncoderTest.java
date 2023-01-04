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
package org.openhab.binding.dbquery.internal.domain;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.lessThan;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
class QueryResultJSONEncoderTest {
    public static final double TOLERANCE = 0.001d;
    private final DBQueryJSONEncoder instance = new DBQueryJSONEncoder();
    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();

    @Test
    void givenQueryResultIsSerializedToJson() {
        String json = instance.encode(givenQueryResultWithResults());

        assertThat(jsonParser.parse(json), notNullValue());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void givenQueryResultItsContentIsCorrectlySerializedToJson() {
        String json = instance.encode(givenQueryResultWithResults());

        Map<String, Object> map = gson.fromJson(json, Map.class);
        assertThat(map, Matchers.hasEntry("correct", Boolean.TRUE));
        assertThat(map, Matchers.hasKey("data"));
        List<Map> data = (List<Map>) map.get("data");
        assertThat(data, Matchers.hasSize(2));
        Map firstRow = data.get(0);

        assertReadGivenValuesDecodedFromJson(firstRow);
    }

    private void assertReadGivenValuesDecodedFromJson(Map<?, ?> firstRow) {
        assertThat(firstRow.get("strValue"), is("a string"));

        Object doubleValue = firstRow.get("doubleValue");
        assertThat(doubleValue, instanceOf(Number.class));
        assertThat(((Number) doubleValue).doubleValue(), closeTo(2.3d, TOLERANCE));

        Object intValue = firstRow.get("intValue");
        assertThat(intValue, instanceOf(Number.class));
        assertThat(((Number) intValue).intValue(), is(3));

        Object longValue = firstRow.get("longValue");
        assertThat(longValue, instanceOf(Number.class));
        assertThat(((Number) longValue).longValue(), is(Long.MAX_VALUE));

        Object date = Objects.requireNonNull(firstRow.get("date"));
        assertThat(date, instanceOf(String.class));
        var parsedDate = Instant.from(DateTimeFormatter.ISO_INSTANT.parse((String) date));
        assertThat(Duration.between(parsedDate, Instant.now()).getSeconds(), lessThan(10L));

        Object instant = Objects.requireNonNull(firstRow.get("instant"));
        assertThat(instant, instanceOf(String.class));
        var parsedInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse((String) instant));
        assertThat(Duration.between(parsedInstant, Instant.now()).getSeconds(), lessThan(10L));

        assertThat(firstRow.get("booleanValue"), is(Boolean.TRUE));
        assertThat(firstRow.get("object"), is("an object"));
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    void givenQueryResultWithIncorrectResultItsContentIsCorrectlySerializedToJson() {
        String json = instance.encode(QueryResult.ofIncorrectResult("Incorrect"));

        Map<String, Object> map = gson.fromJson(json, Map.class);
        assertThat(map, Matchers.hasEntry("correct", Boolean.FALSE));
        assertThat(map.get("errorMessage"), is("Incorrect"));
    }

    @Test
    void givenQueryParametersAreCorrectlySerializedToJson() {
        QueryParameters queryParameters = new QueryParameters(givenRowValues());

        String json = instance.encode(queryParameters);

        Map<?, ?> map = Objects.requireNonNull(gson.fromJson(json, Map.class));
        assertReadGivenValuesDecodedFromJson(map);
    }

    private QueryResult givenQueryResultWithResults() {
        return QueryResult.of(new ResultRow(givenRowValues()), new ResultRow(givenRowValues()));
    }

    private Map<String, @Nullable Object> givenRowValues() {
        Map<String, @Nullable Object> values = new HashMap<>();
        values.put("strValue", "a string");
        values.put("doubleValue", 2.3d);
        values.put("intValue", 3);
        values.put("longValue", Long.MAX_VALUE);
        values.put("date", new Date());
        values.put("instant", Instant.now());
        values.put("booleanValue", Boolean.TRUE);
        values.put("object", new Object() {
            @Override
            public String toString() {
                return "an object";
            }
        });
        return values;
    }
}
