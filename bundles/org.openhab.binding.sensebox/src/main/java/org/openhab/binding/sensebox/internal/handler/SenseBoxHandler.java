/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sensebox.internal.SenseBoxAPIConnection;
import org.openhab.binding.sensebox.internal.config.SenseBoxConfiguration;
import org.openhab.binding.sensebox.internal.model.SenseBoxData;
import org.openhab.binding.sensebox.internal.model.SenseBoxLocation;
import org.openhab.binding.sensebox.internal.model.SenseBoxSensor;
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
    private Logger logger = LoggerFactory.getLogger(SenseBoxHandler.class);

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
        logger.debug("Thing Configuration {} initialized {}", getThing().getUID().toString(), senseBoxId);

        String offlineReason = "";
        boolean validConfig = true;

        if (StringUtils.trimToNull(senseBoxId) == null) {
            offlineReason = "senseBox ID is mandatory and must be configured";
            validConfig = false;
        }

        if (thingConfiguration.getRefreshInterval() < MINIMUM_UPDATE_INTERVAL) {
            logger.info("Refresh interval is much too small, setting to default of {} seconds",
                    MINIMUM_UPDATE_INTERVAL);
            thingConfiguration.setRefreshInterval(MINIMUM_UPDATE_INTERVAL);
        }

        cache.put(CACHE_KEY_DATA, () -> {
            return connection.reallyFetchDataFromServer(senseBoxId);
        });

        if (validConfig) {
            updateStatus(ThingStatus.UNKNOWN);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, offlineReason);
        }

        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            data = fetchData();
            if (data != null && ThingStatus.ONLINE == data.getStatus()) {
                publishDataForChannel(channelUID.getId());

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

            publishDataForChannel(CHANNEL_LOCATION);

            publishDataForChannel(CHANNEL_UV_INTENSITY);
            publishDataForChannel(CHANNEL_ILLUMINANCE);
            publishDataForChannel(CHANNEL_PRESSURE);
            publishDataForChannel(CHANNEL_HUMIDITY);
            publishDataForChannel(CHANNEL_TEMPERATURE);
            publishDataForChannel(CHANNEL_PARTICULATE_MATTER_2_5);
            publishDataForChannel(CHANNEL_PARTICULATE_MATTER_10);

            publishDataForChannel(CHANNEL_UV_INTENSITY_LR);
            publishDataForChannel(CHANNEL_ILLUMINANCE_LR);
            publishDataForChannel(CHANNEL_PRESSURE_LR);
            publishDataForChannel(CHANNEL_HUMIDITY_LR);
            publishDataForChannel(CHANNEL_TEMPERATURE_LR);
            publishDataForChannel(CHANNEL_PARTICULATE_MATTER_2_5_LR);
            publishDataForChannel(CHANNEL_PARTICULATE_MATTER_10_LR);

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
            thing.setProperty(PROPERTY_NAME, localData.getName());
            thing.setProperty(PROPERTY_EXPOSURE, localData.getExposure());
            thing.setProperty(PROPERTY_IMAGE_URL, localData.getDescriptor().getImageUrl());
            thing.setProperty(PROPERTY_MAP_URL, localData.getDescriptor().getMapUrl());
        }
    }

    private void publishDataForChannel(String channelID) {
        SenseBoxData localData = data;
        if (localData != null && isLinked(channelID)) {
            switch (channelID) {
                case CHANNEL_LOCATION:
                    updateState(CHANNEL_LOCATION, locationFromData(localData.getLocation()));
                    break;
                case CHANNEL_UV_INTENSITY:
                    updateState(CHANNEL_UV_INTENSITY, decimalFromSensor(localData.getUvIntensity()));
                    break;
                case CHANNEL_ILLUMINANCE:
                    updateState(CHANNEL_ILLUMINANCE, decimalFromSensor(localData.getLuminance()));
                    break;
                case CHANNEL_PRESSURE:
                    updateState(CHANNEL_PRESSURE, decimalFromSensor(localData.getPressure()));
                    break;
                case CHANNEL_HUMIDITY:
                    updateState(CHANNEL_HUMIDITY, decimalFromSensor(localData.getHumidity()));
                    break;
                case CHANNEL_TEMPERATURE:
                    updateState(CHANNEL_TEMPERATURE, decimalFromSensor(localData.getTemperature()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5:
                    updateState(CHANNEL_PARTICULATE_MATTER_2_5,
                            decimalFromSensor(localData.getParticulateMatter2dot5()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_10:
                    updateState(CHANNEL_PARTICULATE_MATTER_10, decimalFromSensor(localData.getParticulateMatter10()));
                    break;
                case CHANNEL_UV_INTENSITY_LR:
                    updateState(CHANNEL_UV_INTENSITY_LR, dateTimeFromSensor(localData.getUvIntensity()));
                    break;
                case CHANNEL_ILLUMINANCE_LR:
                    updateState(CHANNEL_ILLUMINANCE_LR, dateTimeFromSensor(localData.getLuminance()));
                    break;
                case CHANNEL_PRESSURE_LR:
                    updateState(CHANNEL_PRESSURE_LR, dateTimeFromSensor(localData.getPressure()));
                    break;
                case CHANNEL_HUMIDITY_LR:
                    updateState(CHANNEL_HUMIDITY_LR, dateTimeFromSensor(localData.getHumidity()));
                    break;
                case CHANNEL_TEMPERATURE_LR:
                    updateState(CHANNEL_TEMPERATURE_LR, dateTimeFromSensor(localData.getTemperature()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_2_5_LR:
                    updateState(CHANNEL_PARTICULATE_MATTER_2_5_LR,
                            dateTimeFromSensor(localData.getParticulateMatter2dot5()));
                    break;
                case CHANNEL_PARTICULATE_MATTER_10_LR:
                    updateState(CHANNEL_PARTICULATE_MATTER_10_LR,
                            dateTimeFromSensor(localData.getParticulateMatter10()));
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelID);
                    break;
            }
        }
    }

    private State dateTimeFromSensor(@Nullable SenseBoxSensor sensorData) {
        State result = UnDefType.UNDEF;

        if (sensorData != null && sensorData.getLastMeasurement() != null
                && StringUtils.isNotEmpty(sensorData.getLastMeasurement().getCreatedAt())) {
            result = new DateTimeType(sensorData.getLastMeasurement().getCreatedAt());
        }

        return result;
    }

    private State decimalFromSensor(@Nullable SenseBoxSensor sensorData) {
        State result = UnDefType.UNDEF;

        if (sensorData != null && sensorData.getLastMeasurement() != null
                && StringUtils.isNotEmpty(sensorData.getLastMeasurement().getValue())) {
            logger.debug("About to determine quantity for {} / {}", sensorData.getLastMeasurement().getValue(),
                    sensorData.getUnit());
            BigDecimal bd = new BigDecimal(sensorData.getLastMeasurement().getValue());

            switch (sensorData.getUnit()) {
                case "%":
                    result = new QuantityType<>(bd, SmartHomeUnits.PERCENT);
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
                    result = new QuantityType<>(bd, SmartHomeUnits.LUX);
                    break;
                case "\u00b5g/m³":
                    result = new QuantityType<>(bd, SmartHomeUnits.MICROGRAM_PER_CUBICMETRE);
                    break;
                case "\u00b5W/cm²":
                    result = new QuantityType<>(bd, SmartHomeUnits.MICROWATT_PER_SQUARE_CENTIMETRE);
                    break;
                default:
                    // The data provider might have configured some unknown unit, accept at least the
                    // measurement
                    logger.debug("Could not determine unit for '{}', using default", sensorData.getUnit());
                    result = new QuantityType<>(bd, SmartHomeUnits.ONE);
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
