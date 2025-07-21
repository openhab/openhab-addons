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
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.dto.LastMeasure;
import org.openhab.binding.ondilo.internal.dto.Pool;
import org.openhab.binding.ondilo.internal.dto.PoolConfiguration;
import org.openhab.binding.ondilo.internal.dto.PoolInfo;
import org.openhab.binding.ondilo.internal.dto.Recommendation;
import org.openhab.core.i18n.LocaleProvider;
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
    private final LocaleProvider localeProvider;
    private final int configPoolId;

    private int recommendationId; // Used to track the last recommendation ID processed
    private AtomicReference<String> ondiloId = new AtomicReference<>(NO_ID);

    private @Nullable ScheduledFuture<?> bridgeRecoveryJob;

    // Store last value and valueTime for trend calculation
    private OndiloMeasureState lastTemperatureState = new OndiloMeasureState(Double.NaN, null);
    private OndiloMeasureState lastPhState = new OndiloMeasureState(Double.NaN, null);
    private OndiloMeasureState lastOrpState = new OndiloMeasureState(Double.NaN, null);
    private OndiloMeasureState lastSaltState = new OndiloMeasureState(Double.NaN, null);
    private OndiloMeasureState lastTdsState = new OndiloMeasureState(Double.NaN, null);

    public OndiloHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);

        this.localeProvider = localeProvider;

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

    private void updateTrendChannel(String channel, String trendChannel, double value, Instant valueTime,
            OndiloMeasureState lastMeasureState, Object unitOrType) {
        Instant lastMeasureTime = lastMeasureState.time;
        if (lastMeasureTime != null) {
            if (!valueTime.equals(lastMeasureTime)) {
                double delta = value - lastMeasureState.value;
                if (unitOrType instanceof Unit<?> unit) {
                    updateState(trendChannel, new QuantityType<>(delta, (Unit<?>) unit));
                } else { // DecimalType
                    updateState(trendChannel, new DecimalType(delta));
                }
                logger.trace(
                        "channel: {}, trendChannel: {}, value: {}, valueTime: {}, lastValue: {}, lastValueTime: {},  unitOrType: {} ==> delta: {}",
                        channel, trendChannel, value, valueTime.toString(), lastMeasureState.value,
                        lastMeasureTime.toString(), unitOrType.toString(), delta);
            } // else: keep current value
        } else {
            updateState(trendChannel, UnDefType.UNDEF);
        }
        // Update the current value channel
        if (unitOrType instanceof Unit<?> unit) {
            updateState(channel, new QuantityType<>(value, (Unit<?>) unit));
        } else { // DecimalType
            updateState(channel, new DecimalType(value));
        }

        lastMeasureState.value = value;
        lastMeasureState.time = valueTime;
    }

    public Instant updateLastMeasuresChannels(LastMeasure measure) {
        Instant valueTime = parseUtcTimeToInstant(measure.valueTime);
        switch (measure.dataType) {
            case "temperature":
                updateTrendChannel(CHANNEL_TEMPERATURE, CHANNEL_TEMPERATURE_TREND, measure.value, valueTime,
                        lastTemperatureState, SIUnits.CELSIUS);
                break;
            case "ph":
                updateTrendChannel(CHANNEL_PH, CHANNEL_PH_TREND, measure.value, valueTime, lastPhState,
                        DecimalType.class);
                break;
            case "orp":
                updateTrendChannel(CHANNEL_ORP, CHANNEL_ORP_TREND, measure.value / 1000.0, valueTime, lastOrpState,
                        Units.VOLT); // Convert mV to V
                break;
            case "salt":
                updateTrendChannel(CHANNEL_SALT, CHANNEL_SALT_TREND, measure.value * 0.001, valueTime, lastSaltState,
                        Units.KILOGRAM_PER_CUBICMETRE); // Convert mg/l to kg/m³
                break;
            case "tds":
                updateTrendChannel(CHANNEL_TDS, CHANNEL_TDS_TREND, measure.value, valueTime, lastTdsState,
                        Units.PARTS_PER_MILLION);
                break;
            case "battery":
                updateState(CHANNEL_BATTERY, new QuantityType<>(measure.value, Units.PERCENT));
                break;
            case "rssi":
                updateState(CHANNEL_RSSI, new DecimalType(measure.value));
                break;
            default:
                logger.warn("Unknown data type: {}", measure.dataType);
        }
        // Update value time channel (expect that it is the same for all measures)
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

    public void updatePool(Pool pool) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_ONDILO_ID, String.valueOf(pool.id));
        properties.put(PROPERTY_ONDILO_NAME, pool.name);
        properties.put(PROPERTY_ONDILO_TYPE, pool.type);
        properties.put(PROPERTY_ONDILO_VOLUME, pool.getVolume());
        properties.put(PROPERTY_ONDILO_DISINFECTION, pool.getDisinfection());
        properties.put(PROPERTY_ONDILO_ADDRESS, pool.getAddress());
        properties.put(PROPERTY_ONDILO_LOCATION, pool.getLocation());
        updateProperties(properties);
    }

    public void updatePoolInfo(PoolInfo poolInfo) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_ONDILO_INFO_UUID, poolInfo.uuid);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, poolInfo.serialNumber);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, poolInfo.swVersion);
        updateProperties(properties);
    }

    public void updatePoolInfo(PoolConfiguration poolConfiguration) {
        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_ONDILO_INFO_POOL_GUY_NUMBER, poolConfiguration.poolGuyNumber);
        properties.put(PROPERTY_ONDILO_INFO_MAINTENANCE_DAY,
                poolConfiguration.getMaintenanceDay(localeProvider.getLocale()));
        updateProperties(properties);
    }

    public void updatePoolConfiguration(PoolConfiguration poolConfiguration) {
        updateState(CHANNEL_CONFIGURATION_TEMPERATURE_LOW,
                new QuantityType<>(poolConfiguration.temperatureLow, SIUnits.CELSIUS));
        updateState(CHANNEL_CONFIGURATION_TEMPERATURE_HIGH,
                new QuantityType<>(poolConfiguration.temperatureHigh, SIUnits.CELSIUS));
        updateState(CHANNEL_CONFIGURATION_PH_LOW, new DecimalType(poolConfiguration.phLow));
        updateState(CHANNEL_CONFIGURATION_PH_HIGH, new DecimalType(poolConfiguration.phHigh));
        updateState(CHANNEL_CONFIGURATION_ORP_LOW, new QuantityType<>(poolConfiguration.orpLow / 1000.0, Units.VOLT));
        updateState(CHANNEL_CONFIGURATION_ORP_HIGH, new QuantityType<>(poolConfiguration.orpHigh / 1000.0, Units.VOLT));
        updateState(CHANNEL_CONFIGURATION_SALT_LOW,
                new QuantityType<>(poolConfiguration.saltLow * 0.001, Units.KILOGRAM_PER_CUBICMETRE));
        updateState(CHANNEL_CONFIGURATION_SALT_HIGH,
                new QuantityType<>(poolConfiguration.saltHigh * 0.001, Units.KILOGRAM_PER_CUBICMETRE));
        updateState(CHANNEL_CONFIGURATION_TDS_LOW,
                new QuantityType<>(poolConfiguration.tdsLow, Units.PARTS_PER_MILLION));
        updateState(CHANNEL_CONFIGURATION_TDS_HIGH,
                new QuantityType<>(poolConfiguration.tdsHigh, Units.PARTS_PER_MILLION));
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
