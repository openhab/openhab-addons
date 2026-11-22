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
package org.openhab.binding.plugwiseha.internal.api.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.ConnectException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;

/**
 * Tests cache retention and discovery updates through the gateway API.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHAControllerTest {

    private static final String ORIGINAL_OBJECTS = """
            <domain_objects>
                <gateway id="gateway"><name>Adam</name></gateway>
                <appliance id="original-appliance"><name>Original appliance</name></appliance>
                <location id="original-location"><name>Original location</name></location>
            </domain_objects>
            """;
    private static final String UPDATED_OBJECTS = """
            <domain_objects>
                <appliance id="new-appliance"><name>New appliance</name></appliance>
                <location id="new-location"><name>New location</name></location>
            </domain_objects>
            """;

    private final HttpClient client = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);

    @BeforeEach
    void setUp() {
        when(client.newRequest(anyString())).thenReturn(request);
    }

    @Test
    void refreshRetainsCacheWhileGatewayDataIsStillFresh() throws Exception {
        ContentResponse originalResponse = response(ORIGINAL_OBJECTS);
        when(request.send()).thenReturn(originalResponse);
        PlugwiseHAController controller = controller();
        controller.getAndMergeDomainObjects();
        Appliance appliance = controller.getAppliance("original-appliance");
        Location location = controller.getLocation("original-location");

        controller.refresh();
        controller.refresh();

        assertSame(appliance, controller.getAppliance("original-appliance"));
        assertSame(location, controller.getLocation("original-location"));
        assertEquals("Adam", controller.getGatewayInfo().getName());
        verify(request).send();
    }

    @Test
    void discoveryAddsNewIdsWithoutLosingExistingObjects() throws Exception {
        ContentResponse originalResponse = response(ORIGINAL_OBJECTS);
        ContentResponse updatedResponse = response(UPDATED_OBJECTS);
        when(request.send()).thenReturn(originalResponse, updatedResponse);
        PlugwiseHAController controller = controller();
        controller.getAndMergeDomainObjects();
        Appliance originalAppliance = controller.getAppliance("original-appliance");
        Location originalLocation = controller.getLocation("original-location");

        controller.getAndMergeDomainObjects();

        assertNotNull(controller.getAppliance("new-appliance"));
        assertNotNull(controller.getLocation("new-location"));
        assertSame(originalAppliance, controller.getAppliance("original-appliance"));
        assertSame(originalLocation, controller.getLocation("original-location"));
        verify(request, times(2)).send();
    }

    @Test
    void failedDiscoveryPreservesCacheAndCanBeRetried() throws Exception {
        ContentResponse originalResponse = response(ORIGINAL_OBJECTS);
        ContentResponse updatedResponse = response(UPDATED_OBJECTS);
        when(request.send()).thenReturn(originalResponse)
                .thenThrow(new ExecutionException(new ConnectException("Gateway unavailable")))
                .thenReturn(updatedResponse);
        PlugwiseHAController controller = controller();
        controller.getAndMergeDomainObjects();
        Appliance originalAppliance = controller.getAppliance("original-appliance");

        assertThrows(PlugwiseHAException.class, controller::getAndMergeDomainObjects);
        assertSame(originalAppliance, controller.getAppliance("original-appliance"));

        controller.getAndMergeDomainObjects();

        assertNotNull(controller.getAppliance("new-appliance"));
        assertNotNull(controller.getLocation("new-location"));
        assertSame(originalAppliance, controller.getAppliance("original-appliance"));
        verify(request, times(3)).send();
    }

    private PlugwiseHAController controller() throws PlugwiseHAException {
        return new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 300);
    }

    private ContentResponse response(String xml) {
        ContentResponse response = mock(ContentResponse.class);
        HttpFields headers = HttpFields.build().put(HttpHeader.DATE,
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getContentAsString()).thenReturn(xml);
        return response;
    }
}
