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
package org.openhab.persistence.influxdb.internal.influx3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxPoint;

/**
 * Tests for the line-protocol serialization done by {@link InfluxDB3RepositoryImpl}.
 *
 * @author Cedric Boon - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class InfluxDB3RepositoryImplTest {
    private static final Instant TIME = Instant.ofEpochMilli(1700000000000L);

    private @Mock InfluxDBConfiguration configuration;

    private InfluxDB3RepositoryImpl repository;

    @BeforeEach
    public void before() {
        InfluxDBMetadataService metadataService = new InfluxDBMetadataService(mock(MetadataRegistry.class));
        repository = new InfluxDB3RepositoryImpl(configuration, metadataService);
    }

    @Test
    public void convertsStringValue() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("text").build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=\"text\" 1700000000000")));
    }

    @Test
    public void convertsIntegerValueWithIntegerSuffix() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(1).build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=1i 1700000000000")));
    }

    @Test
    public void convertsLongValueWithIntegerSuffix() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(1_000L).build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=1000i 1700000000000")));
    }

    @Test
    public void convertsWholeNumberBigDecimalAsFloatNotInteger() {
        // A DecimalType/QuantityType value can be fractional on other writes for the same item, so it must always
        // be written as a float, never with the integer suffix, to avoid an InfluxDB 3 schema conflict.
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(new BigDecimal("20")).build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=20 1700000000000")));
    }

    @Test
    public void convertsFractionalBigDecimalAsFloat() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(new BigDecimal("20.5"))
                .build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=20.5 1700000000000")));
    }

    @Test
    public void convertsBooleanValue() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(true).build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=true 1700000000000")));
    }

    @Test
    public void escapesCommaSpaceInMeasurementName() {
        InfluxPoint point = InfluxPoint.newBuilder("sample, item name").withTime(TIME).withValue("text").build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sample\\,\\ item\\ name value=\"text\" 1700000000000")));
    }

    @Test
    public void escapesCommaEqualsSpaceInTags() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("text")
                .withTag("item", "living, room=1").build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem,item=living\\,\\ room\\=1 value=\"text\" 1700000000000")));
    }

    @Test
    public void tagsAreSortedByKey() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("text").withTag("zebra", "z")
                .withTag("alpha", "a").build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem,alpha=a,zebra=z value=\"text\" 1700000000000")));
    }

    @Test
    public void escapesQuotesAndBackslashesInStringFieldValue() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("say \"hi\"\\now").build();
        assertThat(repository.convertPointToLineProtocol(point),
                equalTo(Optional.of("sampleItem value=\"say \\\"hi\\\"\\\\now\" 1700000000000")));
    }

    @Test
    public void discardsUnsupportedValueType() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue(new Object()).build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }

    @Test
    public void discardsPointWithNewlineInStringFieldValue() {
        // Line protocol has no escape for newlines: a literal one would split a single point into multiple
        // malformed lines, so the whole point must be rejected rather than written corrupted.
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("line1\nline2").build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }

    @Test
    public void discardsPointWithCarriageReturnInStringFieldValue() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("line1\rline2").build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }

    @Test
    public void discardsPointWithNewlineInTagValue() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("text")
                .withTag("item", "line1\nline2").build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }

    @Test
    public void discardsPointWithNewlineInTagKey() {
        InfluxPoint point = InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("text")
                .withTag("li\nne", "value").build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }

    @Test
    public void discardsPointWithNewlineInMeasurementName() {
        InfluxPoint point = InfluxPoint.newBuilder("sample\nItem").withTime(TIME).withValue("text").build();
        assertThat(repository.convertPointToLineProtocol(point), equalTo(Optional.empty()));
    }
}
