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

package org.openhab.binding.tedee.internal.handler;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests routing of Tedee webhook events to lock handlers.
 *
 * @author Alexander Goll - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TedeeWebhookServletTest {
    private static final int DEVICE_ID = 12345;

    @Test
    void shouldRouteDuplicateDeviceIdToLocalLockOnly() throws Exception {
        ThingRegistry registry = mock(ThingRegistry.class);
        TedeeWebhookServlet servlet = new TedeeWebhookServlet(registry);

        TedeeLockHandler localHandler = mock(TedeeLockHandler.class);
        TedeeLockHandler cloudHandler = mock(TedeeLockHandler.class);
        TedeeBridgeHandler localBridgeHandler = mock(TedeeBridgeHandler.class);
        when(localBridgeHandler.isWebhookSecret("test-secret")).thenReturn(true);

        Bridge localBridge = localBridge(localBridgeHandler);
        Bridge cloudBridge = cloudBridge();
        ThingUID localBridgeUID = new ThingUID("tedee:local-bridge:local");
        ThingUID cloudBridgeUID = new ThingUID("tedee:cloud-bridge:cloud");
        when(registry.get(localBridgeUID)).thenReturn(localBridge);
        when(registry.get(cloudBridgeUID)).thenReturn(cloudBridge);

        Thing localThing = lockThing(localBridgeUID, localHandler);
        Thing cloudThing = lockThing(cloudBridgeUID, cloudHandler);
        when(registry.getAll()).thenReturn(List.of(cloudThing, localThing));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("""
                {
                  "event": "lock-status-changed",
                  "data": {
                    "deviceType": 2,
                    "deviceId": 12345,
                    "state": 6,
                    "doorState": 3,
                    "jammed": 0
                  }
                }
                """)));
        when(request.getHeader(TedeeBindingConstants.WEBHOOK_SECRET_HEADER)).thenReturn("test-secret");
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doPost(request, response);

        verify(localHandler).handleWebhook(6, 3, 0);
        verifyNoInteractions(cloudHandler);
    }

    @Test
    void shouldRejectWebhookWithInvalidSecret() throws Exception {
        ThingRegistry registry = mock(ThingRegistry.class);
        TedeeWebhookServlet servlet = new TedeeWebhookServlet(registry);

        TedeeLockHandler localHandler = mock(TedeeLockHandler.class);
        TedeeBridgeHandler localBridgeHandler = mock(TedeeBridgeHandler.class);
        when(localBridgeHandler.isWebhookSecret("invalid-secret")).thenReturn(false);

        ThingUID localBridgeUID = new ThingUID("tedee:local-bridge:local");
        Bridge localBridge = localBridge(localBridgeHandler);
        when(registry.get(localBridgeUID)).thenReturn(localBridge);
        Thing localThing = lockThing(localBridgeUID, localHandler);
        when(registry.getAll()).thenReturn(List.of(localThing));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("""
                {
                  "event": "lock-status-changed",
                  "data": {
                    "deviceType": 2,
                    "deviceId": 12345,
                    "state": 6,
                    "doorState": 3,
                    "jammed": 0
                  }
                }
                """)));
        when(request.getHeader(TedeeBindingConstants.WEBHOOK_SECRET_HEADER)).thenReturn("invalid-secret");
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid webhook secret");
        verifyNoInteractions(localHandler);
    }

    @Test
    void shouldRejectWebhookWithMissingData() throws Exception {
        ThingRegistry registry = mock(ThingRegistry.class);
        TedeeWebhookServlet servlet = new TedeeWebhookServlet(registry);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("""
                {
                  "event": "lock-status-changed"
                }
                """)));
        HttpServletResponse response = mock(HttpServletResponse.class);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload");
    }

    private static Thing lockThing(ThingUID bridgeUID, TedeeLockHandler handler) {
        Thing thing = mock(Thing.class);
        Configuration configuration = mock(Configuration.class);
        when(thing.getThingTypeUID()).thenReturn(TedeeBindingConstants.LOCK);
        when(thing.getBridgeUID()).thenReturn(bridgeUID);
        lenient().when(thing.getConfiguration()).thenReturn(configuration);
        lenient().when(configuration.get("deviceId")).thenReturn(DEVICE_ID);
        lenient().when(thing.getHandler()).thenReturn(handler);
        return thing;
    }

    private static Bridge localBridge(TedeeBridgeHandler handler) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(handler);
        return bridge;
    }

    private static Bridge cloudBridge() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(mock(TedeeCloudBridgeHandler.class));
        return bridge;
    }
}
