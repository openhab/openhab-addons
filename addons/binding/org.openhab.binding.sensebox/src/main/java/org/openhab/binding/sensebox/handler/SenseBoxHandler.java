/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.handler;

import static org.openhab.binding.sensebox.SenseBoxBindingConstants.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import org.openhab.binding.sensebox.config.SenseBoxConfiguration;
import org.openhab.binding.sensebox.internal.ExpiringCache;
import org.openhab.binding.sensebox.model.SenseBoxData;
import org.openhab.binding.sensebox.model.SenseBoxDescriptor;
import org.openhab.binding.sensebox.model.SenseBoxLoc;
import org.openhab.binding.sensebox.model.SenseBoxLocation;
import org.openhab.binding.sensebox.model.SenseBoxSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SenseBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(SenseBoxHandler.class);

    protected SenseBoxConfiguration thingConfiguration;

    private Gson gson = new Gson();

    private SenseBoxData data = new SenseBoxData();

    ScheduledFuture<?> refreshJob;

    private final int CACHE_EXPIRY = 10 * 1000; // 10s

    public SenseBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        thingConfiguration = getConfigAs(SenseBoxConfiguration.class);

        logger.debug("Thing Configuration {} initialized {}", getThing().getUID().toString(),
                thingConfiguration.getSenseBoxId());

        String offlineReason = "";
        boolean validConfig = true;

        if (StringUtils.trimToNull(thingConfiguration.getSenseBoxId()) == null) {
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

        logger.debug("Thing {} initialized {}", getThing().getUID(), getThing().getStatus());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            try {
                data = fetchData();
                publishDataForChannel(channelUID.getId());
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                logger.debug("Exception occurred during fetching data: {}", e.getMessage());
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

                try {
                    data = fetchData();

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
                } catch (IOException e) {
                    logger.debug("Exception occurred during fetching data: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        };

        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, thingConfiguration.getRefreshInterval(),
                TimeUnit.SECONDS);
    }

    private SenseBoxData fetchData() throws IOException {
        return CACHE.get(thingConfiguration.getSenseBoxId());
    }

    private final ExpiringCache<String, SenseBoxData> CACHE = new ExpiringCache<>(CACHE_EXPIRY,
            new ExpiringCache.LoadAction<String, SenseBoxData>() {
                @Override
                public SenseBoxData load(String senseBoxId) throws IOException {
                    return reallyFetchDataFromServer(senseBoxId);
                }
            });

    private SenseBoxData reallyFetchDataFromServer(String senseBoxId) {
        String query = SENSEMAP_API_URL_BASE + "/boxes/" + senseBoxId;

        // the caching layer does not like null values
        SenseBoxData result = new SenseBoxData();

        try {
            URL url = new URL(query);
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                String body = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

                logger.trace("Fetched Data: {}", body);
                SenseBoxData parsedData = gson.fromJson(body, SenseBoxData.class);

                // Could perhaps be simplified via triply-nested arrays
                // http://stackoverflow.com/questions/36946875/how-can-i-parse-geojson-with-gson
                for (SenseBoxLoc loc : parsedData.getLocs()) {
                    if (loc.getGeometry() != null) {
                        List<Double> locationData = loc.getGeometry().getData();
                        if (locationData != null) {
                            SenseBoxLocation location = new SenseBoxLocation();

                            if (locationData.size() > 0) {
                                location.setLongitude(locationData.get(0));
                            }

                            if (locationData.size() > 1) {
                                location.setLatitude(locationData.get(1));
                            }

                            if (locationData.size() > 2) {
                                location.setHeight(locationData.get(2));
                            }

                            parsedData.setLocation(location);
                        }
                    }
                }

                for (SenseBoxSensor sensor : parsedData.getSensors()) {
                    if ("VEML6070".equals(sensor.getSensorType())) {
                        // "unit" is not nicely comparable, so use sensor type for now
                        parsedData.setUvIntensity(sensor);
                    } else if ("SDS 011".equals(sensor.getSensorType())) {
                        // "unit" is not nicely comparable, neither is type, so use sensor title for now
                        if ("PM2.5".equals(sensor.getTitle())) {
                            parsedData.setParticulateMatter2dot5(sensor);
                        } else if ("PM10".equals(sensor.getTitle())) {
                            parsedData.setParticulateMatter10(sensor);
                        } else {
                            logger.debug("SDS 011 sensor title is {}", sensor.getTitle());
                        }
                    } else if ("lx".equals(sensor.getUnit())) {
                        parsedData.setLuminance(sensor);
                    } else if ("hPa".equals(sensor.getUnit())) {
                        parsedData.setPressure(sensor);
                    } else if ("%".equals(sensor.getUnit())) {
                        parsedData.setHumidity(sensor);
                    } else if ("°C".equals(sensor.getUnit())) {
                        parsedData.setTemperature(sensor);
                    } else {
                        logger.debug("    Sensor: {}", sensor);
                        logger.debug("    Sensor unit: {}", sensor.getUnit());
                        logger.debug("    Sensor type: {}", sensor.getSensorType());
                        logger.debug("    Sensor LM: {}", sensor.getLastMeasurement());
                        if (sensor.getLastMeasurement() != null) {
                            logger.debug("    Sensor LM value: {}", sensor.getLastMeasurement().getValue());
                            logger.debug("    Sensor LM date: '{}'", sensor.getLastMeasurement().getCreatedAt());
                        }
                    }
                }

                SenseBoxDescriptor descriptor = new SenseBoxDescriptor();
                descriptor.setApiUrl(query);
                if (StringUtils.isNotEmpty(parsedData.getImage())) {
                    descriptor.setImageUrl(SENSEMAP_IMAGE_URL_BASE + "/" + parsedData.getImage());
                }
                descriptor.setMapUrl(SENSEMAP_MAP_URL_BASE + "/explore/" + senseBoxId);
                parsedData.setDescriptor(descriptor);

                logger.trace("=================================");

                result = parsedData;
            }
        } catch (IOException e) {
            logger.debug("IO problems while fetching data: {} / {}", query, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        return result;
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
