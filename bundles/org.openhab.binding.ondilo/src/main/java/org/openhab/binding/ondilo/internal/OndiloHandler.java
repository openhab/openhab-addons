/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal;

import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.LastMeasure;
import org.openhab.binding.ondilo.internal.dto.Recommendation;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link OndiloHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloHandler extends BaseThingHandler {
    private static final String NO_ID = "NO_ID";
    private final Logger logger = LoggerFactory.getLogger(OndiloHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private AtomicReference<String> ondiloId = new AtomicReference<>(NO_ID);

    private @Nullable ScheduledFuture<?> ondiloPollingJob;

    public OndiloHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            // not implemented as it would causes >10 channel updates in a row during setup (exceed API limits)
        } else {
            if (channelUID.getId().equals(CHANNEL_POLL_UPDATE)) {
                if (command instanceof OnOffType cmd) {
                    if (cmd == OnOffType.ON) {
                        poll();
                        updateState(CHANNEL_POLL_UPDATE, OnOffType.OFF);
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            OndiloConfiguration currentConfig = getConfigAs(OndiloConfiguration.class);
            final int configPoolId = currentConfig.id;
            final int refreshInterval = currentConfig.refreshInterval;

            if (configPoolId == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No ID configured");
                return;
            } else {
                ondiloId.set(String.valueOf(configPoolId));
            }
            updateStatus(ThingStatus.ONLINE);
            startOndiloPolling(refreshInterval);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void dispose() {
        if (!ondiloId.get().equals(NO_ID)) {
            stopOndiloPolling();
            logger.debug("Stopped polling for Ondilo device with ID: {}", ondiloId.get());
            ondiloId.set(NO_ID);
        }
    }

    private synchronized void poll() {
        OndiloBridge bridge = getOndiloBridge();
        if ((bridge != null) && (bridge.apiClient != null)) {
            OndiloApiClient apiClient = bridge.apiClient;
            String poolsJson = apiClient.get("/pools/" + ondiloId.get()
                    + "/lastmeasures?types[]=temperature&types[]=ph&types[]=orp&types[]=salt&types[]=tds&types[]=battery&types[]=rssi");
            logger.trace("LastMeasures: {}", poolsJson);
            // Parse JSON to DTO
            Gson gson = new Gson();
            List<LastMeasure> lastMeasures = gson.fromJson(poolsJson, new TypeToken<List<LastMeasure>>() {
            }.getType());

            logger.trace("Parsed {} LastMeasures", lastMeasures.size());
            if (lastMeasures.isEmpty()) {
                logger.trace("No lastMeasures available for pool with ID: {}", ondiloId.get());
                updateLastMeasuresChannels(null);
            } else {
                for (LastMeasure lastMeasure : lastMeasures) {
                    logger.trace("LastMeasure: type={}, value={}", lastMeasure.data_type, lastMeasure.value);
                    updateLastMeasuresChannels(lastMeasure);
                }
            }

            String recommendationsJson = apiClient.get("/pools/" + ondiloId.get() + "/recommendations");
            logger.trace("recommendations: {}", recommendationsJson);
            // Parse JSON to DTO
            List<Recommendation> recommendations = gson.fromJson(recommendationsJson,
                    new TypeToken<List<Recommendation>>() {
                    }.getType());

            logger.trace("Parsed {} Recommendations", recommendations.size());
            if (recommendations.isEmpty()) {
                logger.trace("No Recommendations available for pool with ID: {}", ondiloId.get());
                updateRecommendationChannels(null);
            } else {
                Recommendation recommendation = recommendations.getFirst();
                // for (Recommendation recommendation : recommendations) {
                logger.info("Recommentation: id={}, title={}", recommendation.id, recommendation.title);
                updateRecommendationChannels(recommendation);
                // }
            }
        }
    }

    private void updateLastMeasuresChannels(@Nullable LastMeasure lastMeasures) {
        if (lastMeasures != null) {
            switch (lastMeasures.data_type) {
                case "temperature":
                    updateState(CHANNEL_TEMPERATURE, new QuantityType<>(lastMeasures.value, SIUnits.CELSIUS));
                    break;
                case "ph":
                    updateState(CHANNEL_PH, new DecimalType(lastMeasures.value));
                    break;
                case "orp":
                    updateState(CHANNEL_ORP, new QuantityType<>(lastMeasures.value / 1000.0, Units.VOLT));
                    // Convert mV to V
                    break;
                case "salt":
                    updateState(CHANNEL_SALT,
                            new QuantityType<>(lastMeasures.value * 0.001, Units.KILOGRAM_PER_CUBICMETRE));
                    // Convert mg/l to kg/m³
                    break;
                case "tds":
                    updateState(CHANNEL_TDS, new QuantityType<>(lastMeasures.value, Units.PARTS_PER_MILLION));
                    break;
                case "battery":
                    updateState(CHANNEL_BATTERY, new QuantityType<>(lastMeasures.value, Units.PERCENT));
                    break;
                case "rssi":
                    updateState(CHANNEL_RSSI, new DecimalType(lastMeasures.value));
                    break;
                default:
                    logger.warn("Unknown data type: {}", lastMeasures.data_type);
            }
            // Update value time channel (expect that it is the same for all measures)
            updateState(CHANNEL_VALUE_TIME, new DateTimeType(convertUtcToSystemTimeZone(lastMeasures.value_time)));
        } else {
            updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
            updateState(CHANNEL_PH, UnDefType.UNDEF);
            updateState(CHANNEL_ORP, UnDefType.UNDEF);
            updateState(CHANNEL_SALT, UnDefType.UNDEF);
            updateState(CHANNEL_TDS, UnDefType.UNDEF);
            updateState(CHANNEL_BATTERY, UnDefType.UNDEF);
            updateState(CHANNEL_RSSI, UnDefType.UNDEF);
        }
    }

    private void updateRecommendationChannels(@Nullable Recommendation recommendation) {
        if (recommendation != null) {
            updateState(CHANNEL_RECOMMENDATION_ID, new DecimalType(recommendation.id));
            updateState(CHANNEL_RECOMMENDATION_TITLE, new StringType(recommendation.title));
            updateState(CHANNEL_RECOMMENDATION_MESSAGE, new StringType(recommendation.message));
            updateState(CHANNEL_RECOMMENDATION_CREATED_AT, new DateTimeType(recommendation.created_at));
            updateState(CHANNEL_RECOMMENDATION_UPDATED_AT, new DateTimeType(recommendation.updated_at));
            updateState(CHANNEL_RECOMMENDATION_STATUS, new StringType(recommendation.status));
            updateState(CHANNEL_RECOMMENDATION_DEADLINE, new DateTimeType(recommendation.deadline));
        } else {
            updateState(CHANNEL_RECOMMENDATION_ID, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_TITLE, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_MESSAGE, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_CREATED_AT, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_UPDATED_AT, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_STATUS, UnDefType.NULL);
            updateState(CHANNEL_RECOMMENDATION_DEADLINE, UnDefType.NULL);
        }
    }

    private void startOndiloPolling(Integer refreshInterval) {
        ScheduledFuture<?> currentPollingJob = ondiloPollingJob;
        if (currentPollingJob == null) {
            ondiloPollingJob = scheduler.scheduleWithFixedDelay(() -> poll(), 1, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void stopOndiloPolling() {
        ScheduledFuture<?> currentPollingJob = ondiloPollingJob;
        if (currentPollingJob != null) {
            currentPollingJob.cancel(true);
            ondiloPollingJob = null;
        }
    }

    @Nullable
    private OndiloBridge getOndiloBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof OndiloBridgeHandler bridgeHandler) {
                return bridgeHandler.getOndiloBridge();
            }
        }
        return null;
    }

    public ZonedDateTime convertUtcToSystemTimeZone(String utcTime) {
        // Define the input format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Parse as LocalDateTime (no zone)
        LocalDateTime localDateTime = LocalDateTime.parse(utcTime, inputFormatter);
        // Attach UTC zone
        ZonedDateTime utcZoned = localDateTime.atZone(ZoneId.of("UTC"));
        // Convert to system default zone
        ZonedDateTime systemZoned = utcZoned.withZoneSameInstant(timeZoneProvider.getTimeZone());
        // Format as string (same pattern or as needed)
        return systemZoned;
    }
}
