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
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayRun;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
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
 * Handler for a Smart Hose Timer Program.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioValveProgramHandler extends AbstractRachioThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioValveProgramHandler.class);

    private @Nullable RachioValveProgram program;
    private @Nullable RachioValveDayRun nextProgramRun;
    private @Nullable RachioValveDayRun nextSkippedProgramRun;
    private String lastEvent = "";
    private @Nullable DateTimeType lastEventTime;
    private String lastRainSkipPlannedRunStartTime = "";
    private String lastRainSkipCanceledPlannedRunStartTime = "";

    public RachioValveProgramHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        String programId = getThingConfigurationOrPropertyString(PROPERTY_VALVE_PROGRAM_ID);
        logger.debug("Initializing Rachio Valve Program Thing '{}', configured programId='{}'", getThing().getUID(),
                programId);

        if (programId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing Rachio programId. Add the Program via Inbox discovery or configure the Rachio Program UUID manually.");
            return;
        }
        if (!initializeCloudHandler()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        refreshProgram(programId, true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        RachioBridgeHandler handler = cloudHandler;
        RachioValveProgram currentProgram = program;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (command == RefreshType.REFRESH) {
            if (currentProgram == null) {
                initialize();
            } else {
                refreshProgram(currentProgram.id, false);
            }
            return;
        }

        if (currentProgram == null) {
            logger.debug("{}: Program model is not initialized; command for channel '{}' ignored", thingId, channel);
            return;
        }

        String errorMessage = "";
        try {
            if (CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN.equals(channel)) {
                if (command == OnOffType.ON) {
                    skipNextProgramRun(handler, currentProgram);
                    updateChannel(CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN, OnOffType.OFF);
                }
            } else if (CHANNEL_VALVE_PROGRAM_CANCEL_NEXT_PLANNED_RUN_SKIP.equals(channel)) {
                if (command == OnOffType.ON) {
                    cancelNextProgramRunSkip(handler, currentProgram);
                    updateChannel(CHANNEL_VALVE_PROGRAM_CANCEL_NEXT_PLANNED_RUN_SKIP, OnOffType.OFF);
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

    private boolean refreshProgram(String programId, boolean initialLoad) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        try {
            program = initialLoad ? loadValveProgramForInitialization(programId) : loadValveProgram(programId);
            RachioValveProgram currentProgram = program;
            if (currentProgram == null || currentProgram.id.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configured Rachio programId was not found in the account.");
                return false;
            }
            thingId = currentProgram.getThingName();
            registerStatusListener();
            if (initialLoad || getThing().getStatus() != ThingStatus.ONLINE) {
                // Smart Hose Timer program webhooks are optional; polling keeps schedule-like state current otherwise.
                handler.registerValveProgramWebHook(currentProgram.id,
                        initialLoad ? RequestPurpose.INITIALIZATION : RequestPurpose.BACKGROUND_REFRESH);
            }
            refreshProgramSummary(currentProgram);
            logger.debug("{}: Valve Program model lookup succeeded: programId='{}', valveId='{}'", thingId,
                    currentProgram.id, currentProgram.getValveId());
            if (resetLocalThrottleRetry()) {
                logger.debug(
                        "{}: Deferred initialization succeeded for Smart Hose Timer Program '{}'; Thing is ONLINE.",
                        thingId, currentProgram.id);
            }
            goOnline();
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = initialLoad
                    ? scheduleInitializationThrottleRetry("loading Smart Hose Timer Program '" + programId + "'",
                            () -> refreshProgram(programId, true), e)
                    : scheduleLocalThrottleRetry("loading Smart Hose Timer Program '" + programId + "'",
                            () -> refreshProgram(programId, false));
            if (delaySeconds > 0) {
                if (initialLoad) {
                    logger.debug(
                            "{}: Deferring initialization REST request for Smart Hose Timer Program '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                            thingId, programId, delaySeconds);
                } else {
                    logger.debug(
                            "{}: Local Rachio API throttle hit while loading Smart Hose Timer Program '{}'; retry scheduled in {} seconds.",
                            thingId, programId, delaySeconds);
                }
            }
            return false;
        } catch (RachioApiException e) {
            String message = "Unable to load Rachio Valve Program '" + programId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            return false;
        } catch (RuntimeException e) {
            String message = "Unable to initialize Rachio Valve Program '" + programId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message, e);
            updateStatus(ThingStatus.OFFLINE,
                    initialLoad ? ThingStatusDetail.CONFIGURATION_ERROR : ThingStatusDetail.COMMUNICATION_ERROR,
                    message);
            return false;
        }
    }

    protected RachioValveProgram loadValveProgram(String programId) throws RachioApiException {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            throw new RachioApiException("Bridge handler is not initialized.");
        }
        return handler.getValveProgram(programId);
    }

    protected RachioValveProgram loadValveProgramForInitialization(String programId) throws RachioApiException {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            throw new RachioApiException("Bridge handler is not initialized.");
        }
        return handler.getValveProgramForInitialization(programId);
    }

    private void refreshProgramSummary(RachioValveProgram currentProgram) {
        RachioBridgeHandler handler = cloudHandler;
        String valveId = firstNonBlank(currentProgram.getValveId(),
                getThingConfigurationOrPropertyString(PROPERTY_VALVE_ID));
        if (handler == null || valveId.isBlank()) {
            nextProgramRun = null;
            nextSkippedProgramRun = null;
            return;
        }

        try {
            RachioValveDayViewsResponse summary = handler.getValveDayViews(valveId);
            long now = System.currentTimeMillis();
            nextProgramRun = summary.getRuns().stream()
                    .filter(run -> currentProgram.id.equalsIgnoreCase(run.getProgramId()))
                    .filter(run -> run.getStartEpochMillis() >= now)
                    .min(Comparator.comparingLong(RachioValveDayRun::getStartEpochMillis)).orElse(null);
            nextSkippedProgramRun = summary.getRuns().stream()
                    .filter(run -> currentProgram.id.equalsIgnoreCase(run.getProgramId()))
                    .filter(RachioValveDayRun::isSkipped).filter(run -> run.getStartEpochMillis() >= now)
                    .min(Comparator.comparingLong(RachioValveDayRun::getStartEpochMillis)).orElse(null);
        } catch (RachioApiThrottledException e) {
            logger.debug(
                    "{}: Skipping Smart Hose Timer Program summary refresh for program '{}' because the local API budget guard is active: {}",
                    thingId, currentProgram.id, e.getMessage());
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load Smart Hose Timer Program summary for program '{}': {}; retaining values",
                    thingId, currentProgram.id, e.getMessage());
        }
    }

    private void skipNextProgramRun(RachioBridgeHandler handler, RachioValveProgram currentProgram)
            throws RachioApiException {
        refreshProgramSummary(currentProgram);
        RachioValveDayRun run = nextProgramRun;
        if (run == null) {
            throw new RachioApiException("No upcoming Smart Hose Timer program run is available to skip.");
        }
        if (!RachioValveHandler.hasSkipOverrideIdentifier(run)) {
            logger.debug("{}: Unable to skip Smart Hose Timer program run because summary identifiers are missing",
                    thingId);
            return;
        }
        RachioValveHandler.applySkipOverride(handler, run, true);
        refreshProgramSummary(currentProgram);
        postChannelData();
    }

    private void cancelNextProgramRunSkip(RachioBridgeHandler handler, RachioValveProgram currentProgram)
            throws RachioApiException {
        refreshProgramSummary(currentProgram);
        RachioValveDayRun run = nextSkippedProgramRun;
        if (run == null) {
            throw new RachioApiException("No upcoming skipped Smart Hose Timer program run is available to cancel.");
        }
        if (!RachioValveHandler.hasSkipOverrideIdentifier(run)) {
            logger.debug(
                    "{}: Unable to cancel Smart Hose Timer program run skip because summary identifiers are missing",
                    thingId);
            return;
        }
        RachioValveHandler.applySkipOverride(handler, run, false);
        refreshProgramSummary(currentProgram);
        postChannelData();
    }

    public boolean handlesProgramId(String programId) {
        RachioValveProgram currentProgram = program;
        return currentProgram != null && currentProgram.id.equalsIgnoreCase(programId);
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        RachioValveProgram currentProgram = program;
        RachioWebhookPayload payload = event.payload;
        String eventProgramId = firstNonBlank(event.resourceId, payload != null ? payload.programId : "");
        if (currentProgram == null || !handlesProgramId(eventProgramId)) {
            return false;
        }

        if (payload == null) {
            payload = new RachioWebhookPayload();
        }
        lastEvent = event.eventType.isBlank() ? event.type : event.eventType;
        lastEventTime = getTimestamp();
        if (EVENT_PROGRAM_RAIN_SKIP_CREATED.equals(event.eventType)) {
            lastRainSkipPlannedRunStartTime = payload.plannedRunStartTime;
        } else if (EVENT_PROGRAM_RAIN_SKIP_CANCELED.equals(event.eventType)) {
            lastRainSkipCanceledPlannedRunStartTime = payload.plannedRunStartTime;
        } else {
            logger.debug("{}: Unhandled Smart Hose Timer Program event '{}' for program '{}'", thingId, event.eventType,
                    currentProgram.id);
            return false;
        }

        refreshProgramSummary(currentProgram);
        postChannelData();
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
        return true;
    }

    @Override
    protected void postChannelData() {
        RachioValveProgram currentProgram = program;
        if (currentProgram == null) {
            return;
        }

        RachioValveDayRun nextRun = nextProgramRun;
        String valveId = firstNonBlank(currentProgram.getValveId(),
                getThingConfigurationOrPropertyString(PROPERTY_VALVE_ID));
        Boolean enabled = currentProgram.enabled;
        updateChannel(CHANNEL_VALVE_PROGRAM_NAME, new StringType(currentProgram.getThingName()));
        updateChannel(CHANNEL_VALVE_PROGRAM_ENABLED,
                enabled != null ? enabled.booleanValue() ? OnOffType.ON : OnOffType.OFF : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_PROGRAM_TYPE, stringOrUndef(currentProgram.getProgramType()));
        updateChannel(CHANNEL_VALVE_PROGRAM_VALVE_ID, stringOrUndef(valveId));
        updateChannel(CHANNEL_VALVE_PROGRAM_START_TIME, stringOrUndef(currentProgram.startTime));
        updateChannel(CHANNEL_VALVE_PROGRAM_NEXT_RUNTIME, nextRun != null ? dateTimeOrUndef(nextRun.getStartTime())
                : dateTimeOrUndef(currentProgram.nextRunTime));
        updateChannel(CHANNEL_VALVE_PROGRAM_LAST_RUNTIME, dateTimeOrUndef(currentProgram.lastRunTime));
        updateChannel(CHANNEL_VALVE_PROGRAM_DURATION, RachioQuantityTypes.seconds(currentProgram.getDurationSeconds()));
        updateChannel(CHANNEL_VALVE_PROGRAM_DAYS_OF_WEEK, stringOrUndef(currentProgram.getDaysOfWeek()));
        updateChannel(CHANNEL_VALVE_PROGRAM_INTERVAL_DAYS, RachioQuantityTypes.days(currentProgram.intervalDays));
        updateChannel(CHANNEL_VALVE_PROGRAM_SEASONAL_ADJUSTMENT,
                RachioQuantityTypes.fractionOrUndef(currentProgram.seasonalAdjustment));
        updateChannel(CHANNEL_VALVE_PROGRAM_UPDATED_AT,
                dateTimeOrUndef(firstNonBlank(currentProgram.updatedAt, currentProgram.lastUpdateDate)));
        updateChannel(CHANNEL_VALVE_PROGRAM_NEXT_RUN_SKIPPED,
                nextRun != null ? nextRun.isSkipped() ? OnOffType.ON : OnOffType.OFF : UnDefType.UNDEF);
        updateChannel(CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_START, dateTimeOrUndef(lastRainSkipPlannedRunStartTime));
        updateChannel(CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_CANCELED_START,
                dateTimeOrUndef(lastRainSkipCanceledPlannedRunStartTime));
        updateChannel(CHANNEL_LAST_EVENT, stringOrUndef(lastEvent));
        DateTimeType eventTime = lastEventTime;
        updateChannel(CHANNEL_LAST_EVENTTS, eventTime != null ? eventTime : UnDefType.UNDEF);
    }

    @Override
    protected void goOnline() {
        RachioValveProgram currentProgram = program;
        if (currentProgram != null) {
            updateProperties(currentProgram.fillProperties());
        }
        postChannelData();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected void onBridgeOnline() {
        RachioValveProgram currentProgram = program;
        if (currentProgram == null) {
            initialize();
        } else {
            refreshProgram(currentProgram.id, false);
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
        RachioValveProgram currentProgram = program;
        if (handler != null && currentProgram != null) {
            try {
                handler.registerValveProgramWebHook(currentProgram.id, requestPurpose);
            } catch (RachioApiException e) {
                logger.debug("{}: Unable to renew Program webhook registration, cause={}", thingId,
                        e.getClass().getSimpleName());
            }
        }
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
