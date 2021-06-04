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
package org.openhab.binding.carnet.internal.api;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations.CarNetDestinationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNFindCarResponse;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlertConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlertConfig.CarNetGeoFenceConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNHeaterVentilation;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNHeaterVentilation.CarNetHeaterVentilationStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNHonkFlashResponse;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList.CarNetServiceInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList.CarNetServiceInfo.CNServiceOperation;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRequestStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRoleRights;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNRoleRights.CarNetUserRoleRights;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlertConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlertConfig.CarNetSpeedAlertConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNStoredPosition;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleDetails;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNVehicleDetails.CarNetVehicleDetails;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetActionResponse.CNActionResponse;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetHomeRegion;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetJwtToken;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetMbbStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetOidcConfig;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetPersonalData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetPosition;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetTripData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.binding.carnet.internal.config.CarNetVehicleConfiguration;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link CarNetApiBase} implements the http based API access to CarNet
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public abstract class CarNetApiBase implements CarNetBrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(CarNetApiBase.class);
    protected final Gson gson = new Gson();

    protected String thingId = "";
    protected CarNetCombinedConfig config = new CarNetCombinedConfig();
    protected CarNetHttpClient http = new CarNetHttpClient();
    protected CarNetTokenManager tokenManager = new CarNetTokenManager();
    protected @Nullable CarNetEventListener eventListener;

    private boolean initialzed = false;
    private Map<String, CarNetPendingRequest> pendingRequests = new ConcurrentHashMap<>();

    public CarNetApiBase() {
    }

    public CarNetApiBase(CarNetHttpClient httpClient, CarNetTokenManager tokenManager,
            @Nullable CarNetEventListener eventListener) {
        logger.debug("Initializing CarNet API");
        this.http = httpClient;
        this.tokenManager = tokenManager;
        this.eventListener = eventListener;
    }

    public void setConfig(CarNetCombinedConfig config) {
        config.api = getProperties();
        this.config = config;
        http.setConfig(this.config);
    }

    public void setConfig(CarNetVehicleConfiguration config) {
        this.config.vehicle = config;
        http.setConfig(this.config);
    }

    public CarNetHttpClient getHttp() {
        return this.http;
    }

    /**
     * Simple initialization, in fact used by Account Handler
     *
     * @param configIn
     * @throws CarNetException
     */
    public void initialize(CarNetCombinedConfig configIn) throws CarNetException {
        config = configIn;
        setConfig(config); // derive from account config
        if (!config.api.oidcConfigUrl.isEmpty()) {
            config.oidcConfig = getOidcConfig();
        }
        tokenManager.refreshTokens(config);
        initialzed = true;
        thingId = config.account.brand;
    }

    /**
     * VIN-based initialization. Initialized the API itself then does the VIN-related initialization
     *
     * @param vin Vehicle ID (VIN)
     * @param configIn Combined config, which gets updated and will be returned
     * @return Updated config
     * @throws CarNetException
     */
    public CarNetCombinedConfig initialize(String vin, CarNetCombinedConfig configIn) throws CarNetException {
        initialize(configIn);

        // update based von VIN specific settings
        config.vehicle.vin = vin.toUpperCase();
        thingId = config.vehicle.vin;
        config.vstatus.homeRegionUrl = getHomeReguionUrl();
        config.vstatus.apiUrlPrefix = getApiUrl();

        CarNetOperationList ol = getOperationList();
        if (ol != null) {
            config.vstatus.operationList = ol;
            config.user.id = ol.userId;
            config.user.role = ol.role;
            config.user.status = ol.status;
            config.user.securityLevel = ol.securityLevel;

            config.user.identity = getUserIdentity();
            config.user.profileUrl = getProfileUrl();
        }

        try {
            config.vstatus.pairingInfo = getPairingStatus();
        } catch (CarNetException e) {
            logger.debug("{}: Unable to verify pairing status: {}", thingId, e.toString());
        }

        getImageUrls();

        setConfig(config);
        return config;
    }

    public boolean isInitialized() {
        return initialzed;
    }

    public CarNetApiProperties getProperties() {
        return new CarNetApiProperties();
    }

    @Override
    public String updateAuthorizationUrl(String url) throws CarNetException {
        return url; // default: no modification
    }

    public String getUserIdentity() throws CarNetException {
        String idToken = createIdToken(); // extract identity id from jwt token
        CarNetJwtToken jwt = decodeJwt(idToken);
        return jwt.sub;// get identity from JWT sub
    }

    public String getProfileUrl() throws CarNetException {
        if (config.user.identity.isEmpty()) {
            config.user.identity = getUserIdentity();
        }
        return "https://customer-profile.apps.emea.vwapps.io/v3/customers/" + config.user.identity;
    }

    public CarNetPersonalData getPersonalData() throws CarNetException {
        return callProfileApi("getPersonalData", "/personalData", CarNetPersonalData.class);
    }

    public CarNetMbbStatus getMbbStatus() throws CarNetException {
        if (config.vstatus.mbb.mbbUserId.isEmpty()) {
            // currently empty, query from API, otherwise return cached result
            config.vstatus.mbb = callProfileApi("getMbbStatus", "/mbbStatusData", CarNetMbbStatus.class);
        }
        return config.vstatus.mbb;
    }

    public <T> T callProfileApi(String api, String uri, Class<T> classOfT) throws CarNetException {
        return callApi("", config.user.profileUrl + uri, fillAppHeaders(tokenManager.createProfileToken(config)), api,
                classOfT);
    }

    public String[] getImageUrls() throws CarNetException {
        // Default: No image URLs (will be overwritten by brand specific API)
        return config.vstatus.imageUrls;
    }

    public CarNetPairingInfo getPairingStatus() throws CarNetException {
        return callApi("usermanagement/users/v1/{0}/{1}/vehicles/{2}/pairing", "", CNPairingInfo.class).pairingInfo;
    }

    public String getMyDestinationsFeed() throws CarNetException {
        return callApi("destinationfeedservice/mydestinations/v1/{0}/{1}/vehicles/{2}/users/{3}/destinations",
                "getMyDestinationsFeed", String.class);
    }

    public CarNetVehicleList getVehicles() throws CarNetException {
        return callApi("https://msg.volkswagen.de/fs-car/usermanagement/users/v1/{0}/{1}/vehicles", "getVehicles",
                CarNetVehicleList.class);
    }

    public CarNetVehicleDetails getVehicleDetails(String vin) throws CarNetException {
        Map<String, String> headers = fillAppHeaders();
        headers.put("Accept",
                "application/vnd.vwg.mbb.vehicleDataDetail_v2_1_0+json, application/vnd.vwg.mbb.genericError_v1_0_2+json");
        CNVehicleDetails details = callApi(vin, "vehicleMgmt/vehicledata/v2/{0}/{1}/vehicles/{2}", headers,
                "getVehicleDetails", CNVehicleDetails.class);
        return details.vehicleDataDetail;
    }

    public CarNetVehicleStatus getVehicleStatus() throws CarNetException {
        return callApi(getApiUrl() + "/" + "bs/vsr/v1/{0}/{1}/vehicles/{2}/status", "getVehicleStatus",
                CarNetVehicleStatus.class);
    }

    public String refreshVehicleStatus() throws CarNetException {
        String json = http.post("bs/vsr/v1/{0}/{1}/vehicles/{2}/requests", fillAppHeaders(), "", "");
        return queuePendingAction(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, "status", json);
    }

    public String getVehicleRequets() throws CarNetException {
        return http.post("bs/vsr/v1/{0}/{1}/vehicles/{2}/requests", fillAppHeaders(), "", "");
    }

    public CarNetPosition getVehiclePosition() throws CarNetException {
        // needs explicit Accept: application/json, otherwhise storedPosition is returned
        Map<String, String> headers = fillAppHeaders();
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
        return new CarNetPosition(callApi("", "bs/cf/v1/{0}/{1}/vehicles/{2}/position", headers, "getVehiclePosition",
                CNFindCarResponse.class));
    }

    public CarNetPosition getStoredPosition() throws CarNetException {
        return new CarNetPosition(
                callApi("bs/cf/v1/{0}/{1}/vehicles/{2}/position", "getStoredPosition", CNStoredPosition.class));
    }

    public CarNetDestinationList getDestinations() throws CarNetException {
        // The API returns 403 when service is not available, but
        CNDestinations dest = callApi("destinationfeedservice/mydestinations/v1/{0}/{1}/vehicles/{2}/destinations",
                "getDestinations", CNDestinations.class);
        if (dest.destinations == null) {
            // API may returns {\"destinations\":null}
            CarNetDestinationList empty = new CarNetDestinationList();
            empty.destination = new ArrayList<>();
            dest.destinations = empty;
        }
        return dest.destinations;
    }

    public String getHistory() throws CarNetException {
        return callApi("bs/dwap/v1/{0}/{1}/vehicles/{2}/history", "getHistory", String.class);
    }

    public @Nullable String getVehicleUsers() throws CarNetException {
        return callApi("bs//uic/v1/vin/{2}/users", "getVehicleUsers", String.class);
    }

    public CarNetChargerStatus getChargerStatus() throws CarNetException {
        return callApi("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charge", "chargerStatus", CNChargerInfo.class).charger;
    }

    public CarNetTripData getTripData(String type) throws CarNetException {
        String url = "bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/" + type + "?type=list";
        return callApi(url, "getTripData", CarNetTripData.class);
    }

    public @Nullable CarNetOperationList getOperationList() throws CarNetException {
        return config.vstatus.operationList != null ? config.vstatus.operationList
                : callApi(config.vstatus.rolesRightsUrl + "/rolesrights/operationlist/v3/vehicles/{2}?scope=ALL",
                        "getOperationList", CNOperationList.class).operationList;
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

    public String controlClimater(boolean start, String heaterSource) throws CarNetException {
        String contentType = "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml;charset=utf-8";
        String body = "", action = "";
        boolean secToken = !CNAPI_HEATER_SOURCE_ELECTRIC.equals(heaterSource);
        if (start) {
            if ((config.account.apiLevelClimatisation == 1) || heaterSource.isEmpty()) {
                // simplified format without header source
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>startClimatisation</type></action>";
            } else {
                // standard format with header source
                // body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><action><type>startClimatisation</type>"
                // + "<settings><heaterSource>" + heaterSource + "</heaterSource></settings></action>";
                contentType = "application/vnd.vwg.mbb.ClimaterAction_v1_0_2+json";
                body = "{\"action\": {\"settings\": {\"climatisationWithoutHVpower\": \"without_hv_power\", \"heaterSource\": \""
                        + heaterSource + "\"}, \"type\": \"startClimatisation\"}";
            }
            action = CNAPI_HEATER_SOURCE_ELECTRIC.equalsIgnoreCase(heaterSource)
                    ? CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_ELECTRIC
                    : CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_AUX_OR_AUTO;
        } else {
            // stop climater
            body = "<action><type>stopClimatisation</type></action>";
        }
        return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions",
                CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, start ? action : CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP,
                secToken, contentType, body);
    }

    public String controlClimaterTemp(double tempC, String heaterSource) throws CarNetException {
        try {
            int tempdK = (int) SIUnits.CELSIUS.getConverterToAny(DKELVIN).convert(tempC);
            String contentType = "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml;charset=utf-8";
            String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<action><type>setSettings</type><settings>"
                    + "<targetTemperature>" + tempdK + "</targetTemperature>"
                    + "<climatisationWithoutHVpower>false</climatisationWithoutHVpower>" + "<heaterSource>"
                    + heaterSource + "</heaterSource>" + "</settings></action>";

            return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING,
                    CNAPI_ACTION_REMOTE_HEATING_QUICK_START, false, contentType, data);
        } catch (IncommensurableException e) {
            return CNAPI_REQUEST_ERROR;
        }
    }

    public String controlPreHeating(boolean start, int duration) throws CarNetException {
        String contentType = "", body = "";
        final String action = start ? CNAPI_ACTION_REMOTE_HEATING_QUICK_START : CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP;
        if (config.account.apiLevelVentilation == 1) {
            // Version 2.0 format
            contentType = "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml";
            body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                    + "<performAction xmlns=\"http://audi.de/connect/rs\"><quickstart>" + "<active>"
                    + (start ? "true" : "false") + "</active>" + "</quickstart></performAction>";
            return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING, action,
                    true, contentType, body);
        } else {
            // Version 2.0.2 format
            contentType = "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_2+json";
            body = start
                    ? "{\"performAction\":{\"quickstart\":{\"startMode\":\"heating\",\"active\":true,\"climatisationDuration\":"
                            + duration + "}}}"
                    : "{\"performAction\":{\"quickstop\":{\"active\":false}}}";
            return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING, action,
                    true, contentType, body);
        }
    }

    public String controlVentilation(boolean start, int duration) throws CarNetException {
        String contentType = "", body = "";
        final String action = start ? CNAPI_ACTION_REMOTE_HEATING_QUICK_START : CNAPI_ACTION_REMOTE_HEATING_QUICK_STOP;
        if (config.account.apiLevelVentilation == 1) {
            // Version 2.0 format
            contentType = "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml";
            body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><performAction xmlns=\"http://audi.de/connect/rs\">"
                    + (start ? "<quickstart><active>true</active>" + "<climatisationDuration>" + duration
                            + "</climatisationDuration>" + "<startMode>ventilation</startMode></quickstart>"
                            : "<quickstop><active>false</active></quickstop>")
                    + "</performAction>";
            return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/climater/actions", CNAPI_SERVICE_REMOTE_HEATING, action,
                    true, contentType, body);
        } else {
            // Version 2.0.2 format
            contentType = "application/vnd.vwg.mbb.RemoteStandheizung_v2_0_2+json";
            body = start
                    ? "{\"performAction\":{\"quickstart\":{\"startMode\":\"ventilation\",\"active\":true,\"climatisationDuration\":"
                            + duration + "}}}"
                    : "{\"performAction\":{\"quickstop\":{\"active\":false}}}";
            return sendAction("bs/rs/v1/{0}/{1}/vehicles/{2}/action", CNAPI_SERVICE_REMOTE_HEATING, action, true,
                    contentType, body);
        }
    }

    public String controlCharger(boolean start) throws CarNetException {
        String action = start ? "start" : "stop";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>" + action + "</type></action>";
        return sendAction("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions", "batterycharge_v1", action, false,
                "application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml", data);
    }

    public String controlMaxCharge(int maxCurrent) throws CarNetException {
        String contentType = "application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml";
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>setSettings</type>"
                + "<settings><maxChargeCurrent>" + maxCurrent + "</maxChargeCurrent></settings></action>";
        // String contentType = "application/vnd.vwg.mbb.ChargerAction_v1_0_0+json";
        // String body = "{\"action\": {\"settings\": {\"maxChargeCurrent\": " + maxCurrent
        // + ", \"type\": \"setSettings\"}}}";
        return sendAction("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions",
                CNAPI_SERVICE_REMOTE_BATTERY_CHARGE, "setMaxCharge", false, contentType, body);
    }

    public CarNetClimaterStatus getClimaterStatus() throws CarNetException {
        return callApi("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater", "climaterStatus",
                CNClimater.class).climater;
    }

    public CarNetHeaterVentilationStatus getHeaterVentilationStatus() throws CarNetException {
        return callApi("bs/rs/v1/{0}/{1}/vehicles/{2}/status", "heaterVentilationStatus",
                CNHeaterVentilation.class).statusResponse;
    }

    public CarNetSpeedAlertConfig getSpeedAlertConfig() throws CarNetException {
        return callApi("bs/speedalert/v1/{0}/{1}/vehicles/{2}/speedAlertConfiguration", "getSpeedAlertConfig",
                CNSpeedAlertConfig.class).speedAlertConfiguration;
    }

    public CarNetSpeedAlerts getSpeedAlerts() throws CarNetException {
        CNSpeedAlerts sa = callApi("bs/speedalert/v1/{0}/{1}/vehicles/{2}/speedAlerts", "getSpeedAlerts",
                CNSpeedAlerts.class);
        return sa.speedAlerts != null ? sa.speedAlerts : new CarNetSpeedAlerts();
    }

    public CarNetGeoFenceConfig getGeoFenceConfig() throws CarNetException {
        return callApi("bs/geofencing/v1/{0}/{1}/vehicles/{2}/geofencingConfiguration", "getGeoFenceConfig",
                CNGeoFenceAlertConfig.class).geofencingConfiguration;
    }

    public CarNetGeoFenceAlerts getGeoFenceAlerts() throws CarNetException {
        CNGeoFenceAlerts gfa = callApi("bs/geofencing/v1/{0}/{1}/vehicles/{2}/geofencingAlerts", "getGeoFenceAlerts",
                CNGeoFenceAlerts.class);
        return gfa.geofencingAlerts != null ? gfa.geofencingAlerts : new CarNetGeoFenceAlerts();
    }

    public String getClimaterTimer() throws CarNetException {
        String json = callApi("bs/departuretimer/v1/{0}/{1}/vehicles/{2}/timer", "getClimaterTimer", String.class);
        return json;
    }

    public String controlWindowHeating(boolean start) throws CarNetException {
        final String action = start ? "startWindowHeating" : "stopWindowHeating";
        String contentType = "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>" + action + "</type></action>";
        return sendAction("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater/actions",
                CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION, action, false, contentType, data);
    }

    /**
     * Send Honk & FLash remote command
     *
     * @param honk true=honk&flash, false=flash only
     * @param position Geo position of the vehicle
     * @return
     * @throws CarNetException
     */
    public String controlHonkFlash(boolean honk, PointType position, int duration) throws CarNetException {
        /*
         * data = {'honkAndFlashRequest': {
         * 'serviceOperationCode': mode,
         * 'serviceDuration': 15,
         * 'userPosition': {
         * 'latitude': lat,
         * 'longitude': long}}}
         */
        double latd = position.getLatitude().doubleValue() * 1000000.0;
        double longd = position.getLongitude().doubleValue() * 1000000.0;
        String latitude = String.format("%08d", (int) latd);
        String longitude = String.format("%08d", (int) longd);

        String action = honk ? CNAPI_CMD_HONK_FLASH : CNAPI_CMD_FLASH;
        String body = "{\"honkAndFlashRequest\":{\"serviceOperationCode\":\"" + action + "\"," + "\"serviceDuration\": "
                + duration + "," + "\"userPosition\":{\"latitude\":" + latitude + ", \"longitude\":" + longitude
                + "}}}";
        String contentType = "application/json; charset=UTF-8";
        return sendAction("bs/rhf/v1/{0}/{1}/vehicles/{2}/honkAndFlash", CNAPI_SERVICE_REMOTE_HONK_AND_FLASH, action,
                false, contentType, body);
    }

    private String sendAction(String uri, String service, String action, boolean reqSecToken, String contentType,
            String body) throws CarNetException {
        String message = "";
        try {
            if (reqSecToken && config.vehicle.pin.isEmpty()) {
                message = "Action " + service + "." + action + " requires the SPIN, but it's not configured!";
            } else if (isRequestPending(service)) {
                message = "Request " + service + "." + action
                        + " is rejected, there is already a request pending for this service!";
            } else {
                logger.debug("{}: Sending action request for {}.{}, reqSecToken={}, contentType={}", config.vehicle.vin,
                        service, action, reqSecToken, contentType);
                Map<String, String> headers = fillActionHeaders(contentType, createVwToken(),
                        CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_AUX_OR_AUTO.equals(action) ? " X-securityToken"
                                : "x-mbbSecToken",
                        reqSecToken ? createSecurityToken(service, action) : "");
                String json = http.post(uri, headers, body);
                return queuePendingAction(service, action, json);
            }
        } catch (CarNetException e) {
            message = e.toString();
        }
        if (eventListener != null) {
            eventListener.onActionNotification(service, action, message);
        }
        return CNAPI_REQUEST_REJECTED;
    }

    public String getTripStats(String tripType) throws CarNetException {
        String json = callApi("bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/" + tripType + "?newest", "",
                String.class);
        return json;
    }

    protected <T> T callApi(String uri, String function, Class<T> classOfT) throws CarNetException {
        return callApi("", uri, fillAppHeaders(), function, classOfT);
    }

    protected <T> T callApi(String vin, String uri, Map<String, String> headers, String function, Class<T> classOfT)
            throws CarNetException {
        String json = "";
        try {
            json = http.get(uri, vin, headers);
        } catch (CarNetException e) {
            CarNetApiResult res = e.getApiResult();
            if (e.isSecurityException() || res.isHttpUnauthorized()) {
                json = loadJson(function);
            } else if (e.getApiResult().isRedirect()) {
                // Handle redirect
                String newLocation = res.getLocation();
                logger.debug("{}: Handle HTTP Redirect -> {}", config.vehicle.vin, newLocation);
                json = http.get(newLocation, vin, fillAppHeaders());
            }

            if ((json == null) || json.isEmpty()) {
                logger.debug("{}: API call {} failed: {}", config.vehicle.vin, function, e.toString());
                throw e;
            }
        } catch (RuntimeException e) {
            logger.debug("{}: API call {} failed", config.vehicle.vin, function, e);
            throw new CarNetException("API call failes: RuntimeException", e);
        }

        if (classOfT.isInstance(json)) {
            // special case on target class == String (return raw info)
            return wrap(classOfT).cast(json);
        }
        return fromJson(gson, json, classOfT);
    }

    protected @Nullable String loadJson(String filename) {
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

    private String queuePendingAction(String service, String action, String json) throws CarNetException {
        CNActionResponse in;
        if (CNAPI_SERVICE_REMOTE_HONK_AND_FLASH.equals(service)) {
            // Honk&Flash has special format
            in = new CNActionResponse();
            in.honkAndFlashRequest = fromJson(gson, json, CNHonkFlashResponse.class).honkAndFlashRequest;
        } else {
            in = fromJson(gson, json, CNActionResponse.class);
        }
        CarNetPendingRequest rsp = new CarNetPendingRequest(service, action, in);
        logger.debug("{}: Request {} queued for status updates", config.vehicle.vin, rsp.requestId);
        pendingRequests.put(rsp.requestId, rsp);
        if (eventListener != null) {
            eventListener.onActionSent(service, action, rsp.requestId);
        }

        // Check if action was accepted
        return getRequestStatus(rsp.requestId, rsp.status);
    }

    private boolean isRequestPending(String serviceId) {
        for (Map.Entry<String, CarNetPendingRequest> r : pendingRequests.entrySet()) {
            if (r.getValue().service.equals(serviceId)) {
                return true;
            }
        }
        return false;
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
            status = CNAPI_REQUEST_TIMEOUT;
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
                        CNRequestStatus rs = callApi(request.checkUrl, "getRequestStatus", CNRequestStatus.class);
                        if (rs.requestStatusResponse != null) {
                            status = rs.requestStatusResponse.status;
                            if (rs.requestStatusResponse.error != null) {
                                error = rs.requestStatusResponse.error;
                            }
                        } else if (rs.action != null) {
                            status = getString(rs.action.actionState);
                            error = getInteger(rs.action.errorCode);
                        } else if (rs.status != null) {
                            status = getString(rs.status.statusCode);
                        }
                    }
                }

                status = status.toLowerCase(); // Hon&Flash returns in upper case
                String actionStatus = status;
                switch (status) {
                    case CNAPI_REQUEST_SUCCESSFUL:
                    case CNAPI_REQUEST_SUCCEEDED:
                        actionStatus = CNAPI_REQUEST_SUCCESSFUL; // normalize status
                        remove = true;
                        break;
                    case CNAPI_REQUEST_IN_PROGRESS:
                    case CNAPI_REQUEST_QUEUED:
                    case CNAPI_REQUEST_FETCHED:
                    case CNAPI_REQUEST_STARTED:
                        actionStatus = CNAPI_REQUEST_IN_PROGRESS; // normalize status
                        break;
                    case CNAPI_REQUEST_NOT_FOUND:
                    case CNAPI_REQUEST_FAIL:
                    case CNAPI_REQUEST_FAILED:
                        logger.warn("{}: Action {}.{} failed with status {}, error={} (requestId={})",
                                config.vehicle.vin, request.service, request.action, status, error, request.requestId);
                        remove = true;
                        actionStatus = CNAPI_REQUEST_FAILED; // normalize status
                        break;
                    default:
                        logger.debug("{}: Request {} has unknown status: {}", config.vehicle.vin, requestId, status);
                }

                if (eventListener != null && remove) {
                    eventListener.onActionResult(request.service, request.action, request.requestId,
                            actionStatus.toUpperCase(), status);
                }
            } catch (CarNetException e) {
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

    /**
     * Get OpenID Connect configuration
     *
     * @return OIDC config
     * @throws CarNetException
     */
    private CarNetOidcConfig getOidcConfig() throws CarNetException {
        // get OIDC confug
        String url = config.api.oidcConfigUrl;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
        headers.put(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_FORM_URLENC);
        String json = http.get(url, headers);
        config.api.oidcDate = http.getResponseDate();
        return fromJson(gson, json, CarNetOidcConfig.class);
    }

    /**
     * Return serviceId with available version (e.g. xxx_v4)
     *
     * @param serviceId Base serviceId (usually with suffix _v1)
     * @return id from Operation List
     */
    public String getServiceIdEx(String serviceId) {
        CarNetServiceInfo si = getServiceDescriptor(serviceId);
        return si != null ? si.serviceId : serviceId;
    }

    /**
     * Check if service is available (listed in Operation List)
     *
     * @param serviceId Service to look up
     * @return true=service available
     */
    public boolean isRemoteServiceAvailable(String serviceId) {
        return getServiceDescriptor(serviceId) != null;
    }

    /**
     * Check if service is available AND the requested operation id
     *
     * @param serviceId Service Id
     * @param actionId Action Id
     * @return true: Service is with requested action available
     */
    public boolean isRemoteActionAvailable(String serviceId, String actionId) {
        CarNetServiceInfo si = getServiceDescriptor(serviceId);
        // Check service enabled status, maybe we need also to check serviceEol
        if (si != null && "Enabled".equalsIgnoreCase(si.serviceStatus.status)
                && (!si.licenseRequired || "ACTIVATED".equalsIgnoreCase(si.cumulatedLicense.status))) {
            for (CNServiceOperation oi : si.operation) {
                if (oi.id.equalsIgnoreCase(actionId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Lookup service description from Operation List
     *
     * @param serviceId Service id to check
     * @return Service Descriptor from Operations List
     */
    private @Nullable CarNetServiceInfo getServiceDescriptor(String serviceId) {
        CarNetOperationList ol = config.vstatus.operationList;
        if (ol != null) {
            for (CarNetServiceInfo si : ol.serviceInfo) {
                // Check service enabled status, maybe we need also to check serviceEol
                String id = substringBefore(serviceId, "_v");
                if (si.serviceId.equalsIgnoreCase(serviceId) || si.serviceId.startsWith(id)) {
                    return si;
                }
            }
        }
        return null;
    }

    public CarNetUserRoleRights getRoleRights() throws CarNetException {
        return callApi("rolesrights/permissions/v1/{0}/{1}/vehicles/{2}/fetched-role", "getRoleRights",
                CNRoleRights.class).role;
    }

    public String getHomeReguionUrl() {
        if (!config.vstatus.homeRegionUrl.isEmpty()) {
            return config.vstatus.homeRegionUrl;
        }
        String url = "";
        try {
            CarNetHomeRegion region = callApi(CNAPI_VWURL_HOMEREGION, "getHomeRegion", CarNetHomeRegion.class);
            url = getString(region.homeRegion.baseUri.content);
        } catch (CarNetException e) {
            url = CNAPI_VWG_MAL_1A_CONNECT;
        }
        config.vstatus.homeRegionUrl = url;
        return url;
    }

    public String getApiUrl() throws CarNetException {
        String url = getHomeReguionUrl();
        config.vstatus.rolesRightsUrl = url;
        if (!CNAPI_VWG_MAL_1A_CONNECT.equalsIgnoreCase(url)) {
            // Change base url depending on country selector
            url = url.replace("https://mal-", "https://fal-").replace("/api", "/fs-car");
            config.vstatus.apiUrlPrefix = url;
            return url;
        }
        return http.getBaseUrl();
    }

    protected Map<String, String> fillActionHeaders(String contentType, String accessToken, String secTokenHeader,
            String securityToken) throws CarNetException {
        // "User-Agent": "okhttp/3.7.0",
        // "Host": "msg.volkswagen.de",
        // "X-App-Version": "3.14.0",
        // "X-App-Name": "myAudi",
        // "Authorization": "Bearer " + self.vwToken.get("access_token"),
        // "Accept-charset": "UTF-8",
        // "Content-Type": content_type,
        // "Accept": "application/json,
        // application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml,application/vnd.volkswagenag.com-error-v1+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,
        // application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml,
        // application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml,*/*","
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(CNAPI_HEADER_APP, config.api.xappName);
        headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
        if (!contentType.isEmpty()) {
            headers.put(HttpHeader.CONTENT_TYPE.toString(), contentType);
        }
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON
                + ", application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml,application/vnd.volkswagenag.com-error-v1+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml,application/vnd.vwg.mbb.operationList_v3_0_2+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,*/*");
        headers.put(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString());

        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + accessToken);
        String host = substringBetween(config.api.apiDefaultUrl, "//", "/");
        headers.put(HttpHeader.HOST.toString(), host);
        if (!securityToken.isEmpty()) {
            headers.put(secTokenHeader, securityToken);
        }
        return headers;
    }

    protected Map<String, String> fillAppHeaders(String token) throws CarNetException {
        return http.fillAppHeaders(new HashMap<>(), token);
    }

    protected Map<String, String> fillAppHeaders() throws CarNetException {
        return fillAppHeaders(createVwToken());
    }

    protected String createVwToken() throws CarNetException {
        return tokenManager.createVwToken(config);
    }

    protected String createIdToken() throws CarNetException {
        return tokenManager.createIdToken(config);
    }

    private CarNetJwtToken decodeJwt(String token) throws CarNetException {
        Base64.Decoder decoder = Base64.getDecoder();
        String[] chunks = token.split("\\.");
        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        return fromJson(gson, payload, CarNetJwtToken.class);
    }

    protected String createSecurityToken(String service, String action) throws CarNetException {
        return tokenManager.createSecurityToken(config, service, action);
    }

    public boolean refreshTokens() throws CarNetException {
        return tokenManager.refreshTokens(config);
    }
}
