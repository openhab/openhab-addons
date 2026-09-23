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
import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;
import static org.openhab.binding.rachio.internal.RachioUtils.i18nText;

import java.util.Objects;
import java.util.OptionalDouble;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common lifecycle, command, and channel handling for fixed and flex schedule Things.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractRachioScheduleHandler<T extends RachioScheduleRuleResponse>
        extends AbstractRachioThingHandler {
    protected static final ScheduleDefinition FIXED_SCHEDULE = new ScheduleDefinition("schedule",
            PROPERTY_SCHEDULE_RULE_ID, "thing-status.rachio.schedule.missing-schedule-rule-id",
            "thing-status.rachio.schedule.command-failed", "thing-status.rachio.schedule.load-failed",
            "schedule handler", "schedule handler externalName", "schedule Thing label", "FIXED",
            CHANNEL_SCHEDULE_START, CHANNEL_SCHEDULE_SKIP, CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT,
            CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN, CHANNEL_SCHEDULE_NAME, CHANNEL_SCHEDULE_ENABLED,
            CHANNEL_SCHEDULE_TYPE, CHANNEL_SCHEDULE_START_TIME, CHANNEL_SCHEDULE_LAST_RUN, CHANNEL_SCHEDULE_NEXT_RUN,
            CHANNEL_SCHEDULE_ZONES, CHANNEL_LAST_UPDATE);
    protected static final ScheduleDefinition FLEX_SCHEDULE = new ScheduleDefinition("flex schedule",
            PROPERTY_FLEX_SCHEDULE_RULE_ID, "thing-status.rachio.flex-schedule.missing-flex-schedule-rule-id",
            "thing-status.rachio.flex-schedule.command-failed", "thing-status.rachio.flex-schedule.load-failed",
            "flex handler", "flex handler externalName", "flex Thing label", "FLEX", CHANNEL_FLEX_SCHEDULE_START,
            CHANNEL_FLEX_SCHEDULE_SKIP, CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT,
            CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN, CHANNEL_FLEX_SCHEDULE_NAME, CHANNEL_FLEX_SCHEDULE_ENABLED,
            CHANNEL_FLEX_SCHEDULE_TYPE, CHANNEL_FLEX_SCHEDULE_START_TIME, CHANNEL_FLEX_SCHEDULE_LAST_RUN,
            CHANNEL_FLEX_SCHEDULE_NEXT_RUN, CHANNEL_FLEX_SCHEDULE_ZONES, CHANNEL_FLEX_SCHEDULE_LAST_UPDATE);

    private final Logger logger = LoggerFactory.getLogger(AbstractRachioScheduleHandler.class);
    private final ScheduleDefinition definition;
    protected T scheduleRule;
    private boolean scheduleRuleLoaded;

    protected AbstractRachioScheduleHandler(Thing thing, ScheduleDefinition definition, T initialRule) {
        super(thing);
        this.definition = definition;
        this.scheduleRule = initialRule;
    }

    @Override
    public void initialize() {
        long generation = beginHandlerInitialization();
        thingId = getThing().getUID().getAsString();
        String resolvedRuleId = resolveRuleId();
        synchronized (this) {
            setRuleId(resolvedRuleId);
            scheduleRule = createRule();
            scheduleRuleLoaded = false;
        }
        logger.debug("Initializing Rachio {} Thing '{}' with rule id '{}'", definition.kind(), thingId, resolvedRuleId);

        if (resolvedRuleId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    i18nText(definition.missingRuleStatus()));
            return;
        }
        if (!initializeCloudHandler()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        registerStatusListener();
        scheduleHandlerTask(generation, this::goOnline);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        if (command == RefreshType.REFRESH) {
            if (loadRuleForRefreshIfCacheMissing(channel)) {
                updateStatus(ThingStatus.ONLINE);
            }
            return;
        }

        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            logger.debug("{}: Cloud handler is not initialized", thingId);
            return;
        }
        String requestedRuleId = getRuleId();

        try {
            if (channel.equals(definition.startChannel()) && command == OnOffType.ON) {
                handler.startScheduleRule(requestedRuleId);
                updateChannel(definition.startChannel(), OnOffType.OFF);
            } else if (channel.equals(definition.skipChannel()) && command == OnOffType.ON) {
                handler.skipScheduleRule(requestedRuleId);
                updateChannel(definition.skipChannel(), OnOffType.OFF);
            } else if (channel.equals(definition.seasonalAdjustmentChannel())) {
                handleSeasonalAdjustment(handler, requestedRuleId, command);
            } else if (channel.equals(definition.skipForwardZoneRunChannel()) && command == OnOffType.ON) {
                handler.skipForwardZoneRun(requestedRuleId);
                updateChannel(definition.skipForwardZoneRunChannel(), OnOffType.OFF);
            }
        } catch (RachioApiException e) {
            String message = exceptionMessage(e);
            logger.debug("{}: {} command failed: {}", thingId, definition.kind(), message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    i18nText(definition.commandFailedStatus(), message));
        }
    }

    private void handleSeasonalAdjustment(RachioBridgeHandler handler, String requestedRuleId, Command command)
            throws RachioApiException {
        OptionalDouble adjustment = RachioQuantityTypes.dimensionless(command);
        if (adjustment.isEmpty()) {
            logger.debug("{}: Seasonal adjustment command value is not dimensionless: {}", thingId, command);
            return;
        }

        double value = adjustment.getAsDouble();
        handler.setScheduleRuleSeasonalAdjustment(requestedRuleId, value);
        boolean currentRule;
        synchronized (this) {
            currentRule = getRuleId().equals(requestedRuleId);
            if (currentRule) {
                scheduleRule.seasonalAdjustment = value;
            }
        }
        if (currentRule) {
            updateChannel(definition.seasonalAdjustmentChannel(), RachioQuantityTypes.fractionOrUndef(value));
        }
    }

    private boolean loadRuleForRefreshIfCacheMissing(String channel) {
        boolean loadMissingCache;
        String ruleId;
        synchronized (this) {
            loadMissingCache = !scheduleRuleLoaded;
            ruleId = getRuleId();
        }
        if (loadMissingCache) {
            logger.debug("{}: {} cache is empty; loading rule '{}' for channel '{}' REFRESH", thingId,
                    definition.kind(), ruleId, channel);
            return refreshRuleForOnline();
        }
        logger.trace("{}: Serving {} channel '{}' REFRESH from cached rule '{}'", thingId, definition.kind(), channel,
                ruleId);
        postCachedChannelData(channel);
        return false;
    }

    @Override
    protected void goOnline() {
        long generation = getHandlerLifecycleGeneration();
        if (refreshRuleForOnline() && isHandlerLifecycleCurrent(generation)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected final boolean refreshRule() {
        long generation = getHandlerLifecycleGeneration();
        if (generation < 0) {
            return false;
        }
        RachioBridgeHandler handler;
        String requestedRuleId;
        synchronized (this) {
            handler = cloudHandler;
            requestedRuleId = getRuleId();
        }
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        try {
            T loadedRule = loadRule(handler, requestedRuleId);
            if (!applyLoadedRule(handler, requestedRuleId, loadedRule, generation)) {
                logger.debug("{}: Ignoring stale {} rule '{}' refresh after bridge or rule id changed", thingId,
                        definition.kind(), requestedRuleId);
                return false;
            }
            logger.debug("{}: Loaded {} rule '{}'", thingId, definition.kind(), requestedRuleId);
            postChannelData();
            updateChannel(definition.lastUpdateChannel(), getTimestamp());
            if (resetLocalThrottleRetry()) {
                logger.debug("{}: Deferred initialization succeeded for {} rule '{}'; Thing is ONLINE.", thingId,
                        definition.kind(), requestedRuleId);
            }
            return true;
        } catch (RachioApiThrottledException e) {
            if (!isHandlerLifecycleCurrent(generation)) {
                return false;
            }
            long delaySeconds = scheduleInitializationThrottleRetry(
                    "loading " + definition.kind() + " rule '" + requestedRuleId + "'", this::goOnline, e);
            if (delaySeconds > 0) {
                logger.debug(
                        "{}: Deferring initialization REST request for {} rule '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                        thingId, definition.kind(), requestedRuleId, delaySeconds);
            }
            return false;
        } catch (RachioApiException e) {
            String message = exceptionMessage(e);
            logger.debug("{}: Unable to load {} rule '{}': {}", thingId, definition.kind(), requestedRuleId, message);
            if (isHandlerLifecycleCurrent(generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        i18nText(definition.loadFailedStatus(), requestedRuleId, message));
            }
            return false;
        }
    }

    private synchronized boolean applyLoadedRule(RachioBridgeHandler handler, String requestedRuleId, T loadedRule,
            long generation) {
        if (!isHandlerLifecycleCurrent(generation) || !Objects.equals(handler, cloudHandler)
                || !getRuleId().equals(requestedRuleId)) {
            return false;
        }
        scheduleRule = loadedRule;
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

    @Override
    protected void postChannelData() {
        ScheduleChannelState state;
        synchronized (this) {
            T rule = scheduleRule;
            String ruleId = getRuleId();
            state = new ScheduleChannelState(stringOrUndef(firstNonBlank(rule.name)),
                    rule.enabled ? OnOffType.ON : OnOffType.OFF, stringOrUndef(firstNonBlank(rule.type)),
                    scheduleDateTime(ruleId, definition.startTimeChannel(), "startDate", rule.startDate, "startTime",
                            rule.startTime),
                    scheduleDateTime(ruleId, definition.lastRunChannel(), "lastRun", rule.lastRun, "lastRunDate",
                            rule.lastRunDate, "lastRunTime", rule.lastRunTime, "lastRunAt", rule.lastRunAt),
                    scheduleDateTime(ruleId, definition.nextRunChannel(), "nextRun", rule.nextRun, "nextRunDate",
                            rule.nextRunDate, "nextRunTime", rule.nextRunTime, "nextRunAt", rule.nextRunAt,
                            "nextScheduledRun", rule.nextScheduledRun, "nextScheduledStart", rule.nextScheduledStart),
                    stringOrUndef(rule.getZoneSummary()), RachioQuantityTypes.fractionOrUndef(rule.seasonalAdjustment));
        }

        updateChannel(definition.nameChannel(), state.name());
        updateChannel(definition.enabledChannel(), state.enabled());
        updateChannel(definition.typeChannel(), state.type());
        updateChannel(definition.startTimeChannel(), state.startTime());
        updateChannel(definition.lastRunChannel(), state.lastRun());
        updateChannel(definition.nextRunChannel(), state.nextRun());
        updateChannel(definition.zonesChannel(), state.zones());
        updateChannel(definition.seasonalAdjustmentChannel(), state.seasonalAdjustment());
    }

    private State scheduleDateTime(String ruleId, String channel, String... fieldNamesAndValues) {
        return RachioScheduleDateTime.dateTimeOrUndef(thingId, logger, ruleId, channel, fieldNamesAndValues);
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    boolean handlesScheduleRule(String scheduleId) {
        String ruleId = getRuleId();
        return !ruleId.isBlank() && ruleId.equalsIgnoreCase(scheduleId);
    }

    synchronized String getScheduleRuleNameForRunSummary() {
        return firstNonBlank(scheduleRule.name, scheduleRule.externalName, getThingLabel());
    }

    synchronized String getScheduleRuleNameSourceForRunSummary() {
        if (!firstNonBlank(scheduleRule.name).isBlank()) {
            return definition.nameSource();
        }
        if (!firstNonBlank(scheduleRule.externalName).isBlank()) {
            return definition.externalNameSource();
        }
        if (!getThingLabel().isBlank()) {
            return definition.thingLabelSource();
        }
        return definition.nameSource();
    }

    synchronized String getScheduleRuleTypeForRunSummary() {
        return firstNonBlank(scheduleRule.type, definition.defaultType());
    }

    protected abstract T createRule();

    protected abstract String getRuleId();

    protected abstract void setRuleId(String ruleId);

    protected abstract T loadRule(RachioBridgeHandler handler, String requestedRuleId) throws RachioApiException;

    protected abstract boolean refreshRuleForOnline();

    private String resolveRuleId() {
        String configuredId = getThingConfigurationString(definition.idProperty());
        if (!configuredId.isBlank()) {
            return configuredId;
        }
        String propertyId = getThing().getProperties().get(definition.idProperty());
        return propertyId != null ? propertyId.trim() : "";
    }

    private String getThingLabel() {
        String label = getThing().getLabel();
        return label != null ? label.trim() : "";
    }

    protected record ScheduleDefinition(String kind, String idProperty, String missingRuleStatus,
            String commandFailedStatus, String loadFailedStatus, String nameSource, String externalNameSource,
            String thingLabelSource, String defaultType, String startChannel, String skipChannel,
            String seasonalAdjustmentChannel, String skipForwardZoneRunChannel, String nameChannel,
            String enabledChannel, String typeChannel, String startTimeChannel, String lastRunChannel,
            String nextRunChannel, String zonesChannel, String lastUpdateChannel) {
    }

    private record ScheduleChannelState(State name, State enabled, State type, State startTime, State lastRun,
            State nextRun, State zones, State seasonalAdjustment) {
    }
}
