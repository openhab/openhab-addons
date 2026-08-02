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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.JSONConfiguration;
import eu.chargetime.ocpp.JSONServer;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.ServerEvents;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.feature.profile.ServerRemoteTriggerProfile;
import eu.chargetime.ocpp.feature.profile.ServerSmartChargingProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;

/**
 * {@link OcppTransport} backed by the ChargeTime OCA-OCPP 1.6-J {@link JSONServer}. This is the only
 * class in the binding that touches {@code eu.chargetime.*}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class ChargeTimeTransport implements OcppTransport {

    private final Logger logger = LoggerFactory.getLogger(ChargeTimeTransport.class);
    private final JSONServer server;
    private final OcppServerListener listener;
    private volatile boolean started;
    private volatile boolean running;
    private final AtomicBoolean closed = new AtomicBoolean();

    public ChargeTimeTransport(OcppServerListener listener, int pingIntervalSeconds) {
        this.listener = listener;
        ServerCoreProfile core = new ServerCoreProfile(new InboundCoreHandler(listener));
        JSONConfiguration configuration = JSONConfiguration.get();
        // SO_REUSEADDR so the server can rebind the port immediately (e.g. when replacing another
        // OCPP server that just released it and left the socket in TIME_WAIT).
        configuration = configuration.setParameter(JSONConfiguration.REUSE_ADDR_PARAMETER, true);
        // This maps to the WebSocket connection-lost timeout: the server pings every interval and
        // CLOSES any connection that doesn't answer with a pong in time. Many OCPP chargers never
        // answer WebSocket pings, so enabling this drops healthy sessions on a fixed cycle. Liveness
        // is tracked at the OCPP level instead (heartbeats + the charge point liveness watchdog), so
        // ping-based detection stays off (0) unless a positive interval is explicitly configured.
        configuration = configuration.setParameter(JSONConfiguration.PING_INTERVAL_PARAMETER,
                pingIntervalSeconds > 0 ? pingIntervalSeconds : 0);
        this.server = new JSONServer(core, configuration);
        // Feature profiles the server sends beyond Core: SmartCharging carries SetChargingProfile
        // (the charge-limit and pause path), RemoteTrigger carries TriggerMessage (status refresh on
        // connect and optional MeterValues polling). Without a profile registered, sending its
        // requests fails with UnsupportedFeatureException.
        this.server.addFeatureProfile(new ServerSmartChargingProfile());
        this.server.addFeatureProfile(new ServerRemoteTriggerProfile());
    }

    @Override
    public void start(String host, int port) {
        server.open(host, port, new ServerEvents() {
            @Override
            public void authenticateSession(@Nullable SessionInformation information, @Nullable String username,
                    byte @Nullable [] password) {
                // LAN posture for the proof-of-concept: accept every charger. Basic-auth / token
                // validation is a later phase.
            }

            @Override
            public void newSession(@Nullable UUID sessionIndex, @Nullable SessionInformation information) {
                if (sessionIndex == null) {
                    return;
                }
                String identifier = normalizeIdentifier(information != null ? information.getIdentifier() : null);
                logger.debug("Charger session opened: {} (id={})", sessionIndex, identifier);
                listener.onSessionOpened(sessionIndex, identifier,
                        information != null ? information.getAddress() : null);
            }

            @Override
            public void lostSession(@Nullable UUID sessionIndex) {
                if (sessionIndex == null) {
                    return;
                }
                logger.debug("Charger session lost: {}", sessionIndex);
                listener.onSessionClosed(sessionIndex);
            }
        });
        started = true;
        running = true;
        logger.info("OCPP JSON server listening on {}:{}", host, port);
    }

    /**
     * The library reports the WebSocket URL path as the session identifier, including its leading
     * slash (e.g. {@code "/charx"}). The OCPP charge point id is that path without the leading
     * slash, which is what a chargepoint thing is configured with.
     */
    static @Nullable String normalizeIdentifier(@Nullable String identifier) {
        return identifier == null ? null : identifier.replaceFirst("^/+", "");
    }

    @Override
    public void stop() {
        running = false;
        // The handler publishes this transport before starting it (so session callbacks can always
        // reach it) — which means a dispose racing the startup task can invoke stop() before start(),
        // and both the dispose path and the losing startup path can invoke it. Closing the library
        // server before open() would fail on its unopened listener, and closing twice is not
        // guaranteed safe either, so: never-started is a no-op and the close runs exactly once.
        if (!started) {
            return;
        }
        if (closed.compareAndSet(false, true)) {
            server.close();
        }
    }

    @Override
    public boolean isRunning() {
        return running && !server.isClosed();
    }

    @Override
    public void closeSession(UUID session) {
        server.closeSession(session);
    }

    @Override
    public CompletionStage<Confirmation> send(UUID session, Request request) {
        try {
            return server.send(session, request);
        } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException e) {
            CompletableFuture<Confirmation> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
