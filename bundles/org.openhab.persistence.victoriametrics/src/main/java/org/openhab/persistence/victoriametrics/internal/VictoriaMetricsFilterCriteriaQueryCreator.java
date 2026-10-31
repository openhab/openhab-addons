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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
        return new VictoriaMetricsQuery(createSeriesSelector(localAlias), begin != null ? begin.toInstant() : null,
                end != null ? end.toInstant() : null);
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
            if (filterValue == null) {
                throw new IllegalArgumentException(
                        "State " + filterState + " cannot be used as a filter, only numeric states are supported");
            }
            result = result.filter(valueFilter(criteria.getOperator(), new BigDecimal(filterValue.toString())));
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
            int comparison = row.value().compareTo(filterValue);
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

    /**
     * Query parameters for the VictoriaMetrics export API
     *
     * @param selector the series selector ({@code match[]})
     * @param start the inclusive start of the time range, or <code>null</code> for no limit
     * @param end the inclusive end of the time range, or <code>null</code> for no limit
     */
    public record VictoriaMetricsQuery(String selector, @Nullable Instant start, @Nullable Instant end) {
    }
}
