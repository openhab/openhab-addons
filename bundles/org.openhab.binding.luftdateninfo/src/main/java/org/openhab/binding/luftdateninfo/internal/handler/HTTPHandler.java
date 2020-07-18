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
    private final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);

    private static final Gson GSON = new Gson();
    private static final HTTPHandler HTTP_HANDLER = new HTTPHandler();

    public static final String P1 = "P1";
    public static final String P2 = "P2";

    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String PRESSURE = "pressure";
    public static final String PRESSURE_SEALEVEL = "pressure_at_sealevel";

    public static final String NOISE_EQ = "noise_LAeq";
    public static final String NOISE_MIN = "noise_LA_min";
    public static final String NOISE_MAX = "noise_LA_max";

    private static String sensorUrl = "http://data.sensor.community/airrohr/v1/sensor/";
    private static @Nullable HttpClient commonHttpClient;

    public static void init(HttpClient httpClient) {
        commonHttpClient = httpClient;
    }

    public static HTTPHandler getHandler() {
        return HTTP_HANDLER;
    }

    public @Nullable String getResponse(int sensorId) {
        HttpClient localClient = commonHttpClient;
        if (localClient == null) {
            logger.warn("HTTP Client not initialized");
            return null;
        } else {
            String url = sensorUrl + sensorId + "/";
            try {
                ContentResponse contentResponse = localClient.newRequest(url).timeout(10, TimeUnit.SECONDS).send();
                int httpStatus = contentResponse.getStatus();
                String content = contentResponse.getContentAsString();
                logger.debug("Sensor response: {}", httpStatus);
                switch (httpStatus) {
                    case 200:
                        return content;
                    default:
                        logger.debug("Sensor response: {}", httpStatus);
                        return null;
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Exception when calling {}. Response: {}", url, e.getMessage());
                return null;
            }
        }
    }

    public @Nullable List<SensorDataValue> getLatestValues(String response) {
        SensorData[] valueArray = GSON.fromJson(response, SensorData[].class);
        if (valueArray.length == 0) {
            return null;
        } else if (valueArray.length == 1) {
            SensorData v = valueArray[0];
            return v.getSensorDataValues();
        } else if (valueArray.length > 1) {
            // declare first item as latest
            SensorData latestData = valueArray[0];
            String latestTimeStr = latestData.getTimeStamp();
            Date latestTime = DateTimeUtils.toDate(latestTimeStr);
            if (latestTime == null) {
                logDateConversionError(response, latestData);
            }
            for (int i = 1; i < valueArray.length; i++) {
                SensorData iterData = valueArray[i];
                String iterTimeStr = iterData.getTimeStamp();
                Date iterTime = DateTimeUtils.toDate(iterTimeStr);
                if (iterTime == null) {
                    logDateConversionError(response, latestData);
                }
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
            return latestData.getSensorDataValues();
        } else {
            return null;
        }
    }

    public void logDateConversionError(final String response, final Object dto) {
        logger.warn("Unable to get timestamp");
        logger.warn("Response: {}", response);
        String json = GSON.toJson(dto);
        logger.warn("GSon: {}", json);
    }

    public boolean isParticulate(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        return valueList.stream().map(v -> v.getValueType()).filter(t -> t.equals(P1) || t.equals(P2)).findAny()
                .isPresent();
    }

    public boolean isCondition(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        return valueList.stream().map(v -> v.getValueType()).filter(
                t -> t.equals(TEMPERATURE) || t.equals(HUMIDITY) || t.equals(PRESSURE) || t.equals(PRESSURE_SEALEVEL))
                .findAny().isPresent();
    }

    public boolean isNoise(@Nullable List<SensorDataValue> valueList) {
        if (valueList == null) {
            return false;
        }
        return valueList.stream().map(v -> v.getValueType())
                .filter(t -> t.equals(NOISE_EQ) || t.equals(NOISE_MAX) || t.equals(NOISE_MIN)).findAny().isPresent();
    }
}
