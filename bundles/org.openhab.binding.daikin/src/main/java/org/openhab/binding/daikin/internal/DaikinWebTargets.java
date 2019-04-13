/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Daiking air conditioning units.
 *
 * @author Tim Waterhouse - Initial Contribution
 *
 */
public class DaikinWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String setControlInfoUri;
    private String getControlInfoUri;
    private String getSensorInfoUri;
    private Logger logger = LoggerFactory.getLogger(DaikinWebTargets.class);

    public DaikinWebTargets(String ipAddress) {
        String baseUri = "http://" + ipAddress + "/";
        setControlInfoUri = baseUri + "aircon/set_control_info";
        getControlInfoUri = baseUri + "aircon/get_control_info";
        getSensorInfoUri = baseUri + "aircon/get_sensor_info";
    }

    public ControlInfo getControlInfo() throws DaikinCommunicationException {
        String response = invoke(getControlInfoUri);
        return ControlInfo.parse(response);
    }

    public void setControlInfo(ControlInfo info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        invoke(setControlInfoUri, queryParams);
    }

    public SensorInfo getSensorInfo() throws DaikinCommunicationException {
        String response = invoke(getSensorInfoUri);
        return SensorInfo.parse(response);
    }

    private String invoke(String uri) throws DaikinCommunicationException {
        return invoke(uri, new HashMap<>());
    }

    private String invoke(String uri, Map<String, String> params) throws DaikinCommunicationException {
        String uriWithParams = uri + paramsToQueryString(params);
        logger.debug("Calling url: {}", uriWithParams);
        String response;
        synchronized (this) {
            try {
                response = HttpUtil.executeUrl("GET", uriWithParams, TIMEOUT_MS);
            } catch (IOException ex) {
                // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
                // error check below consistent.
                response = null;
            }
        }

        if (response == null) {
            throw new DaikinCommunicationException(
                    String.format("Daikin controller returned error while invoking %s", uriWithParams));
        }

        return response;
    }

    private String paramsToQueryString(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }

        return "?" + params.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue())
                .collect(Collectors.joining("&"));
    }
}
