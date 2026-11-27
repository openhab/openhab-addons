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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

import io.calimero.secure.Security;

/**
 * Tests for the local endpoint check of {@link IPClient}.
 *
 * @author Kai Kreuzer - initial contribution and API.
 */
@NonNullByDefault
class IPClientTest {

    private static final String LOCAL_IP_NOT_AVAILABLE = "local IP address 192.0.2.1 is not available";

    private final ScheduledExecutorService knxScheduler = Executors.newSingleThreadScheduledExecutor();
    private final StatusUpdateCallback statusUpdateCallback = mock(StatusUpdateCallback.class);

    @AfterEach
    void tearDown() {
        knxScheduler.shutdownNow();
    }

    @Test
    void testBindFailureIsReportedAsConfigurationError() {
        // 192.0.2.1 is from the TEST-NET-1 range and cannot be assigned to a local network interface
        InetSocketAddress localEndPoint = new InetSocketAddress("192.0.2.1", 0);
        IPClient client = new IPClient(IPClient.IpConnectionType.TUNNEL, "192.168.0.73", "0.0.0", 3671, localEndPoint,
                false, 0, new byte[0], 0, new byte[0], 0, new byte[0], new ThingUID("knx:ip:bridge"), 5, 50, 1,
                knxScheduler, new CommandExtensionData(new TreeMap<>()), Security.newSecurity(), statusUpdateCallback);

        client.initialize();

        assertFalse(client.isConnected());
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        verify(statusUpdateCallback).updateStatus(eq(ThingStatusDetail.CONFIGURATION_ERROR),
                descriptionCaptor.capture());
        assertTrue(descriptionCaptor.getValue().contains(LOCAL_IP_NOT_AVAILABLE),
                "unexpected status description: " + descriptionCaptor.getValue());
        client.dispose();
    }
}
