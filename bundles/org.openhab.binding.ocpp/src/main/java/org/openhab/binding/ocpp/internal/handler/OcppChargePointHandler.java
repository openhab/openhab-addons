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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ocpp.internal.config.OcppChargePointConfiguration;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.transport.Measurands;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
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
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.ResetRequest;
import eu.chargetime.ocpp.model.core.ResetType;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * The {@link OcppChargePointHandler} represents one physical charger. It registers with the server
 * bridge, tracks its live session, publishes BootNotification data, routes StatusNotification /
 * MeterValues / transaction events to the matching connector, and is the outbound path connectors
 * use to send requests. It also runs the boot-time configuration burst, a heartbeat-derived liveness
 * watchdog, and reconnect self-heal.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppChargePointHandler extends BaseBridgeHandler {

    /**
     * One deferred boot-configuration request. A dedicated interface (rather than
     * {@link java.util.function.Supplier}) so the null annotations carry through.
     */
    @FunctionalInterface
    private interface BootConfigStep {
        CompletableFuture<Confirmation> send();
    }

    private static final long LIVENESS_FLOOR_SECONDS = 180;
    // Only used when a charger reopens its socket without booting again; the normal path requests
    // status after the BootNotification has been accepted and the boot configuration has run.
    private static final long STATUS_FALLBACK_SECONDS = 25;
    private static final int MAX_BOOT_CONFIG_ATTEMPTS = 3;

    private final Logger logger = LoggerFactory.getLogger(OcppChargePointHandler.class);
    private final Map<Integer, OcppConnectorHandler> connectors = new ConcurrentHashMap<>();
    private final Map<Integer, OcppConnectorHandler> transactions = new ConcurrentHashMap<>();

    private String chargePointId = "";
    private int configSettleSeconds;
    private boolean meterless;
    private int heartbeat;

    private @Nullable OcppServerBridgeHandler server;
    private @Nullable UUID session;
    private volatile @Nullable String acceptedMeasurands;
    private @Nullable ScheduledFuture<?> bootConfigTask;
    private @Nullable ScheduledFuture<?> livenessTask;
    private @Nullable ScheduledFuture<?> statusFallbackTask;
    private volatile boolean bootAccepted;
    private volatile boolean bootConfigApplied;
    private final AtomicInteger bootConfigAttempts = new AtomicInteger();

    public OcppChargePointHandler(Bridge bridge) {
        super(bridge);
    }

    public String getChargePointId() {
        return chargePointId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Reset addresses the whole charge point (ResetRequest carries no connector id), so it lives
        // here rather than on each connector.
        if (CHANNEL_RESET.equals(channelUID.getId()) && command == OnOffType.ON) {
            send(new ResetRequest(ResetType.Soft)).whenComplete((confirmation, ex) -> {
                if (ex != null) {
                    logger.warn("Reset of {} failed: {}", chargePointId, ex.getMessage());
                }
            });
            // Momentary: pop the switch back so it does not stick ON.
            updateState(CHANNEL_RESET, OnOffType.OFF);
        }
    }

    @Override
    public void initialize() {
        OcppChargePointConfiguration config = getConfigAs(OcppChargePointConfiguration.class);
        chargePointId = config.chargePointId;
        configSettleSeconds = config.configSettleSeconds;
        meterless = config.meterless;
        heartbeat = config.heartbeat;
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
        // UNKNOWN before registering: registration can find an already-open session and take this
        // charge point ONLINE synchronously, and a status set afterwards would overwrite that.
        updateStatus(ThingStatus.UNKNOWN);
        serverHandler.registerChargePoint(chargePointId, this);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Not calling super: besides the status flip, this charge point must re-register with the
        // server (a bridge that was disposed and re-initialized comes back with an empty charge-point
        // map, and children are not re-initialized for that) and drop its scheduled work when offline.
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
            cancel(bootConfigTask);
            cancel(livenessTask);
            cancel(statusFallbackTask);
            bootConfigTask = null;
            livenessTask = null;
            statusFallbackTask = null;
            session = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        cancel(bootConfigTask);
        cancel(livenessTask);
        cancel(statusFallbackTask);
        bootConfigTask = null;
        livenessTask = null;
        statusFallbackTask = null;
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler != null) {
            serverHandler.unregisterChargePoint(chargePointId);
        }
        server = null;
        session = null;
        connectors.clear();
        transactions.clear();
    }

    private @Nullable OcppServerBridgeHandler serverHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        return handler instanceof OcppServerBridgeHandler serverBridgeHandler ? serverBridgeHandler : null;
    }

    // --- connector registration (called by OcppConnectorHandler) ---

    public void registerConnector(int connectorId, OcppConnectorHandler handler) {
        connectors.put(connectorId, handler);
    }

    public void unregisterConnector(int connectorId) {
        connectors.remove(connectorId);
    }

    /**
     * Send a request to this charger's live session. The returned stage completes exceptionally if
     * the charger is offline.
     */
    public CompletionStage<Confirmation> send(Request request) {
        UUID localSession = session;
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        if (localSession == null || transport == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        return transport.send(localSession, request);
    }

    // --- routed from the server bridge ---

    public void onConnected(UUID session) {
        this.session = session;
        bootAccepted = false;
        logger.debug("Charge point {} online on session {}", chargePointId, session);
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_CONNECTED, OnOffType.ON);
        touch();
        // OCPP 1.6 forbids the central system from sending ANY request before it has accepted the
        // charge point's BootNotification — a request that arrives first can leave the charger
        // waiting for a boot response it then never processes. So nothing is sent here: status is
        // requested once boot configuration has run. This fallback only covers a charger that
        // reopens its socket without booting again.
        cancel(statusFallbackTask);
        statusFallbackTask = scheduler.schedule(() -> {
            if (!bootAccepted) {
                requestConnectorStatuses();
            }
        }, STATUS_FALLBACK_SECONDS, TimeUnit.SECONDS);
    }

    private void requestConnectorStatuses() {
        for (OcppConnectorHandler connector : connectors.values()) {
            connector.requestStatus();
        }
    }

    public void onDisconnected(UUID closedSession) {
        if (!closedSession.equals(session)) {
            return; // a stale session closing after a reconnect — the charger is still live
        }
        session = null;
        cancel(livenessTask);
        livenessTask = null;
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
        touch();
        scheduleBootConfig();
    }

    public void onStatusNotification(StatusNotificationRequest request) {
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        if (connectorId <= 0) {
            return; // connectorId 0 is a charger-wide status; no per-connector channel for it yet
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
            // Connector 0 addresses the charge point itself rather than an outlet, so these samples
            // duplicate what the connectors report and there is nothing to route them to.
            return;
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

    public void onStartTransaction(StartTransactionRequest request, int transactionId) {
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            // A connector has at most one transaction at a time, so drop any earlier one it still
            // maps to: a StopTransaction that never arrived would otherwise leave the entry behind
            // for good. This keeps the map bounded by the number of connectors.
            transactions.values().remove(connector);
            transactions.put(transactionId, connector);
            connector.onTransactionStarted(request, transactionId);
        }
    }

    public void onStopTransaction(StopTransactionRequest request) {
        Integer transactionId = request.getTransactionId();
        if (transactionId == null) {
            return;
        }
        OcppConnectorHandler connector = transactions.remove(transactionId);
        if (connector != null) {
            connector.onTransactionStopped(request);
        }
    }

    // --- boot-time configuration burst ---

    private void scheduleBootConfig() {
        cancel(bootConfigTask);
        bootConfigTask = scheduler.schedule(this::runBootConfig, Math.max(0, configSettleSeconds), TimeUnit.SECONDS);
    }

    private void runBootConfig() {
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler == null) {
            return;
        }
        // Do NOT push configuration on every reconnect. A charger that is busy (e.g. flushing an
        // offline message queue) can leave a ChangeConfiguration unanswered, and an unanswered call
        // times out and tears down the whole session — turning one reconnect into a permanent
        // connect/configure/drop loop. So: skip once it has been accepted, and cap the attempts so a
        // charger that never answers is left alone rather than cycled forever.
        if (bootConfigApplied) {
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
        OcppServerConfiguration config = serverHandler.getServerConfig();
        // Each step is deferred: they are dispatched ONE AT A TIME (see runBootConfigStep). A charger
        // that receives the whole burst at once may leave the queued calls unanswered until they time
        // out, which tears down the session — observed as a permanent connect/boot/drop loop.
        List<BootConfigStep> steps = new ArrayList<>();
        if (meterless) {
            // No internal meter: only disable the periodic clock-aligned emission that would
            // otherwise keep sending empty samples from a previously configured interval.
            steps.add(() -> sendConfig("ClockAlignedDataInterval", "0"));
        } else {
            if (config.meterValueSampleInterval >= 0) {
                steps.add(() -> sendConfig("MeterValueSampleInterval",
                        Integer.toString(config.meterValueSampleInterval)));
            }
            if (!config.meterValuesData.isBlank()) {
                steps.add(() -> negotiateMeasurand("MeterValuesSampledData", startingMeasurands(config)));
                steps.add(() -> negotiateMeasurand("MeterValuesAlignedData", startingMeasurands(config)));
            }
            if (config.clockAlignedDataInterval >= 0) {
                steps.add(() -> sendConfig("ClockAlignedDataInterval",
                        Integer.toString(config.clockAlignedDataInterval)));
            }
        }
        if (config.disableRemoteTxAuthorization) {
            steps.add(() -> sendConfig("AuthorizeRemoteTxRequests", "false"));
        }
        for (String pair : config.vendorConfig) {
            int equals = pair.indexOf('=');
            if (equals > 0) {
                String key = pair.substring(0, equals).trim();
                String value = pair.substring(equals + 1).trim();
                steps.add(() -> sendConfig(key, value));
            }
        }
        runBootConfigStep(steps, 0, new AtomicBoolean(true));
    }

    private String startingMeasurands(OcppServerConfiguration config) {
        String cached = acceptedMeasurands;
        return cached != null ? cached : config.meterValuesData;
    }

    /**
     * Dispatch the boot configuration one request at a time, each waiting for the previous to settle.
     * A failed step is logged and does not abort the rest.
     */
    private void runBootConfigStep(List<BootConfigStep> steps, int index, AtomicBoolean allSucceeded) {
        if (index >= steps.size()) {
            if (allSucceeded.get()) {
                // Latch on SUCCESS only: a burst that failed is retried on the charger's next boot,
                // while a successful one is never repeated (see runBootConfig).
                bootConfigApplied = true;
                if (!steps.isEmpty()) {
                    logger.debug("Boot config for {} complete ({} steps)", chargePointId, steps.size());
                }
            } else {
                logger.warn("Boot config for {} did not fully land; will retry on its next boot", chargePointId);
            }
            // Safe to ask now: the charger is booted and its configuration has settled.
            requestConnectorStatuses();
            return;
        }
        steps.get(index).send().handle((confirmation, ex) -> {
            if (ex != null) {
                allSucceeded.set(false);
                logger.warn("Boot config step {}/{} for {} failed: {}", index + 1, steps.size(), chargePointId,
                        ex.getMessage());
            } else if (!isConfigApplied(confirmation)) {
                // A normal completion is not the same as acceptance: a ChangeConfiguration answered
                // Rejected or NotSupported has not applied, so the burst must not latch on it.
                allSucceeded.set(false);
                logger.warn("Boot config step {}/{} for {} not applied: {}", index + 1, steps.size(), chargePointId,
                        configStatusOf(confirmation));
            }
            return null;
        }).thenRun(() -> runBootConfigStep(steps, index + 1, allSucceeded));
    }

    /**
     * Whether a boot ChangeConfiguration response counts as applied. Accepted and RebootRequired both
     * stored the value (RebootRequired takes effect after a charger reboot, and re-sending will not
     * change that), so the burst may latch; Rejected and NotSupported did not, so it retries on the
     * charger's next boot.
     */
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
                    acceptedMeasurands = value;
                }
            }
            result.complete(confirmation);
        });
    }

    // --- liveness watchdog ---

    private void touch() {
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

    /**
     * Silence threshold derived from the negotiated heartbeat: an idle charger's only periodic
     * message is its heartbeat, so the window must be at least twice that plus a margin, and never
     * below a floor for chargers with a short or absent heartbeat.
     */
    private long livenessThresholdSeconds() {
        int effective = heartbeat;
        if (effective <= 0) {
            OcppServerBridgeHandler serverHandler = server;
            effective = serverHandler != null ? serverHandler.getServerConfig().heartbeatInterval : 300;
        }
        return Math.max(LIVENESS_FLOOR_SECONDS, 2L * effective + 60L);
    }

    private void onLivenessTimeout() {
        UUID localSession = session;
        if (localSession == null) {
            return;
        }
        logger.warn("Charge point {} silent beyond {}s; forcing a reconnect", chargePointId,
                livenessThresholdSeconds());
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        session = null;
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
