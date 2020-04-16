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
package org.openhab.binding.ecobee.internal.api;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.eclipse.smarthome.core.auth.client.oauth2.AccessTokenResponse;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthClientService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthException;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthResponseException;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.ecobee.internal.dto.AbstractResponseDTO;
import org.openhab.binding.ecobee.internal.dto.SelectionDTO;
import org.openhab.binding.ecobee.internal.dto.SelectionType;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatRequestDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatResponseDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatUpdateRequestDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.RevisionDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.RevisionDTODeserializer;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.RunningDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.RunningDTODeserializer;
import org.openhab.binding.ecobee.internal.dto.thermostat.summary.SummaryResponseDTO;
import org.openhab.binding.ecobee.internal.function.FunctionRequest;
import org.openhab.binding.ecobee.internal.handler.EcobeeAccountBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EcobeeApi} is responsible for managing all communication with
 * the Ecobee API service.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeApi implements AccessTokenRefreshListener {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(RevisionDTO.class, new RevisionDTODeserializer())
            .registerTypeAdapter(RunningDTO.class, new RunningDTODeserializer()).create();

    private static final String ECOBEE_THERMOSTAT_URL = ECOBEE_BASE_URL + "1/thermostat";
    private static final String ECOBEE_THERMOSTAT_SUMMARY_URL = ECOBEE_BASE_URL + "1/thermostatSummary";
    private static final String ECOBEE_THERMOSTAT_UPDATE_URL = ECOBEE_THERMOSTAT_URL + "?format=json";

    // These errors from the API will require an Ecobee authorization
    private static final int ECOBEE_TOKEN_EXPIRED = 14;
    private static final int ECOBEE_DEAUTHORIZED_TOKEN = 16;

    public static final Properties HTTP_HEADERS;
    static {
        HTTP_HEADERS = new Properties();
        HTTP_HEADERS.put("Content-Type", "application/json;charset=UTF-8");
        HTTP_HEADERS.put("User-Agent", "openhab-ecobee-api/2.0");
    }

    public static Gson getGson() {
        return GSON;
    }

    private final Logger logger = LoggerFactory.getLogger(EcobeeApi.class);

    private final EcobeeAccountBridgeHandler bridgeHandler;

    private final String apiKey;
    private int apiTimeout;
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;

    private @NonNullByDefault({}) OAuthClientService oAuthClientService;
    private @NonNullByDefault({}) EcobeeAuth ecobeeAuth;

    private String accessToken = "";

    public EcobeeApi(final EcobeeAccountBridgeHandler bridgeHandler, final String apiKey, final int apiTimeout,
            org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory oAuthFactory, HttpClient httpClient) {
        this.bridgeHandler = bridgeHandler;
        this.apiKey = apiKey;
        this.apiTimeout = apiTimeout;
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;

        createOAuthClientService();
    }

    public void createOAuthClientService() {
        logger.debug("API: Creating OAuth Client Service");
        OAuthClientService service = oAuthFactory.createOAuthClientService(
                bridgeHandler.getThing().getUID().getAsString(), ECOBEE_TOKEN_URL, null, apiKey, "", ECOBEE_SCOPE,
                false);
        service.addAccessTokenRefreshListener(this);
        ecobeeAuth = new EcobeeAuth(bridgeHandler, apiKey, apiTimeout, service, httpClient);
        oAuthClientService = service;
    }

    public void deleteOAuthClientService() {
        logger.debug("API: Deleting OAuth Client Service");
        oAuthClientService.removeAccessTokenRefreshListener(this);
        oAuthFactory.deleteServiceAndAccessToken(bridgeHandler.getThing().getUID().getAsString());
    }

    public void closeOAuthClientService() {
        logger.debug("API: Closing OAuth Client Service");
        oAuthClientService.removeAccessTokenRefreshListener(this);
        oAuthFactory.ungetOAuthService(bridgeHandler.getThing().getUID().getAsString());
    }

    /**
     * Check to see if the Ecobee authorization process is complete. This will be determined
     * by requesting an AccessTokenResponse from the OHC OAuth service. If we get a valid
     * response, then assume that the Ecobee authorization process is complete. Otherwise,
     * start the Ecobee authorization process.
     */
    private boolean isAuthorized() {
        boolean isAuthorized = false;
        try {
            AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            if (accessTokenResponse != null) {
                logger.trace("API: Got AccessTokenResponse from OAuth service: {}", accessTokenResponse);
                accessToken = accessTokenResponse.getAccessToken();
                ecobeeAuth.setState(EcobeeAuthState.COMPLETE);
                isAuthorized = true;
            } else {
                logger.debug("API: Didn't get an AccessTokenResponse from OAuth service - doEcobeeAuthorization!!!");
                if (ecobeeAuth.isComplete()) {
                    ecobeeAuth.setState(EcobeeAuthState.NEED_PIN);
                }
            }
            ecobeeAuth.doAuthorization();
        } catch (OAuthException | IOException | RuntimeException e) {
            logger.info("API: Got exception trying to get access token from OAuth service", e);
        } catch (EcobeeAuthException e) {
            logger.info("API: The Ecobee authorization process threw an exception", e);
            ecobeeAuth.setState(EcobeeAuthState.NEED_PIN);
        } catch (OAuthResponseException e) {
            logger.info("API: Exception getting access token: error='{}', description='{}'", e.getError(),
                    e.getErrorDescription());
            // How to handle the possible error codes?
        }
        return isAuthorized;
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse accessTokenResponse) {
        accessToken = accessTokenResponse.getAccessToken();
    }

    public @Nullable SummaryResponseDTO performThermostatSummaryQuery() {
        logger.debug("API: Perform thermostat summary query");
        if (!isAuthorized()) {
            return null;
        }
        SelectionDTO selection = new SelectionDTO();
        selection.selectionType = SelectionType.REGISTERED;
        selection.includeEquipmentStatus = Boolean.TRUE;
        String requestJson = GSON.toJson(new ThermostatRequestDTO(selection), ThermostatRequestDTO.class);
        String response = executeGet(ECOBEE_THERMOSTAT_SUMMARY_URL, requestJson);
        if (response != null) {
            try {
                SummaryResponseDTO summaryResponse = GSON.fromJson(response, SummaryResponseDTO.class);
                if (isSuccess(summaryResponse)) {
                    return summaryResponse;
                }
            } catch (JsonSyntaxException e) {
                logJSException(e, response);
            }
        }
        return null;
    }

    public List<ThermostatDTO> queryRegisteredThermostats() {
        return performThermostatQuery(null);
    }

    public List<ThermostatDTO> performThermostatQuery(final @Nullable Set<String> thermostatIds) {
        logger.debug("API: Perform query on thermostat: '{}'", thermostatIds);
        if (!isAuthorized()) {
            return EMPTY_THERMOSTATS;
        }
        SelectionDTO selection = bridgeHandler.getSelection();
        selection.setThermostats(thermostatIds);
        String requestJson = GSON.toJson(new ThermostatRequestDTO(selection), ThermostatRequestDTO.class);
        String response = executeGet(ECOBEE_THERMOSTAT_URL, requestJson);
        if (response != null) {
            try {
                ThermostatResponseDTO thermostatsResponse = GSON.fromJson(response, ThermostatResponseDTO.class);
                if (isSuccess(thermostatsResponse)) {
                    return thermostatsResponse.thermostatList;
                }
            } catch (JsonSyntaxException e) {
                logJSException(e, response);
            }
        }
        return EMPTY_THERMOSTATS;
    }

    public boolean performThermostatFunction(FunctionRequest request) {
        logger.debug("API: Perform function on thermostat: '{}'", request.selection.selectionMatch);
        if (!isAuthorized()) {
            return false;
        }
        return executePost(ECOBEE_THERMOSTAT_URL, GSON.toJson(request, FunctionRequest.class));
    }

    public boolean performThermostatUpdate(ThermostatUpdateRequestDTO request) {
        logger.debug("API: Perform update on thermostat: '{}'", request.selection.selectionMatch);
        if (!isAuthorized()) {
            return false;
        }
        return executePost(ECOBEE_THERMOSTAT_UPDATE_URL, GSON.toJson(request, ThermostatUpdateRequestDTO.class));
    }

    private String buildQueryUrl(String baseUrl, String requestJson) throws UnsupportedEncodingException {
        final StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?json=");
        urlBuilder.append(URLEncoder.encode(requestJson, StandardCharsets.UTF_8.toString()));
        return urlBuilder.toString();
    }

    private @Nullable String executeGet(String url, String json) {
        String response = null;
        try {
            long startTime = System.currentTimeMillis();
            logger.trace("API: Get Request json is '{}'", json);
            response = HttpUtil.executeUrl("GET", buildQueryUrl(url, json), setHeaders(), null, null, apiTimeout);
            logger.trace("API: Response took {} msec: {}", System.currentTimeMillis() - startTime, response);
        } catch (IOException e) {
            logIOException(e);
        }
        return response;
    }

    private boolean executePost(String url, String json) {
        try {
            logger.trace("API: Post request json is '{}'", json);
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl("POST", url, setHeaders(), new ByteArrayInputStream(json.getBytes()),
                    "application/json", apiTimeout);
            logger.trace("API: Response took {} msec: {}", System.currentTimeMillis() - startTime, response);
            try {
                ThermostatResponseDTO thermostatsResponse = GSON.fromJson(response, ThermostatResponseDTO.class);
                return isSuccess(thermostatsResponse);
            } catch (JsonSyntaxException e) {
                logJSException(e, response);
            }
        } catch (IOException e) {
            logIOException(e);
        }
        return false;
    }

    private void logIOException(Exception e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof TimeoutException || rootCause instanceof EOFException) {
            // These are "normal" errors and should be logged as DEBUG
            logger.debug("API: Call to Ecobee API failed with exception: {}: {}", rootCause.getClass().getSimpleName(),
                    rootCause.getMessage());
        } else {
            // What's left are unexpected errors that should be logged as INFO with a full stack trace
            logger.info("API: Call to Ecobee API failed", e);
        }
    }

    private void logJSException(Exception e, String response) {
        // The API sometimes returns an HTML page complaining of an SSL error
        // Otherwise, this probably should be INFO level
        logger.debug("API: JsonSyntaxException parsing response: {}", response, e);
    }

    private boolean isSuccess(@Nullable AbstractResponseDTO response) {
        boolean success = true;
        if (response == null) {
            logger.info("API: Ecobee API returned null response");
            success = false;
        } else if (response.status.code.intValue() != 0) {
            logger.info("API: Ecobee API returned unsuccessful status: code={}, message={}", response.status.code,
                    response.status.message);
            // The following error indicate that the Ecobee PIN authorization
            // process needs to be restarted
            if (response.status.code == ECOBEE_TOKEN_EXPIRED || response.status.code == ECOBEE_DEAUTHORIZED_TOKEN) {
                deleteOAuthClientService();
                createOAuthClientService();
            }
            success = false;
        }
        return success;
    }

    private Properties setHeaders() {
        Properties headers = new Properties();
        headers.putAll(HTTP_HEADERS);
        headers.put("Authorization", "Bearer " + accessToken);
        return headers;
    }
}
