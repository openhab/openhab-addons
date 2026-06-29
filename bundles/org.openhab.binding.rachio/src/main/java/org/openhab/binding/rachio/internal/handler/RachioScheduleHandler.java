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
import static org.openhab.binding.rachio.internal.RachioUtils.exceptionMessage;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import java.util.Objects;
import java.util.OptionalDouble;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
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
    private boolean scheduleRuleLoaded = false;

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

        registerStatusListener();
        goOnline();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        if (command == RefreshType.REFRESH) {
            if (loadScheduleRuleForRefreshIfCacheMissing(channel)) {
                updateStatus(ThingStatus.ONLINE);
            }
            return;
        }

        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            logger.debug("{}: Cloud handler is not initialized", thingId);
            return;
        }

        try {
            if (channel.equals(CHANNEL_SCHEDULE_START) && command == OnOffType.ON) {
                handler.startScheduleRule(scheduleRuleId);
                updateChannel(CHANNEL_SCHEDULE_START, OnOffType.OFF);
            } else if (channel.equals(CHANNEL_SCHEDULE_SKIP) && command == OnOffType.ON) {
                handler.skipScheduleRule(scheduleRuleId);
                updateChannel(CHANNEL_SCHEDULE_SKIP, OnOffType.OFF);
            } else if (channel.equals(CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT)) {
                OptionalDouble adjustment = RachioQuantityTypes.dimensionless(command);
                if (adjustment.isPresent()) {
                    double value = adjustment.getAsDouble();
                    handler.setScheduleRuleSeasonalAdjustment(scheduleRuleId, value);
                    scheduleRule.seasonalAdjustment = value;
                    updateChannel(CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT, RachioQuantityTypes.fractionOrUndef(value));
                } else {
                    logger.debug("{}: Seasonal adjustment command value is not dimensionless: {}", thingId, command);
                }
            } else if (channel.equals(CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN) && command == OnOffType.ON) {
                handler.skipForwardZoneRun(scheduleRuleId);
                updateChannel(CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN, OnOffType.OFF);
            }
        } catch (RachioApiException e) {
            String message = exceptionMessage(e);
            logger.debug("{}: Schedule command failed: {}", thingId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    private boolean loadScheduleRuleForRefreshIfCacheMissing(String channel) {
        boolean loadMissingCache = false;
        synchronized (this) {
            if (!scheduleRuleLoaded) {
                logger.debug("{}: Schedule rule cache is empty; loading rule '{}' for channel '{}' REFRESH", thingId,
                        scheduleRuleId, channel);
                loadMissingCache = true;
            } else {
                logger.trace("{}: Serving schedule channel '{}' REFRESH from cached rule '{}'", thingId, channel,
                        scheduleRuleId);
            }
        }
        if (loadMissingCache) {
            return refreshScheduleRule();
        }
        postCachedChannelData(channel);
        return false;
    }

    @Override
    protected void goOnline() {
        if (refreshScheduleRule()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected boolean refreshScheduleRule() {
        RachioBridgeHandler handler;
        String requestedScheduleRuleId;
        synchronized (this) {
            handler = cloudHandler;
            requestedScheduleRuleId = scheduleRuleId;
        }
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }
        try {
            RachioScheduleRuleResponse loadedScheduleRule = loadScheduleRule(handler, requestedScheduleRuleId);
            if (!applyLoadedScheduleRule(handler, requestedScheduleRuleId, loadedScheduleRule)) {
                logger.debug("{}: Ignoring stale schedule rule '{}' refresh after bridge or rule id changed", thingId,
                        requestedScheduleRuleId);
                return false;
            }
            logger.debug("{}: Loaded schedule rule '{}'", thingId, requestedScheduleRuleId);
            postChannelData();
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            if (resetLocalThrottleRetry()) {
                logger.debug("{}: Deferred initialization succeeded for schedule rule '{}'; Thing is ONLINE.", thingId,
                        requestedScheduleRuleId);
            }
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = scheduleInitializationThrottleRetry(
                    "loading schedule rule '" + requestedScheduleRuleId + "'", this::goOnline, e);
            if (delaySeconds > 0) {
                logger.debug(
                        "{}: Deferring initialization REST request for schedule rule '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                        thingId, requestedScheduleRuleId, delaySeconds);
            }
            return false;
        } catch (RachioApiException e) {
            String message = exceptionMessage(e);
            logger.debug("{}: Unable to load schedule rule '{}': {}", thingId, requestedScheduleRuleId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            return false;
        }
    }

    private synchronized boolean applyLoadedScheduleRule(RachioBridgeHandler handler, String requestedScheduleRuleId,
            RachioScheduleRuleResponse loadedScheduleRule) {
        if (!Objects.equals(handler, cloudHandler) || !scheduleRuleId.equals(requestedScheduleRuleId)) {
            return false;
        }
        scheduleRule = loadedScheduleRule;
        scheduleRuleLoaded = true;
        return true;
    }

    private void postCachedChannelData(String channel) {
        postChannelData();
        State cachedState = channelData.get(channel);
        if (cachedState != null) {
            updateState(channel, cachedState);
        }
    }

    protected RachioScheduleRuleResponse loadScheduleRule(RachioBridgeHandler handler, String requestedScheduleRuleId)
            throws RachioApiException {
        return handler.getScheduleRuleForInitialization(requestedScheduleRuleId);
    }

    @Override
    protected void postChannelData() {
        updateChannel(CHANNEL_SCHEDULE_NAME, stringOrUndef(scheduleRule.name));
        updateChannel(CHANNEL_SCHEDULE_ENABLED, scheduleRule.enabled ? OnOffType.ON : OnOffType.OFF);
        updateChannel(CHANNEL_SCHEDULE_TYPE, stringOrUndef(scheduleRule.type));
        updateChannel(CHANNEL_SCHEDULE_START_TIME, dateTimeOrUndef(CHANNEL_SCHEDULE_START_TIME, "startDate",
                scheduleRule.startDate, "startTime", scheduleRule.startTime));
        updateChannel(CHANNEL_SCHEDULE_LAST_RUN,
                dateTimeOrUndef(CHANNEL_SCHEDULE_LAST_RUN, "lastRun", scheduleRule.lastRun, "lastRunDate",
                        scheduleRule.lastRunDate, "lastRunTime", scheduleRule.lastRunTime, "lastRunAt",
                        scheduleRule.lastRunAt));
        updateChannel(CHANNEL_SCHEDULE_NEXT_RUN,
                dateTimeOrUndef(CHANNEL_SCHEDULE_NEXT_RUN, "nextRun", scheduleRule.nextRun, "nextRunDate",
                        scheduleRule.nextRunDate, "nextRunTime", scheduleRule.nextRunTime, "nextRunAt",
                        scheduleRule.nextRunAt, "nextScheduledRun", scheduleRule.nextScheduledRun, "nextScheduledStart",
                        scheduleRule.nextScheduledStart));
        updateChannel(CHANNEL_SCHEDULE_ZONES, stringOrUndef(scheduleRule.getZoneSummary()));
        updateChannel(CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT,
                RachioQuantityTypes.fractionOrUndef(scheduleRule.seasonalAdjustment));
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        if (!"SCHEDULE_STATUS".equals(event.type) || scheduleRuleId.isBlank()
                || !scheduleRuleId.equalsIgnoreCase(event.scheduleId)) {
            return false;
        }

        scheduleRule.id = scheduleRuleId;
        if (!event.scheduleName.isBlank()) {
            scheduleRule.name = event.scheduleName;
        }
        if (!event.scheduleType.isBlank()) {
            scheduleRule.type = event.scheduleType;
        }
        if ("SCHEDULE_STARTED".equals(event.subType)) {
            scheduleRule.startTime = event.startTime;
        } else if ("SCHEDULE_STOPPED".equals(event.subType) || "SCHEDULE_COMPLETED".equals(event.subType)) {
            scheduleRule.lastRun = event.endTime.isBlank() ? event.timestamp : event.endTime;
        }
        logger.debug("{}: Schedule webhook event received: {}.{}", thingId, event.type, event.subType);
        postChannelData();
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
        return true;
    }

    boolean handlesScheduleRule(String scheduleId) {
        return !scheduleRuleId.isBlank() && scheduleRuleId.equalsIgnoreCase(scheduleId);
    }

    String getScheduleRuleNameForRunSummary() {
        return firstNonBlank(scheduleRule.name, scheduleRule.externalName, getThingLabel());
    }

    String getScheduleRuleNameSourceForRunSummary() {
        if (!scheduleRule.name.isBlank()) {
            return "schedule handler";
        }
        if (!scheduleRule.externalName.isBlank()) {
            return "schedule handler externalName";
        }
        if (!getThingLabel().isBlank()) {
            return "schedule Thing label";
        }
        return "schedule handler";
    }

    String getScheduleRuleTypeForRunSummary() {
        return scheduleRule.type.isBlank() ? "FIXED" : scheduleRule.type;
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

    private String firstNonBlank(@Nullable String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String getThingLabel() {
        String label = getThing().getLabel();
        return label != null ? label.trim() : "";
    }

    protected State dateTimeOrUndef(String channel, String... fieldNamesAndValues) {
        return RachioScheduleDateTime.dateTimeOrUndef(thingId, logger, scheduleRuleId, channel, fieldNamesAndValues);
    }
}
