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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

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

    /** A minimal in-memory Storage so transaction persistence can be exercised. */
    private static final class MemoryStorage implements Storage<String> {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public @Nullable String put(String key, @Nullable String value) {
            return value == null ? map.remove(key) : map.put(key, value);
        }

        @Override
        public @Nullable String remove(String key) {
            return map.remove(key);
        }

        @Override
        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        @Override
        public @Nullable String get(String key) {
            return map.get(key);
        }

        @Override
        public Collection<String> getKeys() {
            return new HashSet<>(map.keySet());
        }

        @Override
        public Collection<@Nullable String> getValues() {
            return new ArrayList<>(map.values());
        }
    }

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

        StorageService storageService = mock(StorageService.class);
        when(storageService.<String> getStorage(anyString())).thenReturn(new MemoryStorage());

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
    void aTransactionAcceptedBeforeItsHandlerExistsIsStillPersisted() {
        // Passive-discovery race: a charger's session is mapped (it is in the inbox) but no
        // charge-point/connector Thing exists yet, and it starts an authorized transaction. The
        // mapping must be persisted at the bridge from the session identity and the request, so the
        // charger does not hold an id openHAB can never recover, route a stop to, or remote-stop.
        handler.initialize();
        verify(callback, timeout(2000)).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));

        UUID session = UUID.randomUUID();
        handler.onSessionOpened(session, "charx", null); // maps session -> charx; no handler registered
        handler.onStartTransaction(session, new StartTransactionRequest(2, "tag", 0, ZonedDateTime.now()), 77);

        assertEquals(Integer.valueOf(77), handler.openTransactionFor("charx", 2),
                "the transaction must be recoverable even though no handler existed at accept time");
    }

    @Test
    void aTransactionStoppedBeforeItsHandlerExistsIsClearedFromTheStore() {
        // Same passive-discovery race, but the charger STOPS the transaction while still in the inbox.
        // Since the start was persisted at the bridge, the stop must clear it at the bridge too —
        // otherwise the store keeps an already-finished transaction that a later restart recovers as
        // active (routable to a RemoteStop or a TxProfile).
        handler.initialize();
        verify(callback, timeout(2000)).statusUpdated(any(),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));

        UUID session = UUID.randomUUID();
        handler.onSessionOpened(session, "charx", null); // mapped; no handler registered
        handler.onStartTransaction(session, new StartTransactionRequest(2, "tag", 0, ZonedDateTime.now()), 77);
        assertEquals(Integer.valueOf(77), handler.openTransactionFor("charx", 2));

        handler.onStopTransaction(session, new StopTransactionRequest(0, ZonedDateTime.now(), 77));

        org.junit.jupiter.api.Assertions.assertNull(handler.openTransactionFor("charx", 2),
                "a stop before the handler exists must clear the persisted transaction");
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
