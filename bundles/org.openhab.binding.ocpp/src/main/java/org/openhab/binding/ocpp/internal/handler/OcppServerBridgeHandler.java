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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.discovery.OcppDiscoveryService;
import org.openhab.binding.ocpp.internal.transport.ChargeTimeTransport;
import org.openhab.binding.ocpp.internal.transport.OcppServerListener;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
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

    private @Nullable OcppTransport transport;
    private @Nullable OcppDiscoveryService discoveryService;
    private OcppServerConfiguration config = new OcppServerConfiguration();

    public OcppServerBridgeHandler(Bridge bridge) {
        super(bridge);
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
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                OcppTransport newTransport = new ChargeTimeTransport(this, localConfig.pingInterval);
                newTransport.start(localConfig.host, localConfig.port);
                this.transport = newTransport;
                updateStatus(ThingStatus.ONLINE);
            } catch (RuntimeException e) {
                logger.warn("Failed to start OCPP server on {}:{}", localConfig.host, localConfig.port, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not bind OCPP server: " + e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        OcppTransport localTransport = transport;
        if (localTransport != null) {
            localTransport.stop();
            transport = null;
        }
        sessionChargePoints.clear();
        chargePoints.clear();
    }

    public @Nullable OcppTransport getTransport() {
        return transport;
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
            discovery.connectorDiscovered(handler.getThing().getUID(), connectorId);
        }
    }

    // --- OcppServerListener ---

    @Override
    public void onSessionOpened(UUID session, @Nullable String chargePointId, @Nullable InetSocketAddress remote) {
        if (chargePointId == null || chargePointId.isBlank()) {
            logger.warn("Charger session {} opened without an identity path; ignoring", session);
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
        // Reconnect self-heal: a charger that reconnects under a fresh session id leaves its old
        // one behind. Drop any prior session for the same charge point so the stale one can't be
        // treated as live or, when its close finally arrives, knock the charger offline.
        sessionChargePoints.entrySet()
                .removeIf(entry -> chargePointId.equals(entry.getValue()) && !session.equals(entry.getKey()));
        sessionChargePoints.put(session, chargePointId);
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
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onStartTransaction(request, transactionId);
        }
    }

    @Override
    public void onStopTransaction(UUID session, StopTransactionRequest request) {
        OcppChargePointHandler handler = resolve(session);
        if (handler != null) {
            handler.onStopTransaction(request);
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

    private @Nullable OcppChargePointHandler resolve(UUID session) {
        String chargePointId = sessionChargePoints.get(session);
        return chargePointId != null ? chargePoints.get(chargePointId) : null;
    }
}
