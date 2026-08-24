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
 * Owns the OCPP JSON WebSocket endpoint and routes inbound traffic to the matching
 * {@link OcppChargePointHandler}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppServerBridgeHandler extends BaseBridgeHandler implements OcppServerListener {

    private final Logger logger = LoggerFactory.getLogger(OcppServerBridgeHandler.class);

    private final Map<UUID, String> sessionChargePoints = new ConcurrentHashMap<>();
    private final Map<String, OcppChargePointHandler> chargePoints = new ConcurrentHashMap<>();

    private final Object lifecycleLock = new Object();
    private volatile boolean disposed;
    private long lifecycleGeneration;
    private volatile @Nullable Future<?> startupTask;

    private final StorageService storageService;
    private volatile @Nullable TransactionStore transactionStore;
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
    }

    public OcppServerConfiguration getServerConfig() {
        return config;
    }

    @Override
    public void initialize() {
        config = getConfigAs(OcppServerConfiguration.class);
        OcppServerConfiguration localConfig = config;
        if (!localConfig.authPassword.isEmpty() && !localConfig.authPassword.matches("[\\x21-\\x7E]{16,20}")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "authPassword must be 16-20 visible ASCII characters — the OCPP library rejects other lengths "
                            + "before authentication, so every charger connection would fail");
            return;
        }
        disposed = false;
        transactionStore = new TransactionStore(storageService.getStorage(getThing().getUID().getAsString()));
        updateStatus(ThingStatus.UNKNOWN);

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
                    return;
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
                newTransport.stop();
            }
        });
    }

    @Override
    public void dispose() {
        OcppTransport localTransport;
        synchronized (lifecycleLock) {
            disposed = true;
            lifecycleGeneration++;
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

    protected OcppTransport createTransport(OcppServerConfiguration serverConfig) {
        return new ChargeTimeTransport(this, serverConfig.pingInterval, serverConfig.requestTimeoutSeconds,
                serverConfig.authPassword, serverConfig.tlsKeystorePath, serverConfig.tlsKeystorePassword);
    }

    public void registerChargePoint(String chargePointId, OcppChargePointHandler handler) {
        chargePoints.put(chargePointId, handler);
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

    @Override
    public void onSessionOpened(UUID session, @Nullable String chargePointId, @Nullable InetSocketAddress remote) {
        if (chargePointId == null || chargePointId.isBlank()) {
            Object peer = remote != null ? remote : session;
            logger.warn(
                    "Charger connected without a charge point id in its URL path and was ignored (connection {}); it must dial ws://<host>:{}/<chargePointId>, not the bare root",
                    peer, config.port);
            OcppTransport localTransport = transport;
            if (localTransport != null) {
                localTransport.closeSession(session);
            }
            return;
        }
        List<String> allowed = config.chargerIds;
        if (!allowed.isEmpty() && !allowed.contains(chargePointId)) {
            logger.warn("Rejecting charger '{}' — not in the permitted chargers list", chargePointId);
            OcppTransport localTransport = transport;
            if (localTransport != null) {
                localTransport.closeSession(session);
            }
            return;
        }
        // De-map any prior session before closing it, so onSessionClosed stays a no-op.
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
    public void onAuthorize(UUID session, @Nullable String idTag) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onAuthorized(idTag);
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
            // Persist at accept time, even before a Thing exists, so the stop can be routed.
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
        // No handler yet: clear the persisted-at-accept transaction here, guarded by charge point identity.
        String chargePointId = sessionChargePoints.get(session);
        Integer transactionId = request.getTransactionId();
        if (chargePointId != null && transactionId != null
                && transactionConnector(transactionId, chargePointId) != null) {
            forgetTransaction(transactionId);
        }
    }

    @Override
    public boolean isTagAuthorized(@Nullable String idTag) {
        List<String> whitelist = config.whitelistTagIds;
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

    public @Nullable Integer transactionConnector(int transactionId, String chargePointId) {
        TransactionStore store = transactionStore;
        if (store == null) {
            return null;
        }
        TransactionStore.Location location = store.locate(transactionId);
        return location != null && chargePointId.equals(location.chargePointId()) ? location.connectorId() : null;
    }

    public @Nullable Integer openTransactionFor(String chargePointId, int connectorId) {
        TransactionStore store = transactionStore;
        return store != null ? store.openTransaction(chargePointId, connectorId) : null;
    }

    private @Nullable OcppChargePointHandler resolve(UUID session) {
        String chargePointId = sessionChargePoints.get(session);
        return chargePointId != null ? chargePoints.get(chargePointId) : null;
    }
}
