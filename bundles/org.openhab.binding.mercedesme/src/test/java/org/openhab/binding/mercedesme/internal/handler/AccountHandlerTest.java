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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.api.Websocket;
import org.openhab.binding.mercedesme.internal.api.WebsocketMock;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.binding.mercedesme.internal.exception.MercedesMeAuthException;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdates;

/**
 * {@link AccountHandlerTest} regression tests for the typed-push acknowledgment semantics: an update must only
 * be acknowledged once {@link AccountHandler#distributeVehicleUpdates(Map)} reports it was delivered.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandlerTest {

    /**
     * Counts {@link Websocket#websocketUpdate()} calls to observe refresh triggers without a real WebSocket.
     * {@link Websocket#authTokenIsValid()} is forced to {@code true}, otherwise a refresh falls back to a login
     * instead of requesting an update.
     */
    private static class CountingWebsocketMock extends WebsocketMock {
        final AtomicInteger updateCount = new AtomicInteger();

        CountingWebsocketMock(AccountHandler atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l) {
            super(atrl, hc, ac, l, new VolatileStorageService().getStorage(""));
        }

        @Override
        public void websocketUpdate() {
            updateCount.incrementAndGet();
        }

        @Override
        public boolean authTokenIsValid() {
            return true;
        }
    }

    /**
     * Drives the invalid-token path of {@link AccountHandler#refresh()}: the token is invalid until a login succeeds,
     * and {@link WebsocketMock#login()} is replaced by a {@code login()} which either succeeds or fails.
     */
    private static class ConfigurableWebsocketMock extends WebsocketMock {
        final AtomicInteger loginCount = new AtomicInteger();
        final AtomicInteger updateCount = new AtomicInteger();
        private final boolean loginSucceeds;
        private volatile boolean authenticated;

        ConfigurableWebsocketMock(AccountHandler atrl, HttpClient hc, AccountConfiguration ac, LocaleProvider l,
                boolean loginSucceeds) {
            super(atrl, hc, ac, l, new VolatileStorageService().getStorage(""));
            this.loginSucceeds = loginSucceeds;
        }

        @Override
        public void login() throws MercedesMeAuthException {
            loginCount.incrementAndGet();
            if (!loginSucceeds) {
                throw new MercedesMeAuthException("login denied by test");
            }
            // a successful login stores a token pair, which is what authTokenIsValid() reports
            authenticated = true;
        }

        @Override
        public boolean authTokenIsValid() {
            return authenticated;
        }

        @Override
        public void websocketUpdate() {
            updateCount.incrementAndGet();
        }
    }

    /**
     * Counts {@link Websocket#sendAcknowledgeMessage(ClientMessage)} calls and counts down the shared latch,
     * which signals the "acknowledged" outcome (see {@link LatchedAccountHandler} for the other).
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
     * Signals when the async message-processing thread has finished deciding whether to distribute and
     * acknowledge, so tests need no arbitrary sleep. It counts down only on the "not distributed" outcome;
     * on success the acknowledgment from {@link TrackingWebsocketMock} is the last step.
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
            // no-op in tests - the real implementation needs a live HTTP response
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

        // no registerVin() call - activeVehicleHandlerMap stays empty, so no acknowledgment
        ah.enqueueMessage(buildTypedPush(1L, "WDB1230011ANONYMIZ", false));

        assertTrue(ah.processed.await(2, TimeUnit.SECONDS), "message was not processed in time");
        assertEquals(0, ws.ackCount,
                "a VehicleStatusUpdates for a VIN with no active handler must not be acknowledged - "
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

    /**
     * Installs the WebSocket mock through the initialization seam, so the refresh scheduled by
     * {@link AccountHandler#initialize()} already runs against the mock. The mock is handed over after construction
     * because it needs the handler itself; it must be set before {@code initialize()} is called.
     */
    private static class ApiInjectedAccountHandler extends AccountHandler {
        private @Nullable Websocket apiMock;

        ApiInjectedAccountHandler(Bridge bridge, MercedesMeDiscoveryService mmds, HttpClient hc, LocaleProvider lp) {
            super(bridge, mmds, hc, lp, new VolatileStorageService());
        }

        void setApiMock(Websocket apiMock) {
            this.apiMock = apiMock;
        }

        @Override
        public void initialize() {
            initialize(apiMock);
        }
    }

    private static ApiInjectedAccountHandler buildAccountHandler() {
        Map<String, Object> config = new HashMap<>();
        config.put("email", "test@junit.org");
        config.put("password", "junitPassword");
        config.put("region", "row");
        config.put("refreshInterval", 15);
        BridgeImpl bridge = new BridgeImpl(new ThingTypeUID("test", "account"), "MB");
        bridge.setConfiguration(new Configuration(config));
        return new ApiInjectedAccountHandler(bridge, mock(MercedesMeDiscoveryService.class), mock(HttpClient.class),
                mock(LocaleProvider.class));
    }

    private static void tearDown(AccountHandler ah) {
        ah.dispose();
    }

    @Test
    void forceUpdateRefreshesImmediatelyAndRestartsTheRefreshInterval() throws InterruptedException {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
        ApiInjectedAccountHandler ah = buildAccountHandler();
        ThingCallbackListener tcl = new ThingCallbackListener();
        ah.setCallback(tcl);
        try {
            CountingWebsocketMock ws = new CountingWebsocketMock(ah, mock(HttpClient.class), ah.config,
                    mock(LocaleProvider.class));
            ah.setApiMock(ws);
            // the mock is in place before initialize() schedules the first refresh, so that refresh cannot reach the
            // network even when this test thread is delayed
            ah.initialize();
            ScheduledFuture<?> firstRefresh = ah.refreshScheduler;
            assertNotNull(firstRefresh, "initialize must schedule the first refresh");
            assertTrue(firstRefresh.getDelay(TimeUnit.SECONDS) <= 2,
                    "the first refresh must be scheduled shortly after initialization");

            ah.forceUpdate("WDB1230011ANONYMIZ");
            assertEquals(1, ws.updateCount.get(), "forceUpdate must trigger an immediate update");

            ScheduledFuture<?> rescheduledRefresh = ah.refreshScheduler;
            assertNotNull(rescheduledRefresh, "forceUpdate must leave a pending refresh");
            assertNotSame(firstRefresh, rescheduledRefresh,
                    "the refresh scheduled by initialize must be replaced by the forced update");
            assertTrue(firstRefresh.isCancelled(),
                    "the refresh scheduled before the force update must be cancelled, otherwise it would fire "
                            + "shortly after the forced update");
            // refreshInterval is 15 minutes, so anything clearly above the initial 2 seconds means the interval was
            // restarted instead of the old pending refresh being kept
            assertTrue(rescheduledRefresh.getDelay(TimeUnit.SECONDS) > 60,
                    "the next regular refresh must follow the configured interval, not the previous schedule");
        } finally {
            tearDown(ah);
        }
    }

    @Test
    void concurrentForceUpdatesLeaveExactlyOnePendingRefresh() throws InterruptedException {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
        ApiInjectedAccountHandler ah = buildAccountHandler();
        ah.setCallback(new ThingCallbackListener());
        try {
            CountingWebsocketMock ws = new CountingWebsocketMock(ah, mock(HttpClient.class), ah.config,
                    mock(LocaleProvider.class));
            ah.setApiMock(ws);
            ah.initialize();
            assertNotNull(ah.refreshScheduler, "initialize must schedule the first refresh");

            // overlapping force updates must not each install their own timer - the replacement has to be serialized
            int threads = 8;
            CyclicBarrier startTogether = new CyclicBarrier(threads);
            ExecutorService starters = Executors.newFixedThreadPool(threads);
            try {
                List<Future<?>> started = new ArrayList<>();
                for (int i = 0; i < threads; i++) {
                    started.add(starters.submit(() -> {
                        startTogether.await(5, TimeUnit.SECONDS);
                        ah.forceUpdate("WDB1230011ANONYMIZ");
                        return null;
                    }));
                }
                for (Future<?> f : started) {
                    try {
                        f.get(10, TimeUnit.SECONDS);
                    } catch (ExecutionException | TimeoutException e) {
                        fail("force update did not finish in time", e);
                    }
                }
            } finally {
                starters.shutdownNow();
            }

            ScheduledFuture<?> pending = ah.refreshScheduler;
            assertNotNull(pending, "overlapping force updates must leave a pending refresh");
            long delay = pending.getDelay(TimeUnit.SECONDS);
            assertTrue(delay > 60 && delay <= 17 * 60,
                    "the surviving refresh must be the one restarted on the configured interval, but was " + delay
                            + " seconds away");
            assertEquals(threads, ws.updateCount.get(), "every force update must request an immediate update");
        } finally {
            tearDown(ah);
        }
    }

    @Test
    void refreshWithInvalidTokenRequestsUpdateImmediatelyAfterSuccessfulLogin() throws InterruptedException {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
        ApiInjectedAccountHandler ah = buildAccountHandler();
        ah.setCallback(new ThingCallbackListener());
        try {
            ConfigurableWebsocketMock ws = new ConfigurableWebsocketMock(ah, mock(HttpClient.class), ah.config,
                    mock(LocaleProvider.class), true);
            ah.setApiMock(ws);
            ah.initialize();
            assertFalse(ws.authTokenIsValid(), "the test must start without a valid token");

            ah.forceUpdate("WDB1230011ANONYMIZ");

            assertEquals(1, ws.loginCount.get(), "an invalid token must be resumed with a login");
            assertTrue(ws.authTokenIsValid(), "a successful login must yield a valid token");
            // without the fix the login only stores the token and the vehicle data is requested one interval later
            assertEquals(1, ws.updateCount.get(),
                    "a force update must request the vehicle data as soon as the login succeeded");
        } finally {
            tearDown(ah);
        }
    }

    @Test
    void refreshWithFailedLoginDoesNotRequestUpdateAndKeepsRetrying() throws InterruptedException {
        Utils.initialize(Utils.timeZoneProvider, Utils.localeProvider);
        ApiInjectedAccountHandler ah = buildAccountHandler();
        ThingCallbackListener tcl = new ThingCallbackListener();
        ah.setCallback(tcl);
        try {
            ConfigurableWebsocketMock ws = new ConfigurableWebsocketMock(ah, mock(HttpClient.class), ah.config,
                    mock(LocaleProvider.class), false);
            ah.setApiMock(ws);
            ah.initialize();

            ah.forceUpdate("WDB1230011ANONYMIZ");

            assertEquals(1, ws.loginCount.get(), "an invalid token must be resumed with a login");
            assertEquals(0, ws.updateCount.get(), "a failed login must not request vehicle data");
            assertEquals(ThingStatus.OFFLINE, tcl.getThingStatus().getStatus(),
                    "a failed login must report the account offline");
            assertNotNull(ah.refreshScheduler, "a failed login must keep the regular refresh pending for a retry");
        } finally {
            tearDown(ah);
        }
    }

    @Test
    void traceOutputMasksVinAndGpsPosition() {
        VehicleStatusUpdate update = VehicleStatusUpdate.newBuilder().setFinOrVin("W1N9N0CB6SJ140713")
                .setPositionLat(DoubleAttribute.newBuilder().setValue(52.520008).build())
                .setPositionLong(DoubleAttribute.newBuilder().setValue(13.404954).build()).build();

        VehicleStatusUpdate anonymized = AccountHandler.anonymizeForTrace(update);

        assertEquals("*************0713", anonymized.getFinOrVin(),
                "only the last 4 characters of the VIN may be logged");
        assertEquals(1.23, anonymized.getPositionLat().getValue(), 0.0, "latitude replaced by placeholder");
        assertEquals(4.56, anonymized.getPositionLong().getValue(), 0.0, "longitude replaced by placeholder");
    }
}
