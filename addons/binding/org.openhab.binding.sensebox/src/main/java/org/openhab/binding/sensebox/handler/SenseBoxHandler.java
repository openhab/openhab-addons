/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.handler;

import static org.openhab.binding.sensebox.SenseBoxBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
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
 */
public class SenseBoxHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(SenseBoxHandler.class);

    protected SenseBoxConfiguration thingConfiguration;

    private SenseBoxData data = new SenseBoxData();

    ScheduledFuture<?> refreshJob;

    private static final String CACHE_KEY_DATA = "DATA";

    private final ExpiringCacheMap<String, SenseBoxData> cache = new ExpiringCacheMap<String, SenseBoxData>(CACHE_EXPIRY);

    private final SenseBoxAPIConnection connection = new SenseBoxAPIConnection();

    public SenseBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        thingConfiguration = getConfigAs(SenseBoxConfiguration.class);

        String senseBoxId = thingConfiguration.getSenseBoxId();
        logger.debug("Thing Configuration {} initialized {}", getThing().getUID().toString(),
                senseBoxId);

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

        if (validConfig) {
            updateStatus(ThingStatus.UNKNOWN);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_ERROR, offlineReason);
        }

        cache.put(CACHE_KEY_DATA, () -> {
            return connection.reallyFetchDataFromServer(senseBoxId);
        });

        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            data = fetchData();
            if (ThingStatus.ONLINE == data.getStatus()) {
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
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void startAutomaticRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("Refreshing data for box {}, scheduled after {} seconds...",
                        thingConfiguration.getSenseBoxId(), thingConfiguration.getRefreshInterval());

                data = fetchData();
                if (ThingStatus.ONLINE == data.getStatus()) {

                    publishProperties();

                    publishDataForChannel(CHANNEL_LOCATION);

                    publishDataForChannel(CHANNEL_UV_INTENSITY);
                    publishDataForChannel(CHANNEL_LUMINANCE);
                    publishDataForChannel(CHANNEL_PRESSURE);
                    publishDataForChannel(CHANNEL_HUMIDITY);
                    publishDataForChannel(CHANNEL_TEMPERATURE);
                    publishDataForChannel(CHANNEL_PARTICULATE_MATTER_2_5);
                    publishDataForChannel(CHANNEL_PARTICULATE_MATTER_10);

                    publishDataForChannel(CHANNEL_UV_INTENSITY_LR);
                    publishDataForChannel(CHANNEL_LUMINANCE_LR);
                    publishDataForChannel(CHANNEL_PRESSURE_LR);
                    publishDataForChannel(CHANNEL_HUMIDITY_LR);
                    publishDataForChannel(CHANNEL_TEMPERATURE_LR);
                    publishDataForChannel(CHANNEL_PARTICULATE_MATTER_2_5_LR);
                    publishDataForChannel(CHANNEL_PARTICULATE_MATTER_10_LR);

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        };

        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, thingConfiguration.getRefreshInterval(),
                TimeUnit.SECONDS);
    }

    private SenseBoxData fetchData() {
       return cache.get(CACHE_KEY_DATA);
    }

    private void publishProperties() {
        thing.setProperty(PROPERTY_NAME, data.getName());
        thing.setProperty(PROPERTY_EXPOSURE, data.getExposure());
        thing.setProperty(PROPERTY_IMAGE_URL, data.getDescriptor().getImageUrl());
        thing.setProperty(PROPERTY_MAP_URL, data.getDescriptor().getMapUrl());
    }

    private void publishDataForChannel(String channelID) {
        switch (channelID) {
            case CHANNEL_LOCATION:
                updateState(CHANNEL_LOCATION, locationFromData(data.getLocation()));
                break;
            case CHANNEL_UV_INTENSITY:
                updateState(CHANNEL_UV_INTENSITY, decimalFromSensor(data.getUvIntensity()));
                break;
            case CHANNEL_LUMINANCE:
                updateState(CHANNEL_LUMINANCE, decimalFromSensor(data.getLuminance()));
                break;
            case CHANNEL_PRESSURE:
                updateState(CHANNEL_PRESSURE, decimalFromSensor(data.getPressure()));
                break;
            case CHANNEL_HUMIDITY:
                updateState(CHANNEL_HUMIDITY, decimalFromSensor(data.getHumidity()));
                break;
            case CHANNEL_TEMPERATURE:
                updateState(CHANNEL_TEMPERATURE, decimalFromSensor(data.getTemperature()));
                break;
            case CHANNEL_PARTICULATE_MATTER_2_5:
                updateState(CHANNEL_PARTICULATE_MATTER_2_5, decimalFromSensor(data.getParticulateMatter2dot5()));
                break;
            case CHANNEL_PARTICULATE_MATTER_10:
                updateState(CHANNEL_PARTICULATE_MATTER_10, decimalFromSensor(data.getParticulateMatter10()));
                break;
            case CHANNEL_UV_INTENSITY_LR:
                updateState(CHANNEL_UV_INTENSITY_LR, dateTimeFromSensor(data.getUvIntensity()));
                break;
            case CHANNEL_LUMINANCE_LR:
                updateState(CHANNEL_LUMINANCE_LR, dateTimeFromSensor(data.getLuminance()));
                break;
            case CHANNEL_PRESSURE_LR:
                updateState(CHANNEL_PRESSURE_LR, dateTimeFromSensor(data.getPressure()));
                break;
            case CHANNEL_HUMIDITY_LR:
                updateState(CHANNEL_HUMIDITY_LR, dateTimeFromSensor(data.getHumidity()));
                break;
            case CHANNEL_TEMPERATURE_LR:
                updateState(CHANNEL_TEMPERATURE_LR, dateTimeFromSensor(data.getTemperature()));
                break;
            case CHANNEL_PARTICULATE_MATTER_2_5_LR:
                updateState(CHANNEL_PARTICULATE_MATTER_2_5_LR, dateTimeFromSensor(data.getParticulateMatter2dot5()));
                break;
            case CHANNEL_PARTICULATE_MATTER_10_LR:
                updateState(CHANNEL_PARTICULATE_MATTER_10_LR, dateTimeFromSensor(data.getParticulateMatter10()));
                break;
            default:
                logger.debug("Command received for an unknown channel: {}", channelID);
                break;
        }
    }

    private State dateTimeFromSensor(SenseBoxSensor data) {
        State result = UnDefType.UNDEF;

        if (data != null) {
            if (data.getLastMeasurement() != null) {
                if (StringUtils.isNotEmpty(data.getLastMeasurement().getCreatedAt())) {
                    result = new DateTimeType(data.getLastMeasurement().getCreatedAt());
                }
            }
        }

        return result;
    }

    private State decimalFromSensor(SenseBoxSensor data) {
        State result = UnDefType.UNDEF;

        if (data != null) {
            if (data.getLastMeasurement() != null) {
                if (StringUtils.isNotEmpty(data.getLastMeasurement().getValue())) {
                    result = new DecimalType(data.getLastMeasurement().getValue());
                }
            }
        }

        return result;
    }

    private State locationFromData(SenseBoxLocation data) {
        State result = UnDefType.UNDEF;

        if (data != null) {
            result = new PointType(new DecimalType(data.getLatitude()), new DecimalType(data.getLongitude()),
                    new DecimalType(data.getHeight()));
        }

        return result;
    }
}
