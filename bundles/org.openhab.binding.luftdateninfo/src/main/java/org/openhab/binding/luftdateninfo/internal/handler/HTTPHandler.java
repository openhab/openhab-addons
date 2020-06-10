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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.luftdateninfo.internal.dto.SensorData;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link HTTPHandler} is responsible for HTTP requests and JSON handling
 *
 * @author Bernd Weymann - Initial contribution
 */

public class HTTPHandler {
    private static final Logger logger = LoggerFactory.getLogger(HTTPHandler.class);
    private static String sensorUrl = "http://data.sensor.community/airrohr/v1/sensor/";
    private static HttpClient commonHttpClient;

    public final static String P1 = "P1";
    public final static String P2 = "P2";

    public final static String TEMPERATURE = "temperature";
    public final static String HUMIDITY = "humidity";
    public final static String PRESSURE = "pressure";
    public final static String PRESSURE_SEALEVEL = "pressure_at_sealevel";

    public final static String NOISE_EQ = "noise_LAeq";
    public final static String NOISE_MIN = "noise_LA_min";
    public final static String NOISE_MAX = "noise_LA_max";

    public static void init(HttpClient httpClient) {
        commonHttpClient = httpClient;
    }

    public static String getResponse(String sensorId) {
        String url = sensorUrl + sensorId + "/";
        try {
            ContentResponse contentResponse = commonHttpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            System.out.println("Sensor response: " + httpStatus);
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

    public static List<SensorDataValue> getValues(String response) {
        System.out.println(response);
        Gson gson = new Gson();
        SensorData[] valueArray = gson.fromJson(response, SensorData[].class);
        if (valueArray.length > 0) {
            SensorData v = valueArray[0];
            System.out.println(valueArray[0]);
            List<SensorDataValue> l = v.getSensordatavalues();
            System.out.println(l.size());
            System.out.println(l.get(0));
            System.out.println(l.get(1));
            return l;
        } else {
            return null;
        }
    }

    public static boolean isParticulate(List<SensorDataValue> valueList) {
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            if (v.getValue_type().equals(P1) || v.getValue_type().equals(P2)) {
                // continue
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isCondition(List<SensorDataValue> valueList) {
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            // check for temperature and humidty - prssure is optinoal for some sensors
            if (v.getValue_type().equals(TEMPERATURE) || v.getValue_type().equals(HUMIDITY)) {
                // continue
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isNoise(List<SensorDataValue> valueList) {
        Iterator<SensorDataValue> iter = valueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            if (v.getValue_type().equals(NOISE_EQ) || v.getValue_type().equals(NOISE_MAX)
                    || v.getValue_type().equals(NOISE_MIN)) {
                // continue
            } else {
                return false;
            }
        }
        return true;
    }

}
