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
package org.openhab.binding.eyeonwater.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for {@link EyeOnWaterBridgeHandler}, specifically testing race condition resolution
 * when meter configuration or registration changes during in-flight polls.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class EyeOnWaterBridgeHandlerTest {

    private final Bridge bridge = mock(Bridge.class);
    private final BundleContext bundleContext = mock(BundleContext.class);
    private final EyeOnWaterClient client = mock(EyeOnWaterClient.class);
    private final EyeOnWaterMeterHandler meterHandler = mock(EyeOnWaterMeterHandler.class);

    private EyeOnWaterBridgeHandler bridgeHandler = new EyeOnWaterBridgeHandler(bridge, bundleContext, null);

    @BeforeEach
    void setUp() {
        when(bridge.getUID()).thenReturn(new ThingUID("eyeonwater", "bridge", "test-bridge"));
        when(bridge.getStatus()).thenReturn(ThingStatus.UNKNOWN);
        bridgeHandler = new EyeOnWaterBridgeHandler(bridge, bundleContext, null);
        bridgeHandler.setClient(client);
    }

    @Test
    void testPollAllMeters_UuidChangedDuringPoll_ResponseIgnored() throws Exception {
        when(meterHandler.getMeterUuid()).thenReturn("uuid-1");
        when(meterHandler.getMeterId()).thenReturn("id-1");
        bridgeHandler.registerMeterHandler(meterHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        EyeOnWaterMeterData staleData = new EyeOnWaterMeterData("uuid-1", "id-1");

        when(client.pollMeter("uuid-1", "id-1")).thenAnswer(invocation -> {
            // Simulate UUID change during the in-flight poll
            when(meterHandler.getMeterUuid()).thenReturn("uuid-2");
            // Simulate thingUpdated() disposing and re-initializing the handler
            bridgeHandler.unregisterMeterHandler(meterHandler);
            bridgeHandler.registerMeterHandler(meterHandler);
            return staleData;
        });

        bridgeHandler.pollAllMeters();

        verify(meterHandler, never()).updateState(staleData);
    }

    @Test
    void testPollAllMeters_UnregisteredDuringPoll_ResponseIgnored() throws Exception {
        when(meterHandler.getMeterUuid()).thenReturn("uuid-1");
        when(meterHandler.getMeterId()).thenReturn("id-1");
        bridgeHandler.registerMeterHandler(meterHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        EyeOnWaterMeterData staleData = new EyeOnWaterMeterData("uuid-1", "id-1");

        when(client.pollMeter("uuid-1", "id-1")).thenAnswer(invocation -> {
            bridgeHandler.unregisterMeterHandler(meterHandler);
            return staleData;
        });

        bridgeHandler.pollAllMeters();

        verify(meterHandler, never()).updateState(any());
    }

    @Test
    void testPollAllMeters_ExceptionDuringStalePoll_ErrorStatusIgnored() throws Exception {
        when(meterHandler.getMeterUuid()).thenReturn("uuid-1");
        when(meterHandler.getMeterId()).thenReturn("id-1");
        bridgeHandler.registerMeterHandler(meterHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        when(client.pollMeter("uuid-1", "id-1")).thenAnswer(invocation -> {
            // Reconfigure meter during poll
            when(meterHandler.getMeterUuid()).thenReturn("uuid-2");
            bridgeHandler.unregisterMeterHandler(meterHandler);
            bridgeHandler.registerMeterHandler(meterHandler);
            throw new IOException("Connection failed");
        });

        bridgeHandler.pollAllMeters();

        verify(meterHandler, never()).updateStatusOffline(any());
    }

    @Test
    void testPollAllMeters_NormalPoll_UpdatesMeter() throws Exception {
        when(meterHandler.getMeterUuid()).thenReturn("uuid-1");
        when(meterHandler.getMeterId()).thenReturn("id-1");
        bridgeHandler.registerMeterHandler(meterHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        EyeOnWaterMeterData data = new EyeOnWaterMeterData("uuid-1", "id-1");
        when(client.pollMeter("uuid-1", "id-1")).thenReturn(data);

        bridgeHandler.pollAllMeters();

        verify(meterHandler).updateState(data);
    }

    @Test
    void testRegisterMeterHandler_ImmediatePoll_UuidChangedDuringPoll_ResponseIgnored() throws Exception {
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(meterHandler.getMeterUuid()).thenReturn("uuid-1");
        when(meterHandler.getMeterId()).thenReturn("id-1");

        CountDownLatch pollStarted = new CountDownLatch(1);
        CountDownLatch reconfigured = new CountDownLatch(1);
        EyeOnWaterMeterData staleData = new EyeOnWaterMeterData("uuid-1", "id-1");

        when(client.pollMeter("uuid-1", "id-1")).thenAnswer(invocation -> {
            pollStarted.countDown();
            assertTrue(reconfigured.await(5, TimeUnit.SECONDS));
            return staleData;
        });

        bridgeHandler.registerMeterHandler(meterHandler);

        assertTrue(pollStarted.await(5, TimeUnit.SECONDS));

        // Reconfigure meter while poll is in-flight
        when(meterHandler.getMeterUuid()).thenReturn("uuid-2");
        bridgeHandler.unregisterMeterHandler(meterHandler);

        // Resume poll
        reconfigured.countDown();

        // Allow background thread to process completion
        Thread.sleep(150);

        verify(meterHandler, never()).updateState(any());
    }
}
