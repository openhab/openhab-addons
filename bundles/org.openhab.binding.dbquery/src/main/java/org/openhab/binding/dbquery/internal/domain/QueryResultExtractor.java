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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts a result from {@link QueryResult} to a single value to be used in channels
 * (after being converted that it's not responsability of this class)
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryResultExtractor {
    private final Logger logger = LoggerFactory.getLogger(QueryResultExtractor.class);
    private final QueryConfiguration config;

    public QueryResultExtractor(QueryConfiguration config) {
        this.config = config;
    }

    public ResultValue extractResult(QueryResult queryResult) {
        if (queryResult.isCorrect()) {
            if (config.isScalarResult()) {
                return getScalarValue(queryResult);
            } else {
                return ResultValue.of(queryResult);
            }
        } else {
            return ResultValue.incorrect();
        }
    }

    private ResultValue getScalarValue(QueryResult queryResult) {
        if (validateHasScalarValue(queryResult)) {
            var row = queryResult.getData().get(0);
            @Nullable
            Object value;
            if (config.isScalarColumnDefined()) {
                value = row.getValue(Objects.requireNonNull(config.getScalarColumn()));
            } else {
                value = row.getValue(row.getColumnNames().iterator().next());
            }
            return ResultValue.of(value);
        } else {
            return ResultValue.incorrect();
        }
    }

    private boolean validateHasScalarValue(QueryResult queryResult) {
        boolean valid = false;
        String baseErrorMessage = "Can't get scalar value for result: ";
        if (queryResult.isCorrect()) {
            if (queryResult.getData().size() == 1) {
                boolean oneColumn = queryResult.getData().get(0).getColumnsSize() == 1;
                if (oneColumn || config.isScalarColumnDefined()) {
                    valid = true;
                } else {
                    logger.warn("{} Columns size is {} and scalarColumn isn't defined", baseErrorMessage,
                            queryResult.getData().get(0).getColumnNames().size());
                }
            } else {
                logger.warn("{} Rows size is {}", baseErrorMessage, queryResult.getData().size());
            }
        } else {
            logger.debug("{} Incorrect result", baseErrorMessage);
        }
        return valid;
    }
}
