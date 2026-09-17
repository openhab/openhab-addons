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
package org.openhab.binding.mercedesme.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.api.Websocket;
import org.openhab.binding.mercedesme.internal.api.WebsocketMock;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdates;

/**
 * {@link AccountHandlerTest} regression tests for the typed-push acknowledgment semantics fixed per PR
 * #21343 review (wborn): {@code handleMessage()} must only acknowledge a {@code VehicleStatusUpdates}
 * sequence once {@link AccountHandler#distributeVehicleUpdates(Map)} confirms the update was actually
 * delivered to a live handler (or safely cached) - an unconditional acknowledgment would let the server
 * drop an undelivered partial update for good, since it never resends an acknowledged sequence.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerTest {

    /**
     * Counts every {@link Websocket#sendAcknowledgeMessage(ClientMessage)} call instead of actually sending
     * anything - {@code Websocket.session} stays {@code null} in these tests anyway, so the real
     * implementation would just log and no-op, but overriding it here keeps the assertion explicit. Also
     * counts down the shared "processed" latch: when a message IS acknowledged, that call is the last thing
     * {@code handleMessage()} does, so it's the correct completion signal to wait on (see
     * {@link LatchedAccountHandler} for the complementary "not acknowledged" signal).
     */
    private static class TrackingWebsocketMock extends WebsocketMock {
        volatile int ackCount = 0;
        private final CountDownLatch processed;

        TrackingWebsocketMock(AccountHandler atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l,
                CountDownLatch processed) {
            super(atrl, hc, ac, l, new VolatileStorageService().getStorage(""));
            this.processed = processed;
        }

        @Override
        public void sendAcknowledgeMessage(ClientMessage message) {
            ackCount++;
            processed.countDown();
        }
    }

    /**
     * Signals when the async message-processing thread (AccountHandler.scheduleMessage() -> handleMessage())
     * has finished deciding whether to distribute/acknowledge a message, so tests don't need an arbitrary
     * sleep to wait for it.
     * <p>
     * {@code distributeVehicleUpdates()} returning is NOT by itself a safe "done" signal: when delivery
     * succeeds, {@code handleMessage()} (running on the same worker thread) still has to build and send the
     * acknowledgment afterward, and a test thread that wakes up right as this override returns can race
     * ahead of that. So this only counts down on the "not distributed" outcome, where handleMessage() has
     * nothing left to do; the "distributed" / acknowledged outcome is instead signalled from
     * {@link TrackingWebsocketMock#sendAcknowledgeMessage} - the actual last step of that path.
     */
    private static class LatchedAccountHandler extends AccountHandler {
        final CountDownLatch processed = new CountDownLatch(1);

        LatchedAccountHandler(Bridge bridge, MercedesMeDiscoveryService mmds, HttpClient hc, LocaleProvider lp) {
            super(bridge, mmds, hc, lp, new VolatileStorageService());
        }

        @Override
        public boolean distributeVehicleUpdates(Map<String, VehicleStatusAttributes> map) {
            boolean result = super.distributeVehicleUpdates(map);
            if (!result) {
                processed.countDown();
            }
            return result;
        }

        @Override
        public void discovery(String vin) {
            // no-op in tests - the real implementation calls out to RestApi.restGetCapabilities(), which
            // needs a live HTTP response and isn't what these acknowledgment-semantics tests are about.
        }
    }

    private static PushMessage buildTypedPush(long sequenceNumber, String vin, boolean fullUpdate) {
        VehicleStatusUpdate vsu = VehicleStatusUpdate.newBuilder().setFullUpdate(fullUpdate).build();
        VehicleStatusUpdates vsus = VehicleStatusUpdates.newBuilder().setSequenceNumber(sequenceNumber)
                .putVehicleStatusUpdates(vin, vsu).build();
        return PushMessage.newBuilder().setVehicleStatusUpdates(vsus).build();
    }

    @Test
    void typedPushIsNotAcknowledgedWhenNoActiveHandlerExistsForVin() throws InterruptedException {
        LatchedAccountHandler ah = new LatchedAccountHandler(mock(Bridge.class), mock(MercedesMeDiscoveryService.class),
                mock(HttpClient.class), mock(LocaleProvider.class));
        TrackingWebsocketMock ws = new TrackingWebsocketMock(ah, mock(HttpClient.class), ah.config,
                mock(LocaleProvider.class), ah.processed);
        ah.api = ws;

        // no registerVin() call for this VIN - activeVehicleHandlerMap stays empty, so
        // distributeVehicleUpdates() must return false and handleMessage() must not acknowledge
        ah.enqueueMessage(buildTypedPush(1L, "WDB1230011ANONYMIZ", false));

        assertTrue(ah.processed.await(2, TimeUnit.SECONDS), "message was not processed in time");
        assertEquals(0, ws.ackCount,
                "a VehicleStatusUpdates for a VIN with no active handler must not be acknowledged (PR #21343 review, wborn) - "
                        + "the server never resends an acknowledged sequence, so this would silently lose the update");
    }

    @Test
    void typedPushIsAcknowledgedWhenDeliveredToAnActiveHandler() throws InterruptedException {
        LatchedAccountHandler ah = new LatchedAccountHandler(mock(Bridge.class), mock(MercedesMeDiscoveryService.class),
                mock(HttpClient.class), mock(LocaleProvider.class));
        TrackingWebsocketMock ws = new TrackingWebsocketMock(ah, mock(HttpClient.class), ah.config,
                mock(LocaleProvider.class), ah.processed);
        ah.api = ws;

        String vin = "WDB1230011ANONYMIZ";
        Thing thingMock = mock(Thing.class);
        when(thingMock.getThingTypeUID()).thenReturn(Constants.THING_TYPE_BEV);
        when(thingMock.getProperties()).thenReturn(new HashMap<>());
        VehicleHandler handlerMock = mock(VehicleHandler.class);
        when(handlerMock.getThing()).thenReturn(thingMock);
        ah.registerVin(vin, handlerMock);

        ah.enqueueMessage(buildTypedPush(2L, vin, true));

        assertTrue(ah.processed.await(2, TimeUnit.SECONDS), "message was not processed in time");
        verify(handlerMock, times(1)).enqueueUpdate(any());
        assertEquals(1, ws.ackCount, "an update successfully delivered to an active handler must be acknowledged");
    }
}
