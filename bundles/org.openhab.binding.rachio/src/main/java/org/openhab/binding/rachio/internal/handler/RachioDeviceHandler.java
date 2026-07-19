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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioZoneStatus;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudNetworkSettings;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
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
 * The {@link RachioDeviceHandler} is responsible for handling commands, which are
 * sent to one of the device channels.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioDeviceHandler extends AbstractRachioThingHandler {
    private static final long OPTIONAL_ENRICHMENT_REFRESH_INTERVAL_MS = 15 * 60 * 1000L;
    private static final long OPTIONAL_ENRICHMENT_RETRY_AFTER_THROTTLE_MS = 60 * 1000L;
    private static final long WEBHOOK_RUN_SUMMARY_GRACE_MILLIS = 60 * 1000L;
    private static final long MIN_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS = 60;
    private static final long MEDIUM_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS = 120;
    private static final long MAX_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS = 300;
    private final Logger logger = LoggerFactory.getLogger(RachioDeviceHandler.class);

    protected @Nullable RachioDevice dev;
    private long lastOptionalEnrichmentSuccessMillis = 0;
    private long lastOptionalEnrichmentRetryMillis = 0;
    private volatile long webhookRunSummaryProtectUntilMillis = -1;
    private @Nullable ScheduledFuture<?> webhookRegistrationRetryJob;
    private boolean webhookRegistrationPending = false;
    private int webhookRegistrationRetryBackoffStep = 0;
    private @Nullable WebhookRegistrationRetryIntent webhookRegistrationRetryIntent;
    private @Nullable String webhookRegistrationRetryDeviceId;
    private long nextWebhookRegistrationRetryAtMillis = 0;
    private volatile boolean disposed;

    private enum WebhookRegistrationRetryIntent {
        LEGACY_NOTIFICATION_SERVICE,
        MODERN_WEBHOOK_SERVICE
    }

    public RachioDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        disposed = false;
        thingId = getThing().getUID().getAsString();
        logger.debug("Initializing Rachio Thing '{}'.", thingId);

        String errorMessage = "";
        ThingStatusDetail errorStatusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        String configuredDeviceId = getThingConfigurationString(PROPERTY_DEV_ID);
        try {
            if (!initializeCloudHandler()) {
                errorMessage = "Rachio bridge is not initialized";
                return;
            }

            RachioBridgeHandler handler = cloudHandler;
            dev = resolveDevice(handler, configuredDeviceId);
            RachioDevice d = dev;
            if (d == null || handler == null) {
                errorMessage = buildDeviceResolutionError(configuredDeviceId);
                errorStatusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                return;
            }

            thingId = d.name;
            d.setThingHandler(this);
            registerStatusListener();
            registerControllerWebhook(handler, d, RequestPurpose.INITIALIZATION);
            if (configuredDeviceId.isBlank()) {
                logger.debug(
                        "Rachio controller Thing '{}' used legacy UID/property mapping. Configure deviceId='{}' to decouple the openHAB Thing ID from the Rachio controller UUID.",
                        getThing().getUID(), d.id);
            }
            if (!isBridgeOnline()) {
                logger.debug("{}: Rachio Bridge is offline!", thingId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                goOnline();
                logger.debug("{}: Device {} initialized.", thingId, d.name);
                return;
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null) {
                errorMessage = message;
            }
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.warn("{}: ERROR: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, errorStatusDetail, errorMessage);
            }
        }
    }

    private @Nullable RachioDevice resolveDevice(@Nullable RachioBridgeHandler handler, String configuredDeviceId) {
        if (handler == null) {
            return null;
        }

        if (!configuredDeviceId.isBlank()) {
            logger.debug("Resolving Rachio controller Thing '{}' by configured deviceId '{}'", getThing().getUID(),
                    configuredDeviceId);
            return handler.getDevByConfiguredDeviceId(getThing(), configuredDeviceId);
        }

        logger.debug("Rachio controller Thing '{}' has no configured deviceId; trying legacy property/UID fallback",
                getThing().getUID());
        return handler.getDevByThing(getThing());
    }

    @Nullable
    String getBoundControllerId() {
        RachioDevice device = dev;
        return device != null ? device.id : null;
    }

    boolean rebindToCurrentModel(RachioDevice currentDevice, String reason) {
        RachioDevice previousDevice = dev;
        boolean changed = !Objects.equals(previousDevice, currentDevice)
                || !Objects.equals(currentDevice.getThingHandler(), this);
        currentDevice.setThingHandler(this);
        dev = currentDevice;
        thingId = currentDevice.name;
        if (changed) {
            logger.debug("{}: Rebound controller Thing '{}' to current bridge model for controllerId='{}' after {}",
                    thingId, getThing().getUID(), currentDevice.id, reason);
        }
        return true;
    }

    private boolean rebindToCurrentBridgeModel(String reason) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null && !initializeCloudHandler()) {
            return false;
        }
        handler = cloudHandler;
        if (handler == null) {
            return false;
        }
        String configuredDeviceId = "";
        try {
            configuredDeviceId = getThingConfigurationString(PROPERTY_DEV_ID);
        } catch (RuntimeException e) {
            logger.trace("{}: Unable to read configured Rachio deviceId while rebinding controller model", thingId);
        }
        RachioDevice currentDevice = resolveDevice(handler, configuredDeviceId);
        if (currentDevice == null) {
            return false;
        }
        return rebindToCurrentModel(currentDevice, reason);
    }

    private String buildDeviceResolutionError(String configuredDeviceId) {
        if (configuredDeviceId.isBlank()) {
            return "Missing Rachio deviceId. Add the controller via Inbox discovery or configure the Rachio API controller UUID manually.";
        }
        return "Configured Rachio deviceId was not found in the account: '" + configuredDeviceId + "'.";
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        logger.debug("{}: Handle Command {} for channel {}", thingId, command, channel);

        RachioBridgeHandler handler = cloudHandler;
        RachioDevice d = dev;
        if (handler == null || d == null) {
            logger.debug("{}: Cloud handler or device not initialized!", thingId);
            return;
        }

        String errorMessage = "";
        try {
            if (command == RefreshType.REFRESH) {
                if (handleRefreshCommand(channel)) {
                    logger.debug("{}: Return cached data for channel {}: {}", thingId, channel,
                            channelData.get(channel));
                }
                return;
            }

            if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_ACTIVE)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.OFF) {
                        logger.debug("{}: Pause device {} (disable watering, schedules etc.)", thingId, d.name);
                        handler.disableDevice(d.id);
                    } else {
                        logger.debug("{}: Resume device {} (enable watering, schedules etc.)", thingId, d.name);
                        handler.enableDevice(d.id);
                    }
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUNTIME)) {
                RachioQuantityTypes.durationSeconds(command).ifPresentOrElse(runtime -> {
                    logger.debug("Default Runtime for zones set to {} sec", runtime);
                    d.setRunTime(runtime);
                }, () -> logger.debug("{}: Run time command value is not a duration: {}", thingId, command));
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_PAUSE_TIME)) {
                RachioQuantityTypes.durationSeconds(command).ifPresentOrElse(duration -> {
                    d.setPauseDuration(duration);
                    logger.debug("Pause duration for active zone runs set to {} sec", d.getPauseDuration());
                    updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSE_TIME,
                            RachioQuantityTypes.seconds(d.getPauseDuration()));
                }, () -> logger.debug("{}: Pause time command value is not a duration: {}", thingId, command));
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUN_ZONES)) {
                if (command instanceof StringType) {
                    logger.debug("Run multiple zones: '{}' ('' = ALL)", command.toString());
                    d.setRunZones(command.toString());
                } else {
                    logger.debug("Command value is no StringType: {}", command);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUN)) {
                if (command == OnOffType.ON) {
                    int defaultRuntime = handler.getDefaultRuntime();
                    int controllerRuntime = d.getRunTime();
                    int effectiveRuntime = d.getMultiZoneRunTime(defaultRuntime);
                    logger.debug(
                            "Starting multiple zones '{}' with controller runtime {} sec (fallback default {} sec, effective {} sec)",
                            d.getRunZones(), controllerRuntime, defaultRuntime, effectiveRuntime);
                    handler.runMultipleZones(d.getAllRunZonesJson(defaultRuntime));
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_STOP)) {
                if (command == OnOffType.ON) {
                    logger.info("STOP watering for device '{}'", d.name);
                    handler.stopWatering(d.id);
                    updateState(RachioBindingConstants.CHANNEL_DEVICE_STOP, OnOffType.OFF);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RAIN_DELAY)) {
                OptionalInt delaySeconds = RachioQuantityTypes.durationSeconds(command);
                if (delaySeconds.isPresent()) {
                    int duration = delaySeconds.getAsInt();
                    logger.info("Start rain delay cycle for {} sec", duration);
                    d.setRainDelayTime(duration);
                    handler.startRainDelay(d.id, duration);
                } else {
                    logger.debug("{}: Rain delay command value is not a duration: {}", thingId, command);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_PAUSED)) {
                if (command == OnOffType.ON) {
                    logger.info("Pause active zone run for device '{}' for {} sec", d.name, d.getPauseDuration());
                    handler.pauseZoneRun(d.id, d.getPauseDuration());
                    d.setPaused(true);
                    updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSED, OnOffType.ON);
                } else if (command == OnOffType.OFF) {
                    logger.info("Resume active zone run for device '{}'", d.name);
                    handler.resumeZoneRun(d.id);
                    d.setPaused(false);
                    updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSED, OnOffType.OFF);
                }
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            errorMessage = message != null ? message : "";
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("ERROR: {}", errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    @Override
    protected void postChannelData() {
        postChannelData(true);
    }

    private void postChannelData(boolean includeForecast) {
        RachioDevice d = dev;
        if (d != null) {
            logger.debug("Updating device status");
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_NAME, new StringType(d.getThingName()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_ONLINE, d.getOnline());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_ACTIVE, d.getEnabled());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSED, d.getPaused());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSE_TIME,
                    RachioQuantityTypes.seconds(d.getPauseDuration()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_SLEEP_MODE, d.getSleepMode());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RUN_ZONES, new StringType(d.getRunZones()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RUNTIME, RachioQuantityTypes.seconds(d.getRunTime()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RAIN_DELAY, RachioQuantityTypes.seconds(d.rainDelay));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RAIN_SENSOR_TRIPPED,
                    d.rainSensorTripped ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER,
                    d.activeZoneNumber > 0 ? new DecimalType(d.activeZoneNumber) : new DecimalType(0));
            updateChannel(CHANNEL_DEVICE_ACTIVE_ZONE_NAME, optionalStringState(d.activeZoneName));
            updateChannel(CHANNEL_DEVICE_ACTIVE_ZONE_ID, optionalStringState(d.activeZoneId));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_ID, optionalStringState(d.currentScheduleId));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_NAME, optionalStringState(d.currentScheduleName));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_TYPE, optionalStringState(d.currentScheduleType));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_START, optionalDateTimeState(d.currentScheduleStartTime));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_END, optionalDateTimeState(d.currentScheduleEndTime));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_DURATION, RachioQuantityTypes.seconds(d.currentScheduleDuration));
            updateChannel(CHANNEL_CURRENT_SCHEDULE_RUNNING, d.currentScheduleRunning ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_LAST_API_EVENT_TYPE, stringOrUndef(d.lastApiEventType));
            updateChannel(CHANNEL_LAST_API_EVENT_TIME, dateTimeOrUndef(d.lastApiEventTime));
            updateChannel(CHANNEL_LAST_API_EVENT_SUMMARY, stringOrUndef(d.lastApiEventSummary));
            if (includeForecast) {
                RachioBridgeHandler handler = cloudHandler;
                updateForecastChannels(d, handler != null ? handler.getForecastUnits() : DEFAULT_FORECAST_UNITS);
            }
            updateChannel(CHANNEL_LAST_SKIP_TYPE, optionalStringState(d.lastSkipType));
            updateChannel(CHANNEL_LAST_SKIP_SCHEDULE_ID, optionalStringState(d.lastSkipScheduleId));
            updateChannel(CHANNEL_LAST_SKIP_START, optionalDateTimeState(d.lastSkipStartTime));
            updateChannel(CHANNEL_LAST_SKIP_REASON, optionalStringState(d.lastSkipReason));
            updateChannel(RachioBindingConstants.CHANNEL_LAST_EVENT, new StringType(d.getEvent()));
            DateTimeType ts = d.getEventTime();
            updateChannel(RachioBindingConstants.CHANNEL_LAST_EVENTTS, ts != null ? ts : UnDefType.UNDEF);
        }
    }

    private void updateForecastChannels(RachioDevice d, String forecastUnits) {
        updateChannel(CHANNEL_FORECAST_SUMMARY, stringOrUndef(d.forecastSummary));
        updateChannel(CHANNEL_FORECAST_TODAY_HIGH,
                RachioQuantityTypes.temperatureOrUndef(d.forecastTodayHigh, forecastUnits));
        updateChannel(CHANNEL_FORECAST_TODAY_LOW,
                RachioQuantityTypes.temperatureOrUndef(d.forecastTodayLow, forecastUnits));
        updateChannel(CHANNEL_FORECAST_PRECIPITATION,
                RachioQuantityTypes.precipitationOrUndef(d.forecastPrecipitation, forecastUnits));
        updateChannel(CHANNEL_FORECAST_PRECIPITATION_PROBABILITY,
                RachioQuantityTypes.fractionOrUndef(d.forecastPrecipitationProbability));
        updateChannel(CHANNEL_FORECAST_WIND, RachioQuantityTypes.windSpeedOrUndef(d.forecastWind, forecastUnits));
        updateChannel(CHANNEL_FORECAST_UPDATED, dateTimeOrUndef(d.forecastUpdated));
    }

    @Override
    public void onConfigurationUpdated() {
        try {
            rebindToCurrentBridgeModel("bridge configuration update");
            RachioBridgeHandler handler = cloudHandler;
            RachioDevice d = dev;
            if (handler != null && d != null) {
                clearPendingWebhookRegistrationIfObsolete(handler, d.id, "configuration updated");
                registerControllerWebhook(handler, d, RequestPurpose.USER_COMMAND);
            }
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to renew webhook registration, cause={}", thingId, e.getClass().getSimpleName());
        }
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        RachioDevice d = dev;
        if (updatedDev != null && d != null && d.id.equals(updatedDev.id)) {
            logger.debug("Update for device '{}' received.", d.id);
            d.update(updatedDev);
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            postChannelData();
            updateThingStatusAfterSuccessfulCommunication();
            return true;
        }
        return false;
    }

    public void refreshSmartIrrigationReadExtensions(boolean force) {
        refreshSmartIrrigationReadExtensions(force, RequestPurpose.BACKGROUND_REFRESH);
    }

    public void refreshSmartIrrigationReadExtensions(boolean force, RequestPurpose currentSchedulePurpose) {
        refreshSmartIrrigationReadExtensions(force, currentSchedulePurpose, RachioBridgeHandler.RefreshReason.MANUAL);
    }

    public void refreshSmartIrrigationReadExtensions(boolean force, RequestPurpose currentSchedulePurpose,
            RachioBridgeHandler.RefreshReason refreshReason) {
        RachioBridgeHandler handler = cloudHandler;
        RachioDevice d = dev;
        if (handler == null || d == null) {
            return;
        }

        try {
            RachioCurrentScheduleResponse currentSchedule = handler.getCurrentSchedule(d.id, currentSchedulePurpose);
            long now = currentTimeMillis();
            if (currentSchedule.isRunning() || !shouldPreserveWebhookRunSummary(d, refreshReason, now)) {
                d.applyCurrentSchedule(currentSchedule);
                if (d.currentScheduleRunning) {
                    applyResolvedScheduleMetadata(d, d.currentScheduleId);
                }
                if (!currentSchedule.isRunning()) {
                    d.clearActiveZone();
                    clearWebhookRunSummaryProtection();
                }
            } else {
                logger.debug(
                        "{}: Ignoring non-running REST current schedule for controller '{}' during {} because webhook-derived active run state is protected until {}",
                        thingId, d.id, refreshReason,
                        Instant.ofEpochMilli(webhookRunSummaryProtectUntilMillis).toString());
            }
            logger.debug("{}: Loaded current schedule for controller '{}': running={}, id='{}'", thingId, d.id,
                    d.currentScheduleRunning, d.currentScheduleId);
        } catch (RachioApiThrottledException e) {
            logger.debug(
                    "{}: Essential current schedule refresh for controller '{}' was deferred by the local API budget guard: {}",
                    thingId, d.id, e.getMessage());
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load current schedule for controller '{}': {}; retaining last known values",
                    thingId, d.id, e.getMessage());
        }

        refreshOptionalEnrichments(handler, d, force);
        postChannelData(false);
    }

    private void refreshOptionalEnrichments(RachioBridgeHandler handler, RachioDevice d, boolean force) {
        long now = currentTimeMillis();
        if (!force) {
            if (lastOptionalEnrichmentSuccessMillis > 0
                    && (now - lastOptionalEnrichmentSuccessMillis) < OPTIONAL_ENRICHMENT_REFRESH_INTERVAL_MS) {
                logger.trace("{}: Optional Smart Irrigation enrichment skipped; last successful refresh was {} ms ago",
                        thingId, now - lastOptionalEnrichmentSuccessMillis);
                return;
            }
            if (lastOptionalEnrichmentRetryMillis > 0
                    && (now - lastOptionalEnrichmentRetryMillis) < OPTIONAL_ENRICHMENT_RETRY_AFTER_THROTTLE_MS) {
                logger.trace("{}: Optional Smart Irrigation enrichment skipped; last retryable attempt was {} ms ago",
                        thingId, now - lastOptionalEnrichmentRetryMillis);
                return;
            }
        }
        boolean optionalEnrichmentRetrySoon = false;
        boolean optionalEnrichmentLoaded = false;

        try {
            String forecastUnits = handler.getForecastUnits();
            RachioForecastResponse forecast = handler.getDeviceForecast(d.id, forecastUnits);
            Instant retrievedAt = Instant.ofEpochMilli(now);
            boolean usefulForecast = d.applyForecast(forecast, forecastUnits, retrievedAt.toString(), retrievedAt);
            logger.debug("{}: Forecast response for controller '{}' using {} units parsed: {}", thingId, d.id,
                    forecastUnits, forecast.parsedFieldSummary());
            logger.trace("{}: Forecast response shape for controller '{}': {}", thingId, d.id, forecast.shapeSummary());
            if (usefulForecast) {
                optionalEnrichmentLoaded = true;
                updateForecastChannels(d, forecastUnits);
                logger.debug("{}: Loaded useful forecast for controller '{}' using {} units", thingId, d.id,
                        forecastUnits);
                logger.debug("{}: Applied forecast channel updates for controller '{}': {}", thingId, d.id,
                        forecast.parsedFieldSummary());
            } else {
                optionalEnrichmentRetrySoon = true;
                logger.debug(
                        "{}: Forecast response loaded but no usable forecast fields were parsed; retaining previous values",
                        thingId);
            }
        } catch (RachioApiThrottledException e) {
            optionalEnrichmentRetrySoon = true;
            logger.debug(
                    "{}: Skipping forecast refresh for controller '{}' because the local API budget guard is active: {}",
                    thingId, d.id, e.getMessage());
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to load forecast for controller '{}': {}; retaining last known values", thingId,
                    d.id, e.getMessage());
        }

        int lookbackHours = handler.getEventHistoryLookbackHours();
        if (lookbackHours > 0) {
            try {
                long endTime = System.currentTimeMillis();
                long startTime = endTime - (lookbackHours * 60L * 60L * 1000L);
                RachioDeviceEventListResponse events = handler.getDeviceEvents(d.id, startTime, endTime);
                d.applyApiEvent(events.getLatestEvent());
                optionalEnrichmentLoaded = true;
                logger.debug("{}: Loaded {} recent controller events over {} hours", thingId, events.events.size(),
                        lookbackHours);
            } catch (RachioApiThrottledException e) {
                optionalEnrichmentRetrySoon = true;
                logger.debug(
                        "{}: Skipping recent event refresh for controller '{}' because the local API budget guard is active: {}",
                        thingId, d.id, e.getMessage());
            } catch (RachioApiException e) {
                logger.debug("{}: Unable to load recent events for controller '{}': {}; retaining last known values",
                        thingId, d.id, e.getMessage());
            }
        } else {
            d.applyApiEvent(null);
            logger.trace("{}: Event history polling is disabled", thingId);
        }
        if (optionalEnrichmentLoaded && !optionalEnrichmentRetrySoon) {
            lastOptionalEnrichmentSuccessMillis = now;
        }
        if (optionalEnrichmentRetrySoon) {
            lastOptionalEnrichmentRetryMillis = now;
            logger.debug(
                    "{}: Optional enrichments need a short retry independently from core controller and current schedule polling; retry is allowed after {} ms",
                    thingId, OPTIONAL_ENRICHMENT_RETRY_AFTER_THROTTLE_MS);
        } else if (optionalEnrichmentLoaded) {
            lastOptionalEnrichmentRetryMillis = 0;
        }
    }

    @Override
    public void shutdown() {
        disposed = true;
        clearPendingWebhookRegistration();
        RachioDevice d = dev;
        if (d != null) {
            d.setStatus("OFFLINE");
        }
        super.shutdown();
    }

    @Override
    public void dispose() {
        disposed = true;
        clearPendingWebhookRegistration();
        super.dispose();
    }

    @Override
    protected void goOnline() {
        updateProperties();
        postChannelData();
        refreshSmartIrrigationReadExtensions(true);
        RachioDevice d = dev;
        if (d != null) {
            updateThingStatusAfterSuccessfulCommunication();
        }
    }

    @Override
    protected void onBridgeOnline() {
        if (!rebindToCurrentBridgeModel("bridge online")) {
            logger.debug("Bridge is ONLINE; retrying controller initialization for '{}'", getThing().getUID());
            initialize();
        } else {
            retryDeferredWebhookRegistrationIfDue();
            goOnline();
        }
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        boolean update = true;
        RachioDevice d = dev;
        if (d == null) {
            return false;
        }

        try {
            String etype = event.type;
            RachioZone zone = null;
            int zoneNumber = event.getZoneNumberForWebhookHandling();
            String zoneState = event.getZoneRunStateForWebhookHandling();
            boolean directZoneRunEvent = "ZONE_STATUS".equals(etype) && isDirectZoneRunEvent(zoneState);
            if ("ZONE_STATUS".equals(etype)) {
                boolean zoneIdPresent = !event.zoneId.isBlank();
                RachioZone zoneById = zoneIdPresent ? d.getZoneById(event.zoneId) : null;
                RachioZone zoneByNumber = zoneNumber > 0 ? d.getZoneByNumber(zoneNumber) : null;
                if (zoneIdPresent && zoneNumber > 0 && (zoneById == null || zoneByNumber == null
                        || !zoneById.id.equalsIgnoreCase(zoneByNumber.id))) {
                    logger.debug(
                            "{}: Zone webhook event {}.{} has ambiguous zone payload (zoneIdPresent=true, zoneNumber={}); reconciliation remains active",
                            thingId, event.type, zoneState, zoneNumber);
                    return false;
                }
                zone = zoneById != null ? zoneById : zoneByNumber;
                if (directZoneRunEvent && zone == null) {
                    logger.debug(
                            "{}: Zone webhook event {}.{} could not resolve a matching zone (zoneIdPresent={}, zoneNumber={}); reconciliation remains active",
                            thingId, event.type, zoneState, zoneIdPresent, zoneNumber);
                    return false;
                }
            } else if ("ZONE_DELTA".equals(event.subType)) {
                zone = d.getZoneById(event.zoneId);
            }

            boolean zoneUpdated = false;
            if (zone != null) {
                enrichZoneEventFromResolvedZone(event, zone);
                RachioZoneHandler handler = resolveZoneHandler(d, zone, event, zoneState, zoneNumber);
                if (handler != null) {
                    zoneUpdated = handler.webhookEvent(event);
                    if (zoneUpdated && directZoneRunEvent) {
                        logger.debug("{}: Direct zone webhook handling succeeded for {}.{} on zone {}", thingId,
                                event.type, zoneState, zoneNumber);
                    }
                } else if (directZoneRunEvent) {
                    logger.debug(
                            "{}: Zone webhook event {}.{} matched zone {} but its Thing handler is not initialized; reconciliation remains active",
                            thingId, event.type, zoneState, zoneNumber);
                    return false;
                }
            }
            if (directZoneRunEvent && !zoneUpdated) {
                logger.debug(
                        "{}: Zone webhook event {}.{} was not handled by the matching zone Thing; reconciliation remains active",
                        thingId, event.type, zoneState);
                return false;
            }

            String evt = event.subType.isEmpty() ? event.type : event.subType;
            d.setEvent(evt, getTimestamp());
            d.applyWebhookEvent(event);

            boolean devicePauseChanged = false;
            boolean activeZoneChanged = false;
            if ("ZONE_STATUS".equals(etype)) {
                if ("ZONE_STARTED".equals(zoneState) || "ZONE_STOPPED".equals(zoneState)
                        || "ZONE_COMPLETED".equals(zoneState)) {
                    if ("ZONE_STARTED".equals(zoneState) && zone == null && zoneNumber > 0) {
                        logger.debug("{}: Active zone run started for zone number {}, but no matching zone was found",
                                thingId, zoneNumber);
                    }
                    activeZoneChanged = d.applyActiveZoneEvent(zoneState, zoneNumber, zone);
                    if ("ZONE_STARTED".equals(zoneState)) {
                        applyZoneRunStartedSummary(d, event);
                        armWebhookRunSummaryProtection(event, true);
                    } else if (("ZONE_COMPLETED".equals(zoneState) || "ZONE_STOPPED".equals(zoneState))
                            && activeZoneChanged) {
                        d.clearCurrentSchedule();
                        clearWebhookRunSummaryProtection();
                    }
                }
                if ("ZONE_CYCLING".equals(zoneState)) {
                    if (!d.paused) {
                        logger.info("{}: Device detected external pause for zone {}.", thingId,
                                zone != null ? zone.name : event.zoneName);
                        d.setPaused(true);
                        devicePauseChanged = true;
                    }
                } else if ("ZONE_STARTED".equals(zoneState) || "ZONE_STOPPED".equals(zoneState)
                        || "ZONE_COMPLETED".equals(zoneState) || "ZONE_CYCLING_COMPLETED".equals(zoneState)) {
                    if (d.paused) {
                        logger.info("{}: Device detected external resume for zone {}.", thingId,
                                zone != null ? zone.name : event.zoneName);
                        d.setPaused(false);
                        devicePauseChanged = true;
                    }
                }
            }

            if ("ZONE_STATUS".equals(etype)) {
                update = zoneUpdated || devicePauseChanged || activeZoneChanged;
            } else if (event.subType.equals("RAIN_DELAY_ON")) {
                handleRainDelayOnEvent(event, d);
            } else if (event.subType.equals("RAIN_DELAY_OFF")) {
                logger.info("{}: Device reported Rain Delay OFF.", thingId);
                d.setRainDelayTime(0);
            } else if (isDeviceStatusEvent(etype)) {
                // sub types:
                // COLD_REBOOT, ONLINE, OFFLINE, OFFLINE_NOTIFICATION, SLEEP_MODE_ON, SLEEP_MODE_OFF, BROWNOUT_VALVE
                // RAIN_SENSOR_DETECTION_ON, RAIN_SENSOR_DETECTION_OFF, RAIN_DELAY_ON, RAIN_DELAY_OFF
                logger.debug("Device {} ('{}') changed to status '{}'.", d.name, d.id, event.subType);
                if (event.subType.equals("COLD_REBOOT")) {
                    RachioCloudNetworkSettings eventNetwork = event.network;
                    if (eventNetwork != null) {
                        d.setNetwork(eventNetwork);
                    }
                    RachioCloudNetworkSettings network = d.network;
                    if (network != null) {
                        String networkDetails = String.format("ip=%s/%s, gw=%s, dns=%s/%s, wifi rssi=%s", network.ip,
                                network.nm, network.gw, network.dns1, network.dns2, network.rssi);
                        logger.info("{}: Device {} was restarted, {}.", thingId, d.name, networkDetails);
                    } else {
                        logger.info("{}: Device {} was restarted (network information unavailable).", thingId, d.name);
                    }
                } else if (event.subType.equals("ONLINE")) {
                    logger.info("Rachio controller '{}' reports ONLINE.", d.id);
                    d.setStatus(event.subType);
                } else if (event.subType.equals("OFFLINE") || event.subType.equals("OFFLINE_NOTIFICATION")) {
                    logger.info("Rachio controller '{}' reports OFFLINE.", d.id);
                    d.setStatus("OFFLINE");
                } else if (event.subType.equals("SLEEP_MODE_ON")) {
                    logger.info("{}: Device switch to sleep mode.", thingId);
                    d.setSleepMode(event.subType);
                } else if (event.subType.equals("SLEEP_MODE_OFF")) {
                    logger.info("{}: Device was resumed (exit from sleep mode).", thingId);
                    d.setSleepMode(event.subType);
                } else if (event.subType.equals("RAIN_SENSOR_DETECTION_ON")) {
                    logger.info("{}: Device reported Rain Sensor ON.", thingId);
                    d.rainSensorTripped = true;
                } else if (event.subType.equals("RAIN_SENSOR_DETECTION_OFF")) {
                    logger.info("{}: Device reported Rain Sensor OFF.", thingId);
                    d.rainSensorTripped = false;
                } else if ("RAIN_SENSOR_DETECTION".equals(etype)) {
                    logger.debug(
                            "{}: Legacy rain sensor webhook did not include ON/OFF details; reconciliation refresh remains active",
                            thingId);
                } else if (event.subType.equals("BROWNOUT_VALVE")) {
                    logger.debug(
                            "{}: Legacy brownout valve webhook is recorded as last event; reconciliation refresh remains active",
                            thingId);
                } else {
                    update = false; // details missing
                }
            } else if (event.type.equals("SCHEDULE_STATUS")) {
                logger.info("{}: Status {} for schedule {}: {} (start={}, end={}, duration={}min)", thingId,
                        event.subType, event.scheduleName, event.summary, event.startTime, event.endTime,
                        event.durationInMinutes);
                if (event.subType.equals("SCHEDULE_STARTED")) {
                    applyScheduleStartedSummary(d, event);
                    armWebhookRunSummaryProtection(event, false);
                } else if (event.subType.equals("SCHEDULE_STOPPED") || event.subType.equals("SCHEDULE_COMPLETED")) {
                    d.clearCurrentSchedule();
                    d.clearActiveZone();
                    clearWebhookRunSummaryProtection();
                }
                if (event.subType.startsWith("WEATHER_INTELLIGENCE")) {
                    d.applySkipEvent(event.eventType.isBlank() ? event.subType : event.eventType, event.scheduleId,
                            event.startTime, event.summary);
                }
                updateChannel(CHANNEL_SCHED_NAME, optionalStringState(event.scheduleName));
                updateChannel(CHANNEL_SCHED_INFO, optionalStringState(event.summary));
                updateChannel(CHANNEL_SCHED_START, optionalDateTimeState(event.startTime));
                updateChannel(CHANNEL_SCHED_END, optionalDateTimeState(event.endTime));
            } else if (isLegacyRefreshOnlyEvent(etype, event.subType)) {
                logger.debug(
                        "{}: Legacy webhook event {}.{} is intentionally handled by reconciliation refresh; no direct state update was applied",
                        thingId, event.type, event.subType);
            } else {
                update = false; // unknown event
            }

            if (update) {
                postChannelData();
                updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                updateThingStatusAfterSuccessfulCommunication();
                return true;
            }
            logger.debug("{}: Unhandled event {}.{} for device {} ({}): {}", thingId, event.type, event.subType, d.name,
                    d.id, event.summary);
            return false;
        } catch (RuntimeException e) {
            logger.debug("{}: Unable to process event {}.{} - {}", thingId, event.type, event.subType, event.summary,
                    e);
            return false;
        }
    }

    boolean handlesController(String controllerId) {
        RachioDevice d = dev;
        return d != null && !controllerId.isBlank() && d.id.equalsIgnoreCase(controllerId);
    }

    private boolean isDirectZoneRunEvent(String zoneState) {
        return "ZONE_STARTED".equals(zoneState) || "ZONE_STOPPED".equals(zoneState)
                || "ZONE_COMPLETED".equals(zoneState);
    }

    private boolean isDeviceStatusEvent(String eventType) {
        return "DEVICE_STATUS".equals(eventType) || "RAIN_SENSOR_DETECTION".equals(eventType);
    }

    private boolean isLegacyRefreshOnlyEvent(String eventType, String eventSubType) {
        return isLegacyRefreshOnlyType(eventType) || isLegacyRefreshOnlyType(eventSubType);
    }

    private boolean isLegacyRefreshOnlyType(String eventType) {
        return "DELTA".equals(eventType) || "DEVICE_DELTA".equals(eventType) || "SCHEDULE_DELTA".equals(eventType)
                || "ZONE_DELTA".equals(eventType) || "WATER_BUDGET".equals(eventType);
    }

    private void applyZoneRunStartedSummary(RachioDevice device, RachioEventGsonDTO event) {
        RachioZoneStatus runStatus = event.zoneRunStatus;
        RachioWebhookPayload payload = event.payload;
        String scheduleType = firstNonBlank(event.scheduleType, runStatus != null ? runStatus.scheduleType : "",
                payload != null ? payload.runType : "");
        String scheduleName = firstNonBlank(event.scheduleName, payload != null ? payload.scheduleName : "");
        String scheduleId = firstNonBlank(event.scheduleId, payload != null ? payload.scheduleId : "",
                device.currentScheduleId);
        ScheduleMetadata scheduleMetadata = resolveScheduleMetadata(device, scheduleId);

        device.currentScheduleRunning = true;
        device.currentScheduleId = scheduleId;
        if (device.currentScheduleName.isBlank()) {
            if (!scheduleName.isBlank()) {
                device.currentScheduleName = scheduleName;
            } else if (!scheduleMetadata.name().isBlank()) {
                device.currentScheduleName = scheduleMetadata.name();
            } else if ("MANUAL".equalsIgnoreCase(scheduleType) || "QUICK_RUN".equalsIgnoreCase(scheduleType)) {
                device.currentScheduleName = "Quick Run";
            }
        }
        device.currentScheduleType = firstNonBlank(scheduleType, scheduleMetadata.type(), device.currentScheduleType);

        int duration = positiveValue(runStatus != null ? runStatus.duration : null, event.duration,
                payload != null ? payload.getDurationSeconds() : 0);
        String startTime = firstNonBlank(event.startTime, runStatus != null ? runStatus.startTime : "",
                payload != null ? payload.startTime : "", payload != null ? payload.plannedRunStartTime : "",
                device.currentScheduleStartTime, parseableTimestamp(event.timestamp));
        String endTime = firstNonBlank(event.endTime, runStatus != null ? runStatus.endTime : "",
                payload != null ? payload.endTime : "", calculateEndTime(startTime, duration),
                device.currentScheduleEndTime);
        device.currentScheduleStartTime = startTime;
        device.currentScheduleEndTime = endTime;
        if (duration > 0) {
            device.currentScheduleDuration = duration;
        }
    }

    private void applyScheduleStartedSummary(RachioDevice device, RachioEventGsonDTO event) {
        RachioWebhookPayload payload = event.payload;
        String scheduleId = firstNonBlank(event.scheduleId, payload != null ? payload.scheduleId : "",
                device.currentScheduleId);
        ScheduleMetadata scheduleMetadata = resolveScheduleMetadata(device, scheduleId);
        int duration = positiveValue(event.duration, payload != null ? payload.getDurationSeconds() : 0);
        String startTime = firstNonBlank(event.startTime, payload != null ? payload.plannedRunStartTime : "",
                payload != null ? payload.startTime : "", parseableTimestamp(event.timestamp),
                device.currentScheduleStartTime);
        String endTime = firstNonBlank(event.endTime, payload != null ? payload.endTime : "",
                calculateEndTime(startTime, duration), device.currentScheduleEndTime);

        device.currentScheduleId = scheduleId;
        device.currentScheduleName = firstNonBlank(event.scheduleName, payload != null ? payload.scheduleName : "",
                scheduleMetadata.name(), device.currentScheduleName);
        device.currentScheduleType = firstNonBlank(event.scheduleType, payload != null ? payload.runType : "",
                scheduleMetadata.type(), device.currentScheduleType);
        device.currentScheduleStartTime = startTime;
        device.currentScheduleEndTime = endTime;
        if (duration > 0) {
            device.currentScheduleDuration = duration;
        }
        device.currentScheduleRunning = true;
    }

    private void applyResolvedScheduleMetadata(RachioDevice device, String scheduleId) {
        ScheduleMetadata scheduleMetadata = resolveScheduleMetadata(device, scheduleId);
        if (device.currentScheduleName.isBlank() && !scheduleMetadata.name().isBlank()) {
            device.currentScheduleName = scheduleMetadata.name();
        }
        if (device.currentScheduleType.isBlank() && !scheduleMetadata.type().isBlank()) {
            device.currentScheduleType = scheduleMetadata.type();
        }
    }

    private ScheduleMetadata resolveScheduleMetadata(RachioDevice device, String scheduleId) {
        if (scheduleId.isBlank()) {
            return ScheduleMetadata.EMPTY;
        }

        String deviceName = device.getScheduleRuleName(scheduleId);
        String deviceType = device.getScheduleRuleType(scheduleId);
        if (!deviceName.isBlank()) {
            return logScheduleMetadata(scheduleId, new ScheduleMetadata(deviceName, deviceType, "device model"));
        }

        RachioBridgeHandler handler = cloudHandler;
        if (handler != null) {
            Set<RachioStatusListener> listeners = Objects.requireNonNullElse(handler.rachioStatusListeners, Set.of());
            for (RachioStatusListener listener : listeners) {
                if (listener instanceof RachioScheduleHandler scheduleHandler
                        && scheduleHandler.handlesScheduleRule(scheduleId)) {
                    String type = firstNonBlank(deviceType, scheduleHandler.getScheduleRuleTypeForRunSummary());
                    return logScheduleMetadata(scheduleId,
                            new ScheduleMetadata(scheduleHandler.getScheduleRuleNameForRunSummary(), type,
                                    scheduleHandler.getScheduleRuleNameSourceForRunSummary()));
                }
                if (listener instanceof RachioFlexScheduleHandler flexScheduleHandler
                        && flexScheduleHandler.handlesScheduleRule(scheduleId)) {
                    String type = firstNonBlank(deviceType, flexScheduleHandler.getScheduleRuleTypeForRunSummary());
                    return logScheduleMetadata(scheduleId,
                            new ScheduleMetadata(flexScheduleHandler.getScheduleRuleNameForRunSummary(), type,
                                    flexScheduleHandler.getScheduleRuleNameSourceForRunSummary()));
                }
            }
        }
        if (!deviceType.isBlank()) {
            return logScheduleMetadata(scheduleId, new ScheduleMetadata("", deviceType, "device model"));
        }
        logger.trace("{}: No active schedule metadata found for scheduleId '{}'", thingId, scheduleId);
        return ScheduleMetadata.EMPTY;
    }

    private ScheduleMetadata logScheduleMetadata(String scheduleId, ScheduleMetadata metadata) {
        logger.debug(
                "{}: Resolved active schedule metadata for scheduleId='{}' from {} (resolvedNamePresent={}, resolvedType='{}')",
                thingId, scheduleId, metadata.source(), !metadata.name().isBlank(), metadata.type());
        return metadata;
    }

    private String parseableTimestamp(String timestamp) {
        if (timestamp.isBlank()) {
            return "";
        }
        try {
            Instant.parse(timestamp);
            return timestamp;
        } catch (DateTimeParseException e) {
            logger.trace("{}: Ignoring unparsable event timestamp '{}'", thingId, timestamp);
            return "";
        }
    }

    private String calculateEndTime(String startTime, int duration) {
        if (startTime.isBlank() || duration <= 0) {
            return "";
        }
        try {
            return Instant.parse(startTime).plusSeconds(duration).toString();
        } catch (DateTimeParseException e) {
            logger.trace("{}: Unable to calculate end time from start timestamp '{}'", thingId, startTime);
            return "";
        }
    }

    private String firstNonBlank(@Nullable String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int positiveValue(@Nullable Integer... values) {
        for (Integer value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return 0;
    }

    private record ScheduleMetadata(String name, String type, String source) {
        private static final ScheduleMetadata EMPTY = new ScheduleMetadata("", "", "none");
    }

    private void armWebhookRunSummaryProtection(RachioEventGsonDTO event, boolean extendExisting) {
        long protectUntil = calculateWebhookRunSummaryProtectUntil(event, currentTimeMillis());
        if (extendExisting) {
            protectUntil = Math.max(protectUntil, webhookRunSummaryProtectUntilMillis);
        }
        webhookRunSummaryProtectUntilMillis = protectUntil;
    }

    private long calculateWebhookRunSummaryProtectUntil(RachioEventGsonDTO event, long now) {
        RachioZoneStatus runStatus = event.zoneRunStatus;
        RachioWebhookPayload payload = event.payload;
        long endMillis = firstTimestampMillis(event.endTime, runStatus != null ? runStatus.endTime : "",
                payload != null ? payload.endTime : "");
        if (endMillis >= 0) {
            return safeAddMillis(endMillis, WEBHOOK_RUN_SUMMARY_GRACE_MILLIS);
        }

        int duration = positiveValue(runStatus != null ? runStatus.duration : null, event.duration,
                payload != null ? payload.getDurationSeconds() : 0);
        long durationMillis = duration * 1000L;
        long startMillis = firstTimestampMillis(event.startTime, runStatus != null ? runStatus.startTime : "",
                payload != null ? payload.startTime : "", payload != null ? payload.plannedRunStartTime : "");
        if (startMillis >= 0 && duration > 0) {
            return safeAddMillis(startMillis, durationMillis + WEBHOOK_RUN_SUMMARY_GRACE_MILLIS);
        }
        if (duration > 0) {
            return safeAddMillis(now, durationMillis + WEBHOOK_RUN_SUMMARY_GRACE_MILLIS);
        }
        return safeAddMillis(now, WEBHOOK_RUN_SUMMARY_GRACE_MILLIS);
    }

    private long firstTimestampMillis(@Nullable String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                try {
                    return Instant.parse(value).toEpochMilli();
                } catch (DateTimeParseException e) {
                    logger.trace("{}: Ignoring unparsable webhook run timestamp '{}'", thingId, value);
                }
            }
        }
        return -1;
    }

    private long safeAddMillis(long base, long millis) {
        if (Long.MAX_VALUE - base < millis) {
            return Long.MAX_VALUE;
        }
        return base + millis;
    }

    private boolean shouldPreserveWebhookRunSummary(RachioDevice device,
            RachioBridgeHandler.RefreshReason refreshReason, long now) {
        long protectUntil = webhookRunSummaryProtectUntilMillis;
        return device.currentScheduleRunning && isWebhookRunSummaryProtectedRefresh(refreshReason)
                && protectUntil >= now;
    }

    private boolean isWebhookRunSummaryProtectedRefresh(RachioBridgeHandler.RefreshReason refreshReason) {
        return refreshReason == RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION
                || refreshReason == RachioBridgeHandler.RefreshReason.SCHEDULED_POLL;
    }

    private void clearWebhookRunSummaryProtection() {
        webhookRunSummaryProtectUntilMillis = -1;
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private void handleRainDelayOnEvent(RachioEventGsonDTO event, RachioDevice device) {
        int rainDelaySeconds = event.getRainDelaySecondsRemaining();
        if (rainDelaySeconds >= 0) {
            logger.info("{}: Device reported Rain Delay ON for {} sec.", thingId, rainDelaySeconds);
            device.setRainDelayTime(rainDelaySeconds);
        } else {
            logger.info("{}: Device reported Rain Delay ON without duration details; refreshing device state.",
                    thingId);
            refreshRainDelayState();
        }
    }

    private void refreshRainDelayState() {
        RachioBridgeHandler handler = cloudHandler;
        if (handler != null) {
            handler.refreshDeviceStatus(RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);
        } else {
            logger.debug("{}: Unable to refresh rain delay state because cloud handler is not initialized.", thingId);
        }
    }

    private void registerControllerWebhook(RachioBridgeHandler handler, RachioDevice device,
            RequestPurpose requestPurpose) throws RachioApiException {
        WebhookRegistrationRetryIntent retryIntent = resolveWebhookRegistrationRetryIntent(handler);
        try {
            handler.registerWebHook(device.id, requestPurpose);
            clearPendingWebhookRegistration();
        } catch (RachioApiThrottledException e) {
            if (retryIntent != null) {
                deferWebhookRegistration(device.id, retryIntent, e);
            } else {
                clearPendingWebhookRegistration();
                logger.debug(
                        "{}: Skipping deferred webhook registration scheduling for controller '{}' because irrigation webhook mode is disabled.",
                        thingId, device.id);
            }
        } catch (RachioApiException e) {
            clearPendingWebhookRegistration();
            if (requestPurpose == RequestPurpose.USER_COMMAND) {
                throw e;
            }
            logger.warn("Unable to register webhook for controller '{}'; polling fallback remains active, cause={}",
                    device.id, e.getClass().getSimpleName());
        } catch (RuntimeException e) {
            clearPendingWebhookRegistration();
            if (requestPurpose == RequestPurpose.USER_COMMAND) {
                throw e;
            }
            logger.warn("Unable to register webhook for controller '{}'; polling fallback remains active, cause={}",
                    device.id, e.getClass().getSimpleName());
        }
    }

    private void deferWebhookRegistration(String deviceId, WebhookRegistrationRetryIntent retryIntent,
            RachioApiThrottledException throttle) {
        long delaySeconds;
        int attempt;
        boolean repeatedDeferral;
        boolean scheduled = false;
        synchronized (this) {
            repeatedDeferral = webhookRegistrationPending && retryIntent == webhookRegistrationRetryIntent
                    && Objects.equals(deviceId, webhookRegistrationRetryDeviceId);
            if (repeatedDeferral) {
                return;
            }

            if (webhookRegistrationPending && (retryIntent != webhookRegistrationRetryIntent
                    || !Objects.equals(deviceId, webhookRegistrationRetryDeviceId))) {
                clearPendingWebhookRegistration();
            }

            attempt = webhookRegistrationRetryBackoffStep;
            delaySeconds = getWebhookRegistrationRetryDelaySeconds(throttle, attempt);
            webhookRegistrationPending = true;
            webhookRegistrationRetryIntent = retryIntent;
            webhookRegistrationRetryDeviceId = deviceId;
            ScheduledFuture<?> retryJob = webhookRegistrationRetryJob;
            if (retryJob == null || retryJob.isDone()) {
                nextWebhookRegistrationRetryAtMillis = currentTimeMillis() + TimeUnit.SECONDS.toMillis(delaySeconds);
                scheduleWebhookRegistrationRetry(deviceId, delaySeconds);
                scheduled = true;
                webhookRegistrationRetryBackoffStep = Math.min(2, webhookRegistrationRetryBackoffStep + 1);
            }
        }

        if (scheduled) {
            if (attempt == 0) {
                logger.info(
                        "Webhook registration for controller '{}' deferred because the local Rachio API budget guard is active; retrying in {} seconds.",
                        deviceId, delaySeconds);
            } else {
                logger.debug(
                        "{}: Deferred webhook registration retry for controller '{}' scheduled in {} seconds (backoff attempt {}): {}",
                        thingId, deviceId, delaySeconds, attempt + 1, throttle.getMessage());
            }
        } else {
            logger.debug(
                    "{}: Webhook registration for controller '{}' is already deferred because the local Rachio API budget guard is active.",
                    thingId, deviceId);
        }
    }

    protected void scheduleWebhookRegistrationRetry(String deviceId, long delaySeconds) {
        webhookRegistrationRetryJob = scheduler.schedule(() -> {
            synchronized (this) {
                webhookRegistrationRetryJob = null;
            }
            retryDeferredWebhookRegistration();
        }, delaySeconds, TimeUnit.SECONDS);
    }

    void retryDeferredWebhookRegistrationIfDue() {
        synchronized (this) {
            if (!webhookRegistrationPending || System.currentTimeMillis() < nextWebhookRegistrationRetryAtMillis) {
                return;
            }
            ScheduledFuture<?> retryJob = webhookRegistrationRetryJob;
            if (retryJob != null && !retryJob.isDone()) {
                return;
            }
        }
        retryDeferredWebhookRegistration();
    }

    protected void retryDeferredWebhookRegistration() {
        RachioBridgeHandler handler = cloudHandler;
        RachioDevice device = dev;
        if (handler == null || device == null || disposed) {
            return;
        }
        if (handler.isDisposed()) {
            clearPendingWebhookRegistration();
            logger.debug("{}: Skipping deferred webhook registration retry because bridge handler is disposed.",
                    thingId);
            return;
        }
        if (!isBridgeOnline()) {
            logger.debug("{}: Skipping deferred webhook registration retry while bridge is offline.", thingId);
            return;
        }

        WebhookRegistrationRetryIntent retryIntent;
        String retryDeviceId;

        synchronized (this) {
            if (!webhookRegistrationPending) {
                return;
            }
            retryIntent = webhookRegistrationRetryIntent;
            retryDeviceId = webhookRegistrationRetryDeviceId;
        }

        if (retryIntent == null || retryDeviceId == null) {
            clearPendingWebhookRegistration();
            return;
        }
        if (!retryDeviceId.equals(device.id)) {
            clearPendingWebhookRegistration();
            logger.debug(
                    "{}: Skipping deferred webhook registration retry because the controller changed while retry was pending.",
                    thingId);
            return;
        }
        if (!isWebhookRetryIntentStillValid(handler, retryIntent)) {
            clearPendingWebhookRegistration();
            logger.debug(
                    "{}: Skipping deferred webhook registration retry for controller '{}' because webhook mode/configuration changed.",
                    thingId, retryDeviceId);
            return;
        }

        synchronized (this) {
            webhookRegistrationPending = false;
            nextWebhookRegistrationRetryAtMillis = 0;
        }

        logger.debug("{}: Retrying deferred webhook registration for controller '{}'.", thingId, device.id);
        try {
            handler.registerWebHook(device.id, RequestPurpose.BACKGROUND_REFRESH);
            clearPendingWebhookRegistration();
            logger.debug("{}: Deferred webhook registration for controller '{}' completed successfully.", thingId,
                    device.id);
            updateThingStatusAfterSuccessfulCommunication();
        } catch (RachioApiThrottledException e) {
            deferWebhookRegistration(device.id, retryIntent, e);
        } catch (RachioApiException e) {
            clearPendingWebhookRegistration();
            logger.warn("Unable to register webhook for controller '{}'; polling fallback remains active, cause={}",
                    device.id, e.getClass().getSimpleName());
        } catch (RuntimeException e) {
            clearPendingWebhookRegistration();
            logger.warn("Unable to register webhook for controller '{}'; polling fallback remains active, cause={}",
                    device.id, e.getClass().getSimpleName());
        }
    }

    private long getWebhookRegistrationRetryDelaySeconds(RachioApiThrottledException throttle, int backoffStep) {
        Duration suggestedDelay = throttle.getSuggestedRetryDelay();
        long suggestedSeconds = suggestedDelay.isZero() || suggestedDelay.isNegative()
                ? MIN_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS
                : suggestedDelay.getSeconds();
        long boundedSuggestedSeconds = Math.max(MIN_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS,
                Math.min(MAX_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS, suggestedSeconds));
        long backoffSeconds = switch (Math.max(0, backoffStep)) {
            case 0 -> MIN_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS;
            case 1 -> MEDIUM_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS;
            default -> MAX_WEBHOOK_REGISTRATION_RETRY_DELAY_SECONDS;
        };
        return Math.max(backoffSeconds, boundedSuggestedSeconds);
    }

    private @Nullable WebhookRegistrationRetryIntent resolveWebhookRegistrationRetryIntent(
            RachioBridgeHandler handler) {
        RachioWebhookMode mode = handler.getActiveIrrigationWebhookProcessingMode();
        if (mode == RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE) {
            return WebhookRegistrationRetryIntent.LEGACY_NOTIFICATION_SERVICE;
        }
        if (mode == RachioWebhookMode.WEBHOOK_SERVICE) {
            return WebhookRegistrationRetryIntent.MODERN_WEBHOOK_SERVICE;
        }
        return null;
    }

    private boolean isWebhookRetryIntentStillValid(RachioBridgeHandler handler,
            WebhookRegistrationRetryIntent retryIntent) {
        WebhookRegistrationRetryIntent currentIntent = resolveWebhookRegistrationRetryIntent(handler);
        if (currentIntent != retryIntent) {
            return false;
        }
        if (retryIntent == WebhookRegistrationRetryIntent.LEGACY_NOTIFICATION_SERVICE) {
            String callbackUrl = handler.getCallbackUrl();
            return !callbackUrl.isBlank();
        }
        return true;
    }

    private void clearPendingWebhookRegistrationIfObsolete(RachioBridgeHandler handler, String deviceId,
            String reason) {
        synchronized (this) {
            if (!webhookRegistrationPending || !Objects.equals(webhookRegistrationRetryDeviceId, deviceId)
                    || webhookRegistrationRetryIntent == null) {
                return;
            }
        }

        WebhookRegistrationRetryIntent retryIntent;
        synchronized (this) {
            retryIntent = webhookRegistrationRetryIntent;
        }
        if (retryIntent == null || !isWebhookRetryIntentStillValid(handler, retryIntent)) {
            clearPendingWebhookRegistration();
            logger.debug(
                    "{}: Cleared deferred webhook registration retry for controller '{}' because webhook mode/configuration changed ({}).",
                    thingId, deviceId, reason);
        }
    }

    private synchronized void clearPendingWebhookRegistration() {
        ScheduledFuture<?> retryJob = webhookRegistrationRetryJob;
        if (retryJob != null && !retryJob.isCancelled()) {
            retryJob.cancel(true);
        }
        webhookRegistrationRetryJob = null;
        webhookRegistrationPending = false;
        webhookRegistrationRetryBackoffStep = 0;
        webhookRegistrationRetryIntent = null;
        webhookRegistrationRetryDeviceId = null;
        nextWebhookRegistrationRetryAtMillis = 0;
    }

    synchronized boolean isWebhookRegistrationPending() {
        return webhookRegistrationPending;
    }

    void refreshThingStatusAfterSuccessfulCommunication() {
        rebindToCurrentBridgeModel("successful cloud poll");
        updateThingStatusAfterSuccessfulCommunication();
    }

    private @Nullable RachioZoneHandler resolveZoneHandler(RachioDevice device, RachioZone zone,
            RachioEventGsonDTO event, String zoneState, int zoneNumber) {
        RachioZoneHandler handler = zone.getThingHandler();
        if (handler != null) {
            return handler;
        }

        RachioBridgeHandler bridgeHandler = cloudHandler;
        if (bridgeHandler == null) {
            return null;
        }
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioZoneHandler candidate
                    && candidate.rebindToCurrentModel(device, zone, "webhook dispatch")) {
                logger.debug(
                        "{}: Zone webhook event {}.{} matched zone {} and rebound its registered Thing handler before direct dispatch",
                        thingId, event.type, zoneState, zoneNumber);
                return candidate;
            }
        }
        return null;
    }

    private void enrichZoneEventFromResolvedZone(RachioEventGsonDTO event, RachioZone zone) {
        if (event.zoneName.isBlank()) {
            event.zoneName = zone.name;
        }
        if (event.zoneId.isBlank()) {
            event.zoneId = zone.id;
        }
        Integer eventZoneNumber = event.zoneNumber;
        if (eventZoneNumber == null || eventZoneNumber <= 0) {
            event.zoneNumber = zone.zoneNumber;
        }
    }

    private void updateThingStatusAfterSuccessfulCommunication() {
        if (!isBridgeOnline() || dev == null) {
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private State stringOrUndef(String value) {
        return value.isBlank() ? UnDefType.UNDEF : new StringType(value);
    }

    private State optionalStringState(String value) {
        return value.isBlank() ? UnDefType.NULL : new StringType(value);
    }

    private State optionalDateTimeState(String value) {
        return value.isBlank() ? UnDefType.NULL : parseDateTimeState(value);
    }

    private State dateTimeOrUndef(String value) {
        if (value.isBlank()) {
            return UnDefType.UNDEF;
        }
        return parseDateTimeState(value);
    }

    private State parseDateTimeState(String value) {
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

    private void updateProperties() {
        RachioDevice d = dev;
        if (d != null) {
            logger.trace("{}: Updating device properties", thingId);
            updateProperties(d.fillProperties());
        }
    }
}
