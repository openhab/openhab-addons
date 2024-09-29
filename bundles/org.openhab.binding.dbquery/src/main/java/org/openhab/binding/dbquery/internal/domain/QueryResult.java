/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Query result
 * 
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryResult {
    public static final QueryResult NO_RESULT = QueryResult.ofIncorrectResult("No result");

    private final boolean correct;
    private final @Nullable String errorMessage;
    private final List<ResultRow> data;

    private QueryResult(boolean correct, String errorMessage) {
        this.correct = correct;
        this.errorMessage = errorMessage;
        this.data = Collections.emptyList();
    }

    private QueryResult(List<ResultRow> data) {
        this.correct = true;
        this.errorMessage = null;
        this.data = data;
    }

    public static QueryResult ofIncorrectResult(String errorMessage) {
        return new QueryResult(false, errorMessage);
    }

    public static QueryResult of(ResultRow... rows) {
        return new QueryResult(List.of(rows));
    }

    public static QueryResult of(List<ResultRow> rows) {
        return new QueryResult(rows);
    }

    public static QueryResult ofSingleValue(String columnName, Object value) {
        return new QueryResult(List.of(new ResultRow(columnName, value)));
    }

    public boolean isCorrect() {
        return correct;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public List<ResultRow> getData() {
        return data;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QueryResult.class.getSimpleName() + "[", "]").add("correct=" + correct)
                .add("errorMessage='" + errorMessage + "'").add("data=" + data).toString();
    }
}
