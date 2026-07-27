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

import java.net.InetSocketAddress;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * Callbacks raised by the {@link OcppTransport} for inbound OCPP traffic. Every callback is keyed by
 * the library session id; the server bridge resolves that to a charge point (and, via connectorId,
 * to a connector) and updates the corresponding things.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public interface OcppServerListener {

    /**
     * A charger opened a WebSocket session. {@code chargePointId} is the last path segment of the
     * URL it dialled (the OCPP charge point identity); it is used to match the session to a
     * chargepoint thing.
     */
    void onSessionOpened(UUID session, @Nullable String chargePointId, @Nullable InetSocketAddress remote);

    /**
     * The charger's session was lost (socket closed or reconnected under a fresh session id).
     */
    void onSessionClosed(UUID session);

    /**
     * The charger sent a BootNotification — carries vendor / model / firmware for the chargepoint
     * properties.
     */
    void onBootNotification(UUID session, BootNotificationRequest request);

    /**
     * A connector reported a new status (StatusNotification).
     */
    void onStatusNotification(UUID session, StatusNotificationRequest request);

    /**
     * A connector reported metering samples (MeterValues).
     */
    void onMeterValues(UUID session, MeterValuesRequest request);

    /**
     * The charger sent a Heartbeat (or any keep-alive worth recording as "last seen").
     */
    void onHeartbeat(UUID session);

    /**
     * A transaction started on a connector (StartTransaction). The transaction id is the one this
     * server assigned in the confirmation.
     */
    void onStartTransaction(UUID session, StartTransactionRequest request, int transactionId);

    /**
     * A transaction stopped (StopTransaction). OCPP StopTransaction carries only the transaction id,
     * not the connector, so the connector is resolved from the id.
     */
    void onStopTransaction(UUID session, StopTransactionRequest request);

    /**
     * Whether an idTag is authorized to charge. Used to enforce the optional tag whitelist.
     */
    boolean isTagAuthorized(@Nullable String idTag);

    /**
     * The effective heartbeat interval (seconds) to return to the charger of this session — the
     * charge point's own override if set, otherwise the server default.
     */
    int heartbeatFor(UUID session);
}
