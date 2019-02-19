/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sensibosky.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.sensibosky.internal.http.SensiboAcStateRequest;
import org.openhab.binding.sensibosky.internal.http.SensiboAcStateResponse;
import org.openhab.binding.sensibosky.internal.model.AcState;
import org.openhab.binding.sensibosky.internal.model.DeviceId;
import org.openhab.binding.sensibosky.internal.model.SensiboMeasurements;
import org.openhab.binding.sensibosky.internal.model.SensiboPods;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SensiboApiConnection} class handles the connection to the sensibo API.
 *
 * @author Robert Kaczmarczyk - Initial contribution
 */
public class SensiboApiConnection {
    private SensiboSkyConfiguration configuration;

    private Gson gson = new Gson();

    private Logger logger = LoggerFactory.getLogger(SensiboApiConnection.class);

    private static final Properties HEADERS = new Properties();

    private static final String METHOD = "GET";

    private static final int TIMEOUT = 30 * 1000; // 30 seconds

    private static final String API_URL = "https://home.sensibo.com/api/v2";

    public SensiboApiConnection(SensiboSkyConfiguration configuration) {
        Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
        HEADERS.put("User-Agent", "openHAB / SensiboSky binding " + version.toString());
        logger.trace("Headers: {}", HEADERS);
        this.configuration = configuration;
    }

    public String getDeviceId() {
        if (configuration.deviceId != null) {
            return configuration.deviceId;
        }
        String query = API_URL + "/users/me/pods?apiKey=" + configuration.apiKey;
        try {
            String body = HttpUtil.executeUrl(METHOD, query, HEADERS, null, null, TIMEOUT);
            SensiboPods pods = gson.fromJson(body, SensiboPods.class);
            boolean hasOnlyOneDevice = pods.result.size() == 1;
            if (!hasOnlyOneDevice) {
                logger.error("More than one devices detected. Use one of the following:");
                String ids = "";
                for (DeviceId deviceId : pods.result) {
                    logger.error(deviceId.id);
                }
            }
            if (pods.status.equals("success") && hasOnlyOneDevice) {
                return pods.result.get(0).id;
            }

            return "";
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return "";
    }

    public SensiboMeasurements getCurrentTemperatureAndHumidity() {
        String query = API_URL + "/pods/" + getDeviceId() + "?apiKey=" + configuration.apiKey
                + "&fields=measurements,temperatureUnit";
        try {
            String body = HttpUtil.executeUrl(METHOD, query, HEADERS, null, null, TIMEOUT);
            SensiboMeasurements measurements = gson.fromJson(body, SensiboMeasurements.class);
            return measurements;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public void setAcState(AcState newState) {
        String query = API_URL + "/pods/" + getDeviceId() + "/acStates?limit=1&apiKey=" + configuration.apiKey;
        SensiboAcStateRequest request = new SensiboAcStateRequest();
        request.acState = newState;
        String body = gson.toJson(request);
        try {
            String response = HttpUtil.executeUrl("POST", query, HEADERS, new ByteArrayInputStream(body.getBytes()),
                    "application/json", TIMEOUT);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public SensiboAcStateResponse readAcState() {
        String query = API_URL + "/pods/" + getDeviceId() + "/acStates?limit=1&apiKey=" + configuration.apiKey;
        try {
            String body = HttpUtil.executeUrl(METHOD, query, HEADERS, null, null, TIMEOUT);
            SensiboAcStateResponse response = gson.fromJson(body, SensiboAcStateResponse.class);
            return response;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return null;
    }
}
