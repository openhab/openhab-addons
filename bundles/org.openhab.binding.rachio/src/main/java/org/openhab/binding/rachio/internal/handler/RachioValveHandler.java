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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.OptionalInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayRun;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for an individual Smart Hose Timer Valve.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioValveHandler extends AbstractRachioThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioValveHandler.class);

    private @Nullable RachioValve valve;
    private int runTime = 0;
    private OnOffType runState = OnOffType.OFF;
    private String lastEvent = "";
    private @Nullable DateTimeType lastEventTime;
    private @Nullable Boolean lastFlowDetected;
    private String lastRunType = "";
    private String lastEndReason = "";
    private @Nullable RachioValveDayRun nextPlannedRun;
    private @Nullable RachioValveDayRun nextSkippedRun;
    private @Nullable RachioValveDayRun lastCompletedRun;

    public RachioValveHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        String valveId = getThingConfigurationOrPropertyString(PROPERTY_VALVE_ID);
        logger.debug("Initializing Rachio Valve Thing '{}', configured valveId='{}'", getThing().getUID(), valveId);

        if (valveId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing Rachio valveId. Add the Valve via Inbox discovery or configure the Rachio Valve UUID manually.");
            return;
        }
        if (!initializeCloudHandler()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        refreshValve(valveId, true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        RachioBridgeHandler handler = cloudHandler;
        RachioValve currentValve = valve;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command == RefreshType.REFRESH) {
            if (currentValve == null) {
                initialize();
            } else {
                refreshValve(currentValve.id, false);
            }
            return;
        }

        if (currentValve == null) {
            logger.debug("{}: Valve model is not initialized; command for channel '{}' ignored", thingId, channel);
            return;
        }

        String errorMessage = "";
        try {
            if (CHANNEL_VALVE_RUNTIME.equals(channel)) {
                OptionalInt runtimeSeconds = RachioQuantityTypes.durationSeconds(command);
                if (runtimeSeconds.isPresent()) {
                    runTime = Math.max(0, runtimeSeconds.getAsInt());
                    logger.debug("{}: Valve will start for {} sec", thingId, runTime);
                    updateChannel(CHANNEL_VALVE_RUNTIME, RachioQuantityTypes.seconds(runTime));
                } else {
                    logger.debug("{}: runTime command value is not a duration: {}", thingId, command);
                }
            } else if (CHANNEL_VALVE_RUN.equals(channel)) {
                if (command == OnOffType.ON) {
                    int duration = getEffectiveRunTime(currentValve, handler);
                    logger.info("{}: Start Smart Hose Timer valve '{}' for {} sec", thingId,
                            currentValve.getThingName(), duration);
                    handler.startValveWatering(currentValve.id, duration);
                    runState = OnOffType.ON;
                    updateChannel(CHANNEL_VALVE_RUN, runState);
                } else if (command == OnOffType.OFF) {
                    logger.info("{}: Stop Smart Hose Timer valve '{}'", thingId, currentValve.getThingName());
                    handler.stopValveWatering(currentValve.id);
                    runState = OnOffType.OFF;
                    updateChannel(CHANNEL_VALVE_RUN, runState);
                }
            } else if (CHANNEL_VALVE_DEFAULT_RUNTIME.equals(channel)) {
                OptionalInt runtimeSeconds = RachioQuantityTypes.durationSeconds(command);
                if (runtimeSeconds.isPresent()) {
                    int defaultRuntime = runtimeSeconds.getAsInt();
                    if (defaultRuntime <= 0) {
                        logger.debug("{}: Invalid valve defaultRuntime {}; expected a positive number of seconds",
                                thingId, defaultRuntime);
                        return;
                    }
                    logger.info("{}: Set Smart Hose Timer valve '{}' default runtime to {} sec", thingId,
                            currentValve.getThingName(), defaultRuntime);
                    handler.setValveDefaultRuntime(currentValve.id, defaultRuntime);
                    currentValve.defaultRuntimeSeconds = defaultRuntime;
                    logger.debug(
                            "{}: ValveState.matches may remain false until the physical valve synchronizes the cloud-side default runtime update",
                            thingId);
                    postChannelData();
                } else {
                    logger.debug("{}: defaultRuntime command value is not a duration: {}", thingId, command);
                }
            } else if (CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN.equals(channel)) {
                if (command == OnOffType.ON) {
                    skipNextPlannedRun(handler, currentValve);
                    updateChannel(CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN, OnOffType.OFF);
                }
            } else if (CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP.equals(channel)) {
                if (command == OnOffType.ON) {
                    cancelNextPlannedRunSkip(handler, currentValve);
                    updateChannel(CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP, OnOffType.OFF);
                }
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            errorMessage = message != null ? message : e.toString();
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("{}: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    private int getEffectiveRunTime(RachioValve currentValve, RachioBridgeHandler handler) {
        if (runTime > 0) {
            return runTime;
        }
        int valveDefaultRuntime = currentValve.getDefaultRuntimeSeconds();
        if (valveDefaultRuntime > 0) {
            return valveDefaultRuntime;
        }
        int bridgeDefaultRuntime = handler.getDefaultRuntime();
        return bridgeDefaultRuntime > 0 ? bridgeDefaultRuntime : DEFAULT_ZONE_RUNTIME_SEC;
    }

    private boolean refreshValve(String valveId, boolean initialLoad) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        try {
            valve = initialLoad ? handler.getValveForInitialization(valveId) : handler.getValve(valveId);
            RachioValve currentValve = valve;
            if (currentValve == null || currentValve.id.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configured Rachio valveId was not found in the account.");
                return false;
            }
            thingId = currentValve.getThingName();
            registerStatusListener();
            if (initialLoad || getThing().getStatus() != ThingStatus.ONLINE) {
                // Smart Hose Timer webhooks are optional; polling keeps valve state current when they are disabled.
                handler.registerValveWebHook(currentValve.id,
                        initialLoad ? RequestPurpose.INITIALIZATION : RequestPurpose.BACKGROUND_REFRESH);
            }
            refreshSummary(currentValve.id);
            logger.debug("{}: Valve model lookup succeeded: valveId='{}', baseStationId='{}'", thingId, currentValve.id,
                    currentValve.baseStationId);
            if (resetLocalThrottleRetry()) {
                logger.debug("{}: Deferred initialization succeeded for Smart Hose Timer Valve '{}'; Thing is ONLINE.",
                        thingId, currentValve.id);
            }
            goOnline();
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = initialLoad
                    ? scheduleInitializationThrottleRetry("loading Smart Hose Timer Valve '" + valveId + "'",
                            () -> refreshValve(valveId, true), e)
                    : scheduleLocalThrottleRetry("loading Smart Hose Timer Valve '" + valveId + "'",
                            () -> refreshValve(valveId, false));
            if (delaySeconds > 0) {
                if (initialLoad) {
                    logger.debug(
                            "{}: Deferring initialization REST request for Smart Hose Timer Valve '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                            thingId, valveId, delaySeconds);
                } else {
                    logger.debug(
                            "{}: Local Rachio API throttle hit while loading Smart Hose Timer Valve '{}'; retry scheduled in {} seconds.",
                            thingId, valveId, delaySeconds);
                }
            }
            return false;
        } catch (RachioApiException e) {
            String message = "Unable to load Rachio Valve '" + valveId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            return false;
        } catch (RuntimeException e) {
            String message = "Unable to initialize Rachio Valve '" + valveId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message, e);
            updateStatus(ThingStatus.OFFLINE,
                    initialLoad ? ThingStatusDetail.CONFIGURATION_ERROR : ThingStatusDetail.COMMUNICATION_ERROR,
                    message);
            return false;
        }
    }

    public boolean handlesValveId(String valveId) {
        RachioValve currentValve = valve;
        return currentValve != null && currentValve.id.equalsIgnoreCase(valveId);
    }

    private void refreshSummary(String valveId) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            return;
        }
        try {
            RachioValveDayViewsResponse summary = handler.getValveDayViews(valveId);
            nextPlannedRun = summary.findNextPlannedRun().orElse(null);
            nextSkippedRun = summary.findNextSkippedRun().orElse(null);
            lastCompletedRun = summary.findLastCompletedRun().orElse(null);
            logger.debug("{}: Loaded Smart Hose Timer summary for valve '{}': {} day views", thingId, valveId,
                    summary.dayViews.size());
        } catch (RachioApiThrottledException e) {
            logger.debug(
                    "{}: Skipping Smart Hose Timer summary refresh for valve '{}' because the local API budget guard is active: {}",
                    thingId, valveId, e.getMessage());
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load Smart Hose Timer summary for valve '{}': {}; retaining last known values",
                    thingId, valveId, e.getMessage());
        }
    }

    private void skipNextPlannedRun(RachioBridgeHandler handler, RachioValve currentValve) throws RachioApiException {
        refreshSummary(currentValve.id);
        RachioValveDayRun run = nextPlannedRun;
        if (run == null) {
            throw new RachioApiException("No upcoming Smart Hose Timer planned run is available to skip.");
        }
        if (!hasSkipOverrideIdentifier(run)) {
            logger.debug("{}: Unable to skip Smart Hose Timer planned run because summary identifiers are missing",
                    thingId);
            return;
        }
        applySkipOverride(handler, run, true);
        refreshSummary(currentValve.id);
        postChannelData();
    }

    private void cancelNextPlannedRunSkip(RachioBridgeHandler handler, RachioValve currentValve)
            throws RachioApiException {
        refreshSummary(currentValve.id);
        RachioValveDayRun run = nextSkippedRun;
        if (run == null) {
            throw new RachioApiException("No upcoming skipped Smart Hose Timer planned run is available to cancel.");
        }
        if (!hasSkipOverrideIdentifier(run)) {
            logger.debug(
                    "{}: Unable to cancel Smart Hose Timer planned run skip because summary identifiers are missing",
                    thingId);
            return;
        }
        applySkipOverride(handler, run, false);
        refreshSummary(currentValve.id);
        postChannelData();
    }

    static boolean hasSkipOverrideIdentifier(RachioValveDayRun run) {
        String plannedRunId = run.getPlannedRunId();
        String date = run.getSkipOverrideDate();
        if (!plannedRunId.isBlank() && !date.isBlank()) {
            return true;
        }

        String programId = run.getProgramId();
        String timestamp = run.getStartTime();
        return !programId.isBlank() && !timestamp.isBlank();
    }

    static void applySkipOverride(RachioBridgeHandler handler, RachioValveDayRun run, boolean create)
            throws RachioApiException {
        String plannedRunId = run.getPlannedRunId();
        String date = run.getSkipOverrideDate();
        if (!plannedRunId.isBlank() && !date.isBlank()) {
            if (create) {
                handler.createPlannedRunSkipOverride(plannedRunId, date);
            } else {
                handler.deletePlannedRunSkipOverride(plannedRunId, date);
            }
            return;
        }

        String programId = run.getProgramId();
        String timestamp = run.getStartTime();
        if (!programId.isBlank() && !timestamp.isBlank()) {
            if (create) {
                handler.createSkipOverride(programId, timestamp);
            } else {
                handler.deleteSkipOverride(programId, timestamp);
            }
            return;
        }

        throw new RachioApiException(
                "Unable to manage Smart Hose Timer skip override because Summary day-view identifiers are missing.");
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        RachioValve currentValve = valve;
        if (currentValve == null || !handlesValveId(event.resourceId)) {
            return false;
        }

        RachioWebhookPayload payload = event.payload;
        if (payload == null) {
            payload = new RachioWebhookPayload();
        }
        lastEvent = event.eventType.isBlank() ? event.type : event.eventType;
        lastEventTime = getTimestamp();
        if (!payload.runType.isBlank()) {
            lastRunType = payload.runType;
        }
        if (!payload.endReason.isBlank()) {
            lastEndReason = payload.endReason;
        }
        if (payload.hasFlowDetected()) {
            lastFlowDetected = payload.getFlowDetected();
        }
        int durationSeconds = payload.getDurationSeconds();
        if (durationSeconds > 0) {
            runTime = durationSeconds;
        }

        if (EVENT_VALVE_RUN_START.equals(event.eventType)) {
            logger.info("{}: Smart Hose Timer valve '{}' STARTED watering (duration={} sec, runType={})", thingId,
                    currentValve.getThingName(), durationSeconds, payload.runType);
            runState = OnOffType.ON;
        } else if (EVENT_VALVE_RUN_END.equals(event.eventType)) {
            logger.info("{}: Smart Hose Timer valve '{}' STOPPED watering (duration={} sec, endReason={}, runType={})",
                    thingId, currentValve.getThingName(), durationSeconds, payload.endReason, payload.runType);
            runState = OnOffType.OFF;
        } else {
            logger.debug("{}: Unhandled Smart Hose Timer valve event '{}' for valve '{}'", thingId, event.eventType,
                    currentValve.id);
            return false;
        }

        refreshSummary(currentValve.id);
        postChannelData();
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
        return true;
    }

    @Override
    protected void postChannelData() {
        RachioValve currentValve = valve;
        if (currentValve == null) {
            return;
        }
        updateChannel(CHANNEL_VALVE_NAME, new StringType(currentValve.getThingName()));
        updateChannel(CHANNEL_VALVE_ONLINE, onlineState(currentValve));
        updateChannel(CHANNEL_VALVE_RUN, runState);
        updateChannel(CHANNEL_VALVE_RUNTIME, RachioQuantityTypes.seconds(runTime));
        updateChannel(CHANNEL_VALVE_DEFAULT_RUNTIME,
                RachioQuantityTypes.seconds(currentValve.getDefaultRuntimeSeconds()));
        updateChannel(CHANNEL_VALVE_STATE_MATCHES,
                currentValve.hasStateMatches() ? currentValve.stateMatches() ? OnOffType.ON : OnOffType.OFF
                        : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_FLOW_DETECTED, flowDetectedState(currentValve));
        updateChannel(CHANNEL_VALVE_BATTERY_LEVEL, batteryLevelState(currentValve));
        updateChannel(CHANNEL_VALVE_SERIAL_NUMBER, stringOrUndef(currentValve.serialNumber));
        updateChannel(CHANNEL_VALVE_LAST_RUN_TYPE, stringOrUndef(lastRunType));
        updateChannel(CHANNEL_VALVE_LAST_END_REASON, stringOrUndef(lastEndReason));
        postSummaryChannelData();
        updateChannel(CHANNEL_LAST_EVENT, stringOrUndef(lastEvent));
        DateTimeType eventTime = lastEventTime;
        updateChannel(CHANNEL_LAST_EVENTTS, eventTime != null ? eventTime : UnDefType.UNDEF);
    }

    private void postSummaryChannelData() {
        RachioValveDayRun nextRun = nextPlannedRun;
        updateChannel(CHANNEL_VALVE_NEXT_PLANNED_RUNTIME,
                nextRun != null ? dateTimeOrUndef(nextRun.getStartTime()) : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_NEXT_PLANNED_RUN_DURATION,
                nextRun != null ? RachioQuantityTypes.seconds(nextRun.getDurationSeconds()) : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_NEXT_PLANNED_RUN_PROGRAM_ID,
                nextRun != null ? stringOrUndef(nextRun.getProgramId()) : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_NEXT_PLANNED_RUN_SKIPPED,
                nextRun != null ? nextRun.isSkipped() ? OnOffType.ON : OnOffType.OFF : UnDefType.UNDEF);

        RachioValveDayRun completedRun = lastCompletedRun;
        updateChannel(CHANNEL_VALVE_LAST_COMPLETED_RUNTIME,
                completedRun != null ? dateTimeOrUndef(completedRun.getStartTime()) : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_LAST_COMPLETED_RUN_DURATION,
                completedRun != null ? RachioQuantityTypes.seconds(completedRun.getDurationSeconds())
                        : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_LAST_RUN_STATUS,
                completedRun != null ? stringOrUndef(completedRun.getStatus()) : UnDefType.UNDEF);
    }

    @Override
    protected void goOnline() {
        RachioValve currentValve = valve;
        if (currentValve != null) {
            updateProperties(currentValve.fillProperties());
        }
        postChannelData();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected void onBridgeOnline() {
        RachioValve currentValve = valve;
        if (currentValve == null) {
            initialize();
        } else {
            refreshValve(currentValve.id, false);
        }
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    @Override
    public void onConfigurationUpdated() {
        renewWebhookRegistration(RequestPurpose.USER_COMMAND);
    }

    void renewWebhookRegistration(RequestPurpose requestPurpose) {
        RachioBridgeHandler handler = cloudHandler;
        RachioValve currentValve = valve;
        if (handler != null && currentValve != null) {
            try {
                handler.registerValveWebHook(currentValve.id, requestPurpose);
            } catch (RachioApiException e) {
                logger.debug("{}: Unable to renew valve webhook registration, cause={}", thingId,
                        e.getClass().getSimpleName());
            }
        }
    }

    private State onlineState(RachioValve currentValve) {
        if (!currentValve.hasOnlineState()) {
            return UnDefType.UNDEF;
        }
        return currentValve.isOnline() ? OnOffType.ON : OnOffType.OFF;
    }

    private State flowDetectedState(RachioValve currentValve) {
        Boolean webhookFlowDetected = lastFlowDetected;
        if (webhookFlowDetected != null) {
            return webhookFlowDetected.booleanValue() ? OnOffType.ON : OnOffType.OFF;
        }
        if (!currentValve.hasFlowDetected()) {
            return UnDefType.UNDEF;
        }
        return currentValve.flowDetected() ? OnOffType.ON : OnOffType.OFF;
    }

    private State batteryLevelState(RachioValve currentValve) {
        Double batteryLevel = currentValve.batteryLevel;
        if (batteryLevel == null || batteryLevel.isNaN() || batteryLevel.isInfinite()) {
            return UnDefType.UNDEF;
        }
        return RachioQuantityTypes.percentOrUndef(batteryLevel.doubleValue());
    }

    private State stringOrUndef(String value) {
        return value.isBlank() ? UnDefType.UNDEF : new StringType(value);
    }

    private State dateTimeOrUndef(String value) {
        if (value.isBlank()) {
            return UnDefType.UNDEF;
        }
        try {
            if (value.chars().allMatch(Character::isDigit)) {
                long epoch = Long.parseLong(value);
                long epochMillis = value.length() > 10 ? epoch : epoch * 1000L;
                return new DateTimeType(
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()));
            }
            return new DateTimeType(value);
        } catch (RuntimeException e) {
            logger.trace("{}: Unable to parse DateTime channel value '{}'", thingId, value);
            return UnDefType.UNDEF;
        }
    }
}
