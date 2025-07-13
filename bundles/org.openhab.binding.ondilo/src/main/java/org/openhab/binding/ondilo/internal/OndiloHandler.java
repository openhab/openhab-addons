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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.LastMeasure;
import org.openhab.binding.ondilo.internal.dto.Recommendation;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int recommendationId; // Used to track the last recommendation ID processed
    private AtomicReference<String> ondiloId = new AtomicReference<>(NO_ID);
    private final int configPoolId;
    private @Nullable ScheduledFuture<?> bridgeRecoveryJob;

    public OndiloHandler(Thing thing) {
        super(thing);

        OndiloConfiguration currentConfig = getConfigAs(OndiloConfiguration.class);
        this.configPoolId = currentConfig.id;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            // not implemented as it would causes >10 channel updates in a row during setup (exceeds given API quota)
            // If you want to update the values, use the poll channel instead
            return;
        } else if (CHANNEL_RECOMMENDATION_STATUS.equals(channelUID.getId())) {
            if (command instanceof StringType cmd) {
                try {
                    Recommendation.Status status = Recommendation.Status.valueOf(cmd.toString());
                    if (status == Recommendation.Status.ok) {
                        OndiloBridge ondiloBridge = getOndiloBridge();
                        if (ondiloBridge != null && this.recommendationId != 0) {
                            ondiloBridge.validateRecommendation(configPoolId, recommendationId);
                        } else {
                            logger.warn(
                                    "Cannot validate recommendation, as the bridge is not initialized or no recommendation ID is set");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid Status: {}, Error: {}", cmd.toString(), e.getMessage());
                }
            } else {
                logger.warn("Received command for channel {} with unsupported type: {}", channelUID.getId(),
                        command.getClass().getSimpleName());
            }
        } else {
            logger.warn("Received command for unknown channel: {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        OndiloBridge ondiloBridge = getOndiloBridge();
        if (ondiloBridge != null) {
            // Initialize to 0, as no recommendation has been processed yet
            recommendationId = 0;

            if (configPoolId == 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, I18N_ID_INVALID);
                return;
            } else {
                ondiloId.set(String.valueOf(configPoolId));
            }
            ondiloBridge.registerOndiloHandler(configPoolId, this);
            updateStatus(ThingStatus.ONLINE);
            stopBridgeRecoveryJob();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            startBridgeRecoveryJob();
        }
    }

    private void startBridgeRecoveryJob() {
        if (bridgeRecoveryJob == null) {
            // Check every 10 seconds after 5s delay
            bridgeRecoveryJob = scheduler.scheduleWithFixedDelay(() -> initialize(), 5, 10, TimeUnit.SECONDS);
        }
    }

    private void stopBridgeRecoveryJob() {
        ScheduledFuture<?> bridgeRecoveryJob = this.bridgeRecoveryJob;
        if (bridgeRecoveryJob != null) {
            bridgeRecoveryJob.cancel(true);
            this.bridgeRecoveryJob = null;
        }
    }

    @Override
    public void dispose() {
        stopBridgeRecoveryJob();
        OndiloBridge ondiloBridge = getOndiloBridge();
        if (ondiloBridge != null) {
            ondiloBridge.unregisterOndiloHandler(configPoolId);
        }
        if (!ondiloId.get().equals(NO_ID)) {
            ondiloId.set(NO_ID);
        }
        recommendationId = 0; // Reset last processed recommendation ID
    }

    public void clearLastMeasuresChannels() {
        // Undef is used as the state is unknown
        updateState(CHANNEL_TEMPERATURE, UnDefType.UNDEF);
        updateState(CHANNEL_PH, UnDefType.UNDEF);
        updateState(CHANNEL_ORP, UnDefType.UNDEF);
        updateState(CHANNEL_SALT, UnDefType.UNDEF);
        updateState(CHANNEL_TDS, UnDefType.UNDEF);
        updateState(CHANNEL_BATTERY, UnDefType.UNDEF);
        updateState(CHANNEL_RSSI, UnDefType.UNDEF);
    }

    public void clearRecommendationChannels() {
        // Null is used, as there is no recommendation available (no to-do's)
        updateState(CHANNEL_RECOMMENDATION_ID, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_TITLE, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_MESSAGE, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_CREATED_AT, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_UPDATED_AT, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_STATUS, UnDefType.NULL);
        updateState(CHANNEL_RECOMMENDATION_DEADLINE, UnDefType.NULL);
        this.recommendationId = 0; // Reset last processed recommendation ID
    }

    public Instant updateLastMeasuresChannels(LastMeasure lastMeasures) {
        /*
         * The measures are received using the following units:
         * - Temperature: Celsius degrees (°C)
         * - ORP: millivolts (mV)
         * - Salt: milligrams per liter (mg/l)
         * - TDS: parts per million (ppm)
         * - Battery and RSSI: percent (%)
         */
        switch (lastMeasures.dataType) {
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
                logger.warn("Unknown data type: {}", lastMeasures.dataType);
        }
        // Update value time channel (expect that it is the same for all measures)
        Instant valueTime = parseUtcTimeToInstant(lastMeasures.valueTime);
        updateState(CHANNEL_VALUE_TIME, new DateTimeType(valueTime));
        return valueTime;
    }

    public void updateRecommendationChannels(Recommendation recommendation) {
        updateState(CHANNEL_RECOMMENDATION_ID, new DecimalType(recommendation.id));
        updateState(CHANNEL_RECOMMENDATION_TITLE, new StringType(recommendation.title));
        updateState(CHANNEL_RECOMMENDATION_MESSAGE, new StringType(recommendation.message));
        updateState(CHANNEL_RECOMMENDATION_CREATED_AT, new DateTimeType(recommendation.createdAt));
        updateState(CHANNEL_RECOMMENDATION_UPDATED_AT, new DateTimeType(recommendation.updatedAt));
        updateState(CHANNEL_RECOMMENDATION_STATUS, new StringType(recommendation.status.name()));
        updateState(CHANNEL_RECOMMENDATION_DEADLINE, new DateTimeType(recommendation.deadline));
        this.recommendationId = recommendation.id; // Update last processed recommendation ID
    }

    @Nullable
    private OndiloBridge getOndiloBridge() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof OndiloBridgeHandler bridgeHandler) {
            return bridgeHandler.getOndiloBridge();
        }
        return null;
    }

    private Instant parseUtcTimeToInstant(String utcTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
        return Instant.from(formatter.parse(utcTime));
    }
}
