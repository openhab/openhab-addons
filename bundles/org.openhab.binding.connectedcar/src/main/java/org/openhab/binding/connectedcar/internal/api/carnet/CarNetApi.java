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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.measure.IncommensurableException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.ApiActionRequest;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.GeoPosition;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.JwtToken;
import org.openhab.binding.connectedcar.internal.api.ApiResult;
import org.openhab.binding.connectedcar.internal.api.ApiWithOAuth;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNChargerInfo.CarNetChargerStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNClimater;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNClimater.CarNetClimaterStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNDestinations;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNDestinations.CarNetDestinationList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNEluActionHistory;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNFindCarResponse;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNGeoFenceAlertConfig;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNGeoFenceAlertConfig.CarNetGeoFenceConfig;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNGeoFenceAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNHeaterVentilation;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNHeaterVentilation.CarNetHeaterVentilationStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNHonkFlashResponse;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList.CarNetOperationList.CarNetServiceInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNOperationList.CarNetOperationList.CarNetServiceInfo.CNServiceOperation;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNPairingInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNRequestStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNRoleRights;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNRoleRights.CarNetUserRoleRights;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNSpeedAlertConfig;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNSpeedAlertConfig.CarNetSpeedAlertConfig;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNSpeedAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNStoredPosition;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNVehicleDetails;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetActionResponse.CNActionResponse;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetHomeRegion;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetMbbStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetOidcConfig;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetPersonalData;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetTripData;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleList;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetApi} implements the based API access to CarNet
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetApi extends ApiWithOAuth {
    private final Logger logger = LoggerFactory.getLogger(CarNetApi.class);

    public CarNetApi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    /**
     * Simple initialization, in fact used by Account Handler
     *
     * @param configIn
     * @throws ApiException
     */
    @Override
    public void initialize(CombinedConfig configIn) throws ApiException {
        super.initialize(configIn);
        if (!config.api.oidcConfigUrl.isEmpty()) {
            config.oidcConfig = getOidcConfig();
            // default from OIDC config, may be overwritten by brand config
            config.api.issuerRegionMappingUrl = config.oidcConfig.issuer;
        }
    }

    @Override
    public CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException {
        super.initialize(vin, configIn);

        // update based von VIN specific settings
        config.vehicle.vin = vin.toUpperCase();
        thingId = config.vehicle.vin;
        config.vstatus.homeRegionUrl = getHomeReguionUrl();
        config.vstatus.apiUrlPrefix = getApiUrl();

        CarNetOperationList ol = getOperationList();
        if (ol != null) {
            config.vstatus.operationList = ol;
            CarNetServiceInfo desc = getServiceDescriptor(CNAPI_SERVICE_VEHICLE_STATUS_REPORT);
            if (desc != null && desc.invocationUrl != null) {
                String url = desc.invocationUrl.content;
                if (url.startsWith("https://mal-3a.") && url.contains("/api")) {
                    config.vstatus.homeRegionUrl = substringBefore(url, "/api") + "/api";
                    logger.debug("{}: Home Region URL changed to {}", config.getLogId(), config.vstatus.homeRegionUrl);
                }
            }

            config.user.id = ol.userId;
            config.user.role = ol.role;
            config.user.status = ol.status;
            config.user.securityLevel = ol.securityLevel;

            config.user.identity = getUserIdentity();
            config.user.profileUrl = getProfileUrl();
        }

        try {
            config.vstatus.pairingInfo = getPairingStatus();
        } catch (ApiException e) {
            logger.debug("{}: Unable to verify pairing status: {}", thingId, e.toString());
        }

        setConfig(config);
        return config;
    }

    public String getUserIdentity() throws ApiException {
        String idToken = createIdToken(); // extract identity id from jwt token
        JwtToken jwt = decodeJwt(idToken);
        return jwt.sub;// get identity from JWT sub
    }

    public String getProfileUrl() throws ApiException {
        if (config.user.identity.isEmpty()) {
            config.user.identity = getUserIdentity();
        }
        return config.api.customerProfileServiceUrl + "/customers/" + config.user.identity;
    }

    public CarNetPersonalData getPersonalData() throws ApiException {
        return callProfileApi("getPersonalData", "/personalData", CarNetPersonalData.class);
    }

    public CarNetMbbStatus getMbbStatus() throws ApiException {
        if (config.vstatus.mbb.mbbUserId.isEmpty()) {
            // currently empty, query from API, otherwise return cached result
            config.vstatus.mbb = callProfileApi("getMbbStatus", "/mbbStatusData", CarNetMbbStatus.class);
        }
        return config.vstatus.mbb;
    }

    public <T> T callProfileApi(String api, String uri, Class<T> classOfT) throws ApiException {
        return callApi("", config.user.profileUrl + uri, fillAppHeaders(tokenManager.createProfileToken(config)), api,
                classOfT);
    }

    public CarNetPairingInfo getPairingStatus() throws ApiException {
        return callApi("usermanagement/users/v1/{0}/{1}/vehicles/{2}/pairing", "", CNPairingInfo.class).pairingInfo;
    }

    public String getMyDestinationsFeed() throws ApiException {
        return callApi("destinationfeedservice/mydestinations/v1/{0}/{1}/vehicles/{2}/users/{3}/destinations",
                "getMyDestinationsFeed", String.class);
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        // return callApi("https://msg.volkswagen.de/fs-car/usermanagement/users/v1/{0}/{1}/vehicles", "getVehicles",
        CarNetVehicleList vehicles = callApi("usermanagement/users/v1/{0}/{1}/vehicles", "getVehicles",
                CarNetVehicleList.class);
        if (vehicles.userVehicles != null && vehicles.userVehicles.vehicle != null) {
            return vehicles.userVehicles.vehicle;
        }
        throw new ApiException("Account has no registered vehicles, go to online port and add at least 1 vehicle");
    }

    @Override
    public VehicleDetails getVehicleDetails(String vin) throws ApiException {
        Map<String, String> headers = fillAppHeaders();
        headers.put("Accept",
                "application/vnd.vwg.mbb.vehicleDataDetail_v2_1_0+json, application/vnd.vwg.mbb.genericError_v1_0_2+json");
        CNVehicleDetails details = callApi(vin, "vehicleMgmt/vehicledata/v2/{0}/{1}/vehicles/{2}", headers,
                "getVehicleDetails", CNVehicleDetails.class);
        return new VehicleDetails(config, details.vehicleDataDetail);
    }

    @Override
    public VehicleStatus getVehicleStatus() throws ApiException {
        return new VehicleStatus(callApi(getApiUrl() + "/" + "bs/vsr/v1/{0}/{1}/vehicles/{2}/status",
                "getVehicleStatus", CarNetVehicleStatus.class));
    }

    @Override
    public String refreshVehicleStatus() throws ApiException {
        String json = http.post("bs/vsr/v1/{0}/{1}/vehicles/{2}/requests", fillAppHeaders(), "", "").response;
        return queuePendingAction(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, "status", json);
    }

    @Override
    public String getVehicleRequets() throws ApiException {
        return http.post("bs/vsr/v1/{0}/{1}/vehicles/{2}/requests", fillAppHeaders(), "", "").response;
    }

    @Override
    public GeoPosition getVehiclePosition() throws ApiException {
        // needs explicit Accept: application/json, otherwhise storedPosition is returned
        Map<String, String> headers = fillAppHeaders();
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
        return new GeoPosition(callApi("", "bs/cf/v1/{0}/{1}/vehicles/{2}/position", headers, "getVehiclePosition",
                CNFindCarResponse.class));
    }

    @Override
    public GeoPosition getStoredPosition() throws ApiException {
        return new GeoPosition(
                callApi("bs/cf/v1/{0}/{1}/vehicles/{2}/position", "getStoredPosition", CNStoredPosition.class));
    }

    public CarNetDestinationList getDestinations() throws ApiException {
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

    public String getHistory() throws ApiException {
        return callApi("bs/dwap/v1/{0}/{1}/vehicles/{2}/history", "getHistory", String.class);
    }

    public @Nullable String getVehicleUsers() throws ApiException {
        return callApi("bs//uic/v1/vin/{2}/users", "getVehicleUsers", String.class);
    }

    public CarNetChargerStatus getChargerStatus() throws ApiException {
        return callApi("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger", "chargerStatus",
                CNChargerInfo.class).charger;
    }

    public CarNetTripData getTripData(String type) throws ApiException {
        String url = "bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/" + type + "?type=list";
        return callApi(url, "getTripData", CarNetTripData.class);
    }

    public @Nullable CarNetOperationList getOperationList() throws ApiException {
        return config.vstatus.operationList != null ? config.vstatus.operationList
                : callApi(config.vstatus.rolesRightsUrl + "/rolesrights/operationlist/v3/vehicles/{2}?scope=ALL",
                        "getOperationList", CNOperationList.class).operationList;
    }

    @Override
    public String controlLock(boolean lock) throws ApiException {
        String action = lock ? CNAPI_ACTION_REMOTE_LOCK_UNLOCK_LOCK : CNAPI_ACTION_REMOTE_LOCK_UNLOCK_UNLOCK;
        String data = "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"
                + "<rluAction xmlns=\"http://audi.de/connect/rlu\">" + "<action>" + action.toLowerCase()
                + "</action></rluAction>";
        return sendAction("bs/rlu/v1/{0}/{1}/vehicles/{2}/actions", CNAPI_SERVICE_REMOTE_LOCK_UNLOCK, action, true,
                "application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml", data);
    }

    public CarNetRluHistory getRluActionHistory() throws ApiException {
        return callApi("bs/rlu/v1/{0}/{1}/vehicles/{2}/actions", "rluActionHistory",
                CNEluActionHistory.class).actionsResponse;
    }

    @Override
    public String controlClimater(boolean start, String heaterSource) throws ApiException {
        String contentType = "application/vnd.vwg.mbb.ClimaterAction_v1_0_0+xml;charset=utf-8";
        String body = "", action = "";
        boolean secToken = !CNAPI_HEATER_SOURCE_ELECTRIC.equals(heaterSource);
        if (start) {
            if ((config.account.apiLevelClimatisation == 1) || heaterSource.isEmpty()) {
                // simplified format without header source, Skoda?
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>startClimatisation</type></action>";
            } else if (config.account.apiLevelClimatisation == 3) {
                // standard format with header source, e.g. E-Tron
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><action><type>startClimatisation</type>"
                        + "<settings><heaterSource>" + heaterSource + "</heaterSource></settings></action>";
            } else {
                // json format, e.g. VW
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

    @Override
    public String controlClimaterTemp(double tempC, String heaterSource) throws ApiException {
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
            return API_REQUEST_ERROR;
        }
    }

    @Override
    public String controlPreHeating(boolean start, int duration) throws ApiException {
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

    @Override
    public String controlVentilation(boolean start, int duration) throws ApiException {
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

    @Override
    public String controlCharger(boolean start) throws ApiException {
        String action = start ? "start" : "stop";
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>" + action + "</type></action>";
        return sendAction("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions", "batterycharge_v1", action, false,
                "application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml", data);
    }

    @Override
    public String controlMaxCharge(int maxCurrent) throws ApiException {
        String contentType = "application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml";
        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><action><type>setSettings</type>"
                + "<settings><maxChargeCurrent>" + maxCurrent + "</maxChargeCurrent></settings></action>";
        return sendAction("bs/batterycharge/v1/{0}/{1}/vehicles/{2}/charger/actions",
                CNAPI_SERVICE_REMOTE_BATTERY_CHARGE, "setMaxCharge", false, contentType, body);
    }

    @Override
    public String controlTargetChgLevel(int targetLevel) throws ApiException {
        throw new ApiException("Unsupported function");
    }

    public CarNetClimaterStatus getClimaterStatus() throws ApiException {
        return callApi("bs/climatisation/v1/{0}/{1}/vehicles/{2}/climater", "climaterStatus",
                CNClimater.class).climater;
    }

    public CarNetHeaterVentilationStatus getHeaterVentilationStatus() throws ApiException {
        return callApi("bs/rs/v1/{0}/{1}/vehicles/{2}/status", "heaterVentilationStatus",
                CNHeaterVentilation.class).statusResponse;
    }

    public CarNetSpeedAlertConfig getSpeedAlertConfig() throws ApiException {
        return callApi("bs/speedalert/v1/{0}/{1}/vehicles/{2}/speedAlertConfiguration", "getSpeedAlertConfig",
                CNSpeedAlertConfig.class).speedAlertConfiguration;
    }

    public CarNetSpeedAlerts getSpeedAlerts() throws ApiException {
        CNSpeedAlerts sa = callApi("bs/speedalert/v1/{0}/{1}/vehicles/{2}/speedAlerts", "getSpeedAlerts",
                CNSpeedAlerts.class);
        return sa.speedAlerts != null ? sa.speedAlerts : new CarNetSpeedAlerts();
    }

    public CarNetGeoFenceConfig getGeoFenceConfig() throws ApiException {
        return callApi("bs/geofencing/v1/{0}/{1}/vehicles/{2}/geofencingConfiguration", "getGeoFenceConfig",
                CNGeoFenceAlertConfig.class).geofencingConfiguration;
    }

    public CarNetGeoFenceAlerts getGeoFenceAlerts() throws ApiException {
        CNGeoFenceAlerts gfa = callApi("bs/geofencing/v1/{0}/{1}/vehicles/{2}/geofencingAlerts", "getGeoFenceAlerts",
                CNGeoFenceAlerts.class);
        return gfa.geofencingAlerts != null ? gfa.geofencingAlerts : new CarNetGeoFenceAlerts();
    }

    public String getClimaterTimer() throws ApiException {
        String json = callApi("bs/departuretimer/v1/{0}/{1}/vehicles/{2}/timer", "getClimaterTimer", String.class);
        return json;
    }

    @Override
    public String controlWindowHeating(boolean start) throws ApiException {
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
     * @throws ApiException
     */
    @Override
    public String controlHonkFlash(boolean honk, PointType position, int duration) throws ApiException {
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
            String body) throws ApiException {
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
                Map<String, String> headers = fillActionHeaders(contentType, createAccessToken(),
                        CNAPI_ACTION_REMOTE_PRETRIP_CLIMATISATION_START_AUX_OR_AUTO.equals(action) ? " X-securityToken"
                                : "x-mbbSecToken",
                        reqSecToken ? createSecurityToken(service, action) : "");
                String json = http.post(uri, headers, body).response;
                return queuePendingAction(service, action, json);
            }
        } catch (ApiException e) {
            message = e.toString();
        }
        if (eventListener != null) {
            eventListener.onActionNotification(service, action, message);
        }
        return API_REQUEST_REJECTED;
    }

    public String queuePendingAction(String service, String action, String json) throws ApiException {
        CNActionResponse in;
        if (CNAPI_SERVICE_REMOTE_HONK_AND_FLASH.equals(service)) {
            // Honk&Flash has special format
            in = new CNActionResponse();
            in.honkAndFlashRequest = fromJson(gson, json, CNHonkFlashResponse.class).honkAndFlashRequest;
        } else {
            in = fromJson(gson, json, CNActionResponse.class);
        }
        CarNetPendingRequest req = new CarNetPendingRequest(service, action, in);
        return queuePendingAction(new ApiActionRequest(req));
    }

    public String getTripStats(String tripType) throws ApiException {
        String json = callApi("bs/tripstatistics/v1/{0}/{1}/vehicles/{2}/tripdata/" + tripType + "?newest", "",
                String.class);
        return json;
    }

    @Override
    public String getApiRequestStatus(ApiActionRequest req) throws ApiException {
        String status = "", error = "";
        CNRequestStatus rs = callApi(req.checkUrl, "getRequestStatus", CNRequestStatus.class);
        if (rs.requestStatusResponse != null) {
            status = rs.requestStatusResponse.status;
            if (rs.requestStatusResponse.error != null) {
                error = "" + rs.requestStatusResponse.error;
            }
        } else if (rs.action != null) {
            status = getString(rs.action.actionState);
            error = "" + getInteger(rs.action.errorCode);
        } else if (rs.status != null) {
            status = getString(rs.status.statusCode);
        }
        req.status = status;
        req.error = error;
        return status;
    }

    /**
     * Get OpenID Connect configuration
     *
     * @return OIDC config
     * @throws ApiException
     */
    private CarNetOidcConfig getOidcConfig() throws ApiException {
        // get OIDC confug
        String url = config.api.oidcConfigUrl;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_JSON);
        headers.put(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_FORM_URLENC);
        ApiResult res = http.get(url, headers);
        String json = res.response;
        config.api.oidcDate = res.getResponseDate();
        return fromJson(gson, json, CarNetOidcConfig.class);
    }

    /**
     * Return serviceId with available version (e.g. xxx_v4)
     *
     * @param serviceId Base serviceId (usually with suffix _v1)
     * @return id from Operation List
     */
    @Override
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
    @Override
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

    public CarNetUserRoleRights getRoleRights() throws ApiException {
        return callApi("rolesrights/permissions/v1/{0}/{1}/vehicles/{2}/fetched-role", "getRoleRights",
                CNRoleRights.class).role;
    }

    @Override
    public String getHomeReguionUrl() {
        if (!config.vstatus.homeRegionUrl.isEmpty()) {
            return config.vstatus.homeRegionUrl;
        }
        String url = "";
        try {
            CarNetHomeRegion region = callApi(CNAPI_VWG_MAL_1A_CONNECT + "/cs/vds/v1/vehicles/{2}/homeRegion",
                    "getHomeRegion", CarNetHomeRegion.class);
            url = getString(region.homeRegion.baseUri.content);
        } catch (ApiException e) {
            url = CNAPI_VWG_MAL_1A_CONNECT;
        }
        config.vstatus.homeRegionUrl = url;
        return url;
    }

    @Override
    public String getApiUrl() throws ApiException {
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

    private Map<String, String> fillActionHeaders(String contentType, String accessToken, String secTokenHeader,
            String securityToken) throws ApiException {
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
        ApiHttpMap map = new ApiHttpMap();
        map.header(HttpHeader.USER_AGENT, CNAPI_HEADER_USER_AGENT);
        map.header(CNAPI_HEADER_APP, config.api.xappName);
        map.header(CNAPI_HEADER_VERS, config.api.xappVersion);
        if (!contentType.isEmpty()) {
            map.header(HttpHeader.CONTENT_TYPE, contentType);
        }
        map.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON
                + ", application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml,application/vnd.volkswagenag.com-error-v1+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml,application/vnd.vwg.mbb.operationList_v3_0_2+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,*/*");
        map.header(HttpHeader.ACCEPT_CHARSET, StandardCharsets.UTF_8.toString());
        map.header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        String host = substringBetween(config.api.apiDefaultUrl, "//", "/");
        map.header(HttpHeader.HOST.toString(), host);
        if (!securityToken.isEmpty()) {
            map.header(secTokenHeader, securityToken);
        }
        return map.getHeaders();
    }
}
