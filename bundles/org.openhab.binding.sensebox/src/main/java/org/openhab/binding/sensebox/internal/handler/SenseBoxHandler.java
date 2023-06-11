/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sensebox.internal.handler;

import static org.openhab.binding.sensebox.internal.SenseBoxBindingConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensebox.internal.SenseBoxAPIConnection;
import org.openhab.binding.sensebox.internal.config.SenseBoxConfiguration;
import org.openhab.binding.sensebox.internal.dto.SenseBoxData;
import org.openhab.binding.sensebox.internal.dto.SenseBoxLocation;
import org.openhab.binding.sensebox.internal.dto.SenseBoxSensor;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SenseBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Ignore incorrect data for brightness readings
 * @author Hakan Tandogan - Changed use of caching utils to ESH ExpiringCacheMap
 * @author Hakan Tandogan - Unit of Measurement support
 */
@NonNullByDefault
public class SenseBoxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SenseBoxHandler.class);

    protected @NonNullByDefault({}) SenseBoxConfiguration thingConfiguration;

    private @Nullable SenseBoxData data;

    private @Nullable ScheduledFuture<?> refreshJob;

    private static final BigDecimal ONEHUNDRED = BigDecimal.valueOf(100l);

    private static final String CACHE_KEY_DATA = "DATA";

    private final ExpiringCacheMap<String, SenseBoxData> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

    private final SenseBoxAPIConnection connection = new SenseBoxAPIConnection();

    public SenseBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        thingConfiguration = getConfigAs(SenseBoxConfiguration.class);

        String senseBoxId = thingConfiguration.getSenseBoxId();
        logger.debug("Thing Configuration {} initialized {}", getThing().getUID(), senseBoxId);

        String offlineReason = "";
        boolean validConfig = true;

        if (senseBoxId == null || senseBoxId.trim().isEmpty()) {
            offlineReason = "senseBox ID is mandatory and must be configured";
            validConfig = false;
        }

        if (thingConfiguration.getRefreshInterval() < MINIMUM_UPDATE_INTERVAL) {
            logger.warn("Refresh interval is much too small, setting to default of {} seconds",
                    MINIMUM_UPDATE_INTERVAL);
            thingConfiguration.setRefreshInterval(MINIMUM_UPDATE_INTERVAL);
        }

        if (senseBoxId != null && validConfig) {
            cache.put(CACHE_KEY_DATA, () -> {
                return connection.reallyFetchDataFromServer(senseBoxId);
            });
            updateStatus(ThingStatus.UNKNOWN);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, offlineReason);
        }
        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            data = fetchData();
            if (data != null && ThingStatus.ONLINE == data.getStatus()) {
                publishDataForChannel(channelUID);

                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            logger.debug("Unsupported command {}! Supported commands: REFRESH", command);
        }
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
    }

    private void stopAutomaticRefresh() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void publishData() {
        logger.debug("Refreshing data for box {}, scheduled after {} seconds...", thingConfiguration.getSenseBoxId(),
                thingConfiguration.getRefreshInterval());
        data = fetchData();
        if (data != null && ThingStatus.ONLINE == data.getStatus()) {
            publishProperties();
            publishChannels();
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    };

    private void startAutomaticRefresh() {
        stopAutomaticRefresh();
        refreshJob = scheduler.scheduleWithFixedDelay(this::publishData, 0, thingConfiguration.getRefreshInterval(),
                TimeUnit.SECONDS);
    }

    private @Nullable SenseBoxData fetchData() {
        return cache.get(CACHE_KEY_DATA);
    }

    private void publishProperties() {
        SenseBoxData localData = data;
        if (localData != null) {
            Map<String, String> properties = editProperties();
            properties.put(PROPERTY_NAME, localData.getName());
            properties.put(PROPERTY_EXPOSURE, localData.getExposure());
            properties.put(PROPERTY_IMAGE_URL, localData.getDescriptor().getImageUrl());
            properties.put(PROPERTY_MAP_URL, localData.getDescriptor().getMapUrl());
            updateProperties(properties);
        }
    }

    private void publishChannels() {
        thing.getChannels().forEach(channel -> publishDataForChannel(channel.getUID()));
    }

    private void publishDataForChannel(ChannelUID channelUID) {
        SenseBoxData localData = data;
        if (localData != null && isLinked(channelUID)) {
            switch (channelUID.getId()) {
                case CHANNEL_LOCATION:
                    updateState(channelUID, locationFromData(localData.getLocation()));
                    break;
                case CHANNEL_UV_INTENSITY:
                    updateState(channelUID, decimalFromSensor(localData.getUvIntensity()));
                    break;
                case CHANNEL_ILLUMINANCE:
                    updateState(channelUID, decimalFromSensor(localData.getLuminance()));
                    break;
                case CHANNEL_PRESSURE:
                    updateState(channelUID, decimalFromSensor(localData.getPressure()));
                    break;
                case CHANNEL_HUMIDITY:
                    updateState(channelUID, decimalFromSensor(localData.getHumidity()));
                    break;
                case CHANNEL_TEMPERATURE:
                    updateState(channelUID, decimalFromSensor(localData.getTemperature()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5:
                    updateState(channelUID, decimalFromSensor(localData.getParticulateMatter2dot5()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_10:
                    updateState(channelUID, decimalFromSensor(localData.getParticulateMatter10()));
                    break;
                case CHANNEL_UV_INTENSITY_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getUvIntensity()));
                    break;
                case CHANNEL_ILLUMINANCE_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getLuminance()));
                    break;
                case CHANNEL_PRESSURE_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getPressure()));
                    break;
                case CHANNEL_HUMIDITY_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getHumidity()));
                    break;
                case CHANNEL_TEMPERATURE_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getTemperature()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getParticulateMatter2dot5()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_10_LR:
                    updateState(channelUID, dateTimeFromSensor(localData.getParticulateMatter10()));
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        }
    }

    private State dateTimeFromSensor(@Nullable SenseBoxSensor sensorData) {
        State result = UnDefType.UNDEF;
        if (sensorData != null && sensorData.getLastMeasurement() != null
                && sensorData.getLastMeasurement().getCreatedAt() != null
                && !sensorData.getLastMeasurement().getCreatedAt().isEmpty()) {
            result = new DateTimeType(sensorData.getLastMeasurement().getCreatedAt());
        }
        return result;
    }

    private State decimalFromSensor(@Nullable SenseBoxSensor sensorData) {
        State result = UnDefType.UNDEF;
        if (sensorData != null && sensorData.getLastMeasurement() != null
                && sensorData.getLastMeasurement().getValue() != null
                && !sensorData.getLastMeasurement().getValue().isEmpty()) {
            logger.debug("About to determine quantity for {} / {}", sensorData.getLastMeasurement().getValue(),
                    sensorData.getUnit());
            BigDecimal bd = new BigDecimal(sensorData.getLastMeasurement().getValue());
            switch (sensorData.getUnit()) {
                case "%":
                    result = new QuantityType<>(bd, Units.PERCENT);
                    break;
                case "°C":
                    result = new QuantityType<>(bd, SIUnits.CELSIUS);
                    break;
                case "Pa":
                    result = new QuantityType<>(bd, SIUnits.PASCAL);
                    break;
                case "hPa":
                    if (BigDecimal.valueOf(10000l).compareTo(bd) < 0) {
                        // Some stations report measurements in Pascal, but send 'hPa' as units...
                        bd = bd.divide(ONEHUNDRED);
                    }
                    result = new QuantityType<>(bd, MetricPrefix.HECTO(SIUnits.PASCAL));
                    break;
                case "lx":
                    result = new QuantityType<>(bd, Units.LUX);
                    break;
                case "\u00b5g/m³":
                    result = new QuantityType<>(bd, Units.MICROGRAM_PER_CUBICMETRE);
                    break;
                case "\u00b5W/cm²":
                    result = new QuantityType<>(bd, Units.MICROWATT_PER_SQUARE_CENTIMETRE);
                    break;
                default:
                    // The data provider might have configured some unknown unit, accept at least the
                    // measurement
                    logger.debug("Could not determine unit for '{}', using default", sensorData.getUnit());
                    result = new QuantityType<>(bd, Units.ONE);
            }
            logger.debug("State: '{}'", result);
        }
        return result;
    }

    private State locationFromData(@Nullable SenseBoxLocation locationData) {
        State result = UnDefType.UNDEF;
        if (locationData != null) {
            result = new PointType(new DecimalType(locationData.getLatitude()),
                    new DecimalType(locationData.getLongitude()), new DecimalType(locationData.getHeight()));
        }
        return result;
    }
}
