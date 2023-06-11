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

import java.util.Map;
import java.util.concurrent.ExecutionException;
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
import org.openhab.binding.electroluxair.internal.dto.ElectroluxPureA9DTO.AppliancesInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ElectroluxDeltaAPI} class defines the Elextrolux Delta API
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxDeltaAPI {
    private static final String CLIENT_URL = "https://electrolux-wellbeing-client.vercel.app/api/mu52m5PR9X";
    private static final String SERVICE_URL = "https://api.delta.electrolux.com/api/";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String LOGIN = "Users/Login";
    private static final int MAX_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(ElectroluxDeltaAPI.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final ElectroluxAirBridgeConfiguration configuration;
    private String authToken = "";

    public ElectroluxDeltaAPI(ElectroluxAirBridgeConfiguration configuration, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    public boolean refresh(Map<String, ElectroluxPureA9DTO> electroluxAirThings) {
        try {
            // Login
            login();
            // Get all appliances
            String json = getAppliances();
            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();

            for (JsonElement jsonElement : jsonArray) {
                String pncId = jsonElement.getAsJsonObject().get("pncId").getAsString();

                // Get appliance info
                String jsonApplianceInfo = getAppliancesInfo(pncId);
                AppliancesInfo appliancesInfo = gson.fromJson(jsonApplianceInfo, AppliancesInfo.class);

                // Get applicance data
                ElectroluxPureA9DTO dto = getAppliancesData(pncId, ElectroluxPureA9DTO.class);
                if (appliancesInfo != null) {
                    dto.setApplicancesInfo(appliancesInfo);
                }
                electroluxAirThings.put(dto.getTwin().getProperties().getReported().deviceId, dto);
            }
            return true;
        } catch (ElectroluxAirException e) {
            logger.warn("Failed to refresh! {}", e.getMessage());
        }
        return false;
    }

    public boolean workModePowerOff(String pncId) {
        String commandJSON = "{ \"WorkMode\": \"PowerOff\" }";
        try {
            return sendCommand(commandJSON, pncId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode powerOff failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeAuto(String pncId) {
        String commandJSON = "{ \"WorkMode\": \"Auto\" }";
        try {
            return sendCommand(commandJSON, pncId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode auto failed {}", e.getMessage());
        }
        return false;
    }

    public boolean workModeManual(String pncId) {
        String commandJSON = "{ \"WorkMode\": \"Manual\" }";
        try {
            return sendCommand(commandJSON, pncId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    public boolean setFanSpeedLevel(String pncId, int fanSpeedLevel) {
        if (fanSpeedLevel < 1 && fanSpeedLevel > 10) {
            return false;
        } else {
            String commandJSON = "{ \"Fanspeed\": " + fanSpeedLevel + "}";
            try {
                return sendCommand(commandJSON, pncId);
            } catch (ElectroluxAirException e) {
                logger.warn("Work mode manual failed {}", e.getMessage());
            }
        }
        return false;
    }

    public boolean setIonizer(String pncId, String ionizerStatus) {
        String commandJSON = "{ \"Ionizer\": " + ionizerStatus + "}";
        try {
            return sendCommand(commandJSON, pncId);
        } catch (ElectroluxAirException e) {
            logger.warn("Work mode manual failed {}", e.getMessage());
        }
        return false;
    }

    private void login() throws ElectroluxAirException {
        // Fetch ClientToken
        Request request = httpClient.newRequest(CLIENT_URL).method(HttpMethod.GET);

        request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
        request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);

        logger.debug("HTTP GET Request {}.", request.toString());
        try {
            ContentResponse httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAirException("Failed to login " + httpResponse.getContentAsString());
            }
            String json = httpResponse.getContentAsString();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String clientToken = jsonObject.get("accessToken").getAsString();

            // Login using ClientToken
            json = "{ \"Username\": \"" + configuration.username + "\",  \"Password\": \"" + configuration.password
                    + "\" }";
            request = httpClient.newRequest(SERVICE_URL + LOGIN).method(HttpMethod.POST);
            request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
            request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + clientToken);
            request.content(new StringContentProvider(json), JSON_CONTENT_TYPE);

            logger.debug("HTTP POST Request {}.", request.toString());

            httpResponse = request.send();
            if (httpResponse.getStatus() != HttpStatus.OK_200) {
                throw new ElectroluxAirException("Failed to login " + httpResponse.getContentAsString());
            }
            // Fetch AccessToken
            json = httpResponse.getContentAsString();
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
            this.authToken = jsonObject.get("accessToken").getAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private String getFromApi(String uri) throws ElectroluxAirException, InterruptedException {
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = httpClient.newRequest(SERVICE_URL + uri).method(HttpMethod.GET);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);

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
        String uri = "Domains/Appliances";
        try {
            return getFromApi(uri);
        } catch (ElectroluxAirException | InterruptedException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private String getAppliancesInfo(String pncId) throws ElectroluxAirException {
        String uri = "AppliancesInfo/" + pncId;
        try {
            return getFromApi(uri);
        } catch (ElectroluxAirException | InterruptedException e) {
            throw new ElectroluxAirException(e);
        }
    }

    private <T> T getAppliancesData(String pncId, Class<T> dto) throws ElectroluxAirException {
        String uri = "Appliances/" + pncId;
        String json;

        try {
            json = getFromApi(uri);
        } catch (ElectroluxAirException | InterruptedException e) {
            throw new ElectroluxAirException(e);
        }
        return gson.fromJson(json, dto);
    }

    private boolean sendCommand(String commandJSON, String pncId) throws ElectroluxAirException {
        String uri = "Appliances/" + pncId + "/Commands";
        try {
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    Request request = httpClient.newRequest(SERVICE_URL + uri).method(HttpMethod.PUT);
                    request.header(HttpHeader.AUTHORIZATION, "Bearer " + authToken);
                    request.header(HttpHeader.ACCEPT, JSON_CONTENT_TYPE);
                    request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
                    request.content(new StringContentProvider(commandJSON), JSON_CONTENT_TYPE);

                    ContentResponse response = request.send();
                    String content = response.getContentAsString();
                    logger.trace("API response: {}", content);

                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.debug("sendCommand failed, HTTP status: {}", response.getStatus());
                        login();
                    } else {
                        CommandResponseDTO commandResponse = gson.fromJson(content, CommandResponseDTO.class);
                        if (commandResponse != null) {
                            if (commandResponse.code == 200000) {
                                return true;
                            } else {
                                logger.warn("Failed to send command, error code: {}, description: {}",
                                        commandResponse.code, commandResponse.codeDescription);
                                return false;
                            }
                        } else {
                            logger.warn("Failed to send command, commandResponse is null!");
                            return false;
                        }
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

    @SuppressWarnings("unused")
    private static class CommandResponseDTO {
        public int code;
        public String codeDescription = "";
        public String information = "";
        public String message = "";
        public PayloadDTO payload = new PayloadDTO();
        public int status;
    }

    private static class PayloadDTO {
        @SerializedName("Ok")
        public boolean ok;
        @SerializedName("Response")
        public ResponseDTO response = new ResponseDTO();
    }

    private static class ResponseDTO {
        @SerializedName("Workmode")
        public String workmode = "";
    }
}
