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
package org.openhab.binding.dbquery.internal.config;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains fields mapping query things parameters.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryConfiguration {
    public static final int NO_INTERVAL = 0;

    private String query = "";
    private int interval;
    private int timeout;
    private boolean scalarResult;
    private boolean hasParameters;
    private @Nullable String scalarColumn = "";

    public QueryConfiguration() {
        // Used only when configuration is created by reflection using ConfigMapper
    }

    public QueryConfiguration(String query, int interval, int timeout, boolean scalarResult,
            @Nullable String scalarColumn, boolean hasParameters) {
        this.query = query;
        this.interval = interval;
        this.timeout = timeout;
        this.scalarResult = scalarResult;
        this.scalarColumn = scalarColumn;
        this.hasParameters = hasParameters;
    }

    public String getQuery() {
        return query;
    }

    public int getInterval() {
        return interval;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isScalarResult() {
        return scalarResult;
    }

    public @Nullable String getScalarColumn() {
        var currentScalarColumn = scalarColumn;
        return currentScalarColumn != null ? currentScalarColumn : "";
    }

    public boolean isScalarColumnDefined() {
        return !getScalarColumn().isBlank();
    }

    public boolean isHasParameters() {
        return hasParameters;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryConfiguration.class.getSimpleName() + "[", "]").add("query='" + query + "'")
                .add("interval=" + interval).add("timeout=" + timeout).add("scalarResult=" + scalarResult)
                .add("hasParameters=" + hasParameters).add("scalarColumn='" + scalarColumn + "'").toString();
    }
}
