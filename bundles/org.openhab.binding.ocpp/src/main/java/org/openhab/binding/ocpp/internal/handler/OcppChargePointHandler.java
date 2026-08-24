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

import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ocpp.internal.config.OcppChargePointConfiguration;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.transport.ChargerCapabilities;
import org.openhab.binding.ocpp.internal.transport.Measurands;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.AuthorizationStatus;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.GetConfigurationRequest;
import eu.chargetime.ocpp.model.core.IdTagInfo;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.ResetRequest;
import eu.chargetime.ocpp.model.core.ResetType;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import eu.chargetime.ocpp.model.localauthlist.AuthorizationData;
import eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionConfirmation;
import eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionRequest;
import eu.chargetime.ocpp.model.localauthlist.SendLocalListRequest;
import eu.chargetime.ocpp.model.localauthlist.UpdateType;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType;

/**
 * Represents one physical charger: tracks its session and routes its OCPP messages to the connectors.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppChargePointHandler extends BaseBridgeHandler {

    @FunctionalInterface
    private interface BootConfigStep {
        CompletableFuture<Confirmation> send();
    }

    private record PendingSend(UUID session, Request request, CompletableFuture<Confirmation> future) {
    }

    private static final long LIVENESS_FLOOR_SECONDS = 180;
    private static final long STATUS_FALLBACK_SECONDS = 25;
    private static final long LEARN_WINDOW_SECONDS = 60;
    private static final int MAX_BOOT_CONFIG_ATTEMPTS = 3;
    private static final long BOOT_READY_GRACE_MILLIS = 1000;
    private static final int PENDING_SEND_LIMIT = 32;
    private static final int OUTBOUND_LIMIT = 64;

    private final Logger logger = LoggerFactory.getLogger(OcppChargePointHandler.class);
    private final Map<Integer, OcppConnectorHandler> connectors = new ConcurrentHashMap<>();
    private final Map<Integer, OcppConnectorHandler> transactions = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<PendingSend> pendingSends = new ConcurrentLinkedQueue<>();
    private final Object dispatchLock = new Object();
    private final Deque<PendingSend> outbound = new ArrayDeque<>();
    private boolean dispatching;
    private @Nullable PendingSend inFlight;
    private long dispatchEpoch;
    // Guards the coupled (session, operational) write; never takes dispatchLock or does I/O under it.
    private final Object stateLock = new Object();

    private volatile String chargePointId = "";
    private volatile int configSettleSeconds;
    private volatile boolean meterless;
    private volatile int heartbeat;

    private volatile @Nullable OcppServerBridgeHandler server;
    private volatile @Nullable UUID session;
    private volatile boolean operational;
    private final Map<String, String> acceptedMeasurands = new ConcurrentHashMap<>();
    private volatile ChargerCapabilities capabilities = ChargerCapabilities.unknown();
    private volatile List<String> localAuthList = List.of();
    private volatile boolean learningCard;
    private volatile @Nullable ScheduledFuture<?> learnTask;
    private volatile @Nullable ScheduledFuture<?> bootConfigTask;
    private volatile @Nullable ScheduledFuture<?> livenessTask;
    private volatile @Nullable ScheduledFuture<?> statusFallbackTask;
    private volatile @Nullable ScheduledFuture<?> readyTask;
    private volatile boolean bootAccepted;
    private volatile @Nullable String appliedConfigFingerprint;
    private volatile @Nullable String attemptedConfigFingerprint;
    private final AtomicInteger bootConfigAttempts = new AtomicInteger();

    public OcppChargePointHandler(Bridge bridge) {
        super(bridge);
    }

    public String getChargePointId() {
        return chargePointId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_RESET.equals(channelUID.getId()) && command == OnOffType.ON) {
            if (isReady()) {
                send(new ResetRequest(ResetType.Soft)).whenComplete((confirmation, ex) -> {
                    if (ex != null) {
                        logger.warn("Reset of {} failed: {}", chargePointId, ex.getMessage());
                    }
                });
            } else {
                logger.debug("Reset of {} skipped — charge point not ready", chargePointId);
            }
            updateState(CHANNEL_RESET, OnOffType.OFF);
        } else if (CHANNEL_LOCAL_AUTH_LIST.equals(channelUID.getId()) && command instanceof StringType text) {
            setLocalAuthList(parseTagList(text.toString()));
        } else if (CHANNEL_LEARN_CARD.equals(channelUID.getId()) && command instanceof OnOffType onOff) {
            if (onOff == OnOffType.ON) {
                learningCard = true;
                cancel(learnTask);
                learnTask = scheduler.schedule(this::stopLearning, LEARN_WINDOW_SECONDS, TimeUnit.SECONDS);
                logger.info("Learning next card for {} — present a card within {} s", chargePointId,
                        LEARN_WINDOW_SECONDS);
            } else {
                stopLearning();
            }
        }
    }

    private void stopLearning() {
        learningCard = false;
        cancel(learnTask);
        learnTask = null;
        updateState(CHANNEL_LEARN_CARD, OnOffType.OFF);
    }

    private void setLocalAuthList(List<String> tags) {
        localAuthList = tags;
        updateProperty(PROPERTY_LOCAL_AUTH_LIST, tags.isEmpty() ? null : String.join(",", tags));
        updateState(CHANNEL_LOCAL_AUTH_LIST, new StringType(String.join(",", tags)));
        if (isReady() && Boolean.TRUE.equals(capabilities.supportsLocalAuthList().orElse(false))) {
            provisionLocalAuthList(tags).whenComplete((confirmation, ex) -> {
                if (ex != null) {
                    logger.warn("SendLocalList to {} failed: {}", chargePointId, ex.getMessage());
                }
            });
        }
    }

    public void onAuthorized(@Nullable String idTag) {
        if (idTag == null || idTag.isBlank() || !learningCard) {
            return;
        }
        stopLearning();
        if (!localAuthList.contains(idTag)) {
            List<String> updated = new ArrayList<>(localAuthList);
            updated.add(idTag);
            logger.info("Learned card {} for {}", idTag, chargePointId);
            setLocalAuthList(updated);
        }
    }

    @Override
    public void initialize() {
        OcppChargePointConfiguration config = getConfigAs(OcppChargePointConfiguration.class);
        chargePointId = config.chargePointId;
        configSettleSeconds = config.configSettleSeconds;
        meterless = config.meterless;
        heartbeat = config.heartbeat;
        String savedAuthList = getThing().getProperties().get(PROPERTY_LOCAL_AUTH_LIST);
        if (savedAuthList != null && !savedAuthList.isBlank()) {
            localAuthList = parseTagList(savedAuthList);
            updateState(CHANNEL_LOCAL_AUTH_LIST, new StringType(String.join(",", localAuthList)));
        }
        if (chargePointId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "chargePointId must be set to the charger's OCPP identity");
            return;
        }
        OcppServerBridgeHandler serverHandler = serverHandler();
        if (serverHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        this.server = serverHandler;
        // Registration can take this charge point ONLINE synchronously; a status set after would clobber it.
        updateStatus(ThingStatus.UNKNOWN);
        serverHandler.registerChargePoint(chargePointId, this);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Deliberately not super: must re-register with a re-initialized bridge, whose charge-point map is empty.
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            OcppServerBridgeHandler serverHandler = serverHandler();
            if (serverHandler != null && !chargePointId.isBlank()) {
                this.server = serverHandler;
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.UNKNOWN);
                }
                serverHandler.registerChargePoint(chargePointId, this);
            }
        } else {
            cancelScheduledWork();
            synchronized (stateLock) {
                session = null;
                operational = false;
            }
            failPendingSends();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        cancelScheduledWork();
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler != null) {
            serverHandler.unregisterChargePoint(chargePointId);
        }
        server = null;
        synchronized (stateLock) {
            session = null;
            operational = false;
        }
        failPendingSends();
        connectors.clear();
        transactions.clear();
    }

    private void cancelScheduledWork() {
        cancel(bootConfigTask);
        cancel(livenessTask);
        cancel(statusFallbackTask);
        cancel(readyTask);
        cancel(learnTask);
        bootConfigTask = null;
        livenessTask = null;
        statusFallbackTask = null;
        readyTask = null;
        learnTask = null;
        learningCard = false;
    }

    private @Nullable OcppServerBridgeHandler serverHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        return handler instanceof OcppServerBridgeHandler serverBridgeHandler ? serverBridgeHandler : null;
    }

    public void registerConnector(int connectorId, OcppConnectorHandler handler) {
        connectors.put(connectorId, handler);
    }

    public void unregisterConnector(int connectorId) {
        connectors.remove(connectorId);
    }

    public CompletionStage<Confirmation> send(Request request) {
        UUID localSession = session;
        if (localSession == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        CompletableFuture<Confirmation> future = new CompletableFuture<>();
        PendingSend pending = new PendingSend(localSession, request, future);
        if (!operational) {
            if (pendingSends.size() >= PENDING_SEND_LIMIT) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("Charger " + chargePointId + " not ready and its queue is full"));
            }
            pendingSends.add(pending);
            // Re-check after enqueuing: a concurrent disconnect or readiness flip may have missed this entry.
            if (session == null) {
                if (pendingSends.remove(pending)) {
                    future.completeExceptionally(
                            new IllegalStateException("Charger " + chargePointId + " disconnected"));
                }
            } else if (operational && pendingSends.remove(pending)) {
                enqueue(pending);
            }
            return future;
        }
        enqueue(pending);
        return future;
    }

    CompletionStage<Confirmation> sendNow(Request request) {
        UUID localSession = session;
        if (localSession == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        CompletableFuture<Confirmation> future = new CompletableFuture<>();
        enqueue(new PendingSend(localSession, request, future));
        return future;
    }

    private void enqueue(PendingSend pending) {
        boolean full = false;
        boolean startDrain = false;
        long epoch = 0;
        synchronized (dispatchLock) {
            if (outbound.size() >= OUTBOUND_LIMIT) {
                full = true;
            } else {
                outbound.add(pending);
                if (!dispatching) {
                    dispatching = true;
                    epoch = dispatchEpoch;
                    startDrain = true;
                }
            }
        }
        if (full) {
            pending.future().completeExceptionally(
                    new IllegalStateException("Charger " + chargePointId + " outbound queue is full"));
            return;
        }
        if (startDrain) {
            drainOutbound(epoch);
        }
    }

    private void drainOutbound(long epoch) {
        while (true) {
            PendingSend next;
            boolean superseded;
            synchronized (dispatchLock) {
                // A superseded epoch means a session change owns the dispatcher now; stop without clearing dispatching.
                if (epoch != dispatchEpoch) {
                    return;
                }
                next = outbound.poll();
                if (next == null) {
                    dispatching = false;
                    return;
                }
                UUID localSession = session;
                superseded = localSession == null || !next.session().equals(localSession);
                if (!superseded) {
                    inFlight = next;
                }
            }
            if (superseded) {
                next.future().completeExceptionally(
                        new IllegalStateException("Charger " + chargePointId + " reconnected; request superseded"));
                continue;
            }
            PendingSend current = next;
            transmit(current.session(), current.request()).whenComplete((confirmation, ex) -> {
                boolean live;
                synchronized (dispatchLock) {
                    live = epoch == dispatchEpoch;
                    if (live) {
                        inFlight = null;
                    }
                }
                if (!live) {
                    return;
                }
                if (ex != null) {
                    current.future().completeExceptionally(ex);
                } else {
                    current.future().complete(confirmation);
                }
                drainOutbound(epoch);
            });
            return;
        }
    }

    private CompletionStage<Confirmation> transmit(UUID localSession, Request request) {
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        if (transport == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        return transport.send(localSession, request);
    }

    private void becomeReady(UUID expectedSession) {
        synchronized (stateLock) {
            if (!expectedSession.equals(session)) {
                return;
            }
            operational = true;
        }
        PendingSend pending;
        while (expectedSession.equals(session) && (pending = pendingSends.poll()) != null) {
            enqueue(pending);
        }
        if (expectedSession.equals(session)) {
            connectors.values().forEach(OcppConnectorHandler::onChargePointReady);
        }
    }

    private void failPendingSends() {
        List<PendingSend> toFail = new ArrayList<>();
        PendingSend pending;
        while ((pending = pendingSends.poll()) != null) {
            toFail.add(pending);
        }
        synchronized (dispatchLock) {
            // Bump the epoch to kill the active drain chain, then abandon the in-flight request the
            // library never completes on session close — else the successor stalls till its timeout reaper.
            dispatchEpoch++;
            PendingSend current = inFlight;
            if (current != null) {
                toFail.add(current);
                inFlight = null;
            }
            PendingSend queued;
            while ((queued = outbound.poll()) != null) {
                toFail.add(queued);
            }
            dispatching = false;
        }
        // Complete outside the lock: dependent stages run synchronously and can re-enter the dispatcher.
        for (PendingSend p : toFail) {
            p.future().completeExceptionally(new IllegalStateException("Charger " + chargePointId + " disconnected"));
        }
    }

    public boolean isReady() {
        return session != null && operational;
    }

    public void onConnected(UUID session) {
        // Publish the new session and clear readiness together, before abandoning the old session's work.
        synchronized (stateLock) {
            bootAccepted = false;
            operational = false;
            this.session = session;
        }
        failPendingSends();
        cancel(readyTask);
        readyTask = null;
        logger.debug("Charge point {} online on session {}", chargePointId, session);
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_CONNECTED, OnOffType.ON);
        recordActivity();
        // Nothing is sent here: OCPP 1.6 forbids any request before the BootNotification is accepted.
        // This fallback only covers a charger that reopens its socket without booting again.
        cancel(statusFallbackTask);
        UUID connectedSession = session;
        statusFallbackTask = scheduler.schedule(() -> {
            if (!bootAccepted) {
                readCapabilitiesNow(connectedSession);
                requestConnectorStatusesNow();
            }
        }, STATUS_FALLBACK_SECONDS, TimeUnit.SECONDS);
    }

    private void requestConnectorStatuses() {
        for (OcppConnectorHandler connector : connectors.values()) {
            connector.requestStatus();
        }
        triggerUndiscoveredConnectors(false);
    }

    private void requestConnectorStatusesNow() {
        for (OcppConnectorHandler connector : connectors.values()) {
            connector.requestStatusNow();
        }
        triggerUndiscoveredConnectors(true);
    }

    private void triggerUndiscoveredConnectors(boolean bypassReadiness) {
        int count = capabilities.numberOfConnectors().orElse(0);
        for (int id = 1; id <= count; id++) {
            if (connectors.containsKey(id)) {
                continue;
            }
            int connectorId = id;
            TriggerMessageRequest request = new TriggerMessageRequest(TriggerMessageRequestType.StatusNotification);
            request.setConnectorId(connectorId);
            CompletionStage<Confirmation> result = bypassReadiness ? sendNow(request) : send(request);
            result.whenComplete((confirmation, ex) -> {
                if (ex != null) {
                    logger.debug("TriggerMessage[StatusNotification] for undiscovered connector {} on {} failed: {}",
                            connectorId, chargePointId, ex.toString());
                }
            });
        }
    }

    public void onDisconnected(UUID closedSession) {
        synchronized (stateLock) {
            if (!closedSession.equals(session)) {
                return; // a stale session closing after a reconnect — the charger is still live
            }
            session = null;
            operational = false;
        }
        cancelScheduledWork();
        failPendingSends();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Charger disconnected");
        updateState(CHANNEL_CONNECTED, OnOffType.OFF);
    }

    public void onBootNotification(BootNotificationRequest request) {
        bootAccepted = true;
        cancel(statusFallbackTask);
        statusFallbackTask = null;
        setProperty(Thing.PROPERTY_VENDOR, request.getChargePointVendor());
        setProperty(Thing.PROPERTY_MODEL_ID, request.getChargePointModel());
        setProperty(Thing.PROPERTY_FIRMWARE_VERSION, request.getFirmwareVersion());
        setProperty(Thing.PROPERTY_SERIAL_NUMBER, request.getChargePointSerialNumber());
        // Deliberately not touch(): readiness must not flip before the boot confirmation is sent.
        recordActivity();
        UUID bootSession = session;
        if (bootSession == null) {
            return;
        }
        cancel(readyTask);
        readyTask = scheduler.schedule(() -> becomeReady(bootSession), BOOT_READY_GRACE_MILLIS, TimeUnit.MILLISECONDS);
        scheduleBootConfig(bootSession);
    }

    public void onStatusNotification(StatusNotificationRequest request) {
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        if (connectorId <= 0) {
            return; // connectorId 0 is a charger-wide status; no per-connector channel for it
        }
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            connector.onStatusNotification(request);
        } else {
            OcppServerBridgeHandler serverHandler = server;
            if (serverHandler != null) {
                serverHandler.connectorDiscovered(chargePointId, connectorId);
            }
        }
    }

    public void onMeterValues(MeterValuesRequest request) {
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        if (connectorId <= 0) {
            return; // connector 0 addresses the charge point itself; nothing to route to
        }
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            connector.onMeterValues(request);
        } else {
            logger.debug("MeterValues for {} connector {} with no matching thing", chargePointId, connectorId);
        }
    }

    public void onHeartbeat() {
        touch();
    }

    public int getHeartbeatOverride() {
        return heartbeat;
    }

    public ChargerCapabilities getCapabilities() {
        return capabilities;
    }

    public void onStartTransaction(StartTransactionRequest request, int transactionId) {
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            transactions.values().remove(connector);
            transactions.put(transactionId, connector);
            connector.onTransactionStarted(request, transactionId);
        }
    }

    public void onStopTransaction(StopTransactionRequest request) {
        touch();
        Integer transactionId = request.getTransactionId();
        if (transactionId == null) {
            return;
        }
        OcppConnectorHandler connector = transactions.remove(transactionId);
        OcppServerBridgeHandler serverHandler = server;
        boolean ownsTransaction = connector != null;
        if (connector == null && serverHandler != null) {
            // Not in memory (restart mid-transaction): recover from persistence, scoped to this charge point.
            Integer connectorId = serverHandler.transactionConnector(transactionId, chargePointId);
            if (connectorId != null) {
                ownsTransaction = true;
                connector = connectors.get(connectorId);
            }
        }
        if (connector != null) {
            connector.onTransactionStopped(request);
        }
        if (ownsTransaction && serverHandler != null) {
            serverHandler.forgetTransaction(transactionId);
        }
    }

    public @Nullable Integer recoverTransactionId(int connectorId) {
        OcppServerBridgeHandler serverHandler = server;
        return serverHandler != null ? serverHandler.openTransactionFor(chargePointId, connectorId) : null;
    }

    public void transactionCompleted(int transactionId) {
        transactions.remove(transactionId);
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler != null) {
            serverHandler.forgetTransaction(transactionId);
        }
    }

    private void scheduleBootConfig(UUID bootSession) {
        cancel(bootConfigTask);
        bootConfigTask = scheduler.schedule(() -> runBootConfig(bootSession), Math.max(0, configSettleSeconds),
                TimeUnit.SECONDS);
    }

    private void runBootConfig(UUID bootSession) {
        if (!bootSession.equals(session)) {
            logger.debug("Boot config for {} skipped — its session was replaced during the settle delay",
                    chargePointId);
            return;
        }
        readCapabilities(bootSession);
    }

    private void readCapabilities(UUID bootSession) {
        send(new GetConfigurationRequest()).whenComplete((confirmation, ex) -> {
            if (!bootSession.equals(session)) {
                return;
            }
            applyCapabilities(confirmation, ex);
            runBootConfigBurst(bootSession);
        });
    }

    private void readCapabilitiesNow(UUID connectedSession) {
        sendNow(new GetConfigurationRequest()).whenComplete((confirmation, ex) -> {
            if (!connectedSession.equals(session)) {
                return;
            }
            applyCapabilities(confirmation, ex);
        });
    }

    private void applyCapabilities(@Nullable Confirmation confirmation, @Nullable Throwable ex) {
        if (ex != null) {
            logger.debug("GetConfiguration for {} failed ({}); continuing with defaults", chargePointId,
                    ex.getMessage());
            capabilities = ChargerCapabilities.unknown();
            return;
        }
        capabilities = confirmation instanceof GetConfigurationConfirmation gc ? ChargerCapabilities.from(gc)
                : ChargerCapabilities.unknown();
        publishCapabilities(capabilities);
    }

    private void publishCapabilities(ChargerCapabilities caps) {
        if (caps.isEmpty()) {
            logger.debug("Charge point {} reported no configuration", chargePointId);
            return;
        }
        logger.info("Charge point {} capabilities: {}", chargePointId, caps.summary());
        if (logger.isDebugEnabled()) {
            caps.raw().forEach((key, value) -> logger.debug("  {} {} = {}", chargePointId, key, value));
        }
        caps.featureProfiles()
                .ifPresent(profiles -> updateProperty("ocppSupportedFeatureProfiles", String.join(", ", profiles)));
        caps.allowedChargingRateUnits()
                .ifPresent(units -> updateProperty("ocppChargingRateUnit", String.join(", ", units)));
        caps.heartbeatIntervalSeconds().ifPresent(seconds -> updateProperty("ocppHeartbeatInterval", seconds + " s"));
    }

    private void runBootConfigBurst(UUID bootSession) {
        if (!bootSession.equals(session)) {
            logger.debug("Boot config for {} skipped — its session was replaced", chargePointId);
            return;
        }
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler == null) {
            return;
        }
        OcppServerConfiguration config = serverHandler.getServerConfig();
        String fingerprint = configFingerprint(config);
        if (!fingerprint.equals(attemptedConfigFingerprint)) {
            attemptedConfigFingerprint = fingerprint;
            bootConfigAttempts.set(0);
            acceptedMeasurands.clear();
        }
        if (fingerprint.equals(appliedConfigFingerprint)) {
            logger.debug("Boot config for {} already applied; skipping", chargePointId);
            requestConnectorStatuses();
            return;
        }
        if (bootConfigAttempts.incrementAndGet() > MAX_BOOT_CONFIG_ATTEMPTS) {
            logger.debug("Boot config for {} not attempted again after {} failed tries", chargePointId,
                    MAX_BOOT_CONFIG_ATTEMPTS);
            requestConnectorStatuses();
            return;
        }
        List<BootConfigStep> steps = new ArrayList<>();
        if (meterless) {
            steps.add(() -> sendConfig("ClockAlignedDataInterval", "0"));
        } else {
            if (config.meterValueSampleInterval >= 0) {
                steps.add(() -> sendConfig("MeterValueSampleInterval",
                        Integer.toString(config.meterValueSampleInterval)));
            }
            if (!config.meterValuesData.isBlank()) {
                steps.add(() -> negotiateMeasurand("MeterValuesSampledData",
                        startingMeasurands(config, "MeterValuesSampledData")));
                steps.add(() -> negotiateMeasurand("MeterValuesAlignedData",
                        startingMeasurands(config, "MeterValuesAlignedData")));
            }
            if (config.clockAlignedDataInterval >= 0) {
                steps.add(() -> sendConfig("ClockAlignedDataInterval",
                        Integer.toString(config.clockAlignedDataInterval)));
            }
        }
        if (config.disableRemoteTxAuthorization) {
            steps.add(() -> sendConfig("AuthorizeRemoteTxRequests", "false"));
        }
        for (String pair : config.extraConfig) {
            int equals = pair.indexOf('=');
            if (equals > 0) {
                String key = pair.substring(0, equals).trim();
                String value = pair.substring(equals + 1).trim();
                steps.add(() -> sendConfig(key, value));
            }
        }
        if (!localAuthList.isEmpty() && Boolean.TRUE.equals(capabilities.supportsLocalAuthList().orElse(false))) {
            List<String> tags = localAuthList;
            steps.add(() -> provisionLocalAuthList(tags));
        }
        runBootConfigStep(steps, 0, fingerprint, bootSession, new AtomicBoolean(true));
    }

    private String configFingerprint(OcppServerConfiguration config) {
        return chargePointId + "|" + meterless + "|" + config.meterValueSampleInterval + "|"
                + config.clockAlignedDataInterval + "|" + config.meterValuesData + "|"
                + config.disableRemoteTxAuthorization + "|" + String.join(",", config.extraConfig);
    }

    private CompletableFuture<Confirmation> provisionLocalAuthList(List<String> tags) {
        int version = localAuthListVersion(tags);
        return send(new GetLocalListVersionRequest()).thenCompose(current -> {
            if (current instanceof GetLocalListVersionConfirmation reported
                    && Integer.valueOf(version).equals(reported.getListVersion())) {
                return CompletableFuture.completedFuture(current);
            }
            SendLocalListRequest request = new SendLocalListRequest(version, UpdateType.Full);
            request.setLocalAuthorizationList(authorizationData(tags));
            return send(request).thenApply(result -> {
                logger.info("Local authorization list sent to {}: version {}, {} tag(s), {}", chargePointId, version,
                        tags.size(), result);
                return result;
            });
        }).toCompletableFuture();
    }

    private static int localAuthListVersion(List<String> tags) {
        return tags.stream().sorted().toList().hashCode() & Integer.MAX_VALUE;
    }

    private static AuthorizationData[] authorizationData(List<String> tags) {
        return tags.stream().map(tag -> {
            AuthorizationData data = new AuthorizationData(tag);
            data.setIdTagInfo(new IdTagInfo(AuthorizationStatus.Accepted));
            return data;
        }).toArray(AuthorizationData[]::new);
    }

    private static List<String> parseTagList(String csv) {
        return java.util.stream.Stream.of(csv.split(",")).map(String::trim).filter(tag -> !tag.isEmpty()).toList();
    }

    private String startingMeasurands(OcppServerConfiguration config, String key) {
        return acceptedMeasurands.getOrDefault(key, config.meterValuesData);
    }

    private void runBootConfigStep(List<BootConfigStep> steps, int index, String fingerprint, UUID bootSession,
            AtomicBoolean allSucceeded) {
        if (!bootSession.equals(session)) {
            logger.debug("Boot config sequence for {} abandoned — its session was replaced", chargePointId);
            return;
        }
        if (index >= steps.size()) {
            if (allSucceeded.get()) {
                // Latch on success only: a failed burst retries on the next boot.
                appliedConfigFingerprint = fingerprint;
                if (!steps.isEmpty()) {
                    logger.debug("Boot config for {} complete ({} steps)", chargePointId, steps.size());
                }
            } else if (bootConfigAttempts.get() < MAX_BOOT_CONFIG_ATTEMPTS) {
                logger.warn("Boot config for {} did not fully land; will retry on its next boot", chargePointId);
            } else {
                logger.warn(
                        "Boot config for {} did not fully land after {} attempts; giving up until it is "
                                + "reconfigured or reconnects with different settings",
                        chargePointId, MAX_BOOT_CONFIG_ATTEMPTS);
            }
            requestConnectorStatuses();
            return;
        }
        steps.get(index).send().handle((confirmation, ex) -> {
            if (ex != null) {
                allSucceeded.set(false);
                logger.warn("Boot config step {}/{} for {} failed: {}", index + 1, steps.size(), chargePointId,
                        ex.getMessage());
            } else if (!isConfigApplied(confirmation)) {
                allSucceeded.set(false);
                logger.warn("Boot config step {}/{} for {} not applied: {}", index + 1, steps.size(), chargePointId,
                        configStatusOf(confirmation));
            }
            return null;
        }).thenRun(() -> runBootConfigStep(steps, index + 1, fingerprint, bootSession, allSucceeded));
    }

    private boolean isConfigApplied(@Nullable Confirmation confirmation) {
        if (confirmation instanceof ChangeConfigurationConfirmation change) {
            ConfigurationStatus status = change.getStatus();
            if (status == ConfigurationStatus.RebootRequired) {
                logger.warn("Boot config for {} accepted but needs a charger reboot to take effect", chargePointId);
            }
            return status == ConfigurationStatus.Accepted || status == ConfigurationStatus.RebootRequired;
        }
        return true;
    }

    private static String configStatusOf(@Nullable Confirmation confirmation) {
        return confirmation instanceof ChangeConfigurationConfirmation change ? String.valueOf(change.getStatus())
                : String.valueOf(confirmation);
    }

    private CompletableFuture<Confirmation> sendConfig(String key, String value) {
        return send(new ChangeConfigurationRequest(key, value)).toCompletableFuture();
    }

    private CompletableFuture<Confirmation> negotiateMeasurand(String key, String value) {
        CompletableFuture<Confirmation> result = new CompletableFuture<>();
        attemptMeasurand(key, value, result);
        return result;
    }

    private void attemptMeasurand(String key, String value, CompletableFuture<Confirmation> result) {
        send(new ChangeConfigurationRequest(key, value)).whenComplete((confirmation, ex) -> {
            if (ex != null) {
                result.completeExceptionally(ex);
                return;
            }
            if (confirmation instanceof ChangeConfigurationConfirmation change) {
                if (change.getStatus() == ConfigurationStatus.Rejected) {
                    String shorter = Measurands.dropLast(value);
                    if (!shorter.isEmpty() && !shorter.equals(value)) {
                        logger.debug("Charger {} rejected {}={}, retrying with {}", chargePointId, key, value, shorter);
                        attemptMeasurand(key, shorter, result);
                        return;
                    }
                } else if (change.getStatus() == ConfigurationStatus.Accepted) {
                    acceptedMeasurands.put(key, value);
                }
            }
            result.complete(confirmation);
        });
    }

    private void touch() {
        if (!operational) {
            UUID currentSession = session;
            if (currentSession != null) {
                scheduler.execute(() -> becomeReady(currentSession));
            }
        }
        recordActivity();
    }

    private void recordActivity() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(CHANNEL_LAST_SEEN, new DateTimeType());
        rearmLiveness();
    }

    private void rearmLiveness() {
        cancel(livenessTask);
        livenessTask = scheduler.schedule(this::onLivenessTimeout, livenessThresholdSeconds(), TimeUnit.SECONDS);
    }

    private long livenessThresholdSeconds() {
        OcppServerBridgeHandler serverHandler = server;
        int serverDefault = serverHandler != null ? serverHandler.getServerConfig().heartbeatInterval : 300;
        return livenessThreshold(heartbeat, capabilities.heartbeatIntervalSeconds(), serverDefault);
    }

    static long livenessThreshold(int heartbeatOverride, OptionalInt reportedHeartbeat, int serverDefault) {
        int effective = heartbeatOverride;
        if (effective <= 0) {
            effective = reportedHeartbeat.orElse(0);
        }
        if (effective <= 0) {
            effective = serverDefault;
        }
        if (effective <= 0) {
            effective = 300;
        }
        return Math.max(LIVENESS_FLOOR_SECONDS, 2L * effective + 60L);
    }

    private void onLivenessTimeout() {
        UUID localSession = session;
        if (localSession == null) {
            return;
        }
        logger.debug("Charge point {} silent beyond {}s; forcing a reconnect", chargePointId,
                livenessThresholdSeconds());
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        synchronized (stateLock) {
            if (!localSession.equals(session)) {
                return; // already reconnected on a newer session — leave it live
            }
            session = null;
            operational = false;
        }
        failPendingSends();
        if (transport != null) {
            transport.closeSession(localSession);
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "No messages received (liveness timeout)");
        updateState(CHANNEL_CONNECTED, OnOffType.OFF);
    }

    private static void cancel(@Nullable ScheduledFuture<?> task) {
        if (task != null) {
            task.cancel(false);
        }
    }

    private void setProperty(String name, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            updateProperty(name, value);
        }
    }
}
