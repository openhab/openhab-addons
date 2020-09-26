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

@NonNullByDefault
public class ResultRow {
    private static Logger logger = LoggerFactory.getLogger(ResultRow.class);

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
