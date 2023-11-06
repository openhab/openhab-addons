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
package org.openhab.binding.dbquery.internal.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Query result row
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class ResultRow {
    private final Logger logger = LoggerFactory.getLogger(ResultRow.class);

    private final LinkedHashMap<String, @Nullable Object> values;

    public ResultRow(String columnName, @Nullable Object value) {
        this.values = new LinkedHashMap<>();
        put(columnName, value);
    }

    public ResultRow(Map<String, @Nullable Object> values) {
        this.values = new LinkedHashMap<>();
        values.forEach(this::put);
    }

    public Set<String> getColumnNames() {
        return values.keySet();
    }

    public int getColumnsSize() {
        return values.size();
    }

    public @Nullable Object getValue(String column) {
        return values.get(column);
    }

    public static boolean isValidResultRowType(@Nullable Object object) {
        return object == null || object instanceof String || object instanceof Boolean || object instanceof Number
                || object instanceof byte[] || object instanceof Instant || object instanceof Date
                || object instanceof Duration;
    }

    private void put(String columnName, @Nullable Object value) {
        if (!isValidResultRowType(value)) {
            logger.trace("Value {} of type {} converted to String as not supported internal type in dbquery", value,
                    value.getClass());
            value = value.toString();
        }
        values.put(columnName, value);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
