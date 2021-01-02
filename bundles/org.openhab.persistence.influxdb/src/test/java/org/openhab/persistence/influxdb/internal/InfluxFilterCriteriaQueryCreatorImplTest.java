/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.influx1.Influx1FilterCriteriaQueryCreatorImpl;
import org.openhab.persistence.influxdb.internal.influx2.Influx2FilterCriteriaQueryCreatorImpl;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class InfluxFilterCriteriaQueryCreatorImplTest {
    private static final String RETENTION_POLICY = "origin";
    public static final String ITEM_NAME = "sampleItem";

    private static final DateTimeFormatter INFLUX2_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'").withZone(ZoneId.of("UTC"));

    private Influx1FilterCriteriaQueryCreatorImpl instanceV1;
    private Influx2FilterCriteriaQueryCreatorImpl instanceV2;

    @BeforeEach
    public void before() {
        instanceV1 = new Influx1FilterCriteriaQueryCreatorImpl();
        instanceV2 = new Influx2FilterCriteriaQueryCreatorImpl();
    }

    @AfterEach
    public void after() {
        instanceV1 = null;
        instanceV2 = null;
    }

    @Test
    public void testSimpleItemQueryWithoutParams() {
        FilterCriteria criteria = createBaseCriteria();

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1, equalTo("SELECT value FROM origin.sampleItem;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)\n\t"
                + "|> filter(fn: (r) => r[\"_measurement\"] == \"sampleItem\")"));
    }

    @Test
    public void testEscapeSimpleItem() {
        FilterCriteria criteria = createBaseCriteria("sample.Item");

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1, equalTo("SELECT value FROM origin.\"sample.Item\";"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)\n\t"
                + "|> filter(fn: (r) => r[\"_measurement\"] == \"sample.Item\")"));
    }

    @Test
    public void testSimpleUnboundedItemWithoutParams() {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setOrdering(null);

        String queryV1 = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV1, equalTo("SELECT value FROM origin./.*/;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)"));
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
                "SELECT value FROM origin.sampleItem WHERE time >= '%s' AND time <= '%s';", now.toInstant(),
                tomorrow.toInstant());
        assertThat(queryV1, equalTo(expectedQueryV1));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        String expectedQueryV2 = String.format(
                "from(bucket:\"origin\")\n\t" + "|> range(start:%s, stop:%s)\n\t"
                        + "|> filter(fn: (r) => r[\"_measurement\"] == \"sampleItem\")",
                INFLUX2_DATE_FORMATTER.format(now.toInstant()), INFLUX2_DATE_FORMATTER.format(tomorrow.toInstant()));
        assertThat(queryV2, equalTo(expectedQueryV2));
    }

    @Test
    public void testValueOperator() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOperator(FilterCriteria.Operator.LTE);
        criteria.setState(new PercentType(90));

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query, equalTo("SELECT value FROM origin.sampleItem WHERE value <= 90;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2,
                equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)\n\t"
                        + "|> filter(fn: (r) => r[\"_measurement\"] == \"sampleItem\")\n\t"
                        + "|> filter(fn: (r) => (r[\"_field\"] == \"value\" and r[\"_value\"] <= 90))"));
    }

    @Test
    public void testPagination() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setPageNumber(2);
        criteria.setPageSize(10);

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query, equalTo("SELECT value FROM origin.sampleItem LIMIT 10 OFFSET 20;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2, equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)\n\t"
                + "|> filter(fn: (r) => r[\"_measurement\"] == \"sampleItem\")\n\t" + "|> limit(n:10, offset:20)"));
    }

    @Test
    public void testOrdering() {
        FilterCriteria criteria = createBaseCriteria();
        criteria.setOrdering(FilterCriteria.Ordering.ASCENDING);

        String query = instanceV1.createQuery(criteria, RETENTION_POLICY);
        assertThat(query, equalTo("SELECT value FROM origin.sampleItem ORDER BY time ASC;"));

        String queryV2 = instanceV2.createQuery(criteria, RETENTION_POLICY);
        assertThat(queryV2,
                equalTo("from(bucket:\"origin\")\n\t" + "|> range(start:-100y)\n\t"
                        + "|> filter(fn: (r) => r[\"_measurement\"] == \"sampleItem\")\n\t"
                        + "|> sort(desc:false, columns:[\"_time\"])"));
    }

    private FilterCriteria createBaseCriteria() {
        return createBaseCriteria(ITEM_NAME);
    }

    private FilterCriteria createBaseCriteria(String sampleItem) {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(sampleItem);
        criteria.setOrdering(null);
        return criteria;
    }
}
