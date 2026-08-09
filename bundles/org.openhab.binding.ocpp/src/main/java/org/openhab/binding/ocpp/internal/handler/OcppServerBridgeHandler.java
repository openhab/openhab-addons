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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.discovery.OcppDiscoveryService;
import org.openhab.binding.ocpp.internal.transport.ChargeTimeTransport;
import org.openhab.binding.ocpp.internal.transport.OcppServerListener;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.binding.ocpp.internal.transport.TransactionStore;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * The {@link OcppServerBridgeHandler} owns the OCPP JSON WebSocket endpoint and routes inbound
 * traffic. It keeps two maps — library session id to charge point id, and charge point id to its
 * handler — and dispatches each message to the matching {@link OcppChargePointHandler}. A session
 * whose charge point id has no thing is offered to discovery.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppServerBridgeHandler extends BaseBridgeHandler implements OcppServerListener {

    private final Logger logger = LoggerFactory.getLogger(OcppServerBridgeHandler.class);

    private final Map<UUID, String> sessionChargePoints = new ConcurrentHashMap<>();
    private final Map<String, OcppChargePointHandler> chargePoints = new ConcurrentHashMap<>();

    // Guards the transitions of the transport reference and the disposed flag so the asynchronous
    // startup below and dispose() cannot race into leaving a bound server behind on a disposed
    // handler. The transport field itself is additionally volatile: session callbacks and charge
    // point handlers read it from library and scheduler threads without taking this lock, and the
    // lock alone would not give those readers visibility.
    private final Object lifecycleLock = new Object();
    private volatile boolean disposed;
    // Bumped (under the lock) on every initialize and dispose; an asynchronous startup task carries
    // the generation it was created for and abandons itself if the handler has moved on.
    private long lifecycleGeneration;
    private volatile @Nullable Future<?> startupTask;

    private final StorageService storageService;
    private volatile @Nullable TransactionStore transactionStore;
    // Only used if the store is somehow unavailable; the store is created in initialize() before any
    // charger can start a transaction, so in practice ids always come from the persisted counter.
    private final AtomicInteger fallbackSequence = new AtomicInteger();

    private volatile @Nullable OcppTransport transport;
    private volatile @Nullable OcppDiscoveryService discoveryService;
    private volatile OcppServerConfiguration config = new OcppServerConfiguration();

    public OcppServerBridgeHandler(Bridge bridge, StorageService storageService) {
        super(bridge);
        this.storageService = storageService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OcppDiscoveryService.class);
    }

    public void setDiscoveryService(@Nullable OcppDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The server bridge has no writable channels.
    }

    public OcppServerConfiguration getServerConfig() {
        return config;
    }

    @Override
    public void initialize() {
        config = getConfigAs(OcppServerConfiguration.class);
        OcppServerConfiguration localConfig = config;
        // The embedded OCPP library only accepts Basic-auth passwords of 16-20 bytes and rejects the
        // handshake of every charger before the binding's authentication callback runs otherwise.
        // The thing-type pattern enforces this in the UI; this guard covers file-defined things.
        if (!localConfig.authPassword.isEmpty() && !localConfig.authPassword.matches("[\\x21-\\x7E]{16,20}")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "authPassword must be 16-20 visible ASCII characters — the OCPP library rejects other lengths "
                            + "before authentication, so every charger connection would fail");
            return;
        }
        disposed = false;
        // Created synchronously here so a transaction id or lookup is available the moment the
        // transport (started below) begins accepting chargers. Keyed per server bridge.
        transactionStore = new TransactionStore(storageService.getStorage(getThing().getUID().getAsString()));
        updateStatus(ThingStatus.UNKNOWN);

        // Published BEFORE it is started: the session callbacks raised during startup (a charger can
        // connect the moment the socket binds) must be able to reach the transport — to close a
        // rejected or duplicate session — so there must be no window in which the server accepts
        // sessions while the field is still null. Stopping a never-started transport is a no-op, so
        // a dispose that wins the race against the startup task below is safe.
        OcppTransport newTransport = createTransport(localConfig);
        long generation;
        synchronized (lifecycleLock) {
            if (disposed) {
                return;
            }
            generation = ++lifecycleGeneration;
            this.transport = newTransport;
        }

        startupTask = scheduler.submit(() -> {
            synchronized (lifecycleLock) {
                if (disposed || generation != lifecycleGeneration) {
                    return; // disposed (or re-initialized) before the server ever started
                }
            }
            try {
                newTransport.start(localConfig.host, localConfig.port);
            } catch (RuntimeException e) {
                boolean current;
                synchronized (lifecycleLock) {
                    current = !disposed && generation == lifecycleGeneration;
                    if (generation == lifecycleGeneration) {
                        transport = null;
                    }
                }
                // Status only while this startup still belongs to the live lifecycle — an abandoned
                // generation must not mark a re-initialized handler OFFLINE.
                if (current) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Could not start OCPP server: " + e.getMessage());
                }
                return;
            }
            boolean adopted;
            synchronized (lifecycleLock) {
                adopted = !disposed && generation == lifecycleGeneration;
            }
            if (adopted) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                // Lost the race with dispose(): stop the freshly-bound server rather than leaving it
                // running on an already-disposed handler. stop() runs its close exactly once, so this
                // cannot double-close against the dispose path.
                newTransport.stop();
            }
        });
    }

    @Override
    public void dispose() {
        OcppTransport localTransport;
        synchronized (lifecycleLock) {
            disposed = true;
            lifecycleGeneration++; // invalidate any startup task still in flight
            localTransport = transport;
            transport = null;
        }
        Future<?> task = startupTask;
        if (task != null) {
            task.cancel(true);
            startupTask = null;
        }
        if (localTransport != null) {
            localTransport.stop();
        }
        sessionChargePoints.clear();
        chargePoints.clear();
    }

    public @Nullable OcppTransport getTransport() {
        return transport;
    }

    /** The transport backing this server. A seam so a test can supply one without binding a socket. */
    protected OcppTransport createTransport(OcppServerConfiguration serverConfig) {
        return new ChargeTimeTransport(this, serverConfig.pingInterval, serverConfig.requestTimeoutSeconds,
                serverConfig.authPassword);
    }

    // --- charge point registration (called by OcppChargePointHandler) ---

    public void registerChargePoint(String chargePointId, OcppChargePointHandler handler) {
        chargePoints.put(chargePointId, handler);
        // Adopt an already-open session (the charger may have connected before its thing was ready).
        for (Map.Entry<UUID, String> entry : sessionChargePoints.entrySet()) {
            if (chargePointId.equals(entry.getValue())) {
                handler.onConnected(entry.getKey());
                return;
            }
        }
    }

    public void unregisterChargePoint(String chargePointId) {
        chargePoints.remove(chargePointId);
    }

    public void connectorDiscovered(String chargePointId, int connectorId) {
        OcppDiscoveryService discovery = discoveryService;
        OcppChargePointHandler handler = chargePoints.get(chargePointId);
        if (discovery != null && handler != null) {
            discovery.connectorDiscovered(handler.getThing().getUID(), chargePointId, connectorId);
        }
    }

    // --- OcppServerListener ---

    @Override
    public void onSessionOpened(UUID session, @Nullable String chargePointId, @Nullable InetSocketAddress remote) {
        if (chargePointId == null || chargePointId.isBlank()) {
            // remote can be null (the session may carry no peer address); fall back to the session id so
            // the message still names the offending connection. "connection {}" rather than "at {}" so it
            // reads correctly whether that resolves to a socket address or a session id.
            Object peer = remote != null ? remote : session;
            logger.warn(
                    "Charger connected without a charge point id in its URL path and was ignored (connection {}); it must dial ws://<host>:{}/<chargePointId>, not the bare root",
                    peer, config.port);
            return;
        }
        // Connection allow-list: if configured, only listed charge points may connect.
        List<String> allowed = config.chargers;
        if (!allowed.isEmpty() && !allowed.contains(chargePointId)) {
            logger.warn("Rejecting charger '{}' — not in the permitted chargers list", chargePointId);
            OcppTransport localTransport = transport;
            if (localTransport != null) {
                localTransport.closeSession(session);
            }
            return;
        }
        // Reconnect self-heal: a charger that reconnects under a fresh session id leaves its old one
        // behind. De-map any prior session for the same charge point (so the stale one can't be
        // treated as live) and then close its socket (so it can't linger sending ignored traffic).
        // De-mapping first makes the resulting onSessionClosed a no-op, so it can't offline the
        // charger we are about to bring online under the new session.
        List<UUID> staleSessions = new ArrayList<>();
        sessionChargePoints.entrySet().removeIf(entry -> {
            if (chargePointId.equals(entry.getValue()) && !session.equals(entry.getKey())) {
                staleSessions.add(entry.getKey());
                return true;
            }
            return false;
        });
        sessionChargePoints.put(session, chargePointId);
        OcppTransport localTransport = transport;
        if (localTransport != null) {
            for (UUID stale : staleSessions) {
                localTransport.closeSession(stale);
            }
        }
        logger.debug("Charger connected: id={} session={} from={}", chargePointId, session, remote);
        OcppChargePointHandler handler = chargePoints.get(chargePointId);
        if (handler != null) {
            handler.onConnected(session);
        } else {
            OcppDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.chargePointDiscovered(chargePointId);
            }
        }
    }

    @Override
    public void onSessionClosed(UUID session) {
        String chargePointId = sessionChargePoints.remove(session);
        if (chargePointId != null) {
            OcppChargePointHandler handler = chargePoints.get(chargePointId);
            if (handler != null) {
                handler.onDisconnected(session);
            }
        }
    }

    @Override
    public void onBootNotification(UUID session, BootNotificationRequest request) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onBootNotification(request);
        }
    }

    @Override
    public void onStatusNotification(UUID session, StatusNotificationRequest request) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onStatusNotification(request);
        }
    }

    @Override
    public void onMeterValues(UUID session, MeterValuesRequest request) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onMeterValues(request);
        }
    }

    @Override
    public void onHeartbeat(UUID session) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onHeartbeat();
        }
    }

    @Override
    public void onStartTransaction(UUID session, StartTransactionRequest request, int transactionId) {
        String chargePointId = sessionChargePoints.get(session);
        Integer connectorId = request.getConnectorId();
        if (chargePointId != null && connectorId != null) {
            // Persist as soon as the start is accepted, from the session's charge point identity and
            // the request's connector id — even if no charge-point or connector Thing exists yet (the
            // charger may still be in the discovery inbox). Otherwise the charger holds an accepted
            // transaction id the binding could never associate, recover, or route its stop to. When a
            // handler is present it does the in-memory routing; the persistence lives here, once.
            rememberTransaction(transactionId, chargePointId, connectorId);
        }
        OcppChargePointHandler handler = chargePointId != null ? chargePoints.get(chargePointId) : null;
        if (handler != null) {
            handler.onStartTransaction(request, transactionId);
        }
    }

    @Override
    public void onStopTransaction(UUID session, StopTransactionRequest request) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onStopTransaction(request);
            return;
        }
        // No charge-point handler yet (the charger is still in the discovery inbox), but the start was
        // persisted here at accept time — so the stop must clear it here too. Otherwise a transaction
        // that started and stopped before its Thing was added stays open in the store, and a later
        // restart would recover it as active (routable to a RemoteStop or a TxProfile). Guard with the
        // session's charge point identity, exactly as the handler path does, so a charger cannot clear
        // a transaction that belongs to a different one.
        String chargePointId = sessionChargePoints.get(session);
        Integer transactionId = request.getTransactionId();
        if (chargePointId != null && transactionId != null
                && transactionConnector(transactionId, chargePointId) != null) {
            forgetTransaction(transactionId);
        }
    }

    @Override
    public boolean isTagAuthorized(@Nullable String idTag) {
        List<String> whitelist = config.tags;
        return whitelist.isEmpty() || (idTag != null && whitelist.contains(idTag));
    }

    @Override
    public int heartbeatFor(UUID session) {
        OcppChargePointHandler handler = resolve(session);
        int override = handler != null ? handler.getHeartbeatOverride() : 0;
        return override > 0 ? override : config.heartbeatInterval;
    }

    @Override
    public int nextTransactionId() {
        TransactionStore store = transactionStore;
        return store != null ? store.nextTransactionId() : fallbackSequence.incrementAndGet();
    }

    // --- transaction persistence (called by OcppChargePointHandler) ---

    public void rememberTransaction(int transactionId, String chargePointId, int connectorId) {
        TransactionStore store = transactionStore;
        if (store != null) {
            store.begin(transactionId, chargePointId, connectorId);
        }
    }

    public void forgetTransaction(int transactionId) {
        TransactionStore store = transactionStore;
        if (store != null) {
            store.end(transactionId);
        }
    }

    /** The connector a transaction belongs to on this charge point, or {@code null} if not recorded. */
    public @Nullable Integer transactionConnector(int transactionId, String chargePointId) {
        TransactionStore store = transactionStore;
        if (store == null) {
            return null;
        }
        TransactionStore.Location location = store.locate(transactionId);
        return location != null && chargePointId.equals(location.chargePointId()) ? location.connectorId() : null;
    }

    /** A connector's open transaction id recovered after a restart, or {@code null} if none. */
    public @Nullable Integer openTransactionFor(String chargePointId, int connectorId) {
        TransactionStore store = transactionStore;
        return store != null ? store.openTransaction(chargePointId, connectorId) : null;
    }

    private @Nullable OcppChargePointHandler resolve(UUID session) {
        String chargePointId = sessionChargePoints.get(session);
        return chargePointId != null ? chargePoints.get(chargePointId) : null;
    }
}
