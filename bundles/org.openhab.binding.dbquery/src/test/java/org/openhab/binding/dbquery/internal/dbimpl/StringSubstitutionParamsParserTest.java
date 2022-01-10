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
package org.openhab.binding.dbquery.internal.dbimpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class StringSubstitutionParamsParserTest {

    @Test
    public void testMultipleParameters() {
        String query = "from(bucket:\\\"my-bucket\\\") |> range(start: ${start}) |> fill( value: ${fillValue})";
        var parser = new StringSubstitutionParamsParser(query);
        QueryParameters parameters = new QueryParameters(Map.of("start", "0", "fillValue", "1"));

        var result = parser.getQueryWithParametersReplaced(parameters);

        assertThat(result, equalTo("from(bucket:\\\"my-bucket\\\") |> range(start: 0) |> fill( value: 1)"));
    }

    @Test
    public void testRepeatedParameter() {
        String query = "from(bucket:\\\"my-bucket\\\") |> range(start: ${start}) |> limit(n:${start})";
        var parser = new StringSubstitutionParamsParser(query);
        QueryParameters parameters = new QueryParameters(Map.of("start", "0"));

        var result = parser.getQueryWithParametersReplaced(parameters);

        assertThat(result, equalTo("from(bucket:\\\"my-bucket\\\") |> range(start: 0) |> limit(n:0)"));
    }

    @Test
    public void testNullAndNotDefinedParametersAreSubstitutedByEmptyString() {
        String query = "from(bucket:\\\"my-bucket\\\") |> range(start: ${start}) |> limit(n:${start})";
        var parser = new StringSubstitutionParamsParser(query);
        var paramMap = new HashMap<String, @Nullable Object>();
        paramMap.put("start", null);
        QueryParameters parameters = new QueryParameters(paramMap);

        var result = parser.getQueryWithParametersReplaced(parameters);

        assertThat(result, equalTo("from(bucket:\\\"my-bucket\\\") |> range(start: ) |> limit(n:)"));
    }
}
