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
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.GetConfigurationRequest;
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

    /**
     * One request held back until the charge point is ready to receive it. Tagged with the session
     * it was created for: a request queued on one session must not be transmitted on a successor
     * session the charger reconnected with.
     */
    private record PendingSend(UUID session, Request request, CompletableFuture<Confirmation> future) {
    }

    private static final long LIVENESS_FLOOR_SECONDS = 180;
    // Only used when a charger reopens its socket without booting again; the normal path requests
    // status after the BootNotification has been accepted and the boot configuration has run.
    private static final long STATUS_FALLBACK_SECONDS = 25;
    private static final int MAX_BOOT_CONFIG_ATTEMPTS = 3;
    // Delay before treating the charger as ready. The library sends the boot confirmation right after
    // the event handler returns and this binding cannot hook that write, so readiness is flipped on a
    // scheduled task that runs comfortably after it. Usually it flips sooner and provably in order: the
    // charger's first post-boot message marks it ready via touch(), and OCPP-J's one-outstanding-call-
    // per-direction rule means it cannot have sent that before receiving the confirmation.
    private static final long BOOT_READY_GRACE_MILLIS = 1000;
    private static final int PENDING_SEND_LIMIT = 32;
    // Backstop bound on the operational (post-readiness) dispatcher queue, mirroring PENDING_SEND_LIMIT
    // on the readiness queue. Generous: per-connector poll coalescing keeps normal use far below it; it
    // only caps a pathological producer.
    private static final int OUTBOUND_LIMIT = 64;

    private final Logger logger = LoggerFactory.getLogger(OcppChargePointHandler.class);
    private final Map<Integer, OcppConnectorHandler> connectors = new ConcurrentHashMap<>();
    private final Map<Integer, OcppConnectorHandler> transactions = new ConcurrentHashMap<>();
    // Requests accepted while the charge point was connected but not yet ready (readiness gate).
    private final ConcurrentLinkedQueue<PendingSend> pendingSends = new ConcurrentLinkedQueue<>();
    // The single-CALL-at-a-time outbound dispatcher: every request for this session, once past the
    // readiness gate, waits here so only one CALL is outstanding at a time (OCPP-J). Guarded by
    // dispatchLock; the transport send itself happens outside the lock.
    private final Object dispatchLock = new Object();
    private final Deque<PendingSend> outbound = new ArrayDeque<>();
    private boolean dispatching;
    // The request currently handed to the transport (one at a time). Guarded by dispatchLock and held
    // HERE, not in either queue, so a session change can complete its future promptly and reset the
    // dispatcher: the embedded library does not complete an outstanding promise when its session
    // closes, so otherwise the future — and the whole single-CALL dispatcher behind it — would stall
    // until the request-timeout reaper fires, withholding every command to the reconnected charger.
    private @Nullable PendingSend inFlight;
    // Drain-chain generation, guarded by dispatchLock. enqueue starts a chain and captures the epoch;
    // failPendingSends BUMPS it (unconditionally) to kill the active chain. Every drainOutbound pass
    // and every transmit completion carries the epoch its chain started under and stops the instant the
    // epoch moves on — so a chain caught mid-drain when a session change reset the dispatcher can
    // neither keep draining beside the fresh chain the new session starts (two CALLs on the wire), nor
    // complete a request the reset already failed.
    private long dispatchEpoch;
    // Guards the coupled transition of (session, operational): a reconnect that swaps the session
    // must not interleave with becomeReady flipping readiness, or readiness could latch onto a
    // session already replaced. The fields stay volatile for lock-free reads on the hot inbound
    // paths; only their coupled WRITES take this lock. Its critical sections do only field writes —
    // never a transport send, a future completion, or a call that takes dispatchLock — so it cannot
    // participate in a lock cycle.
    private final Object stateLock = new Object();

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
    // Charger configuration read via GetConfiguration on each boot; drives adaptive behaviour. Volatile:
    // written on a completion thread, read on hot paths. Defaults to "unknown" = "behave as before".
    private volatile ChargerCapabilities capabilities = ChargerCapabilities.unknown();
    private volatile @Nullable ScheduledFuture<?> bootConfigTask;
    private volatile @Nullable ScheduledFuture<?> livenessTask;
    private volatile @Nullable ScheduledFuture<?> statusFallbackTask;
    private volatile @Nullable ScheduledFuture<?> readyTask;
    private volatile boolean bootAccepted;
    // The applied/attempted state is keyed on a fingerprint of everything that shapes the boot
    // configuration, not latched for the handler's lifetime: editing the configuration (a Thing
    // update disposes and re-initializes this same instance, and a server-bridge restart does not
    // recreate it at all) must send the changed values on the next boot, while an unchanged
    // configuration keeps the send-once behaviour across ordinary reconnects.
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
     * Two concerns are layered. Readiness: while the charger is connected but not yet ready (its
     * BootNotification confirmation has not gone out), the request is held and released once it is, so
     * nothing precedes the boot answer. Serialization: every request then passes through
     * {@link #enqueue}, a single per-session dispatcher keeping one CALL outstanding at a time, since
     * an OCPP-J peer need not accept a second CALL before answering the first.
     */
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
            // Re-check after enqueuing: a disconnect may have failed-and-drained the readiness queue
            // (then this entry must not sit stranded), or readiness may have just flipped (then it
            // must be released, exactly once — the removal guards against a double-release racing
            // becomeReady).
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

    /**
     * Send bypassing the readiness gate but NOT the serializer. The one caller is the status-recovery
     * probe for a charger that reopened its socket without booting again: it must be allowed to send
     * before readiness is established (that charger is already registered and has no boot pending),
     * yet it still waits behind any request already in flight.
     */
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

    // --- single-CALL-at-a-time outbound dispatcher ---

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
                    epoch = dispatchEpoch; // this pass owns the chain until the epoch moves on
                    startDrain = true;
                }
            }
        }
        if (full) {
            // Backstop against a runaway producer — e.g. polling a charger that keeps its socket open
            // but stops answering, so each request only clears on its timeout. That is coalesced at the
            // connector, but the operational queue is bounded here too: fail the newest request rather
            // than let it grow without limit.
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
                if (epoch != dispatchEpoch) {
                    // A session change (failPendingSends) killed this chain and handed the dispatcher
                    // to the fresh chain the new session starts. Stop WITHOUT touching dispatching —
                    // this is what prevents a second concurrent drain putting two CALLs on the wire.
                    return;
                }
                next = outbound.poll();
                if (next == null) {
                    dispatching = false;
                    return;
                }
                // Poll, the session check, and marking it in-flight are one atomic step, so a
                // concurrent failPendingSends always sees the request in exactly one place — still
                // queued, or in-flight — never in a gap where it is missed by the fail-drain yet
                // still gets transmitted.
                UUID localSession = session;
                superseded = localSession == null || !next.session().equals(localSession);
                if (!superseded) {
                    inFlight = next;
                }
            }
            if (superseded) {
                // The session it was queued for is gone or replaced; it must not run on another.
                next.future().completeExceptionally(
                        new IllegalStateException("Charger " + chargePointId + " reconnected; request superseded"));
                continue;
            }
            PendingSend current = next;
            transmit(current.session(), current.request()).whenComplete((confirmation, ex) -> {
                boolean live;
                synchronized (dispatchLock) {
                    // Act only if this chain still owns the dispatcher. A session change bumps the epoch
                    // and fails this request itself, so a completion arriving afterwards — the
                    // request-timeout reaper included — must neither complete the future again nor
                    // continue draining beside the new session's chain.
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
                drainOutbound(epoch); // only now is the next CALL sent — one outstanding at a time
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

    /**
     * Marks the charge point ready and releases held traffic, provided the session it was armed for
     * is still the live one. Readiness-held requests enter the dispatcher first, in order, then
     * connectors flush what they deferred (also through the dispatcher), so nothing jumps the queue.
     */
    private void becomeReady(UUID expectedSession) {
        synchronized (stateLock) {
            if (!expectedSession.equals(session)) {
                return; // the session changed (or dropped) before readiness applied
            }
            // Check and flip together: were this not atomic with onConnected's session swap, a
            // reconnect landing here could leave operational=true on the successor session before it
            // has even booted — the readiness gate this method exists to honour.
            operational = true;
        }
        // Release only while the armed session is still the live one. If it was replaced between the
        // flip and here, stop — the successor's own boot releases its traffic, and anything left
        // queued for the old session is superseded when the dispatcher reaches it.
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
            // Kill the active drain chain FIRST, unconditionally: any pass or completion still running
            // under the old epoch will see the bump and stop, so it can neither keep draining (a second
            // CALL) nor complete a request this reset is about to fail. It must be unconditional — even
            // when a completion mid-drain has already cleared the in-flight slot, its pending
            // drainOutbound continuation is still out there and has to be stopped.
            dispatchEpoch++;
            // Abandon the in-flight request too: it will never be answered on a session that is going
            // away, and the embedded library does not complete its promise when the session closes.
            // Resetting dispatching here is what lets the successor session's queue drain at once
            // instead of waiting for that request's timeout reaper — the reconnect-stall bug.
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
        // Complete outside the lock: a future's dependent stages run synchronously here and can
        // re-enter the dispatcher.
        for (PendingSend p : toFail) {
            p.future().completeExceptionally(new IllegalStateException("Charger " + chargePointId + " disconnected"));
        }
    }

    // --- routed from the server bridge ---

    /** Whether this charge point can accept outbound traffic: it has a live session and has booted. */
    public boolean isReady() {
        return session != null && operational;
    }

    public void onConnected(UUID session) {
        // Publish the new session (and clear readiness) BEFORE abandoning the previous session's work.
        // Two reasons, both coupled under stateLock so a concurrent becomeReady cannot interleave and
        // re-raise readiness on the successor: (1) clearing operational before the session is visible
        // closes the reconnect-self-heal window where isReady() would see the new session with the old
        // operational=true; (2) failPendingSends below completes the in-flight request's future
        // synchronously, and a boot-config continuation released by that must observe the new session
        // so its own session guard abandons it — otherwise it would advance the old session's sequence
        // and send its next step against a session that is already gone.
        synchronized (stateLock) {
            bootAccepted = false;
            operational = false;
            this.session = session;
        }
        // Anything still queued or in flight belongs to the previous session's context; fail it
        // deliberately (it is tagged with that session, so the dispatcher would supersede it anyway)
        // rather than let it stall this one.
        failPendingSends();
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
        UUID connectedSession = session; // captured for the fallback's session guard
        statusFallbackTask = scheduler.schedule(() -> {
            if (!bootAccepted) {
                // Bare-socket reopen with no fresh BootNotification: the charger will not become ready
                // on its own and runBootConfig never runs, so this fallback reads its configuration and
                // recovers connector status, bypassing the readiness gate.
                readCapabilitiesNow(connectedSession);
                requestConnectorStatusesNow();
            }
        }, STATUS_FALLBACK_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Gated status refresh, used after a boot (and its configuration burst): held behind the
     * BootNotification response rather than racing it. With the default configuration and no boot
     * steps, this keeps the post-boot refresh from preceding the boot acceptance.
     */
    private void requestConnectorStatuses() {
        for (OcppConnectorHandler connector : connectors.values()) {
            connector.requestStatus();
        }
    }

    /** Ungated status refresh — only the bare-reconnect fallback, where the charger will not boot. */
    private void requestConnectorStatusesNow() {
        for (OcppConnectorHandler connector : connectors.values()) {
            connector.requestStatusNow();
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
        // Drop ALL scheduled work for the closed session — not just liveness/ready but also the
        // boot-config burst and the status-recovery fallback — so nothing fires against a dead session.
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
        // Liveness only — deliberately NOT touch(): this runs inside the library's boot handler,
        // before the BootNotification confirmation has been sent, and readiness flipping here would
        // let queued or deferred requests jump ahead of that confirmation on the wire. Readiness
        // arrives either with the charger's next message (which per OCPP-J it can only send after
        // receiving the confirmation) or after a grace delay for a charger that boots then stays
        // silent.
        recordActivity();
        UUID bootSession = session;
        if (bootSession == null) {
            return; // disconnected while its own BootNotification was being handled
        }
        cancel(readyTask);
        readyTask = scheduler.schedule(() -> becomeReady(bootSession), BOOT_READY_GRACE_MILLIS, TimeUnit.MILLISECONDS);
        scheduleBootConfig(bootSession);
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

    /** The charger's configuration as last read via GetConfiguration; unknown until the first read. */
    public ChargerCapabilities getCapabilities() {
        return capabilities;
    }

    public void onStartTransaction(StartTransactionRequest request, int transactionId) {
        touch();
        int connectorId = request.getConnectorId() == null ? 0 : request.getConnectorId();
        OcppConnectorHandler connector = connectors.get(connectorId);
        if (connector != null) {
            // A connector has at most one transaction at a time, so drop any earlier one it still
            // maps to: a StopTransaction that never arrived would otherwise leave the entry behind
            // for good. This keeps the map bounded by the number of connectors. Persistence is done
            // by the server bridge at accept time (so it happens even without this handler).
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
            // Not in memory: openHAB likely restarted mid-transaction. Recover the connector from the
            // persisted mapping so the stop still reaches it — but only if the id actually belongs to
            // THIS charge point (transactionConnector already checks that), so a charger cannot clear
            // another charger's persisted transaction with a forged id.
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

    /** A connector's open transaction id recovered from persistence after a restart, or {@code null}. */
    public @Nullable Integer recoverTransactionId(int connectorId) {
        OcppServerBridgeHandler serverHandler = server;
        return serverHandler != null ? serverHandler.openTransactionFor(chargePointId, connectorId) : null;
    }

    /**
     * Forget a transaction everywhere it is represented — routing map and persistent store. Called
     * by a connector when its charger authoritatively reports the transaction gone (an Available
     * status without a StopTransaction), so a lost stop cannot leave a finished transaction behind
     * to be recovered after a restart.
     */
    public void transactionCompleted(int transactionId) {
        transactions.remove(transactionId);
        OcppServerBridgeHandler serverHandler = server;
        if (serverHandler != null) {
            serverHandler.forgetTransaction(transactionId);
        }
    }

    // --- boot-time configuration burst ---

    private void scheduleBootConfig(UUID bootSession) {
        cancel(bootConfigTask);
        bootConfigTask = scheduler.schedule(() -> runBootConfig(bootSession), Math.max(0, configSettleSeconds),
                TimeUnit.SECONDS);
    }

    private void runBootConfig(UUID bootSession) {
        // The sequence belongs to the session whose boot scheduled it (captured then, not now). If the
        // charger reconnected since — even a bare WebSocket reconnect that sends no fresh
        // BootNotification — abandon it first, so an interruption during the settle delay neither runs
        // stale configuration against the replacement session nor burns an attempt against it.
        if (!bootSession.equals(session)) {
            logger.debug("Boot config for {} skipped — its session was replaced during the settle delay",
                    chargePointId);
            return;
        }
        // Read the charger's own configuration first, so the burst (and connectors) can adapt to what
        // it actually supports. Best-effort: GetConfiguration is a mandatory Core message, but a charger
        // that fails or refuses it falls back to unknown capabilities — the prior behaviour — and the
        // burst still runs.
        readCapabilities(bootSession);
    }

    /**
     * Read the charger's configuration with a single GetConfiguration, store it, then run the
     * ChangeConfiguration burst. The read is one CALL through the ordinary serialized dispatcher, so it
     * cannot race the boot confirmation; a reconnect during it is abandoned by the session guard.
     */
    private void readCapabilities(UUID bootSession) {
        send(new GetConfigurationRequest()).whenComplete((confirmation, ex) -> {
            if (!bootSession.equals(session)) {
                return; // the charger reconnected while its configuration was being read
            }
            applyCapabilities(confirmation, ex);
            runBootConfigBurst(bootSession);
        });
    }

    /**
     * Read the charger's configuration on a bare socket reopen — a reconnect with no fresh
     * BootNotification, so {@link #runBootConfig} (and its read) never runs. Ungated like the
     * status-recovery probe beside it: the charger may not prove itself ready on its own, and one that
     * stays powered for months only ever reconnects this way, so otherwise its capabilities would never
     * be read.
     */
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

    /** Logs the discovered configuration and records the most useful values as Thing properties. */
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

    /**
     * The ChangeConfiguration burst: push the configured keys to the charger, one CALL at a time, once
     * per changed configuration. Entered after the charger's configuration has been read.
     */
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
        // Do NOT push configuration on every reconnect: a busy charger (e.g. flushing an offline
        // message queue) can leave a ChangeConfiguration unanswered, and it is pointless traffic
        // anyway. So skip while the EFFECTIVE configuration is the one already applied, and cap the
        // attempts per configuration so a charger that never answers is left alone rather than cycled
        // forever. A changed configuration resets both.
        String fingerprint = configFingerprint(config);
        if (!fingerprint.equals(attemptedConfigFingerprint)) {
            attemptedConfigFingerprint = fingerprint;
            bootConfigAttempts.set(0);
            // A changed measurand configuration must renegotiate from the configured list, not from
            // what an older configuration was negotiated down to.
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
        // Each step is deferred and dispatched ONE AT A TIME (see runBootConfigStep): sending the
        // whole burst at once can bury a charger that is busy (e.g. flushing an offline queue), and
        // an OCPP-J peer answers one call at a time regardless.
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
        runBootConfigStep(steps, 0, fingerprint, bootSession, new AtomicBoolean(true));
    }

    /**
     * Everything that shapes the boot-configuration burst, in one comparable string. The charge
     * point id is part of it: correcting the identity must not let a different charger inherit the
     * applied state of the one that actually accepted the configuration. The heartbeat settings are
     * excluded — they shape the BootNotification response, not this burst.
     */
    private String configFingerprint(OcppServerConfiguration config) {
        return chargePointId + "|" + meterless + "|" + config.meterValueSampleInterval + "|"
                + config.clockAlignedDataInterval + "|" + config.meterValuesData + "|"
                + config.disableRemoteTxAuthorization + "|" + String.join(",", config.vendorConfig);
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
    private void runBootConfigStep(List<BootConfigStep> steps, int index, String fingerprint, UUID bootSession,
            AtomicBoolean allSucceeded) {
        // The whole sequence belongs to the session whose boot started it. If the charger has
        // reconnected meanwhile, a step completing late (typically by timeout) must not advance this
        // sequence — the next send() would go out on the successor session and interleave with the
        // sequence that session's own boot started — nor latch its fingerprint as applied.
        if (!bootSession.equals(session)) {
            logger.debug("Boot config sequence for {} abandoned — its session was replaced", chargePointId);
            return;
        }
        if (index >= steps.size()) {
            if (allSucceeded.get()) {
                // Latch on SUCCESS only, keyed to the configuration that was sent: a burst that
                // failed is retried on the charger's next boot, a successful one is never repeated
                // for the same configuration (see runBootConfig).
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
                // A normal completion is not the same as acceptance: a ChangeConfiguration answered
                // Rejected or NotSupported has not applied, so the burst must not latch on it.
                allSucceeded.set(false);
                logger.warn("Boot config step {}/{} for {} not applied: {}", index + 1, steps.size(), chargePointId,
                        configStatusOf(confirmation));
            }
            return null;
        }).thenRun(() -> runBootConfigStep(steps, index + 1, fingerprint, bootSession, allSucceeded));
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
        OcppServerBridgeHandler serverHandler = server;
        int serverDefault = serverHandler != null ? serverHandler.getServerConfig().heartbeatInterval : 300;
        return livenessThreshold(heartbeat, capabilities.heartbeatIntervalSeconds(), serverDefault);
    }

    /**
     * The silence window before the watchdog reconnects an idle charger, sized from the heartbeat it
     * actually uses so its heartbeats keep it alive. Precedence: an explicit Thing {@code heartbeat}
     * override, then the reported {@code HeartbeatInterval}, then the server default; never below the
     * floor.
     */
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
        // debug, not warn: the OFFLINE reason string below is logged by the framework, and a silent
        // charger past its window is a normal runtime event, not a binding bug or misconfiguration.
        logger.debug("Charge point {} silent beyond {}s; forcing a reconnect", chargePointId,
                livenessThresholdSeconds());
        OcppServerBridgeHandler serverHandler = server;
        OcppTransport transport = serverHandler != null ? serverHandler.getTransport() : null;
        synchronized (stateLock) {
            if (!localSession.equals(session)) {
                return; // the charger already reconnected on a newer session — leave it live
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
