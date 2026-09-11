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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.AuthenticationException;
import eu.chargetime.ocpp.FeatureRepository;
import eu.chargetime.ocpp.ISession;
import eu.chargetime.ocpp.JSONConfiguration;
import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.OccurenceConstraintException;
import eu.chargetime.ocpp.Server;
import eu.chargetime.ocpp.ServerEvents;
import eu.chargetime.ocpp.SessionFactory;
import eu.chargetime.ocpp.UnsupportedFeatureException;
import eu.chargetime.ocpp.WebSocketListener;
import eu.chargetime.ocpp.WssListenerSupport;
import eu.chargetime.ocpp.feature.profile.ServerCoreProfile;
import eu.chargetime.ocpp.feature.profile.ServerLocalAuthListProfile;
import eu.chargetime.ocpp.feature.profile.ServerRemoteTriggerProfile;
import eu.chargetime.ocpp.feature.profile.ServerSmartChargingProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;
import eu.chargetime.ocpp.wss.BaseWssFactoryBuilder;

/**
 * {@link OcppTransport} backed by the ChargeTime OCA-OCPP 1.6-J server.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class ChargeTimeTransport implements OcppTransport {

    private static final long STARTUP_PROBE_TIMEOUT_MILLIS = 3000;
    private static final int PROBE_CONNECT_TIMEOUT_MILLIS = 250;
    private static final String MIN_BASIC_AUTH_PASSWORD_LENGTH_KEY = "OCPPJ_CP_MIN_PASSWORD_LENGTH";
    private static final String MAX_BASIC_AUTH_PASSWORD_LENGTH_KEY = "OCPPJ_CP_MAX_PASSWORD_LENGTH";

    private final Logger logger = LoggerFactory.getLogger(ChargeTimeTransport.class);
    private final Server server;
    private final WebSocketListener listener;
    private final OcppServerListener ocppListener;
    private final String authPassword;
    private volatile boolean started;
    private volatile boolean running;
    private final AtomicBoolean closed = new AtomicBoolean();

    public ChargeTimeTransport(OcppServerListener ocppListener, int pingIntervalSeconds, int requestTimeoutSeconds,
            String authPassword, String tlsKeystorePath, String tlsKeystorePassword) {
        this.ocppListener = ocppListener;
        this.authPassword = authPassword;
        FeatureRepository featureRepository = new FeatureRepository();
        InboundCoreHandler coreHandler = new InboundCoreHandler(ocppListener);
        featureRepository.addFeatureProfile(new ServerCoreProfile(coreHandler));
        featureRepository.addFeatureProfile(new ServerSmartChargingProfile());
        featureRepository.addFeatureProfile(new ServerRemoteTriggerProfile());
        featureRepository.addFeatureProfile(new ServerLocalAuthListProfile());
        featureRepository.addFeature(new TolerantBootNotificationFeature(coreHandler));

        JSONConfiguration configuration = JSONConfiguration.get();
        configuration = configuration.setParameter(JSONConfiguration.REUSE_ADDR_PARAMETER, true);
        // Off: many chargers never pong, so WebSocket pings would drop healthy sessions.
        configuration = configuration.setParameter(JSONConfiguration.PING_INTERVAL_PARAMETER,
                pingIntervalSeconds > 0 ? pingIntervalSeconds : 0);
        // Relax the library's handshake password-length check; real auth stays in authenticateSession.
        if (authPassword.isBlank()) {
            configuration = configuration.setParameter(MIN_BASIC_AUTH_PASSWORD_LENGTH_KEY, 0);
            configuration = configuration.setParameter(MAX_BASIC_AUTH_PASSWORD_LENGTH_KEY, Integer.MAX_VALUE);
        }

        Draft draft = new Draft_6455(List.of(), List.<IProtocol> of(new Protocol("ocpp1.6"), new Protocol("")));
        Map<String, ISession> requestSessions = new ConcurrentHashMap<>();
        this.listener = new WebSocketListener(
                new TrackingSessionFactory(new SessionFactory(featureRepository), requestSessions), configuration,
                draft);
        if (!tlsKeystorePath.isBlank()) {
            WssListenerSupport.enableWss(listener,
                    BaseWssFactoryBuilder.builder().sslContext(sslContext(tlsKeystorePath, tlsKeystorePassword)));
        }
        this.server = new Server(listener, new TimingOutPromiseRepository(ThreadPoolManager.getScheduledPool("ocpp"),
                requestTimeoutSeconds, requestSessions));
    }

    private static SSLContext sslContext(String keystorePath, String keystorePassword) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] password = keystorePassword.toCharArray();
            try (InputStream in = Files.newInputStream(Path.of(keystorePath))) {
                keyStore.load(in, password);
            }
            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, password);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), null, null);
            return context;
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("cannot load TLS keystore '" + keystorePath + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void start(String host, int port) {
        try (ServerSocket probe = new ServerSocket()) {
            probe.setReuseAddress(true);
            probe.bind(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new IllegalStateException("cannot bind " + host + ":" + port + " (" + e.getMessage() + ")", e);
        }

        server.open(host, port, new ServerEvents() {
            @Override
            public void authenticateSession(@Nullable SessionInformation information, @Nullable String username,
                    byte @Nullable [] password) throws AuthenticationException {
                ChargeTimeTransport.this.authenticateSession(information, username, password);
            }

            @Override
            public void newSession(@Nullable UUID sessionIndex, @Nullable SessionInformation information) {
                if (sessionIndex == null) {
                    return;
                }
                String identifier = normalizeIdentifier(information != null ? information.getIdentifier() : null);
                logger.debug("Charger session opened: {} (id={})", sessionIndex, identifier);
                ocppListener.onSessionOpened(sessionIndex, identifier,
                        information != null ? information.getAddress() : null);
            }

            @Override
            public void lostSession(@Nullable UUID sessionIndex) {
                if (sessionIndex == null) {
                    return;
                }
                logger.debug("Charger session lost: {}", sessionIndex);
                ocppListener.onSessionClosed(sessionIndex);
            }
        });

        if (port > 0) {
            awaitListening(host, port);
        }
        started = true;
        running = true;
        logger.debug("OCPP JSON server listening on {}:{}", host, port);
    }

    private void awaitListening(String host, int port) {
        String probeHost = "0.0.0.0".equals(host) ? "127.0.0.1" : host;
        long deadline = System.currentTimeMillis() + STARTUP_PROBE_TIMEOUT_MILLIS;
        IOException lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try (Socket probe = new Socket()) {
                probe.connect(new InetSocketAddress(probeHost, port), PROBE_CONNECT_TIMEOUT_MILLIS);
                return;
            } catch (IOException e) {
                lastFailure = e;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (closed.compareAndSet(false, true)) {
            try {
                server.close();
            } catch (RuntimeException e) {
                logger.debug("Closing the unstarted server failed: {}", e.getMessage());
            }
        }
        throw new IllegalStateException("server did not start listening on " + host + ":" + port
                + (lastFailure != null ? " (" + lastFailure.getMessage() + ")" : ""));
    }

    private void authenticateSession(@Nullable SessionInformation information, @Nullable String username,
            byte @Nullable [] password) throws AuthenticationException {
        if (authPassword.isBlank()) {
            return;
        }
        String chargePointId = normalizeIdentifier(information != null ? information.getIdentifier() : null);
        if (username == null || !username.equals(chargePointId)) {
            throw new AuthenticationException(401, "basic auth username must be the charge point id");
        }
        if (password == null || !authPassword.equals(new String(password, StandardCharsets.UTF_8))) {
            throw new AuthenticationException(401, "invalid password");
        }
    }

    static @Nullable String normalizeIdentifier(@Nullable String identifier) {
        return identifier == null ? null : identifier.replaceFirst("^/+", "");
    }

    @Override
    public void stop() {
        running = false;
        if (!started) {
            return;
        }
        if (closed.compareAndSet(false, true)) {
            server.close();
        }
    }

    @Override
    public boolean isRunning() {
        return running && !listener.isClosed();
    }

    @Override
    public void closeSession(UUID session) {
        server.closeSession(session);
    }

    @Override
    public CompletionStage<Confirmation> send(UUID session, Request request) {
        try {
            return server.send(session, request);
        } catch (OccurenceConstraintException | UnsupportedFeatureException | NotConnectedException
                | RuntimeException e) {
            CompletableFuture<Confirmation> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
