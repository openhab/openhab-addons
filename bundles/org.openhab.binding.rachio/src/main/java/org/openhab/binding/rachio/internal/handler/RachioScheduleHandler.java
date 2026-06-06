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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
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
 * Handles Rachio ScheduleRule Things.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioScheduleHandler extends AbstractRachioThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioScheduleHandler.class);
    protected String scheduleRuleId = "";
    protected RachioScheduleRuleResponse scheduleRule = new RachioScheduleRuleResponse();

    public RachioScheduleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        scheduleRuleId = resolveScheduleRuleId();
        logger.debug("Initializing Rachio schedule Thing '{}' with scheduleRuleId '{}'", thingId, scheduleRuleId);

        if (scheduleRuleId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing Rachio scheduleRuleId. Use discovery or configure the Rachio schedule rule UUID manually.");
            return;
        }

        if (!initializeCloudHandler()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        RachioBridgeHandler handler = cloudHandler;
        if (handler != null) {
            handler.registerStatusListener(this);
        }
        goOnline();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            logger.debug("{}: Cloud handler is not initialized", thingId);
            return;
        }

        try {
            if (command == RefreshType.REFRESH) {
                if (refreshScheduleRule()) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return;
            }
            RachioScheduleCommandSupport.handleCommand(this, logger, handler, scheduleRuleId, channel, command,
                    value -> scheduleRule.seasonalAdjustment = value);
        } catch (RachioApiException e) {
            logger.debug("{}: Schedule command failed: {}", thingId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void goOnline() {
        if (refreshScheduleRule()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected boolean refreshScheduleRule() {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }
        try {
            scheduleRule = loadScheduleRule();
            logger.debug("{}: Loaded schedule rule '{}'", thingId, scheduleRuleId);
            postChannelData();
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            if (resetLocalThrottleRetry()) {
                logger.debug("{}: Deferred initialization succeeded for schedule rule '{}'; Thing is ONLINE.", thingId,
                        scheduleRuleId);
            }
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = scheduleInitializationThrottleRetry("loading schedule rule '" + scheduleRuleId + "'",
                    this::goOnline, e);
            if (delaySeconds > 0) {
                logger.debug(
                        "{}: Deferring initialization REST request for schedule rule '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                        thingId, scheduleRuleId, delaySeconds);
            }
            return false;
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load schedule rule '{}': {}", thingId, scheduleRuleId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        }
    }

    protected RachioScheduleRuleResponse loadScheduleRule() throws RachioApiException {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            throw new RachioApiException("Bridge handler is not initialized.");
        }
        return handler.getScheduleRuleForInitialization(scheduleRuleId);
    }

    @Override
    protected void postChannelData() {
        updateChannel(CHANNEL_SCHEDULE_NAME, stringOrUndef(scheduleRule.name));
        updateChannel(CHANNEL_SCHEDULE_ENABLED, scheduleRule.enabled ? OnOffType.ON : OnOffType.OFF);
        updateChannel(CHANNEL_SCHEDULE_TYPE, stringOrUndef(scheduleRule.type));
        updateChannel(CHANNEL_SCHEDULE_START_TIME, dateTimeOrUndef(scheduleRule.startTime));
        updateChannel(CHANNEL_SCHEDULE_LAST_RUN, dateTimeOrUndef(scheduleRule.lastRun));
        updateChannel(CHANNEL_SCHEDULE_NEXT_RUN, dateTimeOrUndef(scheduleRule.nextRun));
        updateChannel(CHANNEL_SCHEDULE_ZONES, stringOrUndef(scheduleRule.getZoneSummary()));
        updateChannel(CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT,
                RachioQuantityTypes.fractionOrUndef(scheduleRule.seasonalAdjustment));
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        if (!event.type.equals("SCHEDULE_STATUS") || scheduleRuleId.isBlank()
                || !scheduleRuleId.equalsIgnoreCase(event.scheduleId)) {
            return false;
        }

        scheduleRule.id = scheduleRuleId;
        scheduleRule.name = event.scheduleName;
        scheduleRule.type = event.scheduleType;
        if (event.subType.equals("SCHEDULE_STARTED")) {
            scheduleRule.startTime = event.startTime;
        } else if (event.subType.equals("SCHEDULE_STOPPED") || event.subType.equals("SCHEDULE_COMPLETED")) {
            scheduleRule.lastRun = event.endTime.isBlank() ? event.timestamp : event.endTime;
        }
        logger.debug("{}: Schedule webhook event received: {}.{}", thingId, event.type, event.subType);
        postChannelData();
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
        return true;
    }

    private String resolveScheduleRuleId() {
        String configuredId = getThingConfigurationString(PROPERTY_SCHEDULE_RULE_ID);
        if (!configuredId.isBlank()) {
            return configuredId;
        }
        String propertyId = getThing().getProperties().get(PROPERTY_SCHEDULE_RULE_ID);
        return propertyId != null ? propertyId.trim() : "";
    }

    protected State stringOrUndef(String value) {
        return value.isBlank() ? UnDefType.UNDEF : new StringType(value);
    }

    protected State dateTimeOrUndef(String value) {
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
