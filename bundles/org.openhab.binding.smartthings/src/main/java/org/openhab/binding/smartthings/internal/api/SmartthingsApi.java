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

import java.io.IOException;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.dto.SmartthingsApp;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.OAuthConfigRequest;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class to handle Smartthings Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsApi {

    private static final String BEARER = "Bearer ";

    private final Logger logger = LoggerFactory.getLogger(SmartthingsApi.class);

    private final OAuthClientService oAuthClientService;
    private final SmartthingsNetworkConnector networkConnector;
    private final String token;
    private Gson gson = new Gson();
    private String baseUrl = "https://api.smartthings.com/v1";
    private String deviceEndPoint = "/devices";
    private String appEndPoint = "/apps";

    /**
     * Constructor.
     *
     * @param httpClientFactory The httpClientFactory
     * @param OAuthClientService The oAuthClientService
     * @param token The token to access the API
     */
    public SmartthingsApi(HttpClientFactory httpClientFactory, SmartthingsNetworkConnector networkConnector,
            OAuthClientService oAuthClientService, String token) {
        this.oAuthClientService = oAuthClientService;
        this.token = token;
        this.networkConnector = networkConnector;
    }

    public SmartthingsDevice[] GetAllDevices() {
        SmartthingsDevice[] devices = networkConnector.DoRequest(SmartthingsDevice[].class, baseUrl + deviceEndPoint, null, token, null,
                HttpMethod.GET);
        return devices;
    }

    public AppResponse SetupApp() {

        SmartthingsApp[] appList = ListAllApp();

        GetApp(appList[0].appId);

        AppResponse result = CreateApp();
        return result;
    }

    public SmartthingsApp[] ListAllApp() {
        try {
            String uri = baseUrl + appEndPoint;

            SmartthingsApp[] listApps = networkConnector.DoRequest(SmartthingsApp[].class, uri, null, token, null, HttpMethod.GET);

            logger.info("");
            return listApps;

        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public SmartthingsApp GetApp(String appId) {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId;

            SmartthingsApp app = networkConnector.DoRequest(SmartthingsApp.class, uri, null, token, null, HttpMethod.GET);

            return app;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public AppResponse CreateApp() {
        try {
            String uri = baseUrl + appEndPoint + "?signatureType=ST_PADLOCK&requireConfirmation=true";

            String appName = "openhabnew" + new Random().nextInt(10000);
            AppRequest appRequest = new AppRequest();
            appRequest.appName = appName;
            appRequest.displayName = appName;
            appRequest.description = "Desc " + appName;
            appRequest.appType = "WEBHOOK_SMART_APP";
            appRequest.webhookSmartApp = new AppRequest.webhookSmartApp("https://redirect.clae.net/openhabdev/");
            appRequest.classifications = new String[1];
            appRequest.classifications[0] = "AUTOMATION";

            String body = gson.toJson(appRequest);
            AppResponse appResponse = networkConnector.DoRequest(AppResponse.class, uri, null, token, body,
                    HttpMethod.POST);

            return appResponse;
        } catch (

        final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void CreateAppOAuth(String appId) {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId + "/oauth";

            OAuthConfigRequest oAuthConfig = new OAuthConfigRequest();
            oAuthConfig.clientName = "Openhab Integration";
            oAuthConfig.scope = new String[1];
            oAuthConfig.scope[0] = "r:devices:*";

            // oAuthConfig.redirectUris = new String[1];
            // oAuthConfig.redirectUris[0] = "https://redirect.clae.net/openhabdev/";

            String body = gson.toJson(oAuthConfig);
            JsonObject result = networkConnector.DoRequest(JsonObject.class, uri, null, token, body, HttpMethod.PUT);

            // return appResponse;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void SendCommand(String deviceId, String jsonMsg) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/commands";

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                networkConnector.DoRequest(JsonObject.class, uri, null, accessToken, jsonMsg, HttpMethod.POST);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public @Nullable JsonObject SendStatus(String deviceId, String jsonMsg) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/status";

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                JsonObject res = networkConnector.DoRequest(JsonObject.class, uri, null, accessToken, jsonMsg,
                        HttpMethod.GET);
                return res;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private @Nullable JsonElement DoRequest(String uri) {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            } else {

                JsonObject res = networkConnector.DoRequest(JsonObject.class, uri, null, accessToken, null,
                        HttpMethod.GET);
                return res;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (OAuthException | OAuthResponseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
