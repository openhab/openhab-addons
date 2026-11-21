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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.internal.FakeInfluxDBServer;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository.WriteResult;
import org.openhab.persistence.influxdb.internal.InfluxPoint;

/**
 * Tests for the line-protocol serialization and the write error handling of {@link InfluxDB3RepositoryImpl}.
 *
 * @author Cedric Boon - Initial contribution
 * @author Eugen Freiter - Write error classification tests
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class InfluxDB3RepositoryImplTest {
    private static final Instant TIME = Instant.ofEpochMilli(1700000000000L);

    private @Mock InfluxDBConfiguration configuration;

    private static final List<InfluxPoint> POINTS = List
            .of(InfluxPoint.newBuilder("sampleItem").withTime(TIME).withValue("120,50,80").build());

    private InfluxDB3RepositoryImpl repository;
    private @Nullable FakeInfluxDBServer server;

    @BeforeEach
    public void before() {
        InfluxDBMetadataService metadataService = new InfluxDBMetadataService(mock(MetadataRegistry.class));
        repository = new InfluxDB3RepositoryImpl(configuration, metadataService);
    }

    @AfterEach
    public void after() {
        repository.disconnect();
        FakeInfluxDBServer currentServer = server;
        if (currentServer != null) {
            currentServer.close();
        }
    }

    private FakeInfluxDBServer connectToFakeServer() throws IOException {
        FakeInfluxDBServer currentServer = new FakeInfluxDBServer("/api/v3/write_lp");
        server = currentServer;
        when(configuration.getUrl()).thenReturn(currentServer.getUrl());
        when(configuration.getToken()).thenReturn("token");
        when(configuration.getDatabaseName()).thenReturn("openhab");
        assertThat(repository.connect(), is(true));
        return currentServer;
    }

    @Test
    public void successfulWrite() throws IOException {
        FakeInfluxDBServer currentServer = connectToFakeServer();
        assertThat(repository.write(POINTS), is(WriteResult.SUCCESS));
        assertThat(currentServer.getWriteBodies(), is(List.of("sampleItem value=\"120,50,80\" 1700000000000")));
    }

    @Test
    public void partialWriteIsRejectedWithServerMessage() throws IOException {
        FakeInfluxDBServer currentServer = connectToFakeServer();
        String body = "{\"error\":\"partial write of line protocol occurred\",\"data\":[{\"original_line\":"
                + "\"sampleItem value=\\\"120,50,80\\\" 1700000000000\",\"line_number\":1,\"error_message\":"
                + "\"invalid column type for column 'value', expected iox::column_type::field::float, got "
                + "iox::column_type::field::string\"}]}";
        currentServer.setWriteResponse(400, body);

        WriteResult result = repository.write(POINTS);

        assertThat(result.outcome(), is(WriteResult.Outcome.REJECTED));
        assertThat(result.message(), containsString("invalid column type for column 'value'"));
    }

    @Test
    public void schemaLimitIsRetried() throws IOException {
        // InfluxDB 3 answers 422 when a database, table or schema limit is hit; raising the limit lets the points
        // through, so they must not be dropped.
        connectToFakeServer().setWriteResponse(422, "{\"error\":\"write would exceed the column limit\"}");
        WriteResult result = repository.write(POINTS);
        assertThat(result.outcome(), is(WriteResult.Outcome.RETRY));
        assertThat(result.message(), containsString("column limit"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 401, 403, 404, 408, 413, 429, 500, 503 })
    public void otherErrorsAreRetried(int status) throws IOException {
        connectToFakeServer().setWriteResponse(status, "{\"error\":\"try again\"}");
        WriteResult result = repository.write(POINTS);
        assertThat(result.outcome(), is(WriteResult.Outcome.RETRY));
        assertThat(result.message(), containsString("HTTP status " + status));
    }

    @Test
    public void unreachableServerIsRetried() throws IOException {
        connectToFakeServer().close();
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }

    @Test
    public void disconnectedRepositoryIsRetried() {
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }

    @Test
    public void writeRequestAcceptsPartialWrites() throws IOException {
        // Without accept_partial InfluxDB 3 rejects the whole batch for one bad line, and dropping a rejected batch
        // would then also drop its valid points.
        FakeInfluxDBServer currentServer = connectToFakeServer();
        currentServer.setWriteResponse(204, "");
        repository.write(POINTS);
        assertThat(currentServer.getWriteQueries(), is(List.of("db=openhab&precision=ms&accept_partial=true")));
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
