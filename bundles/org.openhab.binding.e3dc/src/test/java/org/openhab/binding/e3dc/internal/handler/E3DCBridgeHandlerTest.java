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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.rscp.FakeRscpDevice;
import org.openhab.binding.e3dc.internal.rscp.RscpError;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests the life cycle of the bridge handler: what a bad configuration and an unreachable device do to the status.
 *
 * <p>
 * The connection attempts run on the framework scheduler, so the tests wait on a latch that the status callback
 * releases instead of sleeping.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class E3DCBridgeHandlerTest {

    private static final long WAIT_SECONDS = 10;

    private @Nullable E3DCBridgeHandler handler;

    @AfterEach
    void disposeHandler() {
        E3DCBridgeHandler current = handler;
        if (current != null) {
            current.dispose();
        }
    }

    /** Creates a handler on a bridge with the given configuration whose status changes are reported to the latch. */
    private E3DCBridgeHandler createHandler(Map<String, Object> configuration,
            AtomicReference<@Nullable ThingStatusInfo> last, CountDownLatch settled) {
        Bridge bridge = BridgeBuilder
                .create(E3DCBindingConstants.THING_TYPE_BRIDGE, new ThingUID(E3DCBindingConstants.BINDING_ID, "test"))
                .withConfiguration(new Configuration(configuration)).build();
        TimeZoneProvider zoneProvider = mock(TimeZoneProvider.class);
        when(zoneProvider.getTimeZone()).thenReturn(ZoneId.of("Europe/Berlin"));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            ThingStatusInfo info = invocation.getArgument(1);
            bridge.setStatusInfo(info);
            last.set(info);
            if (info.getStatus() != ThingStatus.UNKNOWN) {
                settled.countDown();
            }
            return null;
        }).when(callback).statusUpdated(any(), any());
        E3DCBridgeHandler created = new E3DCBridgeHandler(bridge, zoneProvider);
        created.setCallback(callback);
        handler = created;
        return created;
    }

    /** A port on the loopback interface that nothing listens on, so a connection attempt is refused at once. */
    private static int closedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void whenHostIsBlankThenConfigurationError() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "", "rscpPassword", "secret"), last, settled);

        bridgeHandler.initialize();

        assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));
        ThingStatusInfo info = last.get();
        assertNotNull(info);
        assertEquals(ThingStatus.OFFLINE, info.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, info.getStatusDetail());
        assertFalse(bridgeHandler.isConnected());
    }

    @Test
    void whenRscpPasswordIsBlankThenConfigurationError() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "127.0.0.1", "rscpPassword", ""), last, settled);

        bridgeHandler.initialize();

        assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));
        ThingStatusInfo info = last.get();
        assertNotNull(info);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, info.getStatusDetail());
    }

    @Test
    void whenDeviceIsUnreachableThenCommunicationError() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        E3DCBridgeHandler bridgeHandler = createHandler(
                Map.of("host", "127.0.0.1", "port", closedPort(), "rscpPassword", "secret", "timeout", 2), last,
                settled);

        bridgeHandler.initialize();

        assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));
        ThingStatusInfo info = last.get();
        assertNotNull(info);
        assertEquals(ThingStatus.OFFLINE, info.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, info.getStatusDetail());
        assertFalse(bridgeHandler.isConnected());
    }

    @Test
    void whenDeviceAnswersThenBridgeGoesOnline() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        try (FakeRscpDevice device = FakeRscpDevice
                .start(() -> FakeRscpDevice.emptyContainer(RscpTag.EMS_GET_POWER_SETTINGS))) {
            E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "127.0.0.1", "port", device.port(),
                    "rscpPassword", FakeRscpDevice.RSCP_PASSWORD, "refreshInterval", 60), last, settled);

            bridgeHandler.initialize();

            assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));
            ThingStatusInfo info = last.get();
            assertNotNull(info);
            assertEquals(ThingStatus.ONLINE, info.getStatus());
            assertTrue(bridgeHandler.isConnected());
        }
    }

    @Test
    void whenDisposedAfterGoingOnlineThenTheConnectionIsClosed() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        try (FakeRscpDevice device = FakeRscpDevice
                .start(() -> FakeRscpDevice.emptyContainer(RscpTag.EMS_GET_POWER_SETTINGS))) {
            E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "127.0.0.1", "port", device.port(),
                    "rscpPassword", FakeRscpDevice.RSCP_PASSWORD, "refreshInterval", 60), last, settled);
            bridgeHandler.initialize();
            assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));

            bridgeHandler.dispose();

            assertFalse(bridgeHandler.isConnected());
        }
    }

    @Test
    void whenOfflineThenRefreshRequestIsIgnored() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "", "rscpPassword", "secret"), last, settled);
        bridgeHandler.initialize();
        assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));

        assertDoesNotThrow(bridgeHandler::requestRefresh);
        assertFalse(bridgeHandler.isConnected());
    }

    @Test
    void whenDisposedThenTheHandlerStaysOffline() throws Exception {
        AtomicReference<@Nullable ThingStatusInfo> last = new AtomicReference<>();
        CountDownLatch settled = new CountDownLatch(1);
        E3DCBridgeHandler bridgeHandler = createHandler(Map.of("host", "", "rscpPassword", "secret"), last, settled);
        bridgeHandler.initialize();
        assertTrue(settled.await(WAIT_SECONDS, TimeUnit.SECONDS));

        bridgeHandler.dispose();

        assertFalse(bridgeHandler.isConnected());
    }

    /**
     * Only an error that proves the device does not implement the data point stops the requests. A temporary
     * condition such as "not available" passes, for example while the inverter is switched off.
     */
    @Test
    void onlyAnErrorAboutTheDataPointItselfStopsTheRequests() {
        assertTrue(E3DCBridgeHandler.isUnsupported(RscpError.UNKNOWN_TAG));
        assertTrue(E3DCBridgeHandler.isUnsupported(RscpError.NOT_HANDLED));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.NOT_AVAILABLE));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.AGAIN));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.ACCESS_DENIED));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.FORMAT));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.OUT_OF_BOUNDS));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.ALREADY_IN_USE));
        assertFalse(E3DCBridgeHandler.isUnsupported(RscpError.UNKNOWN));
        assertFalse(E3DCBridgeHandler.isUnsupported(null));
    }
}
