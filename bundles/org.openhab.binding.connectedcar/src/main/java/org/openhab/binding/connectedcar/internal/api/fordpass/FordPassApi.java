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
package org.openhab.binding.connectedcar.internal.api.fordpass;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.*;
import static org.openhab.binding.connectedcar.internal.api.skodaenyak.SEApiJsonDTO.SESERVICE_CHARGING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.measure.IncommensurableException;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.TokenManager;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPRefreshResponse;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleListData;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleListData.FPVehicleData.FPVehicle;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FordPassApi} implements the Ford Connect API
 *
 * @author Markus Michels - Initial contribution
 */
public class FordPassApi extends ApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(FordPassApi.class);
    private final Map<String, FPVehicle> vehicleList = new HashMap<>();
    private final static String URL_PARM_LRDT = "?lrdt=" + urlEncode("01-01-1970 00:00:00");

    public FordPassApi(ApiHttpClient httpClient, TokenManager tokenManager, @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public String getApiUrl() {
        return config.api.apiDefaultUrl;
    }

    @Override
    public String getHomeReguionUrl() {
        return getApiUrl();
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        ApiHttpMap params = createApiParameters();
        FPVehicleListData data = callApi("", "users/vehicles", params.getHeaders(), "getVehicleList",
                FPVehicleListData.class);

        /*
         * String details = callApi("", "users/vehicles/{2}/detail", params.getHeaders(), "getVehicleDetails",
         * String.class);
         * String geo = callApi("", "geofence/v1/vehicles/{2}/geofences", params.getHeaders(), "getGeofences",
         * String.class);
         * String v = callApi("", "capability/v1/vehicles/{2}", params.getHeaders(), "getCapability", String.class);
         */
        vehicleList.clear();
        ArrayList<String> list = new ArrayList<String>();
        for (FPVehicle vehicle : data.vehicles.values) {
            list.add(vehicle.vin);
            vehicleList.put(vehicle.vin, vehicle);
        }
        return list;
    }

    @Override
    public VehicleDetails getVehicleDetails(String vin) throws ApiException {
        if (vehicleList.containsKey(vin)) {
            VehicleDetails details = new VehicleDetails();
            FPVehicle vehicle = vehicleList.get(vin);
            if (vehicle != null) {
                details.vin = vehicle.vin;
                details.brand = API_BRAND_FORD;
                details.color = vehicle.color;
                String model = details.brand + " " + vehicle.vehicleType;
                details.model = !vehicle.nickName.isEmpty() ? vehicle.nickName + " (" + model + ")" : model;
            }
            return details;
        }
        throw new IllegalArgumentException("Unknown VIN " + vin);
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        ApiHttpMap params = createApiParameters();
        String json = http.get("vehicles/v4/{2}/status" + URL_PARM_LRDT, params.getHeaders()).response;
        return new VehicleStatus(fromJson(gson, json, FPVehicleStatusData.class));
    }

    @Override
    public String refreshVehicleStatus() throws ApiException {
        ApiHttpMap parms = createApiParameters();
        String json = http.put("vehicles/v2/{2}/status", parms.getHeaders(), "").response;
        FPRefreshResponse rsp = fromJson(gson, json, FPRefreshResponse.class);
        return "200".equals(rsp.status) ? API_REQUEST_SUCCESSFUL : API_REQUEST_FAILED;
    }

    @Override
    public String controlEngine(boolean start) throws ApiException {
        return sendAction(FPSERVICE_ENGINE, start);
    }

    @Override
    public String controlClimater(boolean start, String heaterSource) throws ApiException {
        return sendAction(FPSERVICE_CLIMATISATION, start);
    }

    @Override
    public String controlClimaterTemp(double tempC, String heaterSource) throws ApiException {
        try {
            Double tempK = SIUnits.CELSIUS.getConverterToAny(Units.KELVIN).convert(tempC);
            String payload = "";
            return sendSettings(FPSERVICE_CLIMATISATION, payload);
        } catch (IncommensurableException e) {
            throw new ApiException("Unable to convert temperature", e);
        }
    }

    @Override
    public String controlCharger(boolean start) throws ApiException {
        String action = (start ? "Start" : "Stop");
        return sendAction(FPSERVICE_CHARGER, start);
    }

    @Override
    public String controlWindowHeating(boolean start) throws ApiException {
        return super.controlWindowHeating(start);
    }

    @Override
    public String controlMaxCharge(int maxCurrent) throws ApiException {
        String payload = "";
        return sendSettings(SESERVICE_CHARGING, payload);
    }

    @Override
    public String controlTargetChgLevel(int targetLevel) throws ApiException {
        String payload = "";
        return sendSettings(SESERVICE_CHARGING, payload);
    }

    private String sendAction(String service, boolean start) throws ApiException {
        ApiHttpMap headers = createApiParameters();
        HttpMethod method = start ? HttpMethod.PUT : HttpMethod.DELETE;
        String action = start ? "start" : "stop";
        String uri = "/vehicles/v2/{2}/" + service + "/" + action;
        return http.request(method, uri, "", headers.getHeaders(), "", "", "", false).response;
    }

    private String sendSettings(String service, String body) throws ApiException {
        ApiHttpMap headers = createApiParameters();
        return API_REQUEST_STARTED;
    }

    @Override
    public void checkPendingRequests() {
        // https://github.com/d4v3y0rk/ffpass-module/blob/47eff3118be54167561a29e31ef8863c696ad640/index.js#L212
        /*
         * if (command == 'start' || command == 'stop') {
         * url = `vehicles/v2/{2}/engine/start/${commandId}`
         * } else if (command == 'lock' || command == 'unlock') {
         * url = `vehicles/v2/{2}/doors/lock/${commandId}`
         * } else {
         */
        /*
         * url: `vehicles/v3/{2}/statusrefresh/${commandId}`,
         */
    }

    public String getRequestStatus(String requestId, String rstatus) throws ApiException {
        return API_REQUEST_SUCCESSFUL;
    }

    protected ApiHttpMap createDefaultParameters() throws ApiException {
        return new ApiHttpMap().header(HttpHeader.USER_AGENT, config.api.userAgent).header(HttpHeaders.ACCEPT_LANGUAGE,
                "en-us");
    }

    protected ApiHttpMap createApiParameters() throws ApiException {
        /*
         * 'Accept': '* /*',
         * 'Accept-Language': 'en-us',
         * 'User-Agent': 'fordpass-na/353 CFNetwork/1121.2.2 Darwin/19.3.0',
         */
        return createDefaultParameters().header("Application-Id", config.api.xClientId)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_JSON)//
                .header("Auth-Token", createAccessToken());
    }
}
