/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics.internal;

import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.types.State;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository.VictoriaMetricsRow;

/**
 * Creates a VictoriaMetrics query given an openHAB persistence {@link FilterCriteria}.
 * <p>
 * The series selector and time range are evaluated by VictoriaMetrics when exporting the raw samples. PromQL/MetricsQL
 * comparison operators only apply to values evaluated on a step grid, not to raw samples, so the state filter, ordering
 * and paging are applied to the exported samples by {@link #applyCriteria(Stream, FilterCriteria)}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsFilterCriteriaQueryCreator {
    // VictoriaMetrics supports timestamps up to 2262
    static final Instant OPEN_END = Instant.parse("2200-01-01T00:00:00Z");
    private static final List<Duration> EXPORT_WINDOWS = List.of(Duration.ofHours(1), Duration.ofDays(1),
            Duration.ofDays(7), Duration.ofDays(30), Duration.ofDays(365));

    private final VictoriaMetricsMetricNameResolver metricNameResolver;

    public VictoriaMetricsFilterCriteriaQueryCreator(VictoriaMetricsMetricNameResolver metricNameResolver) {
        this.metricNameResolver = metricNameResolver;
    }

    /**
     * Create query from {@link FilterCriteria}
     *
     * @param criteria Criteria to create query from
     * @param alias the alias the item is stored with, or <code>null</code> to use the item name
     * @return Created query
     */
    public VictoriaMetricsQuery createQuery(FilterCriteria criteria, @Nullable String alias) {
        String itemName = Objects.requireNonNull(criteria.getItemName()); // we checked non-null before
        String localAlias = alias != null ? alias : itemName;
        ZonedDateTime begin = criteria.getBeginDate();
        ZonedDateTime end = criteria.getEndDate();
        // the export API defaults the end to the current time, which would exclude future states
        return new VictoriaMetricsQuery(createSeriesSelector(localAlias), begin != null ? begin.toInstant() : null,
                end != null ? end.toInstant() : OPEN_END);
    }

    /**
     * Create the queries to export the samples for a {@link FilterCriteria}, to be executed in the returned order
     * <p>
     * Descending queries with a page size (e.g. the last state for restore on startup) only need the most recent
     * samples, but exporting the complete history of an item can take many seconds. For these queries, consecutive
     * non-overlapping time windows of increasing size are returned, starting at the end of the time range. The caller
     * stops as soon as the requested page is complete, see {@link #isComplete(List, FilterCriteria)}. This replaces the
     * {@code last()} optimization of the InfluxDB persistence service.
     *
     * @param criteria Criteria to create the queries from
     * @param alias the alias the item is stored with, or <code>null</code> to use the item name
     * @param now the current time
     * @return the queries, covering the complete time range of the criteria
     */
    public List<VictoriaMetricsQuery> createQueries(FilterCriteria criteria, @Nullable String alias, Instant now) {
        VictoriaMetricsQuery query = createQuery(criteria, alias);
        if (criteria.getOrdering() != FilterCriteria.Ordering.DESCENDING
                || criteria.getPageSize() == Integer.MAX_VALUE) {
            return List.of(query);
        }

        Instant start = query.start();
        // with an open end, the first window includes all future states
        Instant anchor = query.end().isAfter(now) ? now : query.end();
        List<VictoriaMetricsQuery> queries = new ArrayList<>();
        Instant windowEnd = query.end();
        for (Duration window : EXPORT_WINDOWS) {
            Instant windowStart = anchor.minus(window);
            if (start != null && !windowStart.isAfter(start)) {
                break;
            }
            queries.add(new VictoriaMetricsQuery(query.selector(), windowStart, windowEnd));
            windowEnd = windowStart.minusMillis(1);
        }
        queries.add(new VictoriaMetricsQuery(query.selector(), start, windowEnd));
        return queries;
    }

    /**
     * Check if the result of the queries executed so far is complete
     *
     * @param result the result of {@link #applyCriteria(Stream, FilterCriteria)} for the samples exported so far
     * @param criteria the criteria the queries were created from
     * @return <code>true</code> if the remaining queries cannot change the result
     */
    public boolean isComplete(List<VictoriaMetricsRow> result, FilterCriteria criteria) {
        // queries are only split for descending queries, so a full page cannot contain samples of later windows
        return criteria.getPageSize() != Integer.MAX_VALUE && result.size() >= criteria.getPageSize();
    }

    /**
     * Create a series selector matching all series of an item
     * <p>
     * Data is stored with {@link VictoriaMetricsConstants#TAG_ITEM_NAME} set to the alias, so the selector must use the
     * alias, not the item name. The label is always part of the selector, because different items can map to the same
     * metric name.
     *
     * @param alias the item name or alias
     * @return the series selector
     */
    public String createSeriesSelector(String alias) {
        return "{" + LABEL_METRIC_NAME + "=\"" + metricNameResolver.getMetricName(alias) + "\"," + TAG_ITEM_NAME + "=\""
                + VictoriaMetricsPoint.escapeLabelValue(alias) + "\"}";
    }

    /**
     * Apply the time range, state filter, ordering and paging of the {@link FilterCriteria} to the exported samples
     *
     * @param rows the exported samples of all series matching the query
     * @param criteria the criteria the query was created from
     * @return the matching samples
     */
    public List<VictoriaMetricsRow> applyCriteria(Stream<VictoriaMetricsRow> rows, FilterCriteria criteria) {
        Stream<VictoriaMetricsRow> result = rows.filter(timeFilter(criteria));

        State filterState = criteria.getState();
        if (filterState != null) {
            Number filterValue = VictoriaMetricsStateConvertUtils.stateToObject(filterState);
            Map<String, BigDecimal> filterComponents = VictoriaMetricsStateConvertUtils.stateToComponents(filterState);
            if (filterValue != null) {
                result = result.filter(valueFilter(criteria.getOperator(), new BigDecimal(filterValue.toString())));
            } else if (filterComponents != null) {
                result = result.filter(componentsFilter(criteria.getOperator(), filterComponents));
            } else if (filterState instanceof StringType) {
                result = result.filter(stringFilter(criteria.getOperator(), filterState.toFullString()));
            } else {
                throw new IllegalArgumentException("State " + filterState
                        + " cannot be used as a filter, only numeric and string states are supported");
            }
        }

        // Samples of several series (e.g. after a tag change) are merged, so sort even if each series is ordered
        Comparator<VictoriaMetricsRow> byTime = Comparator.comparing(VictoriaMetricsRow::time);
        if (criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING) {
            result = result.sorted(byTime.reversed());
        } else {
            result = result.sorted(byTime);
        }

        if (criteria.getPageSize() != Integer.MAX_VALUE) {
            result = result.skip((long) criteria.getPageNumber() * criteria.getPageSize())
                    .limit(criteria.getPageSize());
        }
        return result.toList();
    }

    private Predicate<VictoriaMetricsRow> timeFilter(FilterCriteria criteria) {
        ZonedDateTime begin = criteria.getBeginDate();
        ZonedDateTime end = criteria.getEndDate();
        Instant beginInstant = begin != null ? begin.toInstant() : Instant.MIN;
        Instant endInstant = end != null ? end.toInstant() : Instant.MAX;
        return row -> !row.time().isBefore(beginInstant) && !row.time().isAfter(endInstant);
    }

    private Predicate<VictoriaMetricsRow> valueFilter(FilterCriteria.Operator operator, BigDecimal filterValue) {
        return row -> {
            if (!(row.value() instanceof BigDecimal value)) {
                return false;
            }
            int comparison = value.compareTo(filterValue);
            return switch (operator) {
                case EQ -> comparison == 0;
                case NEQ -> comparison != 0;
                case GT -> comparison > 0;
                case GTE -> comparison >= 0;
                case LT -> comparison < 0;
                case LTE -> comparison <= 0;
            };
        };
    }

    private Predicate<VictoriaMetricsRow> componentsFilter(FilterCriteria.Operator operator,
            Map<String, BigDecimal> filterComponents) {
        // states with several components (e.g. colors) have no order, only equality can be tested
        if (operator != FilterCriteria.Operator.EQ && operator != FilterCriteria.Operator.NEQ) {
            throw new IllegalArgumentException(
                    "Operator " + operator + " is not supported for states with several components");
        }
        return row -> {
            if (!(row.value() instanceof Map<?, ?> components)) {
                return false;
            }
            boolean equal = components.size() == filterComponents.size() && filterComponents.entrySet().stream()
                    .allMatch(entry -> components.get(entry.getKey()) instanceof BigDecimal value
                            && value.compareTo(entry.getValue()) == 0);
            return operator == FilterCriteria.Operator.EQ ? equal : !equal;
        };
    }

    private Predicate<VictoriaMetricsRow> stringFilter(FilterCriteria.Operator operator, String filterValue) {
        // string states are labels without a meaningful order, only equality can be tested
        if (operator != FilterCriteria.Operator.EQ && operator != FilterCriteria.Operator.NEQ) {
            throw new IllegalArgumentException("Operator " + operator + " is not supported for string states");
        }
        return row -> {
            if (!(row.value() instanceof String value)) {
                return false;
            }
            return operator == FilterCriteria.Operator.EQ ? value.equals(filterValue) : !value.equals(filterValue);
        };
    }

    /**
     * Query parameters for the VictoriaMetrics export API
     *
     * @param selector the series selector ({@code match[]})
     * @param start the inclusive start of the time range, or <code>null</code> for no limit
     * @param end the inclusive end of the time range
     */
    public record VictoriaMetricsQuery(String selector, @Nullable Instant start, Instant end) {
    }
}
