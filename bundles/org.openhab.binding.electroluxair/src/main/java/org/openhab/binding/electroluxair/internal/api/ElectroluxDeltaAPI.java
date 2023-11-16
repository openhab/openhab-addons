/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.electroluxair.internal.api;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.electroluxair.internal.ElectroluxAirBridgeConfiguration;
import org.openhab.binding.electroluxair.internal.ElectroluxAirException;
import org.openhab.binding.electroluxair.internal.dto.ElectroluxPureA9DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ElectroluxDeltaAPI} class defines the Elextrolux Delta API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxDeltaAPI {
    private static final String CLIENT_ID = "ElxOneApp";
    private static final String CLIENT_SECRET = "8UKrsKD7jH9zvTV7rz5HeCLkit67Mmj68FvRVTlYygwJYy4dW6KF2cVLPKeWzUQUd6KJMtTifFf4NkDnjI7ZLdfnwcPtTSNtYvbP7OzEkmQD9IjhMOf5e1zeAQYtt2yN";
    private static final String X_API_KEY = "2AMqwEV5MqVhTKrRCyYfVF8gmKrd2rAmp7cUsfky";

    private static final String BASE_URL = "https://api.ocp.electrolux.one";
    private static final String TOKEN_URL = BASE_URL + "/one-account-authorization/api/v1/token";
    private static final String AUTHENTICATION_URL = BASE_URL + "/one-account-authentication/api/v1/authenticate";
    private static final String API_URL = BASE_URL + "/appliance/api/v2";
    private static final String APPLIANCES_URL = API_URL + "/appliances";

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final int MAX_RETRIES = 3;
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(ElectroluxDeltaAPI.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final ElectroluxAirBridgeConfiguration configuration;
    private String authToken = "";
    private Instant tokenExpiry = Instant.MIN;

    public ElectroluxDeltaAPI(ElectroluxAirBridgeConfiguration configuration, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    public boolean refresh(Map<String, ElectroluxPureA9DTO> electroluxAirThings) {
        try {
            if (Instant.now().isAfter(this.tokenExpiry)) {
                // Login again since token is expired
                login();
            }
            // Get all appliances
            String json = getAppliances();
            ElectroluxPureA9DTO[] dtos = gson.fromJson(json, ElectroluxPureA9DTO[].class);
            if (dtos != null) {
                for (ElectroluxPureA9DTO dto : dtos) {
                    String applianceId = dto.getApplianceId();
                    // Get appliance info
                    String jsonApplianceInfo = getAppliancesInfo(applianceId);
                    ElectroluxPureA9DTO.ApplianceInfo applianceInfo = gson.fromJson(jsonApplianceInfo,
                            ElectroluxPureA9DTO.ApplianceInfo.class);
                    if (applianceInfo != null) {
                        if ("AIR_PURIFIER".equals(applianceInfo.getDeviceType())) {
                            dto.setApplianceInfo(applianceInfo);
                            electroluxAirThings.put(dto.getProperties().getReported().getDeviceId(), dto);
                        }
                    }
                }
                return true;
            }
        } catch (JsonSyntaxException | ElectroluxAirException e) {
            logger.warn("Failed to refresh! {}", e.getMessage());
        }
        return false;
    }

    public boolean workModePowerOff(String applianceId) {
        String commandJSON = "{ \"WorkMode\": \"PowerOff\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode powerOff failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeAuto(String applianceId) {
        String commandJSON = "{ \"WorkMode\": \"Auto\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode auto failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeManual(String applianceId) {
        String commandJSON = "{ \"WorkMode\": \"Manual\" }";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setFanSpeedLevel(String applianceId, int fanSpeedLevel) {
        if (fanSpeedLevel < 1 && fanSpeedLevel > 10) {
            return false;
        } else {
            String commandJSON = "{ \"Fanspeed\": " + fanSpeedLevel + "}";
            try {
                return sendCommand(commandJSON, applianceId);
            } catch (ElectroluxAirException e) {
                logger.warn("Work mode manual failed {}", e.getMessage());
            }
        }
        return false;
    }

    public boolean setIonizer(String applianceId, String ionizerStatus) {
        String commandJSON = "{ \"Ionizer\": " + ionizerStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setUILight(String applianceId, String uiLightStatus) {
        String commandJSON = "{ \"UILight\": " + uiLightStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setSafetyLock(String applianceId, String safetyLockStatus) {
        String commandJSON = "{ \"SafetyLock\": " + safetyLockStatus + "}";
        try {
            return sendCommand(commandJSON, applianceId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    private Request createRequest(String uri, HttpMethod httpMethod) {
        Request request = httpClient.newRequest(uri).method(httpMethod);
        request.timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
        request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);

        logger.debug("HTTP POST Request {}.", request.toString());

        return request;
    }

    private void login() throws ElectroluxAirException {
        try {
            String json = "{\"clientId\": \"" + CLIENT_ID + "\", \"clientSecret\": \"" + CLIENT_SECRET
                    + "\", \"grantType\": \"client_credentials\"}";

            // Fetch ClientToken
            Request request = createRequest(TOKEN_URL, HttpMethod.POST);
            request.content(new StringContentProvider(json), JSON_CONTENT_TYPE);

            logger.debug("HTTP POST Request {}.", request.toString());

            ContentResponse httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAirException("Failed to get token 1" + httpResponse.getContentAsString());
            }
            json = httpResponse.getContentAsString();
            logger.trace("Token 1: {}", json);
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String clientToken = jsonObject.get("accessToken").getAsString();

            // Login using access token 1
            json = "{ \"username\": \"" + configuration.username + "\",  \"password\": \"" + configuration.password
                    + "\" }";
            request = createRequest(AUTHENTICATION_URL, HttpMethod.POST);
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + clientToken);
            request.header("x-api-key", X_API_KEY);

            request.content(new StringContentProvider(json), JSON_CONTENT_TYPE);

            logger.debug("HTTP POST Request {}.", request.toString());

            httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAirException("Failed to login " + httpResponse.getContentAsString());
            }
            json = httpResponse.getContentAsString();
            logger.trace("Token 2: {}", json);
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String idToken = jsonObject.get("idToken").getAsString();
            String countryCode = jsonObject.get("countryCode").getAsString();
            String credentials = "{\"clientId\": \"" + CLIENT_ID + "\", \"idToken\": \"" + idToken
                    + "\", \"grantType\": \"urn:ietf:params:oauth:grant-type:token-exchange\"}";

            // Fetch access token 2
            request = createRequest(TOKEN_URL, HttpMethod.POST);
            request.header("Origin-Country-Code", countryCode);
            request.content(new StringContentProvider(credentials), JSON_CONTENT_TYPE);

            logger.debug("HTTP POST Request {}.", request.toString());

            httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAirException("Failed to get token 1" + httpResponse.getContentAsString());
            }

            // Fetch AccessToken
            json = httpResponse.getContentAsString();
            logger.trace("AccessToken: {}", json);
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
            this.authToken = jsonObject.get("accessToken").getAsString();
            int expiresIn = jsonObject.get("expiresIn").getAsInt();
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private String getFromApi(String uri) throws ElectroluxAirException, InterruptedException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(uri, HttpMethod.GET);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header("x-api-key", X_API_KEY);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("getFromApi failed, HTTP status: {}", response.getStatus());
                        login();
                    } else {
                        return content;
                    }
                } catch (TimeoutException e) {
                    logger.debug("TimeoutException error in get: {}", e.getMessage());
                }
            }
            throw new ElectroluxAirException("Failed to fetch from API!");
        } catch (JsonSyntaxException | ElectroluxAirException | ExecutionException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private String getAppliances() throws ElectroluxAirException {
        try {
            return getFromApi(APPLIANCES_URL);
        } catch (ElectroluxAirException | InterruptedException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private String getAppliancesInfo(String applianceId) throws ElectroluxAirException {
        try {
            return getFromApi(APPLIANCES_URL + "/" + applianceId + "/info");
        } catch (ElectroluxAirException | InterruptedException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private boolean sendCommand(String commandJSON, String applianceId) throws ElectroluxAirException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = createRequest(APPLIANCES_URL + "/" + applianceId + "/command", HttpMethod.PUT);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header("x-api-key", X_API_KEY);
                    request.content(new StringContentProvider(commandJSON), JSON_CONTENT_TYPE);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("sendCommand failed, HTTP status: {}", response.getStatus());
                        login();
                    } else {
                        return true;
                    }
                } catch (TimeoutException | InterruptedException e) {
                    logger.warn("TimeoutException error in get");
                }
            }
        } catch (JsonSyntaxException | ElectroluxAirException | ExecutionException e) {
            throw new ElectroluxAirException(e);
        }
        return false;
    }
}
