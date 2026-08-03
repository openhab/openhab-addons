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
package org.openhab.binding.ocpp.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests session routing in {@link OcppServerBridgeHandler}, in particular that a charger reconnecting
 * under a fresh session id does not leave its previous socket open.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
class OcppServerBridgeHandlerTest {

    private static final ThingUID SERVER_UID = new ThingUID(THING_TYPE_SERVER, "server");

    private @NonNullByDefault({}) OcppTransport transport;
    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) TestableBridgeHandler handler;

    /** Supplies a mock transport instead of binding a real socket. */
    private static final class TestableBridgeHandler extends OcppServerBridgeHandler {
        private final OcppTransport injected;

        TestableBridgeHandler(Bridge bridge, StorageService storageService, OcppTransport injected) {
            super(bridge, storageService);
            this.injected = injected;
        }

        @Override
        protected OcppTransport createTransport(
                org.openhab.binding.ocpp.internal.config.OcppServerConfiguration serverConfig) {
            return injected;
        }
    }

    private @NonNullByDefault({}) Bridge thing;

    @BeforeEach
    void setUp() {
        transport = mock(OcppTransport.class);

        Storage<String> storage = mock(Storage.class);
        StorageService storageService = mock(StorageService.class);
        when(storageService.<String> getStorage(anyString())).thenReturn(storage);

        thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(SERVER_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        callback = mock(ThingHandlerCallback.class);

        handler = new TestableBridgeHandler(thing, storageService, transport);
        handler.setCallback(callback);
    }

    @Test
    void aPasswordTheLibraryWouldRejectFailsInitializationInstead() {
        // The embedded library only accepts 16-20 byte Basic-auth passwords and rejects every
        // charger's handshake otherwise — before the binding's callback runs. A password outside
        // that range must therefore fail the bridge configuration, not silently lock every charger
        // out. (The thing-type pattern guards the UI; this guards file-defined things.)
        when(thing.getConfiguration()).thenReturn(new Configuration(java.util.Map.of("authPassword", "tooshort")));

        handler.initialize();

        verify(callback).statusUpdated(any(), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR));
        verify(transport, org.mockito.Mockito.after(500).never()).start(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void aReconnectUnderANewSessionClosesTheOldSocket() {
        handler.initialize();
        // The server starts asynchronously; ONLINE means the (mock) transport is in place.
        verify(callback, timeout(2000)).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));

        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        handler.onSessionOpened(first, "charx", null);
        handler.onSessionOpened(second, "charx", null);

        // The stale session's socket is closed; the live one is left alone.
        verify(transport).closeSession(first);
        verify(transport, never()).closeSession(second);
    }
}
