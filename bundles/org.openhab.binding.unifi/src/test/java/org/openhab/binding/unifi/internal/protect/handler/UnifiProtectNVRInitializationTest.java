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
package org.openhab.binding.unifi.internal.protect.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.THING_TYPE_CONTROLLER;
import static org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants.THING_TYPE_NVR;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.unifi.internal.api.UniFiSession;
import org.openhab.binding.unifi.internal.handler.UniFiControllerBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * Tests Protect initialization while the controller session is still being established.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class UnifiProtectNVRInitializationTest {

    private static final ThingUID CONTROLLER_UID = new ThingUID(THING_TYPE_CONTROLLER, "controller");

    private final UniFiControllerBridgeHandler parent = mock(UniFiControllerBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Queue<Runnable> initializationTasks = new ArrayDeque<>();
    private final CompletableFuture<UniFiSession> sessionFuture = new CompletableFuture<>();
    private final UniFiSession session = mock(UniFiSession.class);
    private final Bridge nvr = BridgeBuilder.create(THING_TYPE_NVR, "nvr").withBridge(CONTROLLER_UID)
            .withConfiguration(new Configuration(Map.of("autoManageToken", false))).build();
    private final UnifiProtectNVRHandler handler = new UnifiProtectNVRHandler(nvr, mock(ThingTypeRegistry.class));

    @BeforeEach
    void setUp() {
        Bridge controller = mock(Bridge.class);
        when(controller.getHandler()).thenReturn(parent);
        when(callback.getBridge(CONTROLLER_UID)).thenReturn(controller);
        handler.setCallback(callback);

        // Keep executor delivery under test control, while preserving real future completion and cancellation.
        CompletableFuture<UniFiSession> parentFuture = mock(withSettings().defaultAnswer(delegatesTo(sessionFuture)));
        when(parent.getSessionAsync()).thenReturn(parentFuture);
        doAnswer(invocation -> sessionFuture.whenCompleteAsync(invocation.getArgument(0), initializationTasks::add))
                .when(parentFuture).whenCompleteAsync(any(), any());
        when(parent.getHttpClient()).thenAnswer(invocation -> {
            assertTrue(sessionFuture.isDone(), "HTTP client must only be read after the controller is ready");
            return mock(HttpClient.class);
        });
        when(parent.getHost()).thenReturn("console");
        when(parent.getPort()).thenReturn(443);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    @Test
    void waitsForControllerBeforeReadingConnectionDetails() {
        assertDoesNotThrow(handler::initialize);

        verify(callback).statusUpdated(eq(nvr), argThat(status -> status.getStatus() == ThingStatus.UNKNOWN));
        verifyNoConnectionDetailsRead();
        assertTrue(initializationTasks.isEmpty());

        sessionFuture.complete(session);
        verifyNoConnectionDetailsRead();
        runInitialization();

        verify(parent).getHttpClient();
        verify(parent).getHost();
        verify(parent).getPort();
        verifyTokenValidationReached();
    }

    @Test
    void initializesWhenControllerIsAlreadyReady() {
        sessionFuture.complete(session);

        handler.initialize();
        runInitialization();

        verify(parent).getHttpClient();
        verifyTokenValidationReached();
    }

    @Test
    void reportsParentFailureWithoutReadingConnectionDetails() {
        handler.initialize();
        sessionFuture.completeExceptionally(new IllegalStateException("Controller login failed"));
        runInitialization();

        verify(callback).statusUpdated(eq(nvr), eq(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Controller login failed")));
        verifyNoConnectionDetailsRead();
    }

    @Test
    void disposalWhileWaitingDoesNotCancelSharedSessionOrInitializeLater() {
        handler.initialize();
        handler.dispose();
        clearInvocations(callback);

        assertFalse(sessionFuture.isDone());
        assertTrue(sessionFuture.complete(session));
        drainInitializations();

        verifyNoConnectionDetailsRead();
        verifyNoInteractions(callback);
    }

    @Test
    void disposalCancelsInitializationAlreadyQueuedOnExecutor() {
        handler.initialize();
        sessionFuture.complete(session);
        handler.dispose();
        clearInvocations(callback);

        runInitialization();

        verifyNoConnectionDetailsRead();
        verifyNoInteractions(callback);
    }

    @Test
    void reinitializationIgnoresPreviousSessionWait() {
        handler.initialize();
        handler.dispose();
        handler.initialize();

        sessionFuture.complete(session);
        drainInitializations();

        verify(parent).getHttpClient();
        verifyTokenValidationReached();
    }

    @Test
    void disposalSuppressesLateParentFailure() {
        handler.initialize();
        handler.dispose();
        clearInvocations(callback);

        sessionFuture.completeExceptionally(new IllegalStateException("Controller disposed"));
        drainInitializations();

        verifyNoConnectionDetailsRead();
        verifyNoInteractions(callback);
    }

    private void runInitialization() {
        assertFalse(initializationTasks.isEmpty());
        initializationTasks.remove().run();
    }

    private void drainInitializations() {
        while (!initializationTasks.isEmpty()) {
            runInitialization();
        }
    }

    private void verifyNoConnectionDetailsRead() {
        verify(parent, never()).getHttpClient();
        verify(parent, never()).getHost();
        verify(parent, never()).getPort();
    }

    private void verifyTokenValidationReached() {
        verify(callback).statusUpdated(eq(nvr), eq(new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.CONFIGURATION_ERROR, "API token required when auto-manage is disabled")));
    }
}
