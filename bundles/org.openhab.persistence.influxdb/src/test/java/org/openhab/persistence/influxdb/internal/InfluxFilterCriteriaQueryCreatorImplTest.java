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
package org.openhab.persistence.influxdb.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;
import org.openhab.persistence.influxdb.internal.influx1.InfluxDB1FilterCriteriaQueryCreatorImpl;
import org.openhab.persistence.influxdb.internal.influx2.InfluxDB2FilterCriteriaQueryCreatorImpl;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class InfluxFilterCriteriaQueryCreatorImplTest {
    private static final String RETENTION_POLICY = "origin";
    public static final String ITEM_NAME = "sampleItem";

    private static final DateTimeFormatter INFLUX2_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'").withZone(ZoneId.of("UTC"));

    private @Mock InfluxDBConfiguration influxDBConfiguration;
    private @Mock MetadataRegistry metadataRegistry;

    private InfluxDB1FilterCriteriaQueryCreatorImpl instanceV1;
    private InfluxDB2FilterCriteriaQueryCreatorImpl instanceV2;

    @BeforeEach
    public void before() {
        InfluxDBMetadataService influxDBMetadataService = new InfluxDBMetadataService(metadataRegistry);
        instanceV1 = new InfluxDB1FilterCriteriaQueryCreatorImpl(influxDBConfiguration, influxDBMetadataService);
        instanceV2 = new InfluxDB2FilterCriteriaQueryCreatorImpl(influxDBConfiguration, influxDBMetadataService);
    }

    @AfterEach
    public void after() {
        instanceV1 = null;
        instanceV2 = null;
        influxDBConfiguration = null;
        metadataRegistry = null;
    }

    @Test
    public void testSimpleItemQueryWithoutParams() {
        FilterCriteria criteria = createBaseCriteria();

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1,
                equalTo("SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" ORDER BY time DESC;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> sort(desc:true, columns:["_time"])"""));
    }

    @Test
    public void testRangeCriteria() {
        FilterCriteria criteria = createBaseCriteria();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tomorrow = now.plus(1, ChronoUnit.DAYS);
        criteria.setBeginDate(now);
        criteria.setEndDate(tomorrow);

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        String expectedQueryV1 = String.format(
                "SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" WHERE time >= '%s' AND time <= '%s' ORDER BY time DESC;",
                now.toInstant(), tomorrow.toInstant());
        assertThat(queryV1, equalTo(expectedQueryV1));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        String expectedQueryV2 = String.format("""
                from(bucket:"origin")
                \t|> range(start:%s, stop:%s)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> sort(desc:true, columns:["_time"])""", INFLUX2_DATE_FORMATTER.format(now.toInstant()),
                INFLUX2_DATE_FORMATTER.format(tomorrow.toInstant()));
        assertThat(queryV2, equalTo(expectedQueryV2));
    }

    @Test
    public void testValueOperator() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOperator(FilterCriteria.Operator.LTE);
        criteria.setState(new PercentType(90));

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query, equalTo(
                "SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" WHERE value <= 90 ORDER BY time DESC;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> filter(fn: (r) => (r["_field"] == "value" and r["_value"] <= 90))
                \t|> sort(desc:true, columns:["_time"])"""));
    }

    @Test
    public void testPagination() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setPageNumber(2);
        criteria.setPageSize(10);

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query, equalTo(
                "SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" ORDER BY time DESC LIMIT 10 OFFSET 20;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> sort(desc:true, columns:["_time"])
                \t|> limit(n:10, offset:20)"""));
    }

    @Test
    public void testOrdering() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query,
                equalTo("SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" ORDER BY time ASC;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> sort(desc:false, columns:["_time"])"""));
    }

    @Test
    public void testPreviousState() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.DESCENDING);
        criteria.setPageSize(1);
        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> last()"""));
    }

    private FilterCriteria createBaseCriteria() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(ITEM_NAME);
        return criteria;
    }

    @Test
    public void testMeasurementNameFromMetadata() {
        FilterCriteria criteria = createBaseCriteria();
        MetadataKey metadataKey = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, "sampleItem");

        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "measurementName", Map.of("key1", "val1", "key2", "val2")));

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1, equalTo(
                "SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"measurementName\" WHERE item = 'sampleItem' ORDER BY time DESC;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "measurementName")
                \t|> filter(fn: (r) => r["item"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value", "item"])
                \t|> sort(desc:true, columns:["_time"])"""));
        when(metadataRegistry.get(metadataKey))
                .thenReturn(new Metadata(metadataKey, "", Map.of("key1", "val1", "key2", "val2")));

        queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1,
                equalTo("SELECT \"value\"::field,\"item\"::tag FROM \"origin\".\"sampleItem\" ORDER BY time DESC;"));

        queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("""
                from(bucket:"origin")
                \t|> range(start:-100y, stop:100y)
                \t|> filter(fn: (r) => r["_measurement"] == "sampleItem")
                \t|> keep(columns:["_measurement", "_time", "_value"])
                \t|> sort(desc:true, columns:["_time"])"""));
    }
}
