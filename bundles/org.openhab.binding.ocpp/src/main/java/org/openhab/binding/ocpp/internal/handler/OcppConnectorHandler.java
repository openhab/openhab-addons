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

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ocpp.internal.config.OcppConnectorConfiguration;
import org.openhab.binding.ocpp.internal.transport.ChargingProfileBuilder;
import org.openhab.binding.ocpp.internal.transport.MeterValueMapper;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.chargetime.ocpp.model.core.AvailabilityStatus;
import eu.chargetime.ocpp.model.core.AvailabilityType;
import eu.chargetime.ocpp.model.core.ChangeAvailabilityConfirmation;
import eu.chargetime.ocpp.model.core.ChangeAvailabilityRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import eu.chargetime.ocpp.model.core.MeterValue;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest;
import eu.chargetime.ocpp.model.core.RemoteStopTransactionRequest;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import eu.chargetime.ocpp.model.core.UnlockConnectorRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType;
import eu.chargetime.ocpp.model.smartcharging.ChargingProfileStatus;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileConfirmation;

/**
 * The {@link OcppConnectorHandler} represents one connector (outlet) of a charger. It carries the
 * live status, metering and transaction channels (inbound) and the control channels (outbound):
 * charge current limit, pause, remote start/stop, availability, unlock and reset. It optionally
 * polls MeterValues via TriggerMessage and detects a connector stuck in a transient state.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppConnectorHandler extends BaseThingHandler {

    // Statuses in which a vehicle cable is physically present.
    private static final EnumSet<ChargePointStatus> CABLE_PRESENT = EnumSet.of(ChargePointStatus.Preparing,
            ChargePointStatus.Charging, ChargePointStatus.SuspendedEV, ChargePointStatus.SuspendedEVSE,
            ChargePointStatus.Finishing);
    // Statuses in which a transaction is active (charging or suspended) — the charging channel is
    // ON exactly for these, so it can never get stuck ON without a car.
    private static final EnumSet<ChargePointStatus> CHARGING_ACTIVE = EnumSet.of(ChargePointStatus.Charging,
            ChargePointStatus.SuspendedEV, ChargePointStatus.SuspendedEVSE);
    // Transient statuses that should progress; if one persists the connector is likely stuck.
    private static final EnumSet<ChargePointStatus> TRANSIENT = EnumSet.of(ChargePointStatus.Preparing,
            ChargePointStatus.Finishing);
    private static final long STUCK_STATE_SECONDS = 120;

    // Telemetry measurands created as channels on demand — only if the charger actually reports
    // them — instead of declared statically on every connector. channelId -> channel spec.
    private record DynamicChannel(String channelTypeId, String itemType, String label) {
    }

    private static final String ITEM_CURRENT = "Number:ElectricCurrent";
    private static final String ITEM_POWER = "Number:Power";
    private static final String ITEM_ENERGY = "Number:Energy";
    private static final String TYPE_ENERGY = "energy";
    private static final String TYPE_POWER_REACTIVE = "power-reactive";

    private static final Map<String, DynamicChannel> DYNAMIC_CHANNELS = Map.ofEntries(
            Map.entry(CHANNEL_CURRENT_IMPORT, new DynamicChannel("current-measure", ITEM_CURRENT, "Current Imported")),
            Map.entry(CHANNEL_CURRENT_EXPORT, new DynamicChannel("current-measure", ITEM_CURRENT, "Current Exported")),
            Map.entry(CHANNEL_VOLTAGE, new DynamicChannel("voltage-measure", "Number:ElectricPotential", "Voltage")),
            Map.entry(CHANNEL_FREQUENCY, new DynamicChannel("frequency", "Number:Frequency", "Frequency")),
            Map.entry(CHANNEL_POWER_ACTIVE_EXPORT,
                    new DynamicChannel("power-active", ITEM_POWER, "Active Power Exported")),
            Map.entry(CHANNEL_POWER_REACTIVE_IMPORT,
                    new DynamicChannel(TYPE_POWER_REACTIVE, ITEM_POWER, "Reactive Power Imported")),
            Map.entry(CHANNEL_POWER_REACTIVE_EXPORT,
                    new DynamicChannel(TYPE_POWER_REACTIVE, ITEM_POWER, "Reactive Power Exported")),
            Map.entry(CHANNEL_POWER_FACTOR, new DynamicChannel("power-factor", "Number", "Power Factor")),
            Map.entry(CHANNEL_ENERGY_ACTIVE_EXPORT,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Active Energy Exported")),
            Map.entry(CHANNEL_ENERGY_ACTIVE_IMPORT_INTERVAL,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Energy Import Interval")),
            Map.entry(CHANNEL_ENERGY_ACTIVE_EXPORT_INTERVAL,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Energy Export Interval")),
            Map.entry(CHANNEL_ENERGY_REACTIVE_IMPORT,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Reactive Energy Imported")),
            Map.entry(CHANNEL_ENERGY_REACTIVE_EXPORT,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Reactive Energy Exported")),
            Map.entry(CHANNEL_ENERGY_REACTIVE_IMPORT_INTERVAL,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Reactive Import Interval")),
            Map.entry(CHANNEL_ENERGY_REACTIVE_EXPORT_INTERVAL,
                    new DynamicChannel(TYPE_ENERGY, ITEM_ENERGY, "Reactive Export Interval")),
            Map.entry(CHANNEL_SOC, new DynamicChannel("soc", "Number:Dimensionless", "State of Charge")),
            Map.entry(CHANNEL_RPM, new DynamicChannel("rpm", "Number", "RPM")),
            Map.entry(CHANNEL_TEMPERATURE, new DynamicChannel("temperature", "Number:Temperature", "Temperature")));

    private final Logger logger = LoggerFactory.getLogger(OcppConnectorHandler.class);

    // Assigned in initialize() and read from library and scheduler threads; volatile so no reader
    // depends on the registration order for visibility.
    private volatile int connectorId = 1;
    private volatile boolean forceTxDefaultProfile;
    private volatile int profileMinIntervalMs;
    private volatile String hardwareMaxCurrentKey = "";
    private volatile String remoteStartTag = "openhab";
    private volatile int meterValuesPollSeconds;
    private volatile boolean stuckStateRecovery;

    private volatile @Nullable OcppChargePointHandler chargePoint;
    private volatile @Nullable Integer transactionId;
    private volatile double currentLimitAmps;
    private volatile boolean paused;
    // A limit/pause set while the charge point was not ready yet, to be sent once it is.
    private volatile boolean limitDeferred;

    // Guards the coalescing and watchdog state below. A dedicated lock rather than the handler
    // monitor: the base class synchronizes on the handler for things like status updates, so holding
    // its monitor while calling back into the framework risks a lock-ordering deadlock.
    private final Object lock = new Object();

    // SetChargingProfile coalescing state (guarded by lock). profileGeneration counts dispatched
    // profiles; lastPublishedGeneration tracks the newest ACCEPTED one, so an older confirmation can
    // still publish the state the charger genuinely applied (e.g. newer request rejected) while a
    // stale one can never overwrite a newer accepted result.
    private double pendingLimitAmps;
    private long lastProfileSentAt;
    private long profileGeneration;
    private long lastPublishedGeneration;
    private @Nullable ScheduledFuture<?> pendingFlush;
    private @Nullable ScheduledFuture<?> pollTask;
    private @Nullable ScheduledFuture<?> stuckTask;

    /**
     * The state one SetChargingProfile request stands for, captured when it is dispatched. The
     * confirmation publishes these captured values — never the mutable fields, which may have moved
     * on to a newer request by the time the charger answers — and only if this is still the newest
     * dispatched profile, so an out-of-order or stale confirmation cannot misreport the channels.
     */
    private record ProfileClaim(long generation, double amps, double limitAmps, boolean paused) {
    }

    public OcppConnectorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        OcppConnectorConfiguration config = getConfigAs(OcppConnectorConfiguration.class);
        connectorId = config.connectorId;
        forceTxDefaultProfile = config.forceTxDefaultProfile;
        profileMinIntervalMs = config.profileMinIntervalMs;
        hardwareMaxCurrentKey = config.hardwareMaxCurrentKey;
        remoteStartTag = config.remoteStartTag;
        meterValuesPollSeconds = config.meterValuesPollSeconds;
        stuckStateRecovery = config.stuckStateRecovery;
        OcppChargePointHandler parent = chargePointHandler();
        if (parent == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        this.chargePoint = parent;
        // Also set on file-defined things, not just discovered ones, so the framework can match a
        // later discovery result against an already-configured connector.
        updateProperty(PROPERTY_UNIQUE_ID, uniqueConnectorId(parent.getChargePointId(), connectorId));
        // UNKNOWN before registering: registration can adopt an already-open session and take the
        // connector ONLINE, and a status set afterwards would overwrite that.
        updateStatus(ThingStatus.UNKNOWN);
        parent.registerConnector(connectorId, this);
        recoverTransaction(parent);
        startPolling();
        // If the charger is already online (this connector thing was added, or re-initialized, mid
        // session) it will not volunteer a StatusNotification on its own — ask for one so the
        // connector does not sit at UNKNOWN until the next status change.
        if (parent.isReady()) {
            requestStatus();
        }
    }

    private void recoverTransaction(OcppChargePointHandler parent) {
        // If openHAB restarted while a transaction was running, the charger still holds its id.
        // Recover it so RemoteStop and a TxProfile can reference it, and the eventual StopTransaction
        // matches. The charging channel stays status-driven, so it self-corrects from the next report.
        Integer open = parent.recoverTransactionId(connectorId);
        if (open != null) {
            transactionId = open;
            updateState(CHANNEL_TRANSACTION_ID, new DecimalType(open));
            logger.debug("Recovered open transaction {} on connector {} after restart", open, connectorId);
        }
    }

    private void startPolling() {
        cancel(pollTask);
        pollTask = null;
        if (meterValuesPollSeconds > 0) {
            pollTask = scheduler.scheduleWithFixedDelay(this::pollMeterValues, meterValuesPollSeconds,
                    meterValuesPollSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Not calling super: the default only flips status. This connector also has to re-register
        // with its charge point (a bridge that was disposed and re-initialized comes back with empty
        // routing maps) and stop polling a parent that is offline.
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            OcppChargePointHandler parent = chargePointHandler();
            if (parent != null) {
                this.chargePoint = parent;
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.UNKNOWN);
                }
                parent.registerConnector(connectorId, this);
                startPolling();
            }
        } else {
            cancel(pollTask);
            pollTask = null;
            // stuckTask and pendingFlush are assigned under the lock (from library and scheduler
            // threads), so they are cancelled under it too — a lock-free cancel here could race a
            // concurrent re-arm and leak the freshly scheduled task.
            synchronized (lock) {
                cancel(stuckTask);
                cancel(pendingFlush);
                stuckTask = null;
                pendingFlush = null;
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        cancel(pollTask);
        pollTask = null;
        synchronized (lock) {
            cancel(stuckTask);
            cancel(pendingFlush);
            stuckTask = null;
            pendingFlush = null;
        }
        OcppChargePointHandler parent = chargePoint;
        if (parent != null) {
            parent.unregisterConnector(connectorId);
        }
        chargePoint = null;
    }

    private @Nullable OcppChargePointHandler chargePointHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        return handler instanceof OcppChargePointHandler chargePointHandler ? chargePointHandler : null;
    }

    // --- commands (outbound) ---

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return; // state is pushed from the charger, nothing to poll
        }
        switch (channelUID.getId()) {
            case CHANNEL_CHARGE_LIMIT:
                Double limit = toAmps(command);
                if (limit != null) {
                    currentLimitAmps = limit;
                    if (!paused) {
                        applyLimit();
                    }
                }
                break;
            case CHANNEL_PAUSE:
                if (command instanceof OnOffType onOff) {
                    paused = onOff == OnOffType.ON;
                    applyLimit();
                }
                break;
            case CHANNEL_CHARGING:
                if (command instanceof OnOffType onOff) {
                    if (onOff == OnOffType.ON) {
                        remoteStart();
                    } else {
                        remoteStop();
                    }
                }
                break;
            case CHANNEL_AVAILABILITY:
                if (command instanceof OnOffType onOff) {
                    changeAvailability(onOff == OnOffType.ON);
                }
                break;
            case CHANNEL_UNLOCK:
                if (command == OnOffType.ON) {
                    dispatchIfReady(new UnlockConnectorRequest(connectorId), "UnlockConnector");
                    // Momentary: pop the switch back so it does not stick ON.
                    updateState(CHANNEL_UNLOCK, OnOffType.OFF);
                }
                break;
            case CHANNEL_HARDWARE_MAX_CURRENT:
                changeHardwareMaxCurrent(command);
                break;
            default:
                break;
        }
    }

    private void applyLimit() {
        if (isReadyToSend()) {
            coalesceProfile(paused ? 0.0 : currentLimitAmps);
        } else {
            // The charger is not ready yet (just connected, before boot). Hold the intent — the
            // latest values are already stored — and send it the moment the charger is ready, rather
            // than transmit before it can accept the profile or drop the setting outright.
            limitDeferred = true;
        }
    }

    /** Called when the charge point becomes ready; sends a limit/pause that was set while it was not. */
    public void onChargePointReady() {
        if (limitDeferred) {
            limitDeferred = false;
            applyLimit();
        }
    }

    private boolean isReadyToSend() {
        OcppChargePointHandler cp = chargePoint;
        return cp != null && cp.isReady();
    }

    private void coalesceProfile(double amps) {
        ProfileClaim claim = null;
        synchronized (lock) {
            pendingLimitAmps = amps;
            long elapsed = System.currentTimeMillis() - lastProfileSentAt;
            if (profileMinIntervalMs <= 0 || elapsed >= profileMinIntervalMs) {
                claim = claimSend();
            } else if (pendingFlush == null) {
                pendingFlush = scheduler.schedule(this::flushProfile, profileMinIntervalMs - elapsed,
                        TimeUnit.MILLISECONDS);
            }
        }
        if (claim != null) {
            sendProfile(claim);
        }
    }

    private void flushProfile() {
        ProfileClaim claim;
        synchronized (lock) {
            claim = claimSend();
        }
        sendProfile(claim);
    }

    /**
     * Marks a profile as being sent now and captures the state it stands for. Claiming the slot
     * under the lock is what keeps the minimum interval honest when two commands arrive at once: the
     * second one sees the timestamp already moved and coalesces instead of sending as well. Caller
     * must hold {@link #lock}.
     */
    private ProfileClaim claimSend() {
        pendingFlush = null;
        lastProfileSentAt = System.currentTimeMillis();
        profileGeneration++;
        return new ProfileClaim(profileGeneration, pendingLimitAmps, currentLimitAmps, paused);
    }

    /**
     * Whether this accepted claim may publish: only if no newer claim has published yet. An older
     * confirmation still publishes when the newer request was rejected — the charger is then
     * genuinely running the older accepted state and the channels must say so — but once a newer
     * claim has published, an out-of-order older confirmation cannot roll it back.
     */
    private boolean claimPublication(ProfileClaim claim) {
        synchronized (lock) {
            if (claim.generation() <= lastPublishedGeneration) {
                return false;
            }
            lastPublishedGeneration = claim.generation();
            return true;
        }
    }

    /**
     * Sends outside the lock. This calls back into the framework, and holding a lock across that is
     * what turns two independently-correct locks into a deadlock.
     */
    private void sendProfile(ProfileClaim claim) {
        dispatch(ChargingProfileBuilder.currentLimit(connectorId, claim.amps(), forceTxDefaultProfile, transactionId),
                "SetChargingProfile").whenComplete((confirmation, ex) -> {
                    // A non-exceptional completion only means the charger answered; the answer can
                    // still be Rejected or NotSupported. Publish only what THIS request carried —
                    // never the mutable fields, which may describe a newer request by now.
                    if (ex == null && confirmation instanceof SetChargingProfileConfirmation profile
                            && profile.getStatus() == ChargingProfileStatus.Accepted) {
                        if (claimPublication(claim)) {
                            updateState(CHANNEL_CHARGE_LIMIT, new QuantityType<>(claim.limitAmps(), Units.AMPERE));
                            updateState(CHANNEL_PAUSE, OnOffType.from(claim.paused()));
                        } else {
                            logger.debug("Stale SetChargingProfile confirmation on connector {} ignored", connectorId);
                        }
                    } else if (ex == null) {
                        logger.debug("SetChargingProfile on connector {} not accepted: {}", connectorId, confirmation);
                    }
                });
    }

    private void remoteStart() {
        if (!isReadyToSend()) {
            logger.debug("RemoteStart on connector {} skipped — charge point not ready", connectorId);
            return;
        }
        RemoteStartTransactionRequest request = new RemoteStartTransactionRequest(remoteStartTag);
        request.setConnectorId(connectorId);
        dispatch(request, "RemoteStart");
    }

    private void remoteStop() {
        Integer transaction = transactionId;
        if (transaction == null) {
            logger.debug("No active transaction to stop on connector {}", connectorId);
            return;
        }
        dispatchIfReady(new RemoteStopTransactionRequest(transaction), "RemoteStop");
    }

    private void changeAvailability(boolean operative) {
        if (!isReadyToSend()) {
            logger.debug("ChangeAvailability on connector {} skipped — charge point not ready", connectorId);
            return;
        }
        AvailabilityType type = operative ? AvailabilityType.Operative : AvailabilityType.Inoperative;
        dispatch(new ChangeAvailabilityRequest(connectorId, type), "ChangeAvailability")
                .whenComplete((confirmation, ex) -> {
                    // Only Accepted means it took effect now. Rejected leaves the channel as-is;
                    // Scheduled applies later, so let the following StatusNotification publish it.
                    if (ex == null && confirmation instanceof ChangeAvailabilityConfirmation change
                            && change.getStatus() == AvailabilityStatus.Accepted) {
                        updateState(CHANNEL_AVAILABILITY, OnOffType.from(operative));
                    }
                });
    }

    private void changeHardwareMaxCurrent(Command command) {
        if (hardwareMaxCurrentKey.isBlank()) {
            logger.debug("Connector {} has no hardwareMaxCurrentKey configured", connectorId);
            return;
        }
        if (!isReadyToSend()) {
            logger.debug("ChangeConfiguration[hardwareMax] on connector {} skipped — charge point not ready",
                    connectorId);
            return;
        }
        Double amps = toAmps(command);
        if (amps == null) {
            return;
        }
        String value = Integer.toString((int) Math.round(amps));
        dispatch(new ChangeConfigurationRequest(hardwareMaxCurrentKey, value), "ChangeConfiguration[hardwareMax]")
                .whenComplete((confirmation, ex) -> {
                    if (ex == null && confirmation instanceof ChangeConfigurationConfirmation change
                            && change.getStatus() == ConfigurationStatus.Accepted) {
                        updateState(CHANNEL_HARDWARE_MAX_CURRENT, new QuantityType<>(amps, Units.AMPERE));
                    }
                });
    }

    private void pollMeterValues() {
        if (!isReadyToSend()) {
            return; // nothing to poll until the charger has booted
        }
        TriggerMessageRequest request = new TriggerMessageRequest(TriggerMessageRequestType.MeterValues);
        request.setConnectorId(connectorId);
        dispatch(request, "TriggerMessage[MeterValues]");
    }

    /**
     * Ask the charger to (re)send this connector's StatusNotification now, so status and availability
     * are fresh immediately after a (re)connect instead of waiting for the charger to volunteer one.
     *
     * <p>
     * Deliberately bypasses the readiness gate (see {@link OcppChargePointHandler#sendDirect}): this
     * is the probe that recovers a charger which reopened its socket without re-booting — such a
     * charger never becomes ready by itself until its next heartbeat, which can be minutes away, and
     * a gated probe would deadlock on the very readiness it is meant to establish.
     */
    public void requestStatus() {
        OcppChargePointHandler cp = chargePoint;
        if (cp == null) {
            return;
        }
        TriggerMessageRequest request = new TriggerMessageRequest(TriggerMessageRequestType.StatusNotification);
        request.setConnectorId(connectorId);
        cp.sendDirect(request).whenComplete((confirmation, ex) -> {
            if (ex != null) {
                logger.debug("TriggerMessage[StatusNotification] on connector {} failed: {}", connectorId,
                        ex.toString());
            }
        });
    }

    private void dispatchIfReady(eu.chargetime.ocpp.model.Request request, String name) {
        if (isReadyToSend()) {
            dispatch(request, name);
        } else {
            logger.debug("{} on connector {} skipped — charge point not ready", name, connectorId);
        }
    }

    private CompletionStage<eu.chargetime.ocpp.model.Confirmation> dispatch(eu.chargetime.ocpp.model.Request request,
            String name) {
        OcppChargePointHandler cp = chargePoint;
        if (cp == null) {
            logger.debug("Cannot send {} — connector {} has no charge point", name, connectorId);
            return CompletableFuture.failedFuture(new IllegalStateException("no charge point"));
        }
        return cp.send(request).whenComplete((confirmation, ex) -> {
            if (ex != null) {
                Throwable unwrapped = ex.getCause();
                Throwable cause = ex instanceof CompletionException && unwrapped != null ? unwrapped : ex;
                logger.warn("{} on connector {} failed: {}", name, connectorId, cause.toString());
            } else {
                logger.debug("{} on connector {} -> {}", name, connectorId, confirmation);
            }
        });
    }

    private @Nullable Double toAmps(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> inAmperes = quantity.toUnit(Units.AMPERE);
            return inAmperes != null ? inAmperes.doubleValue() : null;
        }
        if (command instanceof DecimalType decimal) {
            return decimal.doubleValue();
        }
        return null;
    }

    // --- inbound (routed from the charge point) ---

    public void onStatusNotification(StatusNotificationRequest request) {
        ChargePointStatus status = request.getStatus();
        if (status != null) {
            updateState(CHANNEL_STATUS, new StringType(status.name()));
            updateState(CHANNEL_CABLE_CONNECTED, OnOffType.from(CABLE_PRESENT.contains(status)));
            // Reflect the charger's reported availability: Unavailable == Inoperative; any operational
            // status == Operative. Faulted is a fault, not an availability state, so leave it be.
            if (status == ChargePointStatus.Unavailable) {
                updateState(CHANNEL_AVAILABILITY, OnOffType.OFF);
            } else if (status != ChargePointStatus.Faulted) {
                updateState(CHANNEL_AVAILABILITY, OnOffType.ON);
            }
            // Drive charging from the reported status (the authoritative physical state) so it always
            // reflects reality and self-heals a stale/phantom transaction (e.g. a StopTransaction we
            // never saw, or a retained item state after a binding restart).
            if (status != ChargePointStatus.Faulted) {
                updateState(CHANNEL_CHARGING, OnOffType.from(CHARGING_ACTIVE.contains(status)));
            }
            if (status == ChargePointStatus.Available) {
                // Available is authoritative: no transaction is active. If one is still recorded
                // (its StopTransaction was lost), clear EVERY representation — the field, the
                // channel, the parent's routing map and the persistent store — or a restart would
                // recover a transaction the charger already declared finished.
                Integer stale = transactionId;
                if (stale != null) {
                    transactionId = null;
                    updateState(CHANNEL_TRANSACTION_ID, UnDefType.UNDEF);
                    OcppChargePointHandler cp = chargePoint;
                    if (cp != null) {
                        cp.transactionCompleted(stale);
                    }
                }
            }
            armStuckWatchdog(status);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    public void onMeterValues(MeterValuesRequest request) {
        Map<String, State> states = MeterValueMapper.toStates(request);
        ensureDynamicChannels(states.keySet());
        states.forEach(this::updateState);
        MeterValue[] meterValues = request.getMeterValue();
        if (meterValues != null && meterValues.length > 0) {
            ZonedDateTime timestamp = meterValues[0].getTimestamp();
            if (timestamp != null) {
                updateState(CHANNEL_TIMESTAMP, new DateTimeType(timestamp));
            }
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        logger.trace("Connector {} applied {} metering states", connectorId, states.size());
    }

    /**
     * Create any telemetry channel that this connector doesn't yet have but the charger has now
     * reported (soc, rpm, temperature, reactive/apparent power, export direction, frequency, ...).
     * The channels the site commonly uses are declared statically; these extras appear only if the
     * hardware actually sends them.
     */
    private void ensureDynamicChannels(Set<String> reportedChannelIds) {
        ThingBuilder builder = null;
        for (String channelId : reportedChannelIds) {
            DynamicChannel spec = DYNAMIC_CHANNELS.get(channelId);
            if (spec == null) {
                continue; // a statically-declared channel
            }
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
            if (getThing().getChannel(channelUID) != null) {
                continue; // already created on an earlier report
            }
            if (builder == null) {
                builder = editThing();
            }
            Channel channel = ChannelBuilder.create(channelUID, spec.itemType())
                    .withType(new ChannelTypeUID(BINDING_ID, spec.channelTypeId())).withLabel(spec.label()).build();
            builder.withChannel(channel);
            logger.debug("Connector {} adding dynamic telemetry channel {}", connectorId, channelId);
        }
        if (builder != null) {
            updateThing(builder.build());
        }
    }

    public void onTransactionStarted(StartTransactionRequest request, int transactionId) {
        // The charging channel itself is status-driven (see onStatusNotification); here we only record
        // the transaction id (needed for RemoteStop / TxProfile) and the start metadata.
        this.transactionId = transactionId;
        updateState(CHANNEL_TRANSACTION_ID, new DecimalType(transactionId));
        String idTag = request.getIdTag();
        if (idTag != null) {
            updateState(CHANNEL_ID_TAG, new StringType(idTag));
        }
        Integer meterStart = request.getMeterStart();
        if (meterStart != null) {
            updateState(CHANNEL_METER_START, new QuantityType<>(meterStart, Units.WATT_HOUR));
        }
        ZonedDateTime timestamp = request.getTimestamp();
        if (timestamp != null) {
            updateState(CHANNEL_TIMESTAMP_START, new DateTimeType(timestamp));
        }
    }

    public void onTransactionStopped(StopTransactionRequest request) {
        // charging channel is status-driven; here we only clear the id and record the stop metadata.
        this.transactionId = null;
        updateState(CHANNEL_TRANSACTION_ID, UnDefType.UNDEF);
        Integer meterStop = request.getMeterStop();
        if (meterStop != null) {
            updateState(CHANNEL_METER_STOP, new QuantityType<>(meterStop, Units.WATT_HOUR));
        }
        ZonedDateTime timestamp = request.getTimestamp();
        if (timestamp != null) {
            updateState(CHANNEL_TIMESTAMP_STOP, new DateTimeType(timestamp));
        }
    }

    // --- stuck-state watchdog ---

    private void armStuckWatchdog(ChargePointStatus status) {
        // Off by default: Preparing and Finishing are normal OCPP states (waiting for the vehicle or
        // for the user to unplug), so treating elapsed time in them as a fault and auto-unlocking is a
        // physical side effect that must be opted into per charger, not the default.
        if (!stuckStateRecovery) {
            return;
        }
        synchronized (lock) {
            cancel(stuckTask);
            stuckTask = null;
            if (TRANSIENT.contains(status)) {
                stuckTask = scheduler.schedule(() -> onStuck(status), STUCK_STATE_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void onStuck(ChargePointStatus status) {
        // Opt-in recovery (see armStuckWatchdog): nudge a connector wedged in a transient state with
        // an UnlockConnector, the OCPP-standard way to clear it.
        logger.warn("Connector {} stuck in {} for over {}s; sending UnlockConnector", connectorId, status,
                STUCK_STATE_SECONDS);
        dispatch(new UnlockConnectorRequest(connectorId), "UnlockConnector[stuck-recovery]");
    }

    private static void cancel(@Nullable ScheduledFuture<?> task) {
        if (task != null) {
            task.cancel(false);
        }
    }
}
