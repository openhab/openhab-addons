/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.measure.IncommensurableException;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.TokenManager;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeConnectApi} implements the WeConnect API calls
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WeConnectApi extends ApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(WeConnectApi.class);
    private Map<String, WCVehicle> vehicleData = new HashMap<>();

    public WeConnectApi(ApiHttpClient httpClient, TokenManager tokenManager, @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public String getApiUrl() {
        return "https://mobileapi.apps.emea.vwapps.io";
    }

    @Override
    public String getHomeReguionUrl() {
        return getApiUrl();
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        ApiHttpMap params = crerateParameters();
        WCVehicleList weList = callApi("", "https://mobileapi.apps.emea.vwapps.io/vehicles", params.getHeaders(),
                "getVehicleList", WCVehicleList.class);
        ArrayList<String> list = new ArrayList<String>();
        for (WCVehicle wev : weList.data) {
            list.add(wev.vin);
            vehicleData.put(wev.vin, wev);
        }
        return list;
    }

    @Override
    public VehicleDetails getVehicleDetails(String vin) throws ApiException {
        if (vehicleData.containsKey(vin)) {
            WCVehicle vehicle = vehicleData.get(vin);
            return new VehicleDetails(config, vehicle);
        } else {
            throw new ApiException("Unknown VIN: " + vin);
        }
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        return new VehicleStatus(getWCStatus());
    }

    @Override
    public String refreshVehicleStatus() {
        // For now it's unclear if there is an API call to request a status update from the vehicle
        return API_REQUEST_SUCCESSFUL;
    }

    private WCVehicleStatus getWCStatus() throws ApiException {
        ApiHttpMap params = crerateParameters();
        return callApi("", "https://mobileapi.apps.emea.vwapps.io/vehicles/{2}/status", params.getHeaders(),
                "getVehicleStatus", WCVehicleStatusData.class).data;
    }

    @Override
    public String controlClimater(boolean start, String heaterSource) throws ApiException {
        String action = (start ? "start" : "stop");
        return sendAction(WCSERVICE_CLIMATISATION, action, "");
    }

    @Override
    public String controlClimaterTemp(double tempC, String heaterSource) throws ApiException {
        try {
            WCVehicleStatus status = getWCStatus();

            Double tempK = SIUnits.CELSIUS.getConverterToAny(Units.KELVIN).convert(tempC);
            status.climatisationSettings.targetTemperature_C = tempC;
            status.climatisationSettings.targetTemperature_K = tempK;
            String payload = gson.toJson(status.climatisationSettings);
            payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
            return sendSettings(WCSERVICE_CLIMATISATION, payload);
        } catch (IncommensurableException e) {
            throw new ApiException("Unable to convert temperature", e);
        }
    }

    @Override
    public String controlWindowHeating(boolean start) throws ApiException {
        WCVehicleStatus status = getWCStatus();
        status.climatisationSettings.windowHeatingEnabled = start;
        String payload = gson.toJson(status.climatisationSettings);
        payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
        return sendSettings(WCSERVICE_CLIMATISATION, payload);
    }

    @Override
    public String controlCharger(boolean start) throws ApiException {
        String action = (start ? "start" : "stop");
        return sendAction(WCSERVICE_CHARGING, action, "");
    }

    @Override
    public String controlMaxCharge(int maxCurrent) throws ApiException {
        WCVehicleStatus status = getWCStatus();
        status.chargingSettings.maxChargeCurrentAC = "" + maxCurrent;
        String payload = gson.toJson(status.climatisationSettings);
        payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
        return sendSettings(WCSERVICE_CHARGING, payload);
    }

    @Override
    public String controlTargetChgLevel(int targetLevel) throws ApiException {
        WCVehicleStatus status = getWCStatus();
        status.chargingSettings.targetSOC_pct = targetLevel;
        String payload = gson.toJson(status.climatisationSettings);
        payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
        return sendSettings(WCSERVICE_CHARGING, payload);
    }

    private String sendAction(String service, String action, String body) throws ApiException {
        ApiHttpMap headers = crerateParameters();
        String json = http.post("https://mobileapi.apps.emea.vwapps.io/vehicles/{2}/" + service + "/" + action,
                headers.getHeaders(), body).response;
        return API_REQUEST_STARTED;
    }

    private String sendSettings(String service, String body) throws ApiException {
        ApiHttpMap headers = crerateParameters();
        String json = http.put("https://mobileapi.apps.emea.vwapps.io/vehicles/{2}/" + service + "/settings",
                headers.getHeaders(), body).response;
        return API_REQUEST_STARTED;
    }

    @Override
    public void checkPendingRequests() {
    }

    public String getRequestStatus(String requestId, String rstatus) throws ApiException {
        return API_REQUEST_SUCCESSFUL;
    }

    private ApiHttpMap crerateParameters() throws ApiException {
        /*
         * accept: "* / *",
         * "content-type": "application/json",
         * "content-version": "1",
         * "x-newrelic-id": "VgAEWV9QDRAEXFlRAAYPUA==",
         * "user-agent": "WeConnect/5 CFNetwork/1206 Darwin/20.1.0",
         * "accept-language": "de-de",
         * authorization: "Bearer " + this.config.atoken,
         */
        return new ApiHttpMap().header(HttpHeaders.ACCEPT, "*/*").header("content-version", "1")
                .header("x-newrelic-id", "VgAEWV9QDRAEXFlRAAYPUA==").header(HttpHeader.USER_AGENT, config.api.userAgent)
                .header(HttpHeader.ACCEPT_LANGUAGE, "de-de")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + createAccessToken());
    }
}
