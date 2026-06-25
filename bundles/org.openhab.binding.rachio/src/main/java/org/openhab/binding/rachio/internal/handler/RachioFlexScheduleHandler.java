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

import java.util.OptionalDouble;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
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
 * Handles Rachio FlexScheduleRule Things.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioFlexScheduleHandler extends AbstractRachioThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioFlexScheduleHandler.class);
    protected String flexScheduleRuleId = "";
    private RachioFlexScheduleRuleResponse scheduleRule = new RachioFlexScheduleRuleResponse();
    private boolean scheduleRuleLoaded = false;

    public RachioFlexScheduleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        flexScheduleRuleId = resolveFlexScheduleRuleId();
        logger.debug("Initializing Rachio flex schedule Thing '{}' with flexScheduleRuleId '{}'", thingId,
                flexScheduleRuleId);

        if (flexScheduleRuleId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing Rachio flexScheduleRuleId. Use discovery or configure the Rachio flex schedule rule UUID manually.");
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
            if (loadFlexScheduleRuleForRefreshIfCacheMissing(channel)) {
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
            if (channel.equals(CHANNEL_FLEX_SCHEDULE_START) && command == OnOffType.ON) {
                handler.startScheduleRule(flexScheduleRuleId);
                updateChannel(CHANNEL_FLEX_SCHEDULE_START, OnOffType.OFF);
            } else if (channel.equals(CHANNEL_FLEX_SCHEDULE_SKIP) && command == OnOffType.ON) {
                handler.skipScheduleRule(flexScheduleRuleId);
                updateChannel(CHANNEL_FLEX_SCHEDULE_SKIP, OnOffType.OFF);
            } else if (channel.equals(CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT)) {
                OptionalDouble adjustment = RachioQuantityTypes.dimensionless(command);
                if (adjustment.isPresent()) {
                    double value = adjustment.getAsDouble();
                    handler.setScheduleRuleSeasonalAdjustment(flexScheduleRuleId, value);
                    scheduleRule.seasonalAdjustment = value;
                    updateChannel(CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT,
                            RachioQuantityTypes.fractionOrUndef(value));
                } else {
                    logger.debug("{}: Seasonal adjustment command value is not dimensionless: {}", thingId, command);
                }
            } else if (channel.equals(CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN) && command == OnOffType.ON) {
                handler.skipForwardZoneRun(flexScheduleRuleId);
                updateChannel(CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN, OnOffType.OFF);
            }
        } catch (RachioApiException e) {
            logger.debug("{}: Flex schedule command failed: {}", thingId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized boolean loadFlexScheduleRuleForRefreshIfCacheMissing(String channel) {
        if (scheduleRuleLoaded) {
            logger.trace("{}: Serving flex schedule channel '{}' REFRESH from cached rule '{}'", thingId, channel,
                    flexScheduleRuleId);
            postCachedChannelData(channel);
            return false;
        }
        logger.debug("{}: Flex schedule rule cache is empty; loading rule '{}' for channel '{}' REFRESH", thingId,
                flexScheduleRuleId, channel);
        return refreshFlexScheduleRule();
    }

    @Override
    protected void goOnline() {
        if (refreshFlexScheduleRule()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected synchronized boolean refreshFlexScheduleRule() {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }
        try {
            scheduleRule = loadFlexScheduleRule();
            scheduleRuleLoaded = true;
            logger.debug("{}: Loaded flex schedule rule '{}'", thingId, flexScheduleRuleId);
            postChannelData();
            updateChannel(CHANNEL_FLEX_SCHEDULE_LAST_UPDATE, getTimestamp());
            if (resetLocalThrottleRetry()) {
                logger.debug("{}: Deferred initialization succeeded for flex schedule rule '{}'; Thing is ONLINE.",
                        thingId, flexScheduleRuleId);
            }
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = scheduleInitializationThrottleRetry(
                    "loading flex schedule rule '" + flexScheduleRuleId + "'", this::goOnline, e);
            if (delaySeconds > 0) {
                logger.debug(
                        "{}: Deferring initialization REST request for flex schedule rule '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                        thingId, flexScheduleRuleId, delaySeconds);
            }
            return false;
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load flex schedule rule '{}': {}", thingId, flexScheduleRuleId, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return false;
        }
    }

    private void postCachedChannelData(String channel) {
        postChannelData();
        State cachedState = channelData.get(channel);
        if (cachedState != null) {
            updateState(channel, cachedState);
        }
    }

    protected RachioFlexScheduleRuleResponse loadFlexScheduleRule() throws RachioApiException {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            throw new RachioApiException("Bridge handler is not initialized.");
        }
        return handler.getFlexScheduleRuleForInitialization(flexScheduleRuleId);
    }

    @Override
    protected void postChannelData() {
        updateChannel(CHANNEL_FLEX_SCHEDULE_NAME, stringOrUndef(scheduleRule.name));
        updateChannel(CHANNEL_FLEX_SCHEDULE_ENABLED, scheduleRule.enabled ? OnOffType.ON : OnOffType.OFF);
        updateChannel(CHANNEL_FLEX_SCHEDULE_TYPE, stringOrUndef(scheduleRule.type));
        updateChannel(CHANNEL_FLEX_SCHEDULE_START_TIME, dateTimeOrUndef(CHANNEL_FLEX_SCHEDULE_START_TIME, "startDate",
                scheduleRule.startDate, "startTime", scheduleRule.startTime));
        updateChannel(CHANNEL_FLEX_SCHEDULE_LAST_RUN,
                dateTimeOrUndef(CHANNEL_FLEX_SCHEDULE_LAST_RUN, "lastRun", scheduleRule.lastRun, "lastRunDate",
                        scheduleRule.lastRunDate, "lastRunTime", scheduleRule.lastRunTime, "lastRunAt",
                        scheduleRule.lastRunAt));
        updateChannel(CHANNEL_FLEX_SCHEDULE_NEXT_RUN,
                dateTimeOrUndef(CHANNEL_FLEX_SCHEDULE_NEXT_RUN, "nextRun", scheduleRule.nextRun, "nextRunDate",
                        scheduleRule.nextRunDate, "nextRunTime", scheduleRule.nextRunTime, "nextRunAt",
                        scheduleRule.nextRunAt, "nextScheduledRun", scheduleRule.nextScheduledRun, "nextScheduledStart",
                        scheduleRule.nextScheduledStart));
        updateChannel(CHANNEL_FLEX_SCHEDULE_ZONES, stringOrUndef(scheduleRule.getZoneSummary()));
        updateChannel(CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT,
                RachioQuantityTypes.fractionOrUndef(scheduleRule.seasonalAdjustment));
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    boolean handlesScheduleRule(String scheduleId) {
        return !flexScheduleRuleId.isBlank() && flexScheduleRuleId.equalsIgnoreCase(scheduleId);
    }

    String getScheduleRuleNameForRunSummary() {
        return firstNonBlank(scheduleRule.name, scheduleRule.externalName, getThingLabel());
    }

    String getScheduleRuleNameSourceForRunSummary() {
        if (!scheduleRule.name.isBlank()) {
            return "flex handler";
        }
        if (!scheduleRule.externalName.isBlank()) {
            return "flex handler externalName";
        }
        if (!getThingLabel().isBlank()) {
            return "flex Thing label";
        }
        return "flex handler";
    }

    String getScheduleRuleTypeForRunSummary() {
        return scheduleRule.type.isBlank() ? "FLEX" : scheduleRule.type;
    }

    private String resolveFlexScheduleRuleId() {
        String configuredId = getThingConfigurationString(PROPERTY_FLEX_SCHEDULE_RULE_ID);
        if (!configuredId.isBlank()) {
            logger.debug("{}: Resolved flexScheduleRuleId '{}' from Thing configuration", thingId, configuredId);
            return configuredId;
        }
        String propertyId = getThing().getProperties().get(PROPERTY_FLEX_SCHEDULE_RULE_ID);
        String resolvedId = propertyId != null ? propertyId.trim() : "";
        if (!resolvedId.isBlank()) {
            logger.debug("{}: Resolved flexScheduleRuleId '{}' from Thing properties", thingId, resolvedId);
            return resolvedId;
        }
        String uidFallbackId = getThing().getUID().getId().trim();
        if (!uidFallbackId.isBlank()) {
            logger.debug("{}: Resolved flexScheduleRuleId '{}' from Thing UID fallback", thingId, uidFallbackId);
            return uidFallbackId;
        }
        return "";
    }

    private State stringOrUndef(String value) {
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

    private State dateTimeOrUndef(String channel, String... fieldNamesAndValues) {
        return RachioScheduleDateTime.dateTimeOrUndef(thingId, logger, flexScheduleRuleId, channel,
                fieldNamesAndValues);
    }
}
