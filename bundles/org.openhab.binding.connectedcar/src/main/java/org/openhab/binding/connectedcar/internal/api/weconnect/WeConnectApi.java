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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.GeoPosition;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiResult;
import org.openhab.binding.connectedcar.internal.api.ApiWithOAuth;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCCapability;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCParkingPosition;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeConnectApi} implements the WeConnect API calls
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class WeConnectApi extends ApiWithOAuth implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(WeConnectApi.class);
    private Map<String, WCVehicle> vehicleData = new HashMap<>();
    private boolean capParkingPos = true;

    public WeConnectApi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public String getApiUrl() {
        return "https://emea.bff.cariad.digital/vehicle/v1";
    }

    @Override
    public String getHomeReguionUrl() {
        return getApiUrl();
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        ApiHttpMap params = crerateParameters();
        WCVehicleList weList = callApi("", "/vehicles", params.getHeaders(), "getVehicleList", WCVehicleList.class);
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
        VehicleStatus status = new VehicleStatus(getWCStatus());
        WCVehicleStatusData wcStatus = status.wcStatus;
        if (wcStatus != null && wcStatus.userCapabilities != null
                && wcStatus.userCapabilities.capabilitiesStatus != null
                && wcStatus.userCapabilities.capabilitiesStatus.value != null
                && hasCapability(wcStatus.userCapabilities.capabilitiesStatus.value, WCCAPABILITY_PARKINGPOS)) {
            status.parkingPosition = getParkingPosition();
        }
        // getChargingStations("50.577417", "7.240451");
        return status;
    }

    public void getChargingStations(String latitude, String longitude) throws ApiException {
        String json = callApi("", "/charging-stations/v2?latitude=" + latitude + "&longitude=" + longitude,
                crerateParameters().getHeaders(), "getChargingStations", String.class);
    }

    public GeoPosition getParkingPosition() throws ApiException {
        if (capParkingPos) {
            try {
                WCParkingPosition pos = callApi("", "/vehicles/{2}/parkingposition", crerateParameters().getHeaders(),
                        "getParkingPosition", WCParkingPosition.class);
                return new GeoPosition(pos.data);
            } catch (ApiException e) {
                ApiResult res = e.getApiResult();
                if (res.isHttpNoContent()) {
                    // (Temporary) not available -> ignore
                } else if ("4112".equals(res.getApiError().code) || res.isHttpNotFound() || res.isHttpUnauthorized()) {
                    // Service/Capability not available: Ignore, but disable further calls
                    capParkingPos = false;
                } else {
                    throw e;
                }
            }
        }
        return new GeoPosition();
    }

    public GeoPosition getVehicleLocation() throws ApiException {
        // so far the endpoint is unknown
        return new GeoPosition();
    }

    @Override
    public String refreshVehicleStatus() {
        // For now it's unclear if there is an API call to request a status update from
        // the vehicle
        return API_REQUEST_SUCCESSFUL;
    }

    private WCVehicleStatusData getWCStatus() throws ApiException {
        ApiHttpMap params = crerateParameters();
        return callApi("",
                "vehicles/{2}/selectivestatus?jobs=access%2CbatteryChargingCare%2CbatterySupport%2Ccharging%2CchargingProfiles%2Cclimatisation%2CclimatisationTimers%2CfuelStatus%2ChonkAndFlash%2CuserCapabilities%2CvehicleHealthWarnings%2CvehicleHealthInspection%2CvehicleLights",
                params.getHeaders(), "getVehicleStatus", WCVehicleStatusData.class);
    }

    @Override
    public String controlClimater(boolean start, String heaterSource) throws ApiException {
        String action = (start ? "start" : "stop");
        return sendAction(WCSERVICE_CLIMATISATION, action, "");
    }

    @Override
    public String controlClimaterTemp(double tempC, String heaterSource) throws ApiException {
        try {
            WCVehicleStatusData status = getWCStatus();

            Double tempK = SIUnits.CELSIUS.getConverterToAny(Units.KELVIN).convert(tempC);
            status.climatisation.climatisationSettings.value.targetTemperature_C = tempC;
            status.climatisation.climatisationSettings.value.targetTemperature_K = tempK;
            String payload = gson.toJson(status.climatisation.climatisationSettings.value);
            payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
            return sendSettings(WCSERVICE_CLIMATISATION, payload);
        } catch (IncommensurableException e) {
            throw new ApiException("Unable to convert temperature", e);
        }
    }

    @Override
    public String controlWindowHeating(boolean start) throws ApiException {
        WCVehicleStatusData status = getWCStatus();
        status.climatisation.climatisationSettings.value.windowHeatingEnabled = start;
        String payload = gson.toJson(status.climatisation.climatisationSettings.value);
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
        WCVehicleStatusData status = getWCStatus();
        status.charging.chargingSettings.value.maxChargeCurrentAC = "" + maxCurrent;
        String payload = gson.toJson(status.charging.chargingSettings.value);
        payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
        return sendSettings(WCSERVICE_CHARGING, payload);
    }

    @Override
    public String controlTargetChgLevel(int targetLevel) throws ApiException {
        WCVehicleStatusData status = getWCStatus();
        status.charging.chargingSettings.value.targetSOC_pct = targetLevel;
        String payload = gson.toJson(status.charging.chargingSettings.value);
        payload = payload.replaceAll("\"carCapturedTimestamp\".*,", payload);
        return sendSettings(WCSERVICE_CHARGING, payload);
    }

    private boolean hasCapability(ArrayList<WCCapability> capabilities, String capability) {
        for (WCCapability cap : capabilities) {
            if (capability.equals(cap.id)) {
                return true;
            }
        }
        return false;
    }

    private String sendAction(String service, String action, String body) throws ApiException {
        ApiHttpMap headers = crerateParameters();
        String json = http.post("vehicles/{2}/" + service + "/" + action, headers.getHeaders(), body).response;
        return API_REQUEST_STARTED;
    }

    private String sendSettings(String service, String body) throws ApiException {
        ApiHttpMap headers = crerateParameters();
        String json = http.put("vehicles/{2}/" + service + "/settings", headers.getHeaders(), body).response;
        return API_REQUEST_STARTED;
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
        return new ApiHttpMap().headers(config.api.stdHeaders).header(HttpHeader.AUTHORIZATION,
                "Bearer " + createAccessToken());
    }
}
