/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPoint;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author Franz - Initial contribution
 */
public class VictoriaMetricsRepositoryHttpTest {
    private MockWebServer mockWebServer;
    private VictoriaMetricsRepository repository;

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // Mock the metadata service
        VictoriaMetricsMetadataService metadataService = Mockito.mock(VictoriaMetricsMetadataService.class);
        Mockito.when(metadataService.getMeasurementNameOrDefault(Mockito.anyString())).thenReturn("test.metric");
        // Create the configuration for VictoriaMetricsRepository
        VictoriaMetricsConfiguration config = new VictoriaMetricsConfiguration(
                Map.of("url", mockWebServer.url("/").toString()));
        repository = new VictoriaMetricsRepository(config, metadataService);
        // No need to connect here, as we are testing HTTP interactions
    }

    @AfterEach
    public void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testWritePoint() throws Exception {
        // Arrange: VM expects HTTP 200 on write
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        VictoriaMetricsPoint point = VictoriaMetricsPoint.newBuilder("test.metric").withTime(Instant.now())
                .withValue(123.3).withTag("location", "livingroom").build();
        // Act
        repository.write(List.of(point));
        // Assert: Check the request format sent to VictoriaMetrics
        var request = mockWebServer.takeRequest();
        Assertions.assertEquals("/api/v1/import/prometheus", request.getPath()); // adapt as needed
        Assertions.assertEquals("POST", request.getMethod());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("test.metric"));
        assertTrue(body.contains("location=\"livingroom\""));
    }

    @Test
    public void testQueryResponse() throws Exception {
        String jsonResponse = "{\"status\":\"success\",\"data\":{\"resultType\":\"vector\",\"result\":[{\"metric\":{\"__name__\":\"test.metric\",\"location\":\"livingroom\"},\"value\":[1655304672,\"123.4\"]}]}}";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(jsonResponse));
        // Build real FilterCriteria object
        ZonedDateTime begin = ZonedDateTime.ofInstant(Instant.now().minusSeconds(3600), ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        FilterCriteria filter = new FilterCriteria().setItemName("test.metric").setBeginDate(begin).setEndDate(end);
        var result = repository.query(filter);
        assertNotNull(result);
        // add more specific assertions depending on what repository.query(filter) returns
    }
}
