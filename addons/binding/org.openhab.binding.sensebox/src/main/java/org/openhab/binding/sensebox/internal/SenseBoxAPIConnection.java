/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sensebox.internal;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.sensebox.internal.model.SenseBoxData;
import org.openhab.binding.sensebox.internal.model.SenseBoxDescriptor;
import org.openhab.binding.sensebox.internal.model.SenseBoxLoc;
import org.openhab.binding.sensebox.internal.model.SenseBoxLocation;
import org.openhab.binding.sensebox.internal.model.SenseBoxSensor;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.openhab.binding.sensebox.SenseBoxBindingConstants.INVALID_BRIGHTNESS;
import static org.openhab.binding.sensebox.SenseBoxBindingConstants.SENSEMAP_API_URL_BASE;
import static org.openhab.binding.sensebox.SenseBoxBindingConstants.SENSEMAP_IMAGE_URL_BASE;
import static org.openhab.binding.sensebox.SenseBoxBindingConstants.SENSEMAP_MAP_URL_BASE;

/**
 * The {@link SenseBoxAPIConnection} is responsible for fetching data from the senseBox API server.
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxAPIConnection {
    private Logger logger = LoggerFactory.getLogger(SenseBoxAPIConnection.class);

    private Gson gson = new Gson();

    private static final Properties HEADERS = new Properties();

    private static final String METHOD = "GET";

    private static final int TIMEOUT = 30 * 1000; // 30 seconds

    public SenseBoxAPIConnection() {
        Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
        HEADERS.put("User-Agent", "openHAB / senseBox binding " + version.toString());
        logger.trace("Headers: {}", HEADERS);
    }

    public SenseBoxData reallyFetchDataFromServer(String senseBoxId) {
        String query = SENSEMAP_API_URL_BASE + "/boxes/" + senseBoxId;

        // the caching layer does not like null values
        SenseBoxData result = new SenseBoxData();

        try {
            String body = HttpUtil.executeUrl(METHOD, query, HEADERS, null, null, TIMEOUT);

            logger.trace("Fetched Data: {}", body);
            SenseBoxData parsedData = gson.fromJson(body, SenseBoxData.class);

            // Assume all is well at first
            parsedData.setStatus(ThingStatus.ONLINE);

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
                    if (sensor.getLastMeasurement() != null) {
                        if (!(INVALID_BRIGHTNESS.equals(sensor.getLastMeasurement().getValue()))) {
                            parsedData.setLuminance(sensor);
                        }
                    }
                } else if ("hPa".equals(sensor.getUnit())) {
                    parsedData.setPressure(sensor);
                } else if ("%".equals(sensor.getUnit())) {
                    parsedData.setHumidity(sensor);
                } else if ("°C".equals(sensor.getUnit())) {
                    parsedData.setTemperature(sensor);
                } else {
                    if (logger.isDebugEnabled()) {
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

        } catch (IOException e) {
            logger.debug("IO problems while fetching data: {} / {}", query, e.getMessage());
            result.setStatus(ThingStatus.OFFLINE);
        }

        return result;
    }
}
