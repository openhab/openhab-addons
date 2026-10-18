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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.tedee.internal.TedeeBindingConstants;
import org.openhab.binding.tedee.internal.api.TedeeApiException;
import org.openhab.binding.tedee.internal.api.TedeeLock;
import org.openhab.binding.tedee.internal.configuration.TedeeLockConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Tests the action dispatching performed by {@link TedeeLockHandler}.
 *
 * @author Alex Goll - Added lifecycle and queue race coverage
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class TedeeLockHandlerTest {

    private static final int DEVICE_ID = 12345;

    private final TedeeTransportProvider provider = mock(TedeeTransportProvider.class);
    private final Thing thing = mock(Thing.class);
    private final TedeeLockHandler handler = new TedeeLockHandler(thing);

    /**
     * Verifies that the {@code UNLOCK} action invokes the unlock operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlock() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK");

        verify(provider).unlock(DEVICE_ID);
    }

    /**
     * Verifies that the {@code UNLOCK_OR_PULL} action invokes the unlock-or-pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlockOrPull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK_OR_PULL");

        verify(provider).unlockOrPull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code UNLOCK_NO_PULL} action invokes the unlock-without-pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseUnlockWithoutPull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "UNLOCK_NO_PULL");

        verify(provider).unlockWithoutPull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code PULL} action invokes the pull operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUsePull() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "PULL");

        verify(provider).pull(DEVICE_ID);
    }

    /**
     * Verifies that the {@code LOCK} action invokes the lock operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldUseLock() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "LOCK");

        verify(provider).lock(DEVICE_ID);
    }

    /**
     * Verifies that an unsupported action does not invoke any provider operation.
     *
     * @throws TedeeApiException if the action execution fails
     */
    @Test
    void shouldIgnoreUnsupportedAction() throws TedeeApiException {
        handler.executeAction(provider, DEVICE_ID, "INVALID");

        verifyNoInteractions(provider);
    }

    @Test
    void shouldNotExecuteQueuedActionAfterDispose() throws Exception {
        TedeeBridgeHandler oldProvider = mock(TedeeBridgeHandler.class);
        TedeeBridgeHandler newProvider = mock(TedeeBridgeHandler.class);
        Bridge bridge = mock(Bridge.class);
        ChannelUID channel = mock(ChannelUID.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeUid = mock(ThingUID.class);
        TedeeLockConfiguration configuration = configuration(123);
        TestableTedeeLockHandler testHandler = new TestableTedeeLockHandler(thing, configuration);
        CountDownLatch initialPollStarted = new CountDownLatch(1);
        CountDownLatch initialPollFinished = new CountDownLatch(1);
        CountDownLatch readinessStarted = new CountDownLatch(1);
        CountDownLatch releaseReadiness = new CountDownLatch(1);
        CountDownLatch readinessReturned = new CountDownLatch(1);

        when(thing.getBridgeUID()).thenReturn(bridgeUid);
        when(callback.getBridge(bridgeUid)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(oldProvider);
        when(channel.getId()).thenReturn(TedeeBindingConstants.C_ACTION);
        testHandler.setCallback(callback);
        when(oldProvider.ready()).thenAnswer(invocation -> {
            initialPollStarted.countDown();
            try {
                return false;
            } finally {
                initialPollFinished.countDown();
            }
        });
        testHandler.initialize();
        assertTrue(initialPollStarted.await(5, TimeUnit.SECONDS));
        assertTrue(initialPollFinished.await(5, TimeUnit.SECONDS));

        reset(oldProvider);
        when(oldProvider.ready()).thenReturn(true).thenAnswer(invocation -> {
            readinessStarted.countDown();
            try {
                releaseReadiness.await(5, TimeUnit.SECONDS);
            } finally {
                readinessReturned.countDown();
            }
            return true;
        });
        testHandler.handleCommand(channel, new StringType("UNLOCK"));

        assertTrue(readinessStarted.await(5, TimeUnit.SECONDS));

        testHandler.dispose();
        configuration.deviceId = 456;
        when(bridge.getHandler()).thenReturn(newProvider);
        testHandler.initialize();
        releaseReadiness.countDown();
        assertTrue(readinessReturned.await(5, TimeUnit.SECONDS));

        verify(oldProvider, never()).unlock(123);
        verify(oldProvider, never()).unlock(456);
        verify(newProvider, never()).unlock(123);
        verify(newProvider, never()).unlock(456);
        testHandler.dispose();
    }

    @Test
    void shouldExecuteQueuedActionForDeviceCapturedWhenCommandWasAccepted() throws Exception {
        TedeeBridgeHandler commandProvider = mock(TedeeBridgeHandler.class);
        CountDownLatch readinessStarted = new CountDownLatch(1);
        CountDownLatch releaseReadiness = new CountDownLatch(1);
        CountDownLatch actionExecuted = new CountDownLatch(1);
        Bridge bridge = mock(Bridge.class);
        ChannelUID channel = mock(ChannelUID.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeUid = mock(ThingUID.class);
        TedeeLockConfiguration configuration = configuration(123);
        TestableTedeeLockHandler testHandler = new TestableTedeeLockHandler(thing, configuration);
        CountDownLatch initialPollStarted = new CountDownLatch(1);
        CountDownLatch initialPollFinished = new CountDownLatch(1);

        when(thing.getBridgeUID()).thenReturn(bridgeUid);
        when(callback.getBridge(bridgeUid)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(commandProvider);
        when(channel.getId()).thenReturn(TedeeBindingConstants.C_ACTION);
        testHandler.setCallback(callback);
        when(commandProvider.ready()).thenAnswer(invocation -> {
            initialPollStarted.countDown();
            try {
                return false;
            } finally {
                initialPollFinished.countDown();
            }
        });
        testHandler.initialize();
        assertTrue(initialPollStarted.await(5, TimeUnit.SECONDS));
        assertTrue(initialPollFinished.await(5, TimeUnit.SECONDS));

        reset(commandProvider);
        when(commandProvider.ready()).thenReturn(true).thenAnswer(invocation -> {
            readinessStarted.countDown();
            releaseReadiness.await(5, TimeUnit.SECONDS);
            return true;
        });
        doAnswer(invocation -> {
            actionExecuted.countDown();
            return null;
        }).when(commandProvider).unlock(123);

        testHandler.handleCommand(channel, new StringType("UNLOCK"));

        assertTrue(readinessStarted.await(5, TimeUnit.SECONDS));
        configuration.deviceId = 456;
        releaseReadiness.countDown();

        assertTrue(actionExecuted.await(5, TimeUnit.SECONDS));
        verify(commandProvider).unlock(123);
        verify(commandProvider, never()).unlock(456);
        testHandler.dispose();
    }

    @Test
    void shouldDiscardRefreshResultAfterReinitialization() throws Exception {
        TedeeBridgeHandler oldProvider = mock(TedeeBridgeHandler.class);
        Bridge bridge = mock(Bridge.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeUid = mock(ThingUID.class);
        TedeeLockConfiguration configuration = configuration(123);
        TestableTedeeLockHandler testHandler = new TestableTedeeLockHandler(thing, configuration);
        CountDownLatch refreshStarted = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);
        CountDownLatch refreshFinished = new CountDownLatch(1);
        TedeeLock lock = new TedeeLock();
        lock.id = 123;
        lock.state = 6;
        lock.isConnected = 1;

        when(thing.getBridgeUID()).thenReturn(bridgeUid);
        when(callback.getBridge(bridgeUid)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(oldProvider);
        testHandler.setCallback(callback);
        when(oldProvider.ready()).thenReturn(true, false);
        when(oldProvider.getLock(anyInt())).thenAnswer(invocation -> {
            refreshStarted.countDown();
            try {
                releaseRefresh.await(5, TimeUnit.SECONDS);
                return lock;
            } finally {
                refreshFinished.countDown();
            }
        });
        testHandler.initialize();
        assertTrue(refreshStarted.await(5, TimeUnit.SECONDS));

        testHandler.dispose();
        configuration.deviceId = 456;
        testHandler.initialize();
        releaseRefresh.countDown();
        assertTrue(refreshFinished.await(5, TimeUnit.SECONDS));

        assertTrue(testHandler.stateUpdates == 0);
        assertTrue(testHandler.propertyUpdates == 0);
        assertTrue(testHandler.onlineUpdates == 0);
        testHandler.dispose();
    }

    @Test
    void shouldDiscardRefreshResultForDifferentDevice() throws Exception {
        TedeeBridgeHandler provider = mock(TedeeBridgeHandler.class);
        Bridge bridge = mock(Bridge.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        ThingUID bridgeUid = mock(ThingUID.class);
        TedeeLockConfiguration configuration = configuration(123);
        TestableTedeeLockHandler testHandler = new TestableTedeeLockHandler(thing, configuration);
        CountDownLatch refreshFinished = new CountDownLatch(1);
        TedeeLock lock = new TedeeLock();
        lock.id = 456;
        lock.state = 6;

        when(thing.getBridgeUID()).thenReturn(bridgeUid);
        when(callback.getBridge(bridgeUid)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(provider);
        testHandler.setCallback(callback);
        when(provider.ready()).thenReturn(true);
        when(provider.getLock(123)).thenAnswer(invocation -> {
            refreshFinished.countDown();
            return lock;
        });

        testHandler.initialize();
        assertTrue(refreshFinished.await(5, TimeUnit.SECONDS));

        assertTrue(testHandler.stateUpdates == 0);
        assertTrue(testHandler.propertyUpdates == 0);
        assertTrue(testHandler.onlineUpdates == 0);
        testHandler.dispose();
    }

    private static TedeeLockConfiguration configuration(int deviceId) {
        TedeeLockConfiguration configuration = new TedeeLockConfiguration();
        configuration.deviceId = deviceId;
        return configuration;
    }

    private static final class TestableTedeeLockHandler extends TedeeLockHandler {
        private final TedeeLockConfiguration configuration;
        private int stateUpdates;
        private int propertyUpdates;
        private int onlineUpdates;

        TestableTedeeLockHandler(Thing thing, TedeeLockConfiguration configuration) {
            super(thing);
            this.configuration = configuration;
        }

        @Override
        protected <T> T getConfigAs(Class<T> configClass) {
            return configClass.cast(configuration);
        }

        @Override
        protected void updateState(ChannelUID channelUID, State state) {
            stateUpdates++;
        }

        @Override
        public void updateThing(Thing thing) {
            propertyUpdates++;
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
        }

        @Override
        protected void updateStatus(ThingStatus status) {
            if (status == ThingStatus.ONLINE) {
                onlineUpdates++;
            }
        }
    }
}
