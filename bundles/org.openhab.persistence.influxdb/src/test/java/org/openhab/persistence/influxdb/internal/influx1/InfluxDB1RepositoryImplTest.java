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
package org.openhab.persistence.influxdb.internal.influx1;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.internal.FakeInfluxDBServer;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository.WriteResult;
import org.openhab.persistence.influxdb.internal.InfluxPoint;

/**
 * Tests how {@link InfluxDB1RepositoryImpl} classifies failed writes.
 *
 * @author Eugen Freiter - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
public class InfluxDB1RepositoryImplTest {
    private static final List<InfluxPoint> POINTS = List.of(InfluxPoint.newBuilder("sampleItem")
            .withTime(Instant.ofEpochMilli(1700000000000L)).withValue("120,50,80").build());

    private FakeInfluxDBServer server;
    private InfluxDB1RepositoryImpl repository;

    @BeforeEach
    public void before() throws IOException {
        server = new FakeInfluxDBServer("/write");
        InfluxDBConfiguration configuration = mock(InfluxDBConfiguration.class);
        when(configuration.getUrl()).thenReturn(server.getUrl());
        when(configuration.getUser()).thenReturn("user");
        when(configuration.getPassword()).thenReturn("password");
        when(configuration.getDatabaseName()).thenReturn("openhab");
        when(configuration.getRetentionPolicy()).thenReturn("autogen");
        repository = new InfluxDB1RepositoryImpl(configuration,
                new InfluxDBMetadataService(mock(MetadataRegistry.class)));
        assertThat(repository.connect(), is(true));
    }

    @AfterEach
    public void after() {
        repository.disconnect();
        server.close();
    }

    @Test
    public void successfulWrite() {
        assertThat(repository.write(POINTS), is(WriteResult.SUCCESS));
        assertThat(server.getWriteBodies(), is(List.of("sampleItem value=\"120,50,80\" 1700000000000000000\n")));
    }

    @Test
    public void fieldTypeConflictIsRejected() {
        String error = "partial write: field type conflict: input field \"value\" on measurement \"sampleItem\" is type string, already exists as type float dropped=1";
        server.setWriteResponse(400, "{\"error\":\"" + error.replace("\"", "\\\"") + "\"}");

        WriteResult result = repository.write(POINTS);

        assertThat(result.outcome(), is(WriteResult.Outcome.REJECTED));
        assertThat(result.message(), is(error));
    }

    @Test
    public void unparsableLineIsRejected() {
        server.setWriteResponse(400, "{\"error\":\"unable to parse 'sampleItem value=': missing field value\"}");
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.REJECTED));
    }

    @Test
    public void pointsBeyondRetentionPolicyAreRejected() {
        server.setWriteResponse(400, "{\"error\":\"partial write: points beyond retention policy dropped=1\"}");
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.REJECTED));
    }

    @Test
    public void databaseNotFoundIsRetried() {
        // Creating the database fixes this, so the points must be kept.
        server.setWriteResponse(404, "{\"error\":\"database not found: \\\"openhab\\\"\"}");
        WriteResult result = repository.write(POINTS);
        assertThat(result.outcome(), is(WriteResult.Outcome.RETRY));
        assertThat(result.message(), containsString("database not found"));
    }

    @Test
    public void authorizationFailureIsRetried() {
        server.setWriteResponse(401, "{\"error\":\"authorization failed\"}");
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }

    @Test
    public void serverErrorIsRetried() {
        server.setWriteResponse(500, "{\"error\":\"timeout\"}");
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }

    @Test
    public void unreachableServerIsRetried() {
        server.close();
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }

    @Test
    public void disconnectedRepositoryIsRetried() {
        repository.disconnect();
        assertThat(repository.write(POINTS).outcome(), is(WriteResult.Outcome.RETRY));
    }
}
