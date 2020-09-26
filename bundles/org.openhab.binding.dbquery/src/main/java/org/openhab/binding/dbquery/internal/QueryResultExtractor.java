package org.openhab.binding.dbquery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.QueryConfiguration;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.ResultValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (config.isScalarColumnDefined())
                value = row.getValue(config.getScalarColumn());
            else
                value = row.getValue(row.getColumnNames().iterator().next());
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
                    logger.warn(baseErrorMessage + "Columns size is {} and scalarColumn isn't defined",
                            queryResult.getData().get(0).getColumnNames().size());
                }
            } else {
                logger.warn(baseErrorMessage + "Rows size is {}", queryResult.getData().size());
            }
        } else {
            logger.debug(baseErrorMessage + " Incorrect result");
        }
        return valid;
    }
}
