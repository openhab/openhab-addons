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
import eu.chargetime.ocpp.feature.profile.ServerRemoteTriggerProfile;
import eu.chargetime.ocpp.feature.profile.ServerSmartChargingProfile;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.SessionInformation;
import eu.chargetime.ocpp.wss.BaseWssFactoryBuilder;

/**
 * {@link OcppTransport} backed by the ChargeTime OCA-OCPP 1.6-J server. This is the only class in
 * the binding that touches {@code eu.chargetime.*}.
 *
 * <p>
 * The server is composed from the library's public parts ({@link Server}, {@link WebSocketListener},
 * {@link FeatureRepository}) rather than through {@code JSONServer}, so a
 * {@link TimingOutPromiseRepository} can be injected: the library itself never times out an
 * outbound request, and an unanswered call would otherwise stay incomplete forever and be retained.
 *
 * <p>
 * {@link #start} verifies the server actually listens before it returns. The embedded WebSocket
 * server binds its socket asynchronously on its own thread and reports a bind failure only to an
 * internal error callback this composition cannot reach — {@code start()} returning says nothing.
 * So the port is bind-tested first (which surfaces the real failure, e.g. an occupied port, as an
 * exception) and the running server is then probed with a real TCP connection until it accepts.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class ChargeTimeTransport implements OcppTransport {

    private static final long STARTUP_PROBE_TIMEOUT_MILLIS = 3000;
    private static final int PROBE_CONNECT_TIMEOUT_MILLIS = 250;
    // The library's own JSONConfiguration keys (not exposed as public constants) for the Basic-auth
    // password-length bounds its WebSocketListener enforces during the handshake.
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
            String authPassword, String tlsKeystore, String tlsKeystorePassword) {
        this.ocppListener = ocppListener;
        this.authPassword = authPassword;
        FeatureRepository featureRepository = new FeatureRepository();
        InboundCoreHandler coreHandler = new InboundCoreHandler(ocppListener);
        // Feature profiles the server sends beyond Core: SmartCharging carries SetChargingProfile
        // (the charge-limit and pause path), RemoteTrigger carries TriggerMessage (status refresh on
        // connect and optional MeterValues polling). Without a profile registered, sending its
        // requests fails with UnsupportedFeatureException.
        featureRepository.addFeatureProfile(new ServerCoreProfile(coreHandler));
        featureRepository.addFeatureProfile(new ServerSmartChargingProfile());
        featureRepository.addFeatureProfile(new ServerRemoteTriggerProfile());
        // Override the core BootNotification feature (a later addFeature wins on the action) so a
        // charger whose model or vendor exceeds the OCPP CiString20 limit — or omits one — is accepted
        // rather than rejected with a CALLERROR, which would leave it unable to come online. Handled by
        // the same core handler once accepted.
        featureRepository.addFeature(new TolerantBootNotificationFeature(coreHandler));

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
        // A charger that opens its connection with an HTTP Basic-auth header must clear the library's
        // handshake password-length check (the OCPP profile-1 rule, 16-20 characters) BEFORE this
        // binding's authenticateSession runs. Some chargers always send that header even with no backend
        // authentication — a V2C Trydan sends its id with an empty password on every connection — and the
        // check would then reject a connection we would otherwise accept, before it is ever logged. When
        // no authPassword is configured (profile 0) relax the bounds so any Basic-auth header is accepted;
        // authenticateSession still enforces the real credentials when a password IS set (profile 1), so
        // authentication is not weakened. These are the library's own (non-public) JSONConfiguration keys.
        if (authPassword.isBlank()) {
            configuration = configuration.setParameter(MIN_BASIC_AUTH_PASSWORD_LENGTH_KEY, 0);
            configuration = configuration.setParameter(MAX_BASIC_AUTH_PASSWORD_LENGTH_KEY, Integer.MAX_VALUE);
        }

        // The same subprotocols the library's own JSON server advertises.
        Draft draft = new Draft_6455(List.of(), List.<IProtocol> of(new Protocol("ocpp1.6"), new Protocol("")));
        // Shared with the promise repository so a timed-out request can be dropped from its session's queue.
        Map<String, ISession> requestSessions = new ConcurrentHashMap<>();
        this.listener = new WebSocketListener(
                new TrackingSessionFactory(new SessionFactory(featureRepository), requestSessions), configuration,
                draft);
        if (!tlsKeystore.isBlank()) {
            // Serve OCPP over TLS (wss://): hand the keystore's SSLContext to the library's WSS factory
            // before the server opens. With authPassword this is OCPP security profile 2; without it, an
            // encrypted profile 0. enableWSS has to be called before the listener opens its socket.
            WssListenerSupport.enableWss(listener,
                    BaseWssFactoryBuilder.builder().sslContext(sslContext(tlsKeystore, tlsKeystorePassword)));
        }
        this.server = new Server(listener, new TimingOutPromiseRepository(ThreadPoolManager.getScheduledPool("ocpp"),
                requestTimeoutSeconds, requestSessions));
    }

    /**
     * Builds an {@link SSLContext} from a PKCS12 keystore holding the server's certificate and key, for
     * serving OCPP over {@code wss://}.
     */
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
        // Surface bind failures synchronously: the embedded server would swallow them (see the class
        // comment), leaving a bridge that believes it is listening. Binding and releasing the port
        // here raises the genuine error — occupied port, unresolvable address, privileged port —
        // before the real server starts. The moment between release and the server's own bind is
        // covered by the listening probe below.
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

        // Port 0 (a test convenience — the thing configuration requires >= 1) binds an ephemeral
        // port this composition cannot learn, so only a real port can be probed.
        if (port > 0) {
            awaitListening(host, port);
        }
        started = true;
        running = true;
        logger.debug("OCPP JSON server listening on {}:{}", host, port);
    }

    /**
     * Probe with real TCP connections until the asynchronously-binding server accepts one. The probe
     * connection is closed before any WebSocket handshake, which the server tolerates quietly.
     */
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
        // Close the half-started server directly: `started` is still false here, so stop() would
        // no-op and leak the server thread.
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

    /**
     * Optional HTTP Basic authentication (OCPP security profile 1): when a password is configured,
     * a charger must present it with its charge point id as the username. With no password
     * configured every connection is accepted — security profile 0, documented in the README as
     * suitable for a trusted LAN only.
     */
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
            // The declared checked exceptions plus any unchecked one the underlying WebSocket can
            // raise — Java-WebSocket throws WebsocketNotConnectedException (a RuntimeException) when a
            // session drops mid-send. All must become a failed stage: a synchronous throw here would
            // abort the caller's drain loop and strand its other queued requests.
            CompletableFuture<Confirmation> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}
