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
package org.openhab.binding.luftdateninfo.internal.handler;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.luftdateninfo.internal.dto.SensorData;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link HTTPHandler} is responsible for HTTP requests and JSON handling
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HTTPHandler {
    private static final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);
    private static String sensorUrl = "http://data.sensor.community/airrohr/v1/sensor/";
    private static @Nullable HttpClient commonHttpClient;

    public static final String P1 = "P1";
    public static final String P2 = "P2";

    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String PRESSURE = "pressure";
    public static final String PRESSURE_SEALEVEL = "pressure_at_sealevel";

    public static final String NOISE_EQ = "noise_LAeq";
    public static final String NOISE_MIN = "noise_LA_min";
    public static final String NOISE_MAX = "noise_LA_max";

    public static void init(HttpClient httpClient) {
        commonHttpClient = httpClient;
    }

    public static @Nullable String getResponse(String sensorId) {
        if (commonHttpClient == null) {
            logger.warn("HTTP Client not initialized");
            return null;
        }
        String url = sensorUrl + sensorId + "/";
        try {
            ContentResponse contentResponse = commonHttpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.debug("Sensor response: {}", httpStatus);
            switch (httpStatus) {
                case 200:
                    return content;
                case 400:
                case 401:
                case 404:
                    logger.info("Sensor response: {}", httpStatus);
                    return null;
                default:
                    return null;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Exception when calling {}", url);
            return null;
        }
    }

    public static @Nullable List<SensorDataValue> getLatestValues(String response) {
        Gson gson = new Gson();
        SensorData[] valueArray = gson.fromJson(response, SensorData[].class);
        if (valueArray.length == 0) {
            return null;
        } else if (valueArray.length == 1) {
            SensorData v = valueArray[0];
            return v.getSensordatavalues();
        } else if (valueArray.length > 1) {
            // declare first item as latest
            SensorData latestData = valueArray[0];
            String latestTimeStr = latestData.getTimeStamp();
            Date latestTime = DateTimeUtils.toDate(latestTimeStr);
            for (int i = 1; i < valueArray.length; i++) {
                SensorData iterData = valueArray[i];
                String iterTimeStr = iterData.getTimeStamp();
                Date iterTime = DateTimeUtils.toDate(iterTimeStr);
                if (iterTime != null && latestTime != null) {
                    if (latestTime.before(iterTime)) {
                        // found item is newer - take it as latest
                        latestTime = iterTime;
                        latestData = iterData;
                    } // else - found item is older - nothing to do

                } else {
                    logger.warn("One or two dates cannot be decoded 1) {} 2) {}", iterTimeStr, latestTimeStr);
                }
            }
            return latestData.getSensordatavalues();
        } else {
            return null;
        }
    }

    public static boolean isParticulate(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            if (!(v.getValue_type().equals(P1) || v.getValue_type().equals(P2))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCondition(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            // check for temperature and humidty - prssure is optinoal for some sensors
            if (!(v.getValue_type().equals(TEMPERATURE) || v.getValue_type().equals(HUMIDITY)
                    || v.getValue_type().equals(PRESSURE) || v.getValue_type().equals(PRESSURE_SEALEVEL))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNoise(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            if (!(v.getValue_type().equals(NOISE_EQ) || v.getValue_type().equals(NOISE_MAX)
                    || v.getValue_type().equals(NOISE_MIN))) {
                return false;
            }
        }
        return true;
    }

}
