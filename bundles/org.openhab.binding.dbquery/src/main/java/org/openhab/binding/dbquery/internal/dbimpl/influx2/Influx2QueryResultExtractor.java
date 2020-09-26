package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.domain.ResultRow;

import com.influxdb.query.FluxRecord;

@NonNullByDefault
public class Influx2QueryResultExtractor implements Function<List<FluxRecord>, QueryResult> {

    @Override
    public QueryResult apply(List<FluxRecord> records) {
        var rows = records.stream().map(Influx2QueryResultExtractor::mapRecord2Row).collect(Collectors.toList());
        return QueryResult.of(rows);
    }

    private static ResultRow mapRecord2Row(FluxRecord record) {
        Map<String, @Nullable Object> values = record.getValues().entrySet().stream()
                .filter(entry -> !Set.of("result", "table").contains(entry.getKey())) // TODO: Change and use a
                                                                                      // fieldName for scalar extraction
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ResultRow(values);
    }
}
