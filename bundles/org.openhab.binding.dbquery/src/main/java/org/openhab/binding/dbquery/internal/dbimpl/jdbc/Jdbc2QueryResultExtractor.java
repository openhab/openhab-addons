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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.ResultRow;

/**
 * Extracts results from JDbc client query result to a {@link QueryResult}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class Jdbc2QueryResultExtractor implements Function<List<Map<String, @Nullable Object>>, QueryResult> {
    @Override
    public QueryResult apply(List<Map<String, @Nullable Object>> records) {
        var rows = records.stream().map(ResultRow::new).collect(Collectors.toList());
        return QueryResult.of(rows);
    }
}
