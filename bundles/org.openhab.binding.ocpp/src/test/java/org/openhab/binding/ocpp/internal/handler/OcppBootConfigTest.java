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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;

/**
 * Tests the configuration a charger receives after it boots.
 *
 * <p>
 * This is deliberately conservative: a charger that is busy — flushing an offline message queue, for
 * instance — may leave a request unanswered, and an unanswered request times out and takes the
 * session down with it. Re-sending configuration on every reconnect therefore turns one reconnect
 * into a loop, which is why it is sent until accepted once and then left alone.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppBootConfigTest {

    private static final ThingUID SERVER_UID = new ThingUID(THING_TYPE_SERVER, "server");
    private static final ThingUID CP_UID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");

    private @NonNullByDefault({}) OcppChargePointHandler handler;
    private @NonNullByDefault({}) OcppTransport transport;
    private @NonNullByDefault({}) OcppServerConfiguration serverConfig;
    private final List<ChangeConfigurationRequest> sent = new ArrayList<>();

    @BeforeEach
    void setUp() {
        serverConfig = new OcppServerConfiguration();
        serverConfig.meterValuesData = "";
        serverConfig.disableRemoteTxAuthorization = true; // exactly one step unless stated otherwise

        transport = mock(OcppTransport.class);
        acceptEverything();

        OcppServerBridgeHandler serverHandler = mock(OcppServerBridgeHandler.class);
        when(serverHandler.getServerConfig()).thenReturn(serverConfig);
        when(serverHandler.getTransport()).thenReturn(transport);

        Bridge serverThing = mock(Bridge.class);
        when(serverThing.getHandler()).thenReturn(serverHandler);

        Bridge cpThing = mock(Bridge.class);
        when(cpThing.getUID()).thenReturn(CP_UID);
        when(cpThing.getBridgeUID()).thenReturn(SERVER_UID);
        when(cpThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(cpThing.getConfiguration())
                .thenReturn(new Configuration(Map.of("chargePointId", "charger", "configSettleSeconds", 0)));

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(SERVER_UID)).thenReturn(serverThing);

        handler = new OcppChargePointHandler(cpThing);
        handler.setCallback(callback);
        handler.initialize();
        handler.onConnected(UUID.randomUUID());
    }

    private void acceptEverything() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });
    }

    private void record(Request request) {
        if (request instanceof ChangeConfigurationRequest change) {
            synchronized (sent) {
                sent.add(change);
            }
        }
    }

    private List<String> sentValuesFor(String key) {
        synchronized (sent) {
            return sent.stream().filter(r -> key.equals(r.getKey())).map(ChangeConfigurationRequest::getValue).toList();
        }
    }

    @Test
    void configurationIsSentWhenAChargerBoots() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(2000)).send(any(), any());
        assertEquals(List.of("false"), sentValuesFor("AuthorizeRemoteTxRequests"));
    }

    @Test
    void anAcceptedConfigurationIsNotSentAgainOnTheNextBoot() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(2000)).send(any(), any());

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        // Still exactly one send: repeating it is what turns a reconnect into a connect/configure/
        // drop loop on a charger that cannot answer promptly.
        verify(transport, timeout(1000).times(1)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void aRejectedConfigurationIsRetriedOnTheNextBoot() {
        // A ChangeConfiguration answered Rejected completes normally but has not applied, so the
        // burst must not latch on it — the key is attempted again when the charger next boots.
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Rejected));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(2000)).send(any(), any());
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(2000).times(2)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void aChargerThatNeverAnswersIsNotRetriedForever() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.failedFuture(new IllegalStateException("no answer"));
        });

        for (int boot = 0; boot < 6; boot++) {
            handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        }

        verify(transport, timeout(3000).atLeast(1)).send(any(), any());
        synchronized (sent) {
            assertTrue(sent.size() <= 3, "attempts must be capped, but saw " + sent.size());
        }
    }

    @Test
    void aRejectedMeasurandIsDroppedUntilTheChargerAcceptsTheList() {
        serverConfig.meterValuesData = "Energy.Active.Import.Register,Power.Active.Import,Temperature";
        serverConfig.disableRemoteTxAuthorization = false;
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(1);
            record(request);
            boolean rejected = request instanceof ChangeConfigurationRequest change && change.getValue() != null
                    && change.getValue().contains("Temperature");
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(
                    rejected ? ConfigurationStatus.Rejected : ConfigurationStatus.Accepted));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(3000).atLeast(2)).send(any(), any());
        List<String> tried = sentValuesFor("MeterValuesSampledData");
        assertEquals("Energy.Active.Import.Register,Power.Active.Import,Temperature", tried.get(0));
        assertEquals("Energy.Active.Import.Register,Power.Active.Import", tried.get(tried.size() - 1),
                "the rejected measurand should have been dropped");
    }
}
