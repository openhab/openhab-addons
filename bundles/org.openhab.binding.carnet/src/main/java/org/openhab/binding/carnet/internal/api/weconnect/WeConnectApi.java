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
package org.openhab.binding.carnet.internal.api.weconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.carnet.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.ApiHttpMap;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.brand.BrandAuthenticator;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WeConnectApi} implements the WeConnect API calls
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class WeConnectApi extends CarNetApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(WeConnectApi.class);
    private Map<String, WCVehicle> vehicleData = new HashMap<>();

    public WeConnectApi(ApiHttpClient httpClient, TokenManager tokenManager, @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
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
    public String getHomeReguionUrl() {
        return "https://mobileapi.apps.emea.vwapps.io";
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        ApiHttpMap params = crerateParameters();
        WCVehicleStatus status = callApi("", "https://mobileapi.apps.emea.vwapps.io/vehicles/{2}/status",
                params.getHeaders(), "getVehicleStatus", WCVehicleStatusData.class).data;
        return new VehicleStatus(status);
    }

    @Override
    public boolean isRemoteServiceAvailable(String serviceId) {
        // there is no WeConnect service registry
        return true;
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
                .header("x-newrelic-id", "VgAEWV9QDRAEXFlRAAYPUA==")
                .header(HttpHeader.USER_AGENT, "WeConnect/5 CFNetwork/1206 Darwin/20.1.0")
                .header(HttpHeader.ACCEPT_LANGUAGE, "de-de")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + createVwToken());
    }
}
