/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.api;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.OAuthConfigRequest;
import org.openhab.binding.smartthings.internal.dto.SmartthingsApp;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapabilitie;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.dto.SmartthingsRoom;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class to handle Smartthings Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsApi {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsApi.class);

    private final SmartthingsNetworkConnector networkConnector;
    private final String token;

    private static final String APP_NAME = "openhabnew024";
    private Gson gson = new Gson();
    private String baseUrl = "https://api.smartthings.com/v1";
    private String deviceEndPoint = "/devices";
    private String appEndPoint = "/apps";
    private String locationEndPoint = "/locations";
    private String roomsEndPoint = "/rooms";
    private String capabilitiesEndPoint = "/capabilities";

    /**
     * Constructor.
     *
     * @param httpClientFactory The httpClientFactory
     * @param OAuthClientService The oAuthClientService
     * @param token The token to access the API
     */
    public SmartthingsApi(HttpClientFactory httpClientFactory, SmartthingsNetworkConnector networkConnector,
            String token) {
        this.token = token;
        this.networkConnector = networkConnector;
    }

    public SmartthingsDevice[] GetAllDevices() throws SmartthingsException {
        SmartthingsDevice[] devices = DoRequest(SmartthingsDevice[].class, baseUrl + deviceEndPoint);
        return devices;
    }

    public AppResponse SetupApp() throws SmartthingsException {
        SmartthingsApp[] appList = GetAllApps();

        Optional<SmartthingsApp> appOptional = Arrays.stream(appList).filter(x -> APP_NAME.equals(x.appName))
                .findFirst();

        if (appOptional.isPresent()) {
            SmartthingsApp app = appOptional.get(); // Get it from optional
            app = GetApp(app.appId);

            AppResponse result = new AppResponse();
            result.app = app;
            result.oauthClientId = null;
            result.oauthClientSecret = null;

            return result;
        } else {
            AppResponse result = CreateApp();
            return result;
        }
    }

    public SmartthingsCapabilitie[] GetAllCapabilities() throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint;
            SmartthingsCapabilitie[] listCapabilities = DoRequest(SmartthingsCapabilitie[].class, uri);
            return listCapabilities;

        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capabilities", e);
        }
    }

    public SmartthingsCapabilitie GetCapabilitie(String capabilityId, String version) throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint + "/" + capabilityId + "/" + version;
            SmartthingsCapabilitie capabilitie = DoRequest(SmartthingsCapabilitie.class, uri);
            return capabilitie;

        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capability", e);
        }
    }

    public SmartthingsLocation[] GetAllLocations() throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint;
            SmartthingsLocation[] listLocations = DoRequest(SmartthingsLocation[].class, uri);
            return listLocations;

        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve locations", e);
        }
    }

    public SmartthingsLocation GetLocation(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId;

            SmartthingsLocation loc = DoRequest(SmartthingsLocation.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve location", e);
        }
    }

    public SmartthingsRoom[] GetRooms(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint;
            SmartthingsRoom[] listRooms = DoRequest(SmartthingsRoom[].class, uri);
            return listRooms;

        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve rooms", e);
        }
    }

    public SmartthingsRoom GetRoom(String locationId, String roomId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint + "/" + roomId;

            SmartthingsRoom loc = DoRequest(SmartthingsRoom.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve room", e);
        }
    }

    public SmartthingsApp[] GetAllApps() throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint;

            SmartthingsApp[] listApps = DoRequest(SmartthingsApp[].class, uri);

            logger.info("");
            return listApps;

        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve apps", e);
        }
    }

    public SmartthingsApp GetApp(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId;

            SmartthingsApp app = DoRequest(SmartthingsApp.class, uri);

            return app;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve app", e);
        }
    }

    public AppResponse CreateApp() throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "?signatureType=ST_PADLOCK&requireConfirmation=true";

            String appName = APP_NAME;
            AppRequest appRequest = new AppRequest();
            appRequest.appName = appName;
            appRequest.displayName = appName;
            appRequest.description = "Desc " + appName;
            appRequest.appType = "WEBHOOK_SMART_APP";
            appRequest.webhookSmartApp = new AppRequest.webhookSmartApp("https://redirect.clae.net/openhabdev/");
            appRequest.classifications = new String[1];
            appRequest.classifications[0] = "AUTOMATION";

            String body = gson.toJson(appRequest);
            AppResponse appResponse = DoRequest(AppResponse.class, uri, body, false);

            return appResponse;
        } catch (

        final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create app", e);
        }
    }

    public void CreateAppOAuth(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId + "/oauth";

            OAuthConfigRequest oAuthConfig = new OAuthConfigRequest();
            oAuthConfig.clientName = "Openhab Integration";
            oAuthConfig.scope = new String[1];
            oAuthConfig.scope[0] = "r:devices:*";

            String body = gson.toJson(oAuthConfig);
            DoRequest(JsonObject.class, uri, body, true);

            logger.info("");

            // return appResponse;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create oauth settings", e);
        }
    }

    public String getToken() {
        // final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
        // final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
        String accessToken = token;
        if (accessToken.isEmpty()) {
            throw new RuntimeException(
                    "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
        }

        return accessToken;
    }

    public void SendCommand(String deviceId, String jsonMsg) {
        try {

            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/commands";
            DoRequest(JsonObject.class, uri, jsonMsg, false);
        } catch (final Exception e) {
            throw new RuntimeException("SmartthingsApi : Unable to send command", e);
        }
    }

    public @Nullable JsonObject SendStatus(String deviceId, String jsonMsg) throws SmartthingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/status";

            JsonObject res = DoRequest(JsonObject.class, uri, jsonMsg, false);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to send status", e);
        }
    }

    public <T> T DoRequest(Class<T> resultClass, String uri) throws SmartthingsException {
        return DoRequest(resultClass, uri, null, false);
    }

    public <T> T DoRequest(Class<T> resultClass, String uri, @Nullable String body, Boolean update)
            throws SmartthingsException {
        try {
            HttpMethod httpMethod = HttpMethod.GET;
            if (body != null) {
                if (update) {
                    httpMethod = HttpMethod.PUT;
                } else {
                    httpMethod = HttpMethod.POST;
                }
            }
            T res = networkConnector.DoRequest(resultClass, uri, null, getToken(), body, httpMethod);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to do request", e);
        }
    }
}
