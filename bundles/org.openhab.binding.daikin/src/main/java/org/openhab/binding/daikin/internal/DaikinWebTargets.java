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
import java.util.stream.IntStream;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseControlInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseBasicInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseModelInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseZoneInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Daikin air conditioning units.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley <paul@smedley.id.au> - Modifications to support Airbase Controllers
 *
 */
public class DaikinWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String setControlInfoUri;
    private String getControlInfoUri;
    private String getSensorInfoUri;
    private String setAirbaseControlInfoUri;
    private String getAirbaseControlInfoUri;
    private String getAirbaseSensorInfoUri;
    private String getAirbaseBasicInfoUri;
    private String getAirbaseModelInfoUri;
    private String getAirbaseZoneInfoUri;
    private String setAirbaseZoneInfoUri;

    private Logger logger = LoggerFactory.getLogger(DaikinWebTargets.class);

    public DaikinWebTargets(String ipAddress) {
        String baseUri = "http://" + ipAddress + "/";
        setControlInfoUri = baseUri + "aircon/set_control_info";
        getControlInfoUri = baseUri + "aircon/get_control_info";
        getSensorInfoUri = baseUri + "aircon/get_sensor_info";

        //Daikin Airbase API
        getAirbaseBasicInfoUri = baseUri + "skyfi/common/basic_info";
        setAirbaseControlInfoUri = baseUri + "skyfi/aircon/set_control_info";
        getAirbaseControlInfoUri = baseUri + "skyfi/aircon/get_control_info";
        getAirbaseSensorInfoUri = baseUri + "skyfi/aircon/get_sensor_info";
        getAirbaseModelInfoUri = baseUri + "skyfi/aircon/get_model_info";
        getAirbaseZoneInfoUri = baseUri + "skyfi/aircon/get_zone_setting";
        setAirbaseZoneInfoUri = baseUri + "skyfi/aircon/set_zone_setting";
    }

    // Standard Daikin API
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

    //Daikin Airbase API
    public AirbaseControlInfo getAirbaseControlInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseControlInfoUri);
        return AirbaseControlInfo.parse(response);
    }

    public void setAirbaseControlInfo(AirbaseControlInfo info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        invoke(setAirbaseControlInfoUri, queryParams);
    }

    public SensorInfo getAirbaseSensorInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseSensorInfoUri);
        return SensorInfo.parse(response);
    }

    public AirbaseBasicInfo getAirbaseBasicInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseBasicInfoUri);
        return AirbaseBasicInfo.parse(response);
    }

    public AirbaseModelInfo getAirbaseModelInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseModelInfoUri);
        return AirbaseModelInfo.parse(response);
    }

    public AirbaseZoneInfo getAirbaseZoneInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseZoneInfoUri);
        return AirbaseZoneInfo.parse(response);
    }

    public void setAirbaseZoneInfo(AirbaseZoneInfo zoneinfo, AirbaseModelInfo modelinfo) throws DaikinCommunicationException {
        long count = IntStream.range(0, zoneinfo.zone.length).filter(idx -> zoneinfo.zone[idx]).count() + modelinfo.commonzone;
        logger.debug("Number of open zones: \"{}\"", count);

        Map<String, String> queryParams = zoneinfo.getParamString();
        if (count >= 1) {
            invoke(setAirbaseZoneInfoUri, queryParams);
        }
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
