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

import static org.openhab.binding.carnet.internal.CarUtils.fromJson;
import static org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.IncommensurableException;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.api.ApiBase;
import org.openhab.binding.carnet.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.carnet.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.carnet.internal.api.ApiErrorDTO;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.ApiHttpMap;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.brand.BrandAuthenticator;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCActionResponse;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCPendingRequest;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleList.WCVehicle;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApiJsonDTO.WCVehicleStatusData.WCVehicleStatus;
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
    private Map<String, WCPendingRequest> pendingRequests = new ConcurrentHashMap<>();

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
    public String getApiUrl() {
        return "https://mobileapi.apps.emea.vwapps.io";
    }

    @Override
    public String getHomeReguionUrl() {
        return getApiUrl();
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        return new VehicleStatus(getWCStatus());
    }

    @Override
    public String refreshVehicleStatus() {
        // For now it's unclear if there is an API call to request a status update from the vehicle
        return WCAPI_REQUEST_SUCCESSFUL;
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
        return queuePendingAction(service, action, json);
    }

    private String sendSettings(String service, String body) throws ApiException {
        ApiHttpMap headers = crerateParameters();
        String json = http.put("https://mobileapi.apps.emea.vwapps.io/vehicles/{2}/" + service + "/settings",
                headers.getHeaders(), body).response;
        return queuePendingAction(service, "settings", json);
    }

    private String queuePendingAction(String service, String action, String json) throws ApiException {
        WCActionResponse rsp = fromJson(gson, json, WCActionResponse.class);
        WCPendingRequest pr = new WCPendingRequest(config.vehicle.vin, service, action, rsp.data.requestID);
        if (eventListener != null) {
            eventListener.onActionSent(service, action, pr.requestId);
        }

        // Check if action was accepted
        String status = getRequestStatus(pr.requestId, "");
        if (!WCAPI_REQUEST_SUCCESSFUL.equals(status)) {
            pendingRequests.put(pr.requestId, pr);
            logger.debug("{}: Request {} queued for status updates", pr.vin, pr.requestId);

        }
        return status;
    }

    public void checkPendingRequests() {
        if (!pendingRequests.isEmpty()) {
            logger.debug("{}: Checking status for {} pending requets", thingId, pendingRequests.size());
            for (Map.Entry<String, WCPendingRequest> e : pendingRequests.entrySet()) {
                WCPendingRequest request = e.getValue();
                try {
                    request.status = getRequestStatus(request.requestId, "");
                } catch (ApiException ex) {
                    ApiErrorDTO error = ex.getApiResult().getApiError();
                    if (error.isTechValidationError()) {
                        // Id is no longer valid
                        request.status = WCAPI_REQUEST_ERROR;
                    }
                }
            }
        }
    }

    public String getRequestStatus(String requestId, String rstatus) throws ApiException {
        if (rstatus.isEmpty()) {
            return WCAPI_REQUEST_SUCCESSFUL;
        }
        if (!pendingRequests.containsKey(requestId)) {
            throw new IllegalArgumentException("Invalid requestId");
        }

        boolean remove = false;
        String status = rstatus;
        WCPendingRequest request = pendingRequests.get(requestId);
        if (request == null) {
            return "";
        }
        if (request.isExpired()) {
            status = WCAPI_REQUEST_TIMEOUT;
            remove = true;
            if (eventListener != null) {
                eventListener.onActionTimeout(request.service, request.action, request.requestId);
            }
        } else {
            try {
                int error = -1;
                if (status.isEmpty()) {
                    if (request.checkUrl.isEmpty()) {
                        // this should not happen
                        logger.warn("{}: Unable to check request {} status for action {}.{}; checkUrl is missing!",
                                config.vehicle.vin, request.requestId, request.service, request.action);
                    } else {
                        logger.debug("{}: Check request {} status for action {}.{}; checkUrl={}", config.vehicle.vin,
                                request.requestId, request.service, request.action, request.checkUrl);
                        WCActionResponse rsp = callApi(request.checkUrl, "getRequestStatus", WCActionResponse.class);
                        status = WCAPI_REQUEST_QUEUED;
                    }
                }

                status = status.toLowerCase(); // Hon&Flash returns in upper case
                String actionStatus = status;
                switch (status) {
                    case WCAPI_REQUEST_SUCCESSFUL:
                        actionStatus = WCAPI_REQUEST_SUCCESSFUL; // normalize status
                        remove = true;
                        break;
                    case WCAPI_REQUEST_QUEUED:
                    case WCAPI_REQUEST_STARTED:
                        actionStatus = WCAPI_REQUEST_STARTED; // normalize status
                        break;
                    case WCAPI_REQUEST_ERROR:
                        logger.warn("{}: Action {}.{} failed with status {}, error={} (requestId={})",
                                config.vehicle.vin, request.service, request.action, status, error, request.requestId);
                        remove = true;
                        actionStatus = WCAPI_REQUEST_ERROR; // normalize status
                        break;
                    default:
                        logger.debug("{}: Request {} has unknown status: {}", config.vehicle.vin, requestId, status);
                }

                if (eventListener != null) {
                    eventListener.onActionResult(request.service, request.action, request.requestId,
                            actionStatus.toUpperCase(), status);
                }
            } catch (ApiException e) {
                logger.debug("{}: Unable to validate request {}, {}", config.vehicle.vin, requestId, e.toString());
            } catch (RuntimeException e) {
                logger.debug("{}: Unable to validate request {}", config.vehicle.vin, requestId, e);
            }
        }

        if (remove) {
            logger.debug("{}: Remove request {} for action {}.{} from queue, status is {}", config.vehicle.vin,
                    request.requestId, request.service, request.action, status);
            pendingRequests.remove(request.requestId);
        }
        return status;
    }

    private boolean isRequestPending(String serviceId) {
        for (Map.Entry<String, WCPendingRequest> r : pendingRequests.entrySet()) {
            if (r.getValue().service.equals(serviceId)) {
                return true;
            }
        }
        return false;
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
                .header(HttpHeader.AUTHORIZATION, "Bearer " + createAccessToken());
    }
}
