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
    private String AirbaseSetControlInfoUri;
    private String AirbaseGetControlInfoUri;
    private String AirbaseGetSensorInfoUri;
    private String AirbaseBasicInfoUri;
    private String AirbaseGetModelInfoUri;
    private String AirbaseGetZoneInfoUri;
    private String AirbaseSetZoneInfoUri;

    private Logger logger = LoggerFactory.getLogger(DaikinWebTargets.class);

    public DaikinWebTargets(String ipAddress) {
        String baseUri = "http://" + ipAddress + "/";
        setControlInfoUri = baseUri + "aircon/set_control_info";
        getControlInfoUri = baseUri + "aircon/get_control_info";
        getSensorInfoUri = baseUri + "aircon/get_sensor_info";

        //Daikin Airbase API
        AirbaseBasicInfoUri = baseUri + "skyfi/common/basic_info";
        AirbaseSetControlInfoUri = baseUri + "skyfi/aircon/set_control_info";
        AirbaseGetControlInfoUri = baseUri + "skyfi/aircon/get_control_info";
        AirbaseGetSensorInfoUri = baseUri + "skyfi/aircon/get_sensor_info";
        AirbaseGetModelInfoUri = baseUri + "skyfi/aircon/get_model_info";
        AirbaseGetZoneInfoUri = baseUri + "skyfi/aircon/get_zone_setting";
        AirbaseSetZoneInfoUri = baseUri + "skyfi/aircon/set_zone_setting";

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
    public AirbaseControlInfo AirbaseGetControlInfo() throws DaikinCommunicationException {
        String response = invoke(AirbaseGetControlInfoUri);
        return AirbaseControlInfo.parse(response);
    }

    public void AirbaseSetControlInfo(AirbaseControlInfo info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        invoke(AirbaseSetControlInfoUri, queryParams);
    }

    public SensorInfo AirbaseGetSensorInfo() throws DaikinCommunicationException {
        String response = invoke(AirbaseGetSensorInfoUri);
        return SensorInfo.parse(response);
    }

    public AirbaseBasicInfo AirbaseBasicInfo() throws DaikinCommunicationException {
        String response = invoke(AirbaseBasicInfoUri);
        return AirbaseBasicInfo.parse(response);
    }

    public AirbaseModelInfo AirbaseGetModelInfo() throws DaikinCommunicationException {
        String response = invoke(AirbaseGetModelInfoUri);
        return AirbaseModelInfo.parse(response);
    }

    public AirbaseZoneInfo AirbaseGetZoneInfo() throws DaikinCommunicationException {
        String response = invoke(AirbaseGetZoneInfoUri);
        return AirbaseZoneInfo.parse(response);
    }

    public void AirbaseSetZoneInfo(AirbaseZoneInfo zoneinfo, AirbaseModelInfo modelinfo) throws DaikinCommunicationException {
        int count=0;
        count = (zoneinfo.zone1 ? 1 : 0) + (zoneinfo.zone2 ? 1 : 0) + (zoneinfo.zone3 ? 1 : 0) + (zoneinfo.zone4 ? 1 : 0) + (zoneinfo.zone5 ? 1 : 0) + (zoneinfo.zone6 ? 1 : 0) + (zoneinfo.zone7 ? 1 : 0) + (zoneinfo.zone8 ? 1 : 0) + modelinfo.commonzone;
        logger.debug("Number of open zones: \"{}\"", count);

        Map<String, String> queryParams = zoneinfo.getParamString();
        if (count >= 1) invoke(AirbaseSetZoneInfoUri, queryParams);
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
