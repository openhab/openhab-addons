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
import java.util.concurrent.ConcurrentLinkedQueue;
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

    /** One request held back until the charge point is ready to receive it. */
    private record PendingSend(Request request, CompletableFuture<Confirmation> future) {
    }

    private static final long LIVENESS_FLOOR_SECONDS = 180;
    // Only used when a charger reopens its socket without booting again; the normal path requests
    // status after the BootNotification has been accepted and the boot configuration has run.
    private static final long STATUS_FALLBACK_SECONDS = 25;
    private static final int MAX_BOOT_CONFIG_ATTEMPTS = 3;
    // Delay between handling a BootNotification and treating the charger as ready. The library sends
    // the boot confirmation right after the event handler returns; this binding cannot hook that
    // write, so readiness is flipped on a scheduled task that runs comfortably after it. Usually the
    // flip happens even sooner and provably in order: the charger's first post-boot message (it may
    // not send one before receiving the confirmation — OCPP-J allows one outstanding call per
    // direction) marks it ready via touch().
    private static final long BOOT_READY_GRACE_MILLIS = 1000;
    private static final int PENDING_SEND_LIMIT = 32;

    private final Logger logger = LoggerFactory.getLogger(OcppChargePointHandler.class);
    private final Map<Integer, OcppConnectorHandler> connectors = new ConcurrentHashMap<>();
    private final Map<Integer, OcppConnectorHandler> transactions = new ConcurrentHashMap<>();
    // Requests accepted while the charge point was connected but not yet ready (see send()).
    private final ConcurrentLinkedQueue<PendingSend> pendingSends = new ConcurrentLinkedQueue<>();

    // Assigned in initialize() and read from library and scheduler threads; volatile so no reader
    // depends on the registration order for visibility.
    private volatile String chargePointId = "";
    private volatile int configSettleSeconds;
    private volatile boolean meterless;
    private volatile int heartbeat;

    // Written from framework, library and scheduler threads; all volatile — no lock is held while
    // reading them on the hot inbound paths.
    private volatile @Nullable OcppServerBridgeHandler server;
    private volatile @Nullable UUID session;
    // True once the charger has proven it is booted on the current session — its BootNotification
    // was answered, or (after a socket reopen without a fresh boot) it sent any application message.
    // Gates caller-initiated outbound traffic so nothing is sent before the charger can accept it.
    private volatile boolean operational;
    // Accepted measurand list per configuration key: a list negotiated down for one key must not
    // narrow the starting point of the other.
    private final Map<String, String> acceptedMeasurands = new ConcurrentHashMap<>();
    private volatile @Nullable ScheduledFuture<?> bootConfigTask;
    private volatile @Nullable ScheduledFuture<?> livenessTask;
    private volatile @Nullable ScheduledFuture<?> statusFallbackTask;
    private volatile @Nullable ScheduledFuture<?> readyTask;
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
            if (isReady()) {
                send(new ResetRequest(ResetType.Soft)).whenComplete((confirmation, ex) -> {
                    if (ex != null) {
                        logger.warn("Reset of {} failed: {}", chargePointId, ex.getMessage());
                    }
                });
            } else {
                logger.debug("Reset of {} skipped — charge point not ready", chargePointId);
            }
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
            cancelScheduledWork();
            session = null;
            operational = false;
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
        session = null;
        operational = false;
        failPendingSends();
        connectors.clear();
        transactions.clear();
    }

    private void cancelScheduledWork() {
        cancel(bootConfigTask);
        cancel(livenessTask);
        cancel(statusFallbackTask);
        cancel(readyTask);
        bootConfigTask = null;
        livenessTask = null;
        statusFallbackTask = null;
        readyTask = null;
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
     *
     * <p>
     * This is the central outbound gate: while the charger is connected but not yet ready — its
     * BootNotification confirmation has not gone out — the request is queued and transmitted once it
     * is, so no caller can violate the OCPP rule that the central system stays quiet until it has
     * answered the boot. The queue drains in order on {@link #becomeReady(UUID)}.
     */
    public CompletionStage<Confirmation> send(Request request) {
        if (session == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        if (!operational) {
            if (pendingSends.size() >= PENDING_SEND_LIMIT) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("Charger " + chargePointId + " not ready and its queue is full"));
            }
            CompletableFuture<Confirmation> future = new CompletableFuture<>();
            PendingSend pending = new PendingSend(request, future);
            pendingSends.add(pending);
            // Re-check after enqueuing: a disconnect may have just failed-and-drained the queue (then
            // this entry must not sit stranded), or readiness may have just flipped (then it must not
            // wait for a drain that already ran).
            if (session == null) {
                if (pendingSends.remove(pending)) {
                    future.completeExceptionally(
                            new IllegalStateException("Charger " + chargePointId + " disconnected"));
                }
            } else if (operational) {
                drainPendingSends();
            }
            return future;
        }
        return sendDirect(request);
    }

    /**
     * Send bypassing the readiness gate. The single legitimate caller besides the queue drain is the
     * status-recovery probe for a charger that reopened its socket without booting again: that
     * charger is already registered from its earlier boot, no BootNotification is pending on the
     * session (a charger with one outstanding could not have stayed silent past the fallback delay),
     * and gating it would leave a silent long-heartbeat charger uncontrollable for minutes.
     */
    CompletionStage<Confirmation> sendDirect(Request request) {
        UUID localSession = session;
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        if (localSession == null || transport == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("Charger " + chargePointId + " is offline"));
        }
        return transport.send(localSession, request);
    }

    /**
     * Marks the charge point ready and releases held traffic, provided the session it was armed for
     * is still the live one. Queued requests go out first, then connectors flush what they deferred.
     */
    private void becomeReady(UUID expectedSession) {
        if (!expectedSession.equals(session)) {
            return; // the session changed (or dropped) before readiness applied
        }
        operational = true;
        drainPendingSends();
        connectors.values().forEach(OcppConnectorHandler::onChargePointReady);
    }

    private void drainPendingSends() {
        PendingSend pending;
        while ((pending = pendingSends.poll()) != null) {
            PendingSend current = pending;
            sendDirect(current.request()).whenComplete((confirmation, ex) -> {
                if (ex != null) {
                    current.future().completeExceptionally(ex);
                } else {
                    current.future().complete(confirmation);
                }
            });
        }
    }

    private void failPendingSends() {
        PendingSend pending;
        while ((pending = pendingSends.poll()) != null) {
            pending.future()
                    .completeExceptionally(new IllegalStateException("Charger " + chargePointId + " disconnected"));
        }
    }

    // --- routed from the server bridge ---

    /** Whether this charge point can accept outbound traffic: it has a live session and has booted. */
    public boolean isReady() {
        return session != null && operational;
    }

    public void onConnected(UUID session) {
        this.session = session;
        bootAccepted = false;
        operational = false;
        cancel(readyTask);
        readyTask = null;
        logger.debug("Charge point {} online on session {}", chargePointId, session);
        updateStatus(ThingStatus.ONLINE);
        updateState(CHANNEL_CONNECTED, OnOffType.ON);
        // Liveness only: the charger has opened a socket but not yet spoken, so it is not ready.
        recordActivity();
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
        operational = false;
        cancel(livenessTask);
        cancel(readyTask);
        livenessTask = null;
        readyTask = null;
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
        // Liveness only — deliberately NOT touch(): this runs inside the library's boot handler,
        // before the BootNotification confirmation has been sent, and readiness flipping here would
        // let queued or deferred requests jump ahead of that confirmation on the wire. Readiness
        // arrives either with the charger's next message (which per OCPP-J it can only send after
        // receiving the confirmation) or after a grace delay for a charger that boots then stays
        // silent.
        recordActivity();
        UUID bootSession = session;
        if (bootSession != null) {
            cancel(readyTask);
            readyTask = scheduler.schedule(() -> becomeReady(bootSession), BOOT_READY_GRACE_MILLIS,
                    TimeUnit.MILLISECONDS);
        }
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
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            // A connector has at most one transaction at a time, so drop any earlier one it still
            // maps to: a StopTransaction that never arrived would otherwise leave the entry behind
            // for good. This keeps the map bounded by the number of connectors.
            transactions.values().remove(connector);
            transactions.put(transactionId, connector);
            connector.onTransactionStarted(request, transactionId);
            OcppServerBridgeHandler serverHandler = server;
            if (serverHandler != null) {
                serverHandler.rememberTransaction(transactionId, chargePointId, connectorId);
            }
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
        if (connector == null && serverHandler != null) {
            // Not in memory: openHAB likely restarted mid-transaction. Recover the connector from the
            // persisted mapping so the stop still reaches it.
            Integer connectorId = serverHandler.transactionConnector(transactionId, chargePointId);
            if (connectorId != null) {
                connector = connectors.get(connectorId);
            }
        }
        if (connector != null) {
            connector.onTransactionStopped(request);
        }
        if (serverHandler != null) {
            serverHandler.forgetTransaction(transactionId);
        }
    }

    /** A connector's open transaction id recovered from persistence after a restart, or {@code null}. */
    public @Nullable Integer recoverTransactionId(int connectorId) {
        OcppServerBridgeHandler serverHandler = server;
        return serverHandler != null ? serverHandler.openTransactionFor(chargePointId, connectorId) : null;
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

    /**
     * The measurand list to open a negotiation with — what this key's negotiation previously
     * settled on, or the configured list. Cached per key: sampled and aligned data may support
     * different measurand sets, so one key's reduction must not narrow the other's starting point.
     */
    private String startingMeasurands(OcppServerConfiguration config, String key) {
        return acceptedMeasurands.getOrDefault(key, config.meterValuesData);
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
                    acceptedMeasurands.put(key, value);
                }
            }
            result.complete(confirmation);
        });
    }

    // --- liveness watchdog ---

    /**
     * Records an application message from the charger — StatusNotification, MeterValues, Heartbeat,
     * transactions. Any such message proves the charger is booted AND that no BootNotification
     * confirmation is pending (OCPP-J permits one outstanding call per direction, so the charger
     * could not have sent this before receiving the boot answer), which makes it the exact moment
     * outbound traffic becomes safe. The flip runs on the scheduler, not this library thread, so the
     * released traffic is not transmitted from inside the message handler.
     */
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
        operational = false;
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
