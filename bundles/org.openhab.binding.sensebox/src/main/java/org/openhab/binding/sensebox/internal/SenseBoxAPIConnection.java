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
package org.openhab.binding.sensebox.internal;

import static org.openhab.binding.sensebox.internal.SenseBoxBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sensebox.internal.dto.SenseBoxData;
import org.openhab.binding.sensebox.internal.dto.SenseBoxDescriptor;
import org.openhab.binding.sensebox.internal.dto.SenseBoxLoc;
import org.openhab.binding.sensebox.internal.dto.SenseBoxLocation;
import org.openhab.binding.sensebox.internal.dto.SenseBoxSensor;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ThingStatus;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SenseBoxAPIConnection} is responsible for fetching data from the senseBox API server.
 *
 * @author Hakan Tandogan - Initial contribution
 */
@NonNullByDefault
public class SenseBoxAPIConnection {

    private final Logger logger = LoggerFactory.getLogger(SenseBoxAPIConnection.class);

    private final Gson gson = new Gson();

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

        String body = null;
        try {
            body = HttpUtil.executeUrl(METHOD, query, HEADERS, null, null, TIMEOUT);

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
                        int locationDataCount = locationData.size();
                        SenseBoxLocation location = new SenseBoxLocation();

                        if (locationDataCount > 0) {
                            location.setLongitude(locationData.get(0));
                        }

                        if (locationDataCount > 1) {
                            location.setLatitude(locationData.get(1));
                        }

                        if (locationDataCount > 2) {
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
                } else if ("Pa".equals(sensor.getUnit()) || "hPa".equals(sensor.getUnit())) {
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
            String image = parsedData.getImage();
            if (image != null && !image.isEmpty()) {
                descriptor.setImageUrl(SENSEMAP_IMAGE_URL_BASE + "/" + image);
            }
            descriptor.setMapUrl(SENSEMAP_MAP_URL_BASE + "/explore/" + senseBoxId);
            parsedData.setDescriptor(descriptor);

            logger.trace("=================================");

            result = parsedData;
        } catch (JsonSyntaxException e) {
            logger.debug("An error occurred while parsing the data into desired class: {} / {} / {}", body,
                    SenseBoxData.class.getName(), e.getMessage());
            result.setStatus(ThingStatus.OFFLINE);
        } catch (IOException e) {
            logger.debug("IO problems while fetching data: {} / {}", query, e.getMessage());
            result.setStatus(ThingStatus.OFFLINE);
        }

        return result;
    }
}
