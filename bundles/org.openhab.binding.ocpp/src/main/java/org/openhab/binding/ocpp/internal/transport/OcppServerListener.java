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
 * Callbacks raised by the {@link OcppTransport} for inbound OCPP traffic, keyed by session id.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public interface OcppServerListener {

    void onSessionOpened(UUID session, @Nullable String chargePointId, @Nullable InetSocketAddress remote);

    void onSessionClosed(UUID session);

    void onBootNotification(UUID session, BootNotificationRequest request);

    void onStatusNotification(UUID session, StatusNotificationRequest request);

    void onMeterValues(UUID session, MeterValuesRequest request);

    void onHeartbeat(UUID session);

    void onStartTransaction(UUID session, StartTransactionRequest request, int transactionId);

    void onStopTransaction(UUID session, StopTransactionRequest request);

    void onAuthorize(UUID session, @Nullable String idTag);

    boolean isTagAuthorized(@Nullable String idTag);

    int heartbeatFor(UUID session);

    int nextTransactionId();
}
