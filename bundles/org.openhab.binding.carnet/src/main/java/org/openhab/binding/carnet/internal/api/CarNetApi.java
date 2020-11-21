/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.API_REQUEST_TIMEOUT_SEC;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.CarNetUtils;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations.CarNetDestinationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList.CarNetServiceInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRequestStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRoleRights;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRoleRights.CarNetUserRoleRights;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleData.CarNetVehicleData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetActionResponse.CNActionResponse;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetHomeRegion;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetOidcConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetServiceAvailability;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetTripData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleDetails;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehiclePosition;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.binding.carnet.internal.config.CarNetVehicleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.internal.Primitives;

/**
 * The {@link CarNetApi} implements the http based API access to CarNet
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetApi {
    private final Logger logger = LoggerFactory.getLogger(CarNetApi.class);
    private final Gson gson = new Gson();

    private boolean initialzed = false;
    private CarNetCombinedConfig config = new CarNetCombinedConfig();
    private CarNetHttpClient http = new CarNetHttpClient();

    private CarNetTokenManager tokenManager = new CarNetTokenManager();

    public class CarNetPendingRequest {
        public String vin = "";
        public String service = "";
        public String action = "";
        public String checkUrl = "";
        public String requestId = "";
        public String status = "";
        public Date creationTime = new Date();

        public CarNetPendingRequest(String service, String action, CNActionResponse rsp) {
            // normalize the resonse type
            this.service = service;
            this.action = action;

            switch (service) {
                case CNAPI_SERVICE_REMOTE_LOCK_UNLOCK:
                    if (rsp.rluActionResponse != null) {
                        this.vin = rsp.rluActionResponse.vin;
                        this.requestId = rsp.rluActionResponse.requestId;
                    }
                    checkUrl = "bs/rlu/v1/{0}/{1}/vehicles/{2}/requests/" + requestId + "/status";
                    break;
                case CNAPI_SERVICE_REMOTE_HEATING:
                    checkUrl = "bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions/" + requestId;
                    break;
                case CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION:
                    if (rsp.action != null) {
                        this.requestId = rsp.action.actionId;
                        this.status = rsp.action.actionState;
                    }
                    checkUrl = "bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions/" + requestId;
                    break;
            }
        }

        public boolean isExpired() {
            Date currentTime = new Date();
            long diff = currentTime.getTime() - creationTime.getTime();
            return (diff / 1000) > API_REQUEST_TIMEOUT_SEC;
        }
    }

    private Map<String, CarNetPendingRequest> pendingRequests = new ConcurrentHashMap<>();

    public CarNetApi() {
    }

    public CarNetApi(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        logger.debug("Initializing CarNet API");
        this.http = httpClient;
        this.tokenManager = tokenManager;
    }

    public void setConfig(CarNetCombinedConfig config) {
        logger.debug("Setting up CarNet API for brand {} ({}), user {}", config.account.brand, config.account.country,
                config.account.user);
        this.config = config;
        http.setConfig(this.config);
        initBrandData();
    }

    public void setConfig(CarNetVehicleConfiguration config) {
        this.config.vehicle = config;
        http.setConfig(this.config);
    }

    public void initialize() throws CarNetException {
        http.setConfig(this.config);
        config.oidcConfig = getOidcConfig();
        tokenManager.refreshTokens(config);
        initialzed = true;
    }

    public boolean isInitialized() {
        return initialzed;
    }

    private CarNetOidcConfig getOidcConfig() throws CarNetException {
        // get OIDC confug
        String url = CNAPI_OIDC_CONFIG_URL; // "https://app-api.live-my.audi.com/myaudiappidk/v1/openid-configuration";
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(HttpHeader.ACCEPT.toString(), CNAPI_ACCEPTT_JSON);
        headers.put(HttpHeader.CONTENT_TYPE.toString(), CNAPI_CONTENTT_FORM_URLENC);
        String json = http.get(url, headers);
        config.oidcDate = http.getResponseDate();
        return CarNetUtils.fromJson(gson, json, CarNetOidcConfig.class);
    }

    public CarNetServiceAvailability getServiceAvailability(CarNetOperationList operation) throws CarNetException {
        CarNetServiceAvailability serviceStatus = new CarNetServiceAvailability();
        for (CarNetServiceInfo si : operation.serviceInfo) {
            // Check service enabled status, maybe we need also to check serviceEol
            boolean enabled = si.serviceStatus.status.equalsIgnoreCase("Enabled")
                    && (!si.licenseRequired || si.cumulatedLicense.status.equalsIgnoreCase("ACTIVATED"));
            switch (si.serviceId) {
                case CNAPI_SERVICE_VEHICLE_STATUS_REPORT:
                    serviceStatus.statusData = enabled;
                    break;
                case CNAPI_SERVICE_REMOTE_LOCK_UNLOCK:
                    serviceStatus.rlu = enabled;
                case CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION:
                    serviceStatus.clima = enabled;
                    break;
                case CNAPI_SERVICE_REMOTE_BATTERY_CHARGE:
                    serviceStatus.charger = enabled;
                    break;
                case CNAPI_SERVICE_CAR_FINDER:
                    serviceStatus.carFinder = enabled;
                    break;
                case CNAPI_SERVICE_MY_AUDI_DESTINATIONS:
                    serviceStatus.destinations = enabled;
                    break;
                case CNAPI_SERVICE_REMOTE_TRIP_STATISTICS:
                    serviceStatus.tripData = enabled;
                    break;
            }
        }
        return serviceStatus;
    }

    public CarNetUserRoleRights getRoleRights() throws CarNetException {
        return callApi("rolesrights/permissions/v1/{0}/{1}/vehicles/{2}/fetched-role", "getRoleRights",
                CNRoleRights.class).role;
    }

    public String getHomeReguionUrl() throws CarNetException {
        try {
            if (!config.vehicle.homeRegionUrl.isEmpty()) {
                return config.vehicle.homeRegionUrl;
            }
            CarNetHomeRegion region = callApi(CNAPI_VWURL_HOMEREGION, "getHomeRegion", CarNetHomeRegion.class);
            // config.vehicle.homeRegionUrl = substringBefore(region.homeRegion.baseUri.content, "/api");
            config.vehicle.homeRegionUrl = getString(region.homeRegion.baseUri.content);
            return config.vehicle.homeRegionUrl;
        } catch (CarNetException e) {
        }
        return "";
    }

    public CarNetVehicleList getVehicles() throws CarNetException {
        return callApi(CNAPI_URI_VEHICLE_LIST, "getVehicles", CarNetVehicleList.class);
    }

    public CarNetVehicleDetails getVehicleDetails(String vin) throws CarNetException {
        return callApi(vin, CNAPI_URI_VEHICLE_DETAILS, "getVehicleDetails", CarNetVehicleDetails.class);
    }

    public CarNetVehicleStatus getVehicleStatus() throws CarNetException {
        return callApi(CNAPI_URI_VEHICLE_STATUS, "getVehicleStatus", CarNetVehicleStatus.class);
    }

    public String getVehicleRequets() throws CarNetException {
        return http.post("bs/vsr/v1/{0}/{1}/vehicles/{2}/requests", fillAppHeaders(), "", "");
    }

    public CarNetVehiclePosition getVehiclePosition() throws CarNetException {
        return callApi(CNAPI_URI_VEHICLE_POSITION, "getVehiclePosition", CarNetVehiclePosition.class);
    }

    public String getVehicleHealthReport() throws CarNetException {
        String json = callApi("bs/vhs/v2/vehicle/{2}", "healthReport", String.class);
        return json;
    }

    public CarNetVehiclePosition getStoredPosition() throws CarNetException {
        return callApi(CNAPI_VWURL_STORED_POS, "getStoredPosition", CarNetVehiclePosition.class);
    }

    public CarNetDestinationList getDestinations() throws CarNetException {
        String json = callApi(CNAPI_URI_DESTINATIONS, "getDestinations", String.class);
        if (json.equals("{\"destinations\":null}")) {
            // This services returns an empty list rather than http 403 when access is not allowed
            // in this case try to load test data
            String test = loadJson("getDestinations");
            if (test != null) {
                json = test;
            }
        }
        CNDestinations dest = fromJson(gson, json, CNDestinations.class, false);
        if ((dest != null) && (dest.destinations != null)) {
            return dest.destinations;
        }
        CarNetDestinationList empty = new CarNetDestinationList();
        empty.destination = new ArrayList<>();
        return empty; // return empty list
    }

    public String getHistory() throws CarNetException {
        String json = callApi(CNAPI_URI_HISTORY, "getHistory", String.class);
        return json;
    }

    public CarNetChargerStatus getChargerStatus() throws CarNetException {
        return callApi(CNAPI_URI_CHARGER_STATUS, "chargerStatus", CNChargerInfo.class).charger;
    }

    public @Nullable CarNetTripData getTripData(String type) throws CarNetException {
        String json = "";
        try {
            String action = "list";
            String url = CNAPI_VWURL_TRIP_DATA.replace("{3}", type).replace("{4}", action);
            json = http.get(url, fillAppHeaders());
        } catch (CarNetException e) {
            logger.debug("{}: API call getTripData failed: {}", config.vehicle.vin, e.toString());
        } catch (RuntimeException e) {
            logger.debug("{}: API call getTripData failed", config.vehicle.vin, e);
        }

        if (json.isEmpty()) {
            json = loadJson("tripData" + type);
        }
        return fromJson(gson, json, CarNetTripData.class, false);
    }

    public @Nullable String getPersonalData() throws CarNetException {
        if (isBrandAudi() || isBrandGo()) {
            return null; // not supported for Audi vehicles
        }

        /*
         * url: "https://customer-profile.apps.emea.vwapps.io/v1/customers/" + this.config.userid + "/personalData",
         * headers: {
         * "user-agent": "okhttp/3.7.0",
         * "X-App-version": this.xappversion,
         * "X-App-name": this.xappname,
         * authorization: "Bearer " + this.config.atoken,
         * accept: "application/json",
         * Host: "customer-profile.apps.emea.vwapps.io",
         * },
         */
        String json = "{}";
        try {
            String url = "https://customer-profile.apps.emea.vwapps.io/v1/customers/"
                    + UrlEncoded.encodeString(config.account.user) + "/personalData";
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
            headers.put(CNAPI_HEADER_APP, config.xappName);
            headers.put(CNAPI_HEADER_VERS, config.xappVersion);
            headers.put(HttpHeader.AUTHORIZATION.toString(), createVwToken());
            headers.put(HttpHeader.ACCEPT.toString(), CNAPI_ACCEPTT_JSON);
            headers.put(HttpHeader.HOST.toString(), "customer-profile.apps.emea.vwapps.io");
            json = http.get(url, headers, createVwToken());
            return json;
        } catch (CarNetException e) {
            logger.debug("{}: API call getPersonalData failed: {}", config.vehicle.vin, e.toString());
        } catch (RuntimeException e) {
            logger.debug("{}: API call getPersonalData failed", config.vehicle.vin, e);
        }
        return null;
    }

    public CarNetOperationList getOperationList() throws CarNetException {
        return callApi(CNAPI_VWURL_OPERATIONS, "getOperationList", CNOperationList.class).operationList;
    }

    public @Nullable String getVehicleUsers() throws CarNetException {
        String json = callApi("bs//uic/v1/vin/{2}/users", "getVehicleUsers", String.class);
        return json;
    }

    public String controlLock(boolean lock) throws CarNetException {
        String action = lock ? CNAPI_ACTION_REMOTE_LOCK_UNLOCK_LOCK : CNAPI_ACTION_REMOTE_LOCK_UNLOCK_UNLOCK;
        String data = "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"
                + "<rluAction xmlns=\"http://audi.de/connect/rlu\">" + "<action>" + action.toLowerCase()
                + "</action></rluAction>";
        return sendAction("bs/rlu/v1/{0}/{1}/vehicles/{2}/actions", CNAPI_SERVICE_REMOTE_LOCK_UNLOCK, action, true,
                "application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml", data);
    }

    public CarNetRluHistory getRluActionHistory() throws CarNetException {
        return callApi("bs/rlu/v1/{0}/{1}/vehicles/{2}/actions", "rluActionHistory",
                CNEluActionHistory.class).actionsResponse;
    }

    public String controlClimater(boolean start) throws CarNetException {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + (start
                ? "<action><type>startClimatisation</type>" + "<settings><heaterSource>" + CNAPI_HEATER_SOURCE_ELECTRIC
                        + "</heaterSource></settings>" + "</action>"
                : "<action><type>stopClimatisation</type></action>");
        return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions",
                CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, CNAPI_ACTION_REMOTE_HEATING_QUICK_START, false,
                "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml;charset=utf-8", data);
    }

    public String controlClimaterTemp(double tempC) throws CarNetException {
        int tempdK = 2950;
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<action><type>setSettings</type><settings>"
                + "<targetTemperature>" + tempdK + "</targetTemperature>"
                + "<climatisationWithoutHVpower>false</climatisationWithoutHVpower>" + "<heaterSource>"
                + CNAPI_HEATER_SOURCE_ELECTRIC + "</heaterSource>" + "</settings></action>";
        return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING,
                CNAPI_ACTION_REMOTE_HEATING_QUICK_START, false,
                "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml;charset=utf-8", data);
    }

    public String controlPreHeating(boolean start) throws CarNetException {
        final String action = start ? CNAPI_ACTION_REMOTE_HEATING_QUICK_START : CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP;
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                + "<performAction xmlns=\"http://audi.de/connect/rs\"><quickstart>" + "<active>"
                + (start ? "true" : "false") + "</active>" + "</quickstart></performAction>";
        return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING, action, true,
                "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml", data);
    }

    public String controlVentilation(boolean start, int duration) throws CarNetException {
        final String action = start ? CNAPI_ACTION_REMOTE_HEATING_QUICK_START : CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP;
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><performAction xmlns=\"http://audi.de/connect/rs\">"
                + (start ? "<quickstart><active>true</active>" + "<climatisationDuration>" + duration
                        + "</climatisationDuration>" + "<startMode>ventilation</startMode></quickstart>"
                        : "<quickstop><active>false</active></quickstop>")
                + "</performAction>";
        return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING, action, true,
                "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml", data);
    }

    public String controlCharger(boolean start) throws CarNetException {
        String action = start ? "start" : "stop";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>" + action + "</type></action>";
        return sendAction("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions", "batterycharge_v1", action, false,
                "application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml", data);
    }

    public CarNetClimaterStatus getClimaterStatus() throws CarNetException {
        return callApi("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater", "climaterStatus",
                CNClimater.class).climater;
        // String json = callApi(CNAPI_VWURL_CLIMATE_STATUS, "climaterStatus");
    }

    public String getClimaterTimer() throws CarNetException {
        String json = callApi(CNAPI_URI_CLIMATER_TIMER, "climaterTimer", String.class);
        return json;
    }

    public String controlWindowHeating(boolean start) throws CarNetException {
        final String action = start ? "startWindowHeating" : "stopWindowHeating";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>" + action + "</type></action>";
        return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions",
                CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, action, false,
                "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml", data);
    }

    private String sendAction(String uri, String service, String action, boolean reqSecToken, String contentType,
            String body) throws CarNetException {
        logger.debug("{}: Sending action request for {}.{}, reqSecToken={}, contentType={}", config.vehicle.vin,
                service, action, reqSecToken, contentType);
        Map<String, String> headers = fillActionHeaders(contentType,
                reqSecToken ? createSecurityToken(service, action) : createVwToken());
        String json = http.post(uri, headers, body);
        logger.debug("{}: Action response={}", config.vehicle.vin, json);
        return queuePendingAction(json, service, action);
    }

    public String getPois() throws CarNetException {
        return callApi("b2c/poinav/v1/{2}/pois", "getPois", String.class);
    }

    public String getUserInfo() throws CarNetException {
        return callApi("core/auth/v1/{0}/{1}/userInfo", "", String.class);
    }

    public CarNetPairingInfo getPairingStatus() throws CarNetException {
        return callApi(CNAPI_URI_GET_USERINFO, "", CNPairingInfo.class).pairingInfo;
    }

    public CarNetVehicleData getVehicleManagementInfo() throws CarNetException {
        return callApi(CNAPI_URI_VEHICLE_MANAGEMENT, "", CNVehicleData.class).vehicleData;
    }

    public String getMyDestinationsFeed(String userId) throws CarNetException {
        return callApi("destinationfeedservice/mydestinations/v1/{0}/{1}/vehicles/{2}/users/{3}/destinations", "",
                String.class);
    }

    public String getUserNews() throws CarNetException {
        // for now not working
        return callApi("https://msg.volkswagen.de/api/news/myfeeds/v1/vehicles/{2}/users/{3}/", "", String.class);
    }

    public String getTripStats(String tripType) throws CarNetException {
        String json = callApi("bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/" + tripType + "?newest", "",
                String.class);
        return json;
    }

    private <T> T callApi(String uri, String function, Class<T> classOfT) throws CarNetException {
        return callApi("", uri, function, classOfT);
    }

    private <T> T callApi(String vin, String uri, String function, Class<T> classOfT) throws CarNetException {
        String json = "";
        try {
            json = http.get(uri, vin, fillAppHeaders());
        } catch (CarNetException e) {
            CarNetApiResult res = e.getApiResult();
            logger.debug("{}: API call {} failed: {}", config.vehicle.vin, function, e.toString());
            if (res.isHttpUnauthorized()) {
                json = loadJson(function);
            }
        } catch (RuntimeException e) {
            logger.debug("{}: API call {} failed", config.vehicle.vin, function, e);
        }

        if (classOfT.isInstance(json)) {
            // special case on target class == String (return raw info)
            return Primitives.wrap(classOfT).cast(json);
        }
        return fromJson(gson, json, classOfT);
    }

    private @Nullable String loadJson(String filename) {
        if (filename.isEmpty()) {
            return null;
        }
        try {
            StringBuffer result = new StringBuffer();
            String path = System.getProperty("user.dir") + "/userdata/";
            File myObj = new File(path + filename + ".json");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                result.append(line);
            }
            myReader.close();
            return result.toString();
        } catch (IOException e) {
        }
        return null;
    }

    public boolean checkRequestSuccessful(String url) {
        return true;
    }

    private String queuePendingAction(String json, String service, String action) throws CarNetException {
        CNActionResponse in = fromJson(gson, json, CNActionResponse.class, false);
        if (in != null) {
            CarNetPendingRequest rsp = new CarNetPendingRequest(service, action, in);
            logger.debug("{}: Request for {}.{} accepted, requestId={}", config.vehicle.vin, service, action,
                    rsp.requestId);
            if (pendingRequests.containsKey(rsp.requestId)) {
                pendingRequests.remove(rsp.requestId); // duplicate id
            }
            pendingRequests.put(rsp.requestId, rsp);

            // Check if action was accepted
            return getRequestStatus(rsp.requestId, rsp.status);
        }
        return CNAPI_REQUEST_NOT_FOUND;
    }

    public Map<String, CarNetPendingRequest> getPendingRequests() {
        return pendingRequests;
    }

    public String getRequestStatus(String requestId, String rstatus) throws CarNetException {
        if (!pendingRequests.containsKey(requestId)) {
            throw new IllegalArgumentException("Invalid requestId");
        }

        boolean remove = false;
        String status = rstatus;
        CarNetPendingRequest request = pendingRequests.get(requestId);
        if (request == null) {
            return "";
        }
        if (request.isExpired()) {
            logger.info("{}: Request {} for action {}.{} has been expired, remove", config.vehicle.vin,
                    request.requestId, request.service, request.action);
            remove = true;
        } else {
            try {
                if (status.isEmpty()) {
                    CNRequestStatus rs = callApi(request.checkUrl, "getRequestStatus", CNRequestStatus.class);
                    status = rs.requestStatusResponse.status;
                }

                logger.debug("{}: Request {} for action {}.{} is in status {}", config.vehicle.vin, request.requestId,
                        request.service, request.action, status);
                switch (status) {
                    case CNAPI_REQUEST_SUCCESSFUL:
                        remove = true;
                        break;
                    case CNAPI_REQUEST_IN_PROGRESS:
                    case CNAPI_REQUEST_QUEUED:
                        break;
                    case CNAPI_REQUEST_NOT_FOUND:
                    case CNAPI_REQUEST_FAIL:
                    case "general_error":
                        remove = true;
                        break;
                }
            } catch (CarNetException e) {
                logger.debug("{}: Unable to validate request {}, {}", config.vehicle.vin, requestId, e.toString());
            }
        }

        if (remove) {
            pendingRequests.remove(request.requestId);
        }
        return status;
    }

    private void initBrandData() {
        if (isBrandAudi()) {
            config.oidcConfigUrl = "https://app-api.live-my.audi.com/myaudiappidk/v1/openid-configuration";
            config.clientId = "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com";
            config.xClientId = "77869e21-e30a-4a92-b016-48ab7d3db1d8";
            config.authScope = "address profile badge birthdate birthplace nationalIdentifier nationality profession email vin phone nickname name picture mbb gallery openid";
            config.redirect_uri = "myaudi:///";
            config.responseType = "token id_token";
            config.xappVersion = "3.14.0";
            config.xappName = "myAudi";
        } else if (isBrandVW()) {
            config.clientId = "9496332b-ea03-4091-a224-8c746b885068@apps_vw-dilab_com";
            config.xClientId = "38761134-34d0-41f3-9a73-c4be88d7d337";
            config.authScope = "openid profile mbb email cars birthdate badge address vin";
            config.redirect_uri = "carnet://identity-kit/Flogin";
            config.xrequest = "de.volkswagen.carnet.eu.eremote";
            config.responseType = "id_token token code";
            config.xappName = "eRemote";
            config.xappVersion = "5.1.2";
        } else if (isBrandSkoda()) {
            config.clientId = "7f045eee-7003-4379-9968-9355ed2adb06@apps_vw-dilab_com";
            config.xClientId = "28cd30c6-dee7-4529-a0e6-b1e07ff90b79";
            config.authScope = "openid profile phone address cars email birthdate badge dealers driversLicense mbb";
            config.redirect_uri = "skodaconnect://oidc.login/";
            config.xrequest = "cz.skodaauto.connect";
            config.responseType = "code id_token";
            config.xappVersion = "3.2.6";
            config.xappName = "cz.skodaauto.connect";
        } else if (isBrandGo()) {
            config.clientId = "ac42b0fa-3b11-48a0-a941-43a399e7ef84@apps_vw-dilab_com";
            config.xClientId = "";
            config.authScope = "openid profile address email phone";
            config.redirect_uri = "vwconnect://de.volkswagen.vwconnect/oauth2redirect/identitykit";
            config.responseType = "code";
        }
        http.setConfig(config);
    }

    public static boolean isBrandAudi(String brand) {
        return brand.equalsIgnoreCase(CNAPI_BRAND_AUDI);
    }

    public static boolean isBrandVW(String brand) {
        return brand.equalsIgnoreCase(CNAPI_BRAND_VW);
    }

    public static boolean isBrandSkoda(String brand) {
        return brand.equalsIgnoreCase(CNAPI_BRAND_SKODA);
    }

    public static boolean isBrandGo(String brand) {
        return brand.equalsIgnoreCase(CNAPI_BRAND_GO);
    }

    private boolean isBrandAudi() {
        return isBrandAudi(config.account.brand);
    }

    private boolean isBrandVW() {
        return isBrandVW(config.account.brand);
    }

    private boolean isBrandSkoda() {
        return isBrandSkoda(config.account.brand);
    }

    private boolean isBrandGo() {
        return isBrandGo(config.account.brand);
    }

    private Map<String, String> fillActionHeaders(String contentType, String securityToken) throws CarNetException {
        return CarNetHttpClient.fillActionHeaders(new HashMap<>(), contentType, createVwToken(), securityToken);
    }

    public Map<String, String> fillAppHeaders() throws CarNetException {
        return http.fillAppHeaders(new HashMap<>(), createVwToken());
    }

    private String createVwToken() throws CarNetException {
        return tokenManager.createVwToken(config);
    }

    private String createSecurityToken(String service, String action) throws CarNetException {
        return tokenManager.createSecurityToken(config, service, action);
    }

    public boolean refreshTokens() throws CarNetException {
        return tokenManager.refreshTokens(config);
    }
}
