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

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.domain.QueryParameters;

/**
 * Provides a parser to substitute query parameters for database like InfluxDB that doesn't support that in it's client.
 * It's not ideal because it's subject to query injection attacks but it does the work if params are from trusted
 * sources.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class StringSubstitutionParamsParser {
    private final Pattern paramPattern = Pattern.compile("\\$\\{([\\w_]*?)}");
    private final String query;

    public StringSubstitutionParamsParser(String query) {
        this.query = query;
    }

    public String getQueryWithParametersReplaced(QueryParameters queryParameters) {
        var matcher = paramPattern.matcher(query);
        int idx = 0;
        StringBuilder substitutedQuery = new StringBuilder();
        while (matcher.find()) {
            String nonParametersPart = query.substring(idx, matcher.start());
            String parameterName = matcher.group(1);
            substitutedQuery.append(nonParametersPart);
            substitutedQuery.append(parameterValue(parameterName, queryParameters));
            idx = matcher.end();
        }
        if (idx < query.length()) {
            substitutedQuery.append(query.substring(idx));
        }

        return substitutedQuery.toString();
    }

    private String parameterValue(String parameterName, QueryParameters queryParameters) {
        var parameter = queryParameters.getParameter(parameterName);
        if (parameter != null) {
            return parameter.toString();
        } else {
            return "";
        }
    }
}
