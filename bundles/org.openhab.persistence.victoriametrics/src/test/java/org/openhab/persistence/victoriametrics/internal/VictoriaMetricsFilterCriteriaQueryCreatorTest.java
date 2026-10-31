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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.victoriametrics.VictoriaMetricsPersistenceService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsFilterCriteriaQueryCreator.VictoriaMetricsQuery;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository.VictoriaMetricsRow;

/**
 * Tests for {@link VictoriaMetricsFilterCriteriaQueryCreator}, adapted from the InfluxDB query creator tests
 *
 * @author Michael Weger - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class VictoriaMetricsFilterCriteriaQueryCreatorTest {
    public static final String ITEM_NAME = "sampleItem";
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private @Mock MetadataRegistry metadataRegistry;

    private VictoriaMetricsFilterCriteriaQueryCreator instance;

    @BeforeEach
    public void before() {
        instance = createInstance("");
    }

    private VictoriaMetricsFilterCriteriaQueryCreator createInstance(String prefix) {
        VictoriaMetricsConfiguration configuration = new VictoriaMetricsConfiguration(
                Map.of(VictoriaMetricsConfiguration.METRIC_PREFIX_PARAM, prefix));
        VictoriaMetricsMetadataService metadataService = new VictoriaMetricsMetadataService(metadataRegistry);
        return new VictoriaMetricsFilterCriteriaQueryCreator(
                new VictoriaMetricsMetricNameResolver(configuration, metadataService));
    }

    @Test
    public void testSimpleItemQueryWithoutParams() {
        FilterCriteria criteria = createBaseCriteria();

        VictoriaMetricsQuery query = instance.createQuery(criteria, null);
        assertThat(query.selector(), equalTo("{__name__=\"sampleItem\",item=\"sampleItem\"}"));
        assertThat(query.start(), nullValue());
        assertThat(query.end(), equalTo(VictoriaMetricsFilterCriteriaQueryCreator.OPEN_END));
    }

    @Test
    public void testMetricPrefix() {
        instance = createInstance("openhab_");
        VictoriaMetricsQuery query = instance.createQuery(createBaseCriteria(), null);
        assertThat(query.selector(), equalTo("{__name__=\"openhab_sampleItem\",item=\"sampleItem\"}"));
    }

    @Test
    public void testRangeCriteria() {
        FilterCriteria criteria = createBaseCriteria();
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime tomorrow = now.plusDays(1);
        criteria.setBeginDate(now);
        criteria.setEndDate(tomorrow);

        VictoriaMetricsQuery query = instance.createQuery(criteria, null);
        assertThat(query.start(), equalTo(now.toInstant()));
        assertThat(query.end(), equalTo(tomorrow.toInstant()));
    }

    @Test
    public void testRangeIsInclusive() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setBeginDate(ZonedDateTime.ofInstant(T0.plusSeconds(1), ZoneOffset.UTC));
        criteria.setEndDate(ZonedDateTime.ofInstant(T0.plusSeconds(3), ZoneOffset.UTC));
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);

        assertThat(times(instance.applyCriteria(rows(0, 1, 2, 3, 4).stream(), criteria)), contains(1L, 2L, 3L));
    }

    @ParameterizedTest
    @CsvSource({ "EQ, 2", "NEQ, 0;1;3;4", "GT, 3;4", "GTE, 2;3;4", "LT, 0;1", "LTE, 0;1;2" })
    public void testValueOperator(FilterCriteria.Operator operator, String expected) {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);
        criteria.setOperator(operator);
        criteria.setState(new PercentType(2));

        List<VictoriaMetricsRow> result = instance.applyCriteria(rows(0, 1, 2, 3, 4).stream(), criteria);
        assertThat(times(result), equalTo(Arrays.stream(expected.split(";")).map(Long::valueOf).toList()));
    }

    @Test
    public void testValueOperatorComparesNumerically() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOperator(FilterCriteria.Operator.EQ);
        criteria.setState(new PercentType(new BigDecimal("2.0")));

        assertThat(times(instance.applyCriteria(rows(0, 1, 2).stream(), criteria)), contains(2L));
    }

    @Test
    public void testNonNumericValueFilterIsRejected() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setState(new StringType("text"));

        assertThrows(IllegalArgumentException.class, () -> instance.applyCriteria(rows(0, 1).stream(), criteria));
    }

    @Test
    public void testPagination() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setPageNumber(2);
        criteria.setPageSize(10);

        List<VictoriaMetricsRow> result = instance.applyCriteria(rows(IntStream.range(0, 35).toArray()).stream(),
                criteria);
        // default ordering is descending: page 0 = 34..25, page 1 = 24..15, page 2 = 14..5
        assertThat(times(result), contains(14L, 13L, 12L, 11L, 10L, 9L, 8L, 7L, 6L, 5L));
    }

    @Test
    public void testPaginationBeyondLastPage() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setPageNumber(Integer.MAX_VALUE);
        criteria.setPageSize(10);

        assertThat(instance.applyCriteria(rows(0, 1, 2).stream(), criteria), empty());
    }

    @Test
    public void testOrdering() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);
        assertThat(times(instance.applyCriteria(rows(2, 0, 1).stream(), criteria)), contains(0L, 1L, 2L));

        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        assertThat(times(instance.applyCriteria(rows(2, 0, 1).stream(), criteria)), contains(2L, 1L, 0L));
    }

    @Test
    public void testPreviousState() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);

        assertThat(times(instance.applyCriteria(rows(0, 3, 1, 2).stream(), criteria)), contains(3L));
    }

    @Test
    public void testPageSizeOneWithOffset() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        criteria.setPageNumber(3);

        assertThat(times(instance.applyCriteria(rows(0, 1, 2, 3, 4).stream(), criteria)), contains(1L));
    }

    @Test
    public void testAliasUsedForItemTagFilter() {
        // Data is stored with TAG_ITEM_NAME = alias, so the label filter must use the alias,
        // not the item name, otherwise queries return no results when alias != itemName.
        FilterCriteria criteria = createBaseCriteria();
        String alias = "aliasName";
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, alias);
        when(metadataRegistry.get(metadataKey)).thenReturn(new Metadata(metadataKey, "measurementName", Map.of()));

        VictoriaMetricsQuery query = instance.createQuery(criteria, alias);
        assertThat(query.selector(), equalTo("{__name__=\"measurementName\",item=\"aliasName\"}"));
    }

    @Test
    public void testMeasurementNameFromMetadata() {
        FilterCriteria criteria = createBaseCriteria();
        MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, ITEM_NAME);

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        VictoriaMetricsQuery query = instance.createQuery(criteria, null);
        assertThat(query.selector(), equalTo("{__name__=\"measurementName\",item=\"sampleItem\"}"));

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        query = instance.createQuery(criteria, null);
        assertThat(query.selector(), equalTo("{__name__=\"sampleItem\",item=\"sampleItem\"}"));
    }

    @Test
    public void testCollidingMetricNamesUseDistinctItemLabel() {
        // both aliases are sanitized to the same metric name, the item label keeps the series apart
        String selector1 = instance.createQuery(createBaseCriteria(), "outside.temp").selector();
        String selector2 = instance.createQuery(createBaseCriteria(), "outside-temp").selector();
        assertThat(selector1, equalTo("{__name__=\"outside_temp\",item=\"outside.temp\"}"));
        assertThat(selector2, equalTo("{__name__=\"outside_temp\",item=\"outside-temp\"}"));

        // a metadata name shared by two items, as in InfluxDB
        for (String itemName : new String[] { "WashingMachineDoor", "WashingMachine_Door" }) {
            MetadataKey metadataKey = new MetadataKey(VictoriaMetricsPersistenceService.SERVICE_NAME, itemName);
            when(metadataRegistry.get(metadataKey)).thenReturn(new Metadata(metadataKey, "door", Map.of()));
            FilterCriteria criteria = new FilterCriteria().setItemName(itemName);
            assertThat(instance.createQuery(criteria, null).selector(),
                    equalTo("{__name__=\"door\",item=\"" + itemName + "\"}"));
        }
    }

    @Test
    public void testItemLabelIsEscaped() {
        VictoriaMetricsQuery query = instance.createQuery(createBaseCriteria(), "a\"b\\c");
        assertThat(query.selector(), equalTo("{__name__=\"a_b_c\",item=\"a\\\"b\\\\c\"}"));
    }

    @Test
    public void testUnpagedOrAscendingQueryIsNotSplit() {
        FilterCriteria criteria = createBaseCriteria();
        assertThat(instance.createQueries(criteria, null, T0), hasSize(1));

        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING).setPageSize(1);
        assertThat(instance.createQueries(criteria, null, T0), hasSize(1));
    }

    @Test
    public void testPreviousStateIsQueriedInWindows() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING).setPageSize(1);

        List<VictoriaMetricsQuery> queries = instance.createQueries(criteria, null, T0);

        // the first window includes future states, the last one the remaining history
        assertThat(queries.stream().map(VictoriaMetricsQuery::start).toList(),
                contains(T0.minus(Duration.ofHours(1)), T0.minus(Duration.ofDays(1)), T0.minus(Duration.ofDays(7)),
                        T0.minus(Duration.ofDays(30)), T0.minus(Duration.ofDays(365)), null));
        assertThat(queries.stream().map(VictoriaMetricsQuery::end).toList(),
                contains(VictoriaMetricsFilterCriteriaQueryCreator.OPEN_END,
                        T0.minus(Duration.ofHours(1)).minusMillis(1), T0.minus(Duration.ofDays(1)).minusMillis(1),
                        T0.minus(Duration.ofDays(7)).minusMillis(1), T0.minus(Duration.ofDays(30)).minusMillis(1),
                        T0.minus(Duration.ofDays(365)).minusMillis(1)));
    }

    @Test
    public void testWindowsAreLimitedByTimeRange() {
        Instant end = T0.minus(Duration.ofDays(10));
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING).setPageSize(1)
                .setBeginDate(ZonedDateTime.ofInstant(end.minus(Duration.ofDays(3)), ZoneOffset.UTC))
                .setEndDate(ZonedDateTime.ofInstant(end, ZoneOffset.UTC));

        List<VictoriaMetricsQuery> queries = instance.createQueries(criteria, null, T0);

        assertThat(queries.stream().map(VictoriaMetricsQuery::start).toList(),
                contains(end.minus(Duration.ofHours(1)), end.minus(Duration.ofDays(1)), end.minus(Duration.ofDays(3))));
        assertThat(queries.get(0).end(), equalTo(end));
        assertThat(queries.get(2).end(), equalTo(end.minus(Duration.ofDays(1)).minusMillis(1)));
    }

    @Test
    public void testIsComplete() {
        FilterCriteria criteria = createBaseCriteria();
        assertThat(instance.isComplete(rows(0, 1), criteria), is(false));

        criteria.setPageSize(2);
        assertThat(instance.isComplete(rows(0), criteria), is(false));
        assertThat(instance.isComplete(rows(0, 1), criteria), is(true));
    }

    private FilterCriteria createBaseCriteria() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(ITEM_NAME);
        return criteria;
    }

    /**
     * Create rows with the given seconds after {@link #T0} as time and value
     */
    private static List<VictoriaMetricsRow> rows(int... seconds) {
        return IntStream.of(seconds)
                .mapToObj(s -> new VictoriaMetricsRow(T0.plusSeconds(s), ITEM_NAME, BigDecimal.valueOf(s))).toList();
    }

    private static List<Long> times(List<VictoriaMetricsRow> rows) {
        return rows.stream().map(row -> row.time().getEpochSecond() - T0.getEpochSecond()).toList();
    }
}
