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
package org.openhab.binding.gme.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gme.internal.GmeBindingConstants;
import org.openhab.binding.gme.internal.api.GmeApiClient;
import org.openhab.binding.gme.internal.api.GmeApiException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Exercises startup recovery and invalidation of obsolete authentication attempts.
 */
@NonNullByDefault
@SuppressWarnings("null")
class GmeApiBridgeHandlerTest {

    @Test
    void recoversFromInitialTimeoutWithoutManualReinitialization() throws Exception {
        GmeApiClient client = mock(GmeApiClient.class);
        when(client.authenticate("test-user", "test-password")).thenThrow(new TimeoutException("temporary timeout"))
                .thenReturn("recovered-token");

        Bridge bridge = configuredBridge();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        GmeApiBridgeHandler handler = handler(bridge, client, callback, 1);
        try {
            handler.initialize();

            verify(callback, timeout(3000)).statusUpdated(eq(bridge),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                            && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
            verify(callback, timeout(6000)).statusUpdated(eq(bridge),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));
            verify(client, times(2)).authenticate("test-user", "test-password");
            assertNotNull(handler.getAuthManager());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void doesNotRetryPermanentAuthenticationFailure() throws Exception {
        GmeApiClient client = mock(GmeApiClient.class);
        when(client.authenticate("test-user", "test-password"))
                .thenThrow(new GmeApiException("Invalid credentials", 401));

        Bridge bridge = configuredBridge();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        GmeApiBridgeHandler handler = handler(bridge, client, callback, 1);
        try {
            handler.initialize();

            verify(callback, timeout(3000)).statusUpdated(eq(bridge),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                            && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
            verify(client, times(1)).authenticate("test-user", "test-password");
            assertNull(handler.getAuthManager());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void obsoleteAuthenticationTimeoutCannotOverrideNewOnlineState() throws Exception {
        GmeApiClient client = mock(GmeApiClient.class);
        CountDownLatch oldAuthenticationStarted = new CountDownLatch(1);
        CountDownLatch releaseOldAuthentication = new CountDownLatch(1);
        CountDownLatch oldAuthenticationFinished = new CountDownLatch(1);
        AtomicInteger attempts = new AtomicInteger();

        when(client.authenticate("test-user", "test-password")).thenAnswer(invocation -> {
            if (attempts.getAndIncrement() == 0) {
                oldAuthenticationStarted.countDown();
                try {
                    if (!releaseOldAuthentication.await(5, TimeUnit.SECONDS)) {
                        throw new TimeoutException("old request did not finish");
                    }
                } finally {
                    oldAuthenticationFinished.countDown();
                }
                throw new TimeoutException("obsolete authentication failed");
            }
            return "new-token";
        });

        Bridge bridge = configuredBridge();
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        GmeApiBridgeHandler handler = handler(bridge, client, callback, 1);
        try {
            handler.initialize();
            assertTrue(oldAuthenticationStarted.await(3, TimeUnit.SECONDS));

            handler.initialize();
            verify(callback, timeout(5000)).statusUpdated(eq(bridge),
                    argThat(info -> info.getStatus() == ThingStatus.ONLINE));

            releaseOldAuthentication.countDown();
            assertTrue(oldAuthenticationFinished.await(3, TimeUnit.SECONDS));
            verify(callback, never()).statusUpdated(eq(bridge),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE));
            assertNotNull(handler.getAuthManager());
        } finally {
            releaseOldAuthentication.countDown();
            handler.dispose();
        }
    }

    private static Bridge configuredBridge() {
        Configuration config = new Configuration();
        config.put("username", "test-user");
        config.put("password", "test-password");
        return BridgeBuilder.create(GmeBindingConstants.THING_TYPE_API, "test-account").withConfiguration(config)
                .build();
    }

    private static GmeApiBridgeHandler handler(Bridge bridge, GmeApiClient client, ThingHandlerCallback callback,
            long retrySeconds) {
        @SuppressWarnings("unchecked")
        Storage<String> storage = mock(Storage.class);
        GmeApiBridgeHandler handler = new GmeApiBridgeHandler(bridge, client, storage,
                Clock.system(ZoneId.of("Europe/Rome")), retrySeconds);
        handler.setCallback(callback);
        return handler;
    }
}
