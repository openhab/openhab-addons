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

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;

/**
 * The seam between this binding and the underlying OCPP protocol library. Everything OCPP-wire lives
 * behind this interface, so the concrete library (currently ChargeTime OCA-OCPP 1.6-J) can be
 * swapped or upgraded without touching the thing handlers.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public interface OcppTransport {

    /**
     * Bind the WebSocket endpoint and start accepting charger connections. Returns immediately; the
     * server runs on its own threads.
     */
    void start(String host, int port);

    /**
     * Close the endpoint and drop all sessions.
     */
    void stop();

    boolean isRunning();

    /**
     * Force-close a session (used to make a silent charger reconnect fresh).
     */
    void closeSession(UUID session);

    /**
     * Send an outbound request (RemoteStart, SetChargingProfile, ChangeConfiguration, ...) to a
     * charger session. The returned stage completes with the charger's confirmation, or completes
     * exceptionally if the session is gone or the feature is unsupported.
     */
    CompletionStage<Confirmation> send(UUID session, Request request);
}
