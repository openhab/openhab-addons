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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.AuthorizeRequest;
import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.DataTransferRequest;
import eu.chargetime.ocpp.model.core.DataTransferStatus;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.RegistrationStatus;
import eu.chargetime.ocpp.model.core.StartTransactionConfirmation;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;

/**
 * Tests the confirmations {@link InboundCoreHandler} returns for inbound Core-profile requests and the
 * events it forwards; the library turns a {@code null} return into a CallError a charger treats as failed.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class InboundCoreHandlerTest {

    private @NonNullByDefault({}) OcppServerListener listener;
    private @NonNullByDefault({}) InboundCoreHandler handler;
    private final UUID session = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listener = mock(OcppServerListener.class);
        when(listener.isTagAuthorized(any())).thenReturn(true);
        when(listener.heartbeatFor(any())).thenReturn(300);
        java.util.concurrent.atomic.AtomicInteger sequence = new java.util.concurrent.atomic.AtomicInteger();
        when(listener.nextTransactionId()).thenAnswer(invocation -> sequence.incrementAndGet());
        handler = new InboundCoreHandler(listener);
    }

    @Test
    void bootNotificationIsAcceptedAndCarriesTheChargePointHeartbeat() {
        when(listener.heartbeatFor(session)).thenReturn(42);

        BootNotificationConfirmation confirmation = handler.handleBootNotificationRequest(session,
                new BootNotificationRequest("vendor", "model"));

        assertEquals(RegistrationStatus.Accepted, confirmation.getStatus());
        assertEquals(42, confirmation.getInterval().intValue());
        assertNotNull(confirmation.getCurrentTime());
        verify(listener).onBootNotification(eq(session), any());
    }

    @Test
    void anUnknownTagIsRejectedWhenAWhitelistIsConfigured() {
        when(listener.isTagAuthorized("stranger")).thenReturn(false);

        assertEquals(AuthorizationStatus.Invalid,
                handler.handleAuthorizeRequest(session, new AuthorizeRequest("stranger")).getIdTagInfo().getStatus());
        assertEquals(AuthorizationStatus.Accepted,
                handler.handleAuthorizeRequest(session, new AuthorizeRequest("known")).getIdTagInfo().getStatus());
        verify(listener).onAuthorize(session, "stranger");
        verify(listener).onAuthorize(session, "known");
    }

    @Test
    void startTransactionFromAnUnknownTagIsRejectedAndNotRoutedOnwards() {
        when(listener.isTagAuthorized("stranger")).thenReturn(false);
        StartTransactionRequest request = new StartTransactionRequest(1, "stranger", 0, java.time.ZonedDateTime.now());

        StartTransactionConfirmation confirmation = handler.handleStartTransactionRequest(session, request);

        assertEquals(AuthorizationStatus.Invalid, confirmation.getIdTagInfo().getStatus());
        // A transaction id is still required by the schema even when the tag is refused.
        assertNotNull(confirmation.getTransactionId());
        verify(listener, never()).onStartTransaction(any(), any(), anyInt());
    }

    @Test
    void anAcceptedStartTransactionIsRoutedWithAUniqueId() {
        StartTransactionRequest request = new StartTransactionRequest(1, "known", 0, java.time.ZonedDateTime.now());

        int first = handler.handleStartTransactionRequest(session, request).getTransactionId();
        int second = handler.handleStartTransactionRequest(session, request).getTransactionId();

        assertEquals(first + 1, second, "each transaction must get its own id");
        verify(listener, org.mockito.Mockito.times(2)).onStartTransaction(eq(session), any(), anyInt());
    }

    @Test
    void anUnrecognisedVendorIdIsAnsweredRatherThanRefused() {
        // The spec has a status for this; answering with a CallError instead upsets some chargers.
        assertEquals(DataTransferStatus.UnknownVendorId,
                handler.handleDataTransferRequest(session, new DataTransferRequest("nobody")).getStatus());
    }

    @Test
    void meterValuesAreStillAcknowledgedWhenProcessingFails() {
        doThrow(new IllegalStateException("boom")).when(listener).onMeterValues(any(), any());

        // Must not propagate: an exception here becomes a CallError, and a charger that cannot get
        // its metering acknowledged will queue and retransmit it indefinitely.
        assertNotNull(handler.handleMeterValuesRequest(session, new MeterValuesRequest(1)));
    }
}
