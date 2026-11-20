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

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Appliance;
import org.openhab.binding.plugwiseha.internal.api.model.dto.GatewayInfo;

/**
 * Tests gateway metadata updates in a cache that already contains domain objects.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHAGatewayInfoTest {

    private final HttpClient client = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);

    @BeforeEach
    void setUp() {
        when(client.newRequest(anyString())).thenReturn(request);
    }

    @Test
    void gatewayMetadataCanBeLoadedAfterApplianceData() throws Exception {
        ContentResponse applianceResponse = response("""
                <domain_objects><appliance id="appliance"><name>Radiator</name></appliance></domain_objects>
                """);
        ContentResponse gatewayResponse = gatewayResponse("4.0");
        when(request.send()).thenReturn(applianceResponse, gatewayResponse);
        PlugwiseHAController controller = new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 15);
        Appliance appliance = requireNonNull(controller.getAppliance("appliance"));

        GatewayInfo gateway = requireNonNull(controller.getGatewayInfo());

        assertEquals("4.0", gateway.getFirmwareVersion());
        assertSame(gateway, controller.getGatewayInfo());
        assertSame(appliance, controller.getAppliance("appliance"));
        verify(request, times(2)).send();
    }

    @Test
    void fullRefreshUpdatesPreviouslyCachedGatewayMetadata() throws Exception {
        ContentResponse original = gatewayResponse("4.0");
        ContentResponse updated = gatewayResponse("4.1");
        ContentResponse withoutGateway = response("""
                <domain_objects><appliance id="appliance"><name>Radiator</name></appliance></domain_objects>
                """);
        when(request.send()).thenReturn(original, updated, withoutGateway);
        PlugwiseHAController controller = new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 15);
        controller.getAndMergeDomainObjects();
        assertEquals("4.0", controller.getGatewayInfo().getFirmwareVersion());

        controller.getAndMergeDomainObjects();

        assertEquals("4.1", controller.getGatewayInfo().getFirmwareVersion());

        controller.getAndMergeDomainObjects();

        assertEquals("4.1", controller.getGatewayInfo().getFirmwareVersion());
        verify(request, times(3)).send();
    }

    private ContentResponse gatewayResponse(String firmware) {
        return response("""
                <domain_objects><gateway id="gateway"><firmware_version>%s</firmware_version></gateway></domain_objects>
                """.formatted(firmware));
    }

    private ContentResponse response(String xml) {
        ContentResponse response = mock(ContentResponse.class);
        HttpFields headers = new HttpFields();
        headers.put(HttpHeader.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getContentAsString()).thenReturn(xml);
        return response;
    }
}
