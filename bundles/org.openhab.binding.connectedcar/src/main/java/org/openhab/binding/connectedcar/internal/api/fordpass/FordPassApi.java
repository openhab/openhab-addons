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
package org.openhab.binding.connectedcar.internal.api.fordpass;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.connectedcar.internal.api.ApiBase;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.ApiActionRequest;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPActionRequest;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPActionRequest.FPActionResponse;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleListData;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleListData.FPVehicleData.FPVehicle;
import org.openhab.binding.connectedcar.internal.api.fordpass.FPApiJsonDTO.FPVehicleStatusData;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FordPassApi} implements the Ford Connect API
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class FordPassApi extends ApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(FordPassApi.class);
    private final Map<String, FPVehicle> vehicleList = new HashMap<>();
    private final static String URL_PARM_LRDT = "?lrdt=" + urlEncode("01-01-1970 00:00:00");

    public FordPassApi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
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
        Map<String, String> params = createApiParameters();
        FPVehicleListData data = callApi("", "users/vehicles", params, "getVehicleList", FPVehicleListData.class);

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
        String json = http.get("vehicles/v4/{2}/status" + URL_PARM_LRDT, createApiParameters()).response;
        return new VehicleStatus(fromJson(gson, json, FPVehicleStatusData.class));
    }

    @Override
    public String refreshVehicleStatus() throws ApiException {
        String json = http.put("vehicles/v2/{2}/status", createApiParameters(), "").response;
        FPActionResponse rsp = fromJson(gson, json, FPActionResponse.class);
        return "200".equals(rsp.status) ? API_REQUEST_SUCCESSFUL : API_REQUEST_FAILED;
    }

    @Override
    public String controlLock(boolean lock) throws ApiException {
        return sendAction(FPSERVICE_DOORS, lock ? "lock" : "unlock", "lock", lock);
    }

    @Override
    public String controlEngine(boolean start) throws ApiException {
        return sendAction(FPSERVICE_ENGINE, start ? "start" : "stop", "start", start);
    }

    private String sendAction(String service, String action, String command, boolean start) throws ApiException {
        logger.debug("{}: Sending action {} ({}, {}))", thingId, action, command, start);
        HttpMethod method = start ? HttpMethod.PUT : HttpMethod.DELETE;
        String uri = "/vehicles/v2/{2}/" + service + "/" + command;
        String json = http.request(method, uri, "", createApiParameters(), "", "", "", false).response;
        FPActionRequest req = new FPActionRequest(service, action, fromJson(gson, json, FPActionResponse.class));
        req.checkUrl = uri + "/" + req.requestId;
        return queuePendingAction(new ApiActionRequest(req));
    }

    @Override
    public String getApiRequestStatus(ApiActionRequest req) throws ApiException {
        String json = http.get(req.checkUrl, createApiParameters()).response;
        FPActionResponse rsp = fromJson(gson, json, FPActionResponse.class);
        if (rsp.isError()) {
            req.error = "API returned error: " + rsp.status;
            logger.debug("{}: Unexpected API status code: {}", thingId, req.error);
        }
        return rsp.mapStatusCode();
    }

    protected ApiHttpMap createDefaultParameters() throws ApiException {
        return new ApiHttpMap().header(HttpHeader.USER_AGENT, config.api.userAgent).header(HttpHeaders.ACCEPT_LANGUAGE,
                "en-us");
    }

    protected Map<String, String> createApiParameters(String token) throws ApiException {
        /*
         * 'Accept': '* /*',
         * 'Accept-Language': 'en-us',
         * 'User-Agent': 'fordpass-na/353 CFNetwork/1121.2.2 Darwin/19.3.0',
         */
        return createDefaultParameters().header("Application-Id", config.api.xClientId)
                .header(HttpHeaders.ACCEPT, CONTENT_TYPE_JSON)//
                .header("Auth-Token", token.isEmpty() ? createAccessToken() : token) //
                .getHeaders();
    }

    protected Map<String, String> createApiParameters() throws ApiException {
        return createApiParameters(createAccessToken());
    }
}
