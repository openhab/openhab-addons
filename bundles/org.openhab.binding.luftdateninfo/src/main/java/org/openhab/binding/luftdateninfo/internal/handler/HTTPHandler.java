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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BufferingResponseListener;
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

    public synchronized void request(int sensorId, BaseSensorHandler callback) {
        HttpClient localClient = commonHttpClient;
        if (localClient == null) {
            logger.warn("HTTP Client not initialized");
        } else {
            String url = sensorUrl + sensorId + "/";
            Request req = localClient.newRequest(url);
            req.timeout(15, TimeUnit.SECONDS).send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(org.eclipse.jetty.client.api.Result result) {
                    if (result.getResponse().getStatus() != 200) {
                        String failure;
                        if (result.getResponse().getReason() != null) {
                            failure = result.getResponse().getReason();
                        } else {
                            failure = result.getFailure().getMessage();
                        }
                        callback.onError(Objects.requireNonNullElse(failure, "Unknown error"));
                    } else {
                        callback.onResponse(getContentAsString());
                    }
                }
            });
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
            LocalDateTime latestTime = DateTimeUtils.toDate(latestTimeStr);
            if (latestTime == null) {
                logDateConversionError(response, latestData);
            }
            for (int i = 1; i < valueArray.length; i++) {
                SensorData iterData = valueArray[i];
                String iterTimeStr = iterData.getTimeStamp();
                LocalDateTime iterTime = DateTimeUtils.toDate(iterTimeStr);
                if (iterTime == null) {
                    logDateConversionError(response, latestData);
                }
                if (iterTime != null && latestTime != null) {
                    if (latestTime.isBefore(iterTime)) {
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
