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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.ecobee.internal.dto.AbstractResponseDTO;
import org.openhab.binding.ecobee.internal.dto.SelectionDTO;
import org.openhab.binding.ecobee.internal.dto.SelectionType;
import org.openhab.binding.ecobee.internal.dto.oauth.AuthorizeResponseDTO;
import org.openhab.binding.ecobee.internal.dto.oauth.TokenResponseDTO;
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
public class EcobeeApi {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(RevisionDTO.class, new RevisionDTODeserializer())
            .registerTypeAdapter(RunningDTO.class, new RunningDTODeserializer()).create();

    private static final String API_BASE_URL = "https://api.ecobee.com/";
    private static final String API_AUTHORIZE_URL = API_BASE_URL + "authorize";
    private static final String API_TOKEN_URL = API_BASE_URL + "token";
    private static final String API_THERMOSTAT_URL = API_BASE_URL + "1/thermostat";
    private static final String API_THERMOSTAT_SUMMARY_URL = API_BASE_URL + "1/thermostatSummary";
    private static final String API_THERMOSTAT_UPDATE_URL = API_THERMOSTAT_URL + "&format=json";
    private static final List<ThermostatDTO> EMPTY_THERMOSTAT_LIST = Collections.<ThermostatDTO> emptyList();

    private static final Properties HTTP_HEADERS;
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
    private int apiTimeout;

    private OAuthCredentials oAuthCredentials;

    public EcobeeApi(final EcobeeAccountBridgeHandler bridgeHandler, final String apiKey, final int apiTimeout) {
        this.bridgeHandler = bridgeHandler;
        this.apiTimeout = apiTimeout * 1000;
        oAuthCredentials = new OAuthCredentials(apiKey);
    }

    public @Nullable SummaryResponseDTO performThermostatSummaryQuery() {
        logger.debug("API: Perform thermostat summary query");
        if (isAccessTokenInvalid()) {
            return null;
        }
        SelectionDTO selection = new SelectionDTO();
        selection.selectionType = SelectionType.REGISTERED;
        selection.includeEquipmentStatus = Boolean.TRUE;
        String requestJson = GSON.toJson(new ThermostatRequestDTO(selection), ThermostatRequestDTO.class);
        logger.trace("API: Request json is '{}'", requestJson);
        try {
            String response = executeGet(buildQueryUrl(API_THERMOSTAT_SUMMARY_URL, requestJson));
            logger.trace("API: Thermostat summary response: {}", response);
            SummaryResponseDTO summaryResponse = GSON.fromJson(response, SummaryResponseDTO.class);
            if (isSuccess(summaryResponse)) {
                return summaryResponse;
            }
        } catch (IOException e) {
            logger.info("API: Exception getting thermostat summary data: {}", e.getMessage());
        }
        return null;
    }

    public List<ThermostatDTO> queryRegisteredThermostats() {
        return performThermostatQuery(null);
    }

    public List<ThermostatDTO> performThermostatQuery(final @Nullable Set<String> thermostatIds) {
        logger.debug("API: Perform full query on thermostats with thermostatId '{}'", thermostatIds);
        if (isAccessTokenInvalid()) {
            return EMPTY_THERMOSTAT_LIST;
        }
        SelectionDTO selection = bridgeHandler.getSelection();
        selection.setThermostats(thermostatIds);
        String requestJson = GSON.toJson(new ThermostatRequestDTO(selection), ThermostatRequestDTO.class);
        logger.trace("API: Request json is '{}'", requestJson);
        try {
            String response = executeGet(buildQueryUrl(API_THERMOSTAT_URL, requestJson));
            logger.trace("API: Thermostat response: {}", response);
            ThermostatResponseDTO thermostatsResponse = GSON.fromJson(response, ThermostatResponseDTO.class);
            if (isSuccess(thermostatsResponse)) {
                return thermostatsResponse.thermostatList;
            }
        } catch (IOException e) {
            logger.info("API: Exception getting thermostat data: {}", e.getMessage());
        }
        return EMPTY_THERMOSTAT_LIST;
    }

    public boolean performThermostatFunction(FunctionRequest request) {
        logger.debug("API: Perform function on thermostat '{}'", request.selection.selectionMatch);
        if (isAccessTokenInvalid()) {
            return false;
        }
        String requestJson = GSON.toJson(request, FunctionRequest.class);
        logger.debug("API: Function request json is '{}'", requestJson);
        return executePost(API_THERMOSTAT_URL, requestJson);
    }

    public boolean performThermostatUpdate(ThermostatUpdateRequestDTO request) {
        logger.debug("API: Perform update on thermostat '{}'", request.selection.selectionMatch);
        if (isAccessTokenInvalid()) {
            return false;
        }
        String requestJson = GSON.toJson(request, ThermostatUpdateRequestDTO.class);
        logger.debug("API: Update request json is '{}'", requestJson);
        return executePost(API_THERMOSTAT_UPDATE_URL, requestJson);
    }

    private boolean isAccessTokenInvalid() {
        if (oAuthCredentials.noAccessToken()) {
            if (!oAuthCredentials.refreshTokens()) {
                logger.warn("API: Skipping thermostat API call due to missing or invalid credentials");
                return true;
            }
        }
        return false;
    }

    private String buildQueryUrl(String baseUrl, String requestJson) throws UnsupportedEncodingException {
        final StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?json=");
        urlBuilder.append(URLEncoder.encode(requestJson, StandardCharsets.UTF_8.toString()));
        return urlBuilder.toString();
    }

    private String executeGet(String url) throws IOException {
        long startTime = System.currentTimeMillis();
        String response = HttpUtil.executeUrl("GET", url, setHeaders(), null, null, apiTimeout);
        logger.trace("API: GET of url took {} msec", System.currentTimeMillis() - startTime);
        return response;
    }

    private boolean executePost(String url, String requestJson) {
        try {
            long startTime = System.currentTimeMillis();
            String response = HttpUtil.executeUrl("POST", url, setHeaders(),
                    new ByteArrayInputStream(requestJson.getBytes()), "application/json", apiTimeout);
            logger.trace("API: POST of url took {} msec", System.currentTimeMillis() - startTime);
            logger.trace("API: Thermostat response: {}", response);
            ThermostatResponseDTO thermostatsResponse = GSON.fromJson(response, ThermostatResponseDTO.class);
            return isSuccess(thermostatsResponse);
        } catch (IOException e) {
            logger.info("API: Exception getting thermostat data: {}", e.getMessage());
        }
        return false;
    }

    private boolean isSuccess(@Nullable AbstractResponseDTO response) {
        if (response == null) {
            logger.info("API: Ecobee API returned null response");
            return false;
        } else if (response.status.code.intValue() != 0) {
            logger.info("API: Ecobee API returned unsuccessful status: code={}, message={}", response.status.code,
                    response.status.message);
            return false;
        }
        return true;
    }

    private Properties setHeaders() {
        Properties headers = new Properties();
        headers.putAll(HTTP_HEADERS);
        headers.put("Authorization", "Bearer " + oAuthCredentials.getAccessToken());
        return headers;
    }

    private void markBridgeOnline() {
        bridgeHandler.markOnline();
    }

    /**
     * The {@link OAuthCredentials} internal class holds the credentials
     * necessary for the OAuth2 flow to work. It also provides basic methods
     * to refresh the tokens.
     *
     * @formatter:off
     *
     * OAuth States
    *
     * authToken    refreshToken    accessToken    State
     * ---------    ------------    -----------    -----
     * null                                        authorize
     * non-null     null                           request tokens
     * non-null     non-null        null           refresh tokens
     * non-null     non-null        non-null       if expired, refresh if any error, authorize
     *
     * @formatter:on
     *
     * @author John Cocula - Initial contribution
     * @author Mark Hilbush - Adapt for OH2/3
     */
    @NonNullByDefault
    private class OAuthCredentials {
        private static final String PROP_API_KEY = "oauthApiKey";
        private static final String PROP_PIN = "oauthPin";
        private static final String PROP_CODE = "oauthCode";
        private static final String PROP_REFRESH_TOKEN = "oauthRefreshToken";
        private static final String PROP_ACCESS_TOKEN = "oauthAccessToken";
        private static final String PROP_ACCESS_TOKEN_EXPIRES = "oauthAccessTokenExpires";
        private static final String SCOPE = "smartWrite";

        private final Logger logger = LoggerFactory.getLogger(OAuthCredentials.class);

        /**
         * The private API key needed in order to interact with the Ecobee API.
         */
        private final String apiKey;

        /**
         * The authorization code needed to request the refresh and access
         * tokens. Obtained and persisted when {@code authorize()} is called.
         */
        private @Nullable String code;

        private @Nullable String pin;

        /**
         * The refresh token to access the Ecobee API. Initial token is received
         * using the <code>authToken</code>, periodically refreshed using the
         * previous refreshToken, and saved in persistent storage so it can be used
         * across activations.
         */
        private @Nullable String refreshToken;

        /**
         * The access token to access the Ecobee API. Automatically renewed from the API
         * using the refresh token and persisted for use across activations.
         */
        private @Nullable String accessToken;

        /**
         * The time when the access token will expire, requiring a refresh, and
         * persisted for use across activations.
         */
        private @Nullable String accessTokenExpires;

        public OAuthCredentials(final String apiKey) {
            this.apiKey = apiKey;
            loadOAuthProperties();
        }

        public @Nullable String getAccessToken() {
            if (accessToken != null) {
                markBridgeOnline();
            }
            return accessToken;
        }

        public boolean noAccessToken() {
            long expirationTimeSeconds;
            try {
                expirationTimeSeconds = Long.parseLong(accessTokenExpires);
            } catch (NumberFormatException e) {
                expirationTimeSeconds = 0;
            }
            boolean expired = ((expirationTimeSeconds - 300) - (System.currentTimeMillis() / 1000)) < 0;
            if (expired) {
                logger.debug("OAuth: Access token is about to or has expired!");
            }
            return accessToken == null || expired;
        }

        @SuppressWarnings({ "unused", "null" })
        public void authorize() {
            try {
                StringBuilder url = new StringBuilder(API_AUTHORIZE_URL);
                url.append("?response_type=ecobeePin");
                url.append("&client_id=").append(apiKey);
                url.append("&scope=").append(SCOPE);
                String response = HttpUtil.executeUrl("GET", url.toString(), HTTP_HEADERS, null, null, apiTimeout);
                logger.trace("OAuth: Auth response: {}", response);
                AuthorizeResponseDTO authResponse = GSON.fromJson(response, AuthorizeResponseDTO.class);
                if (authResponse != null) {
                    code = authResponse.code;
                    pin = authResponse.pin;
                    refreshToken = null;
                    accessToken = null;
                    saveOAuthProperties();
                    writeLogMessage(authResponse.pin, authResponse.expiresIn);
                } else {
                    logger.info("OAuth: Unable to parse authorization response");
                }
            } catch (JsonSyntaxException | IOException e) {
                logger.info("OAuth: Got exception during authorization: {}", e.getMessage());
            }
        }

        private void writeLogMessage(String pin, Integer expiresIn) {
            logger.info("#################################################################");
            logger.info("# Ecobee: U S E R   I N T E R A C T I O N   R E Q U I R E D !!");
            logger.info("# Go to the Ecobee web portal, then:");
            logger.info("# Enter PIN '{}' in My Apps within {} minutes.", pin, expiresIn);
            logger.info("# NOTE: All API attempts will fail in the meantime.");
            logger.info("#################################################################");
        }

        /**
         * This method attempts to advance the authorization process by retrieving the tokens needed to use the API. It
         * returns <code>true</code> if there is reason to believe that an immediately subsequent API call would
         * succeed.
         * <p>
         * This method requests access and refresh tokens to use the Ecobee API. If there is a <code>refreshToken</code>
         * , it will be used to obtain the tokens, but if there is only an <code>authToken</code>, that will be used
         * instead.
         *
         * @return <code>true</code> if there is reason to believe that an immediately subsequent API call would
         *         succeed.
         */
        @SuppressWarnings({ "unused", "null" })
        public boolean refreshTokens() {
            if (code == null) {
                authorize();
                return false;
            }
            try {
                StringBuilder url = new StringBuilder(API_TOKEN_URL);
                boolean initialTokenRequest;
                if (refreshToken == null) {
                    url.append("?grant_type=ecobeePin");
                    url.append("&code=").append(code);
                    initialTokenRequest = true;
                } else {
                    url.append("?grant_type=refresh_token");
                    url.append("&code=").append(refreshToken);
                    initialTokenRequest = false;
                }
                url.append("&client_id=").append(apiKey);

                String response = HttpUtil.executeUrl("POST", url.toString(), HTTP_HEADERS, null, null, apiTimeout);
                final TokenResponseDTO tokenResponse = GSON.fromJson(response, TokenResponseDTO.class);
                if (tokenResponse == null) {
                    logger.debug("OAuth: Got empty auth response from Ecobee api");
                    return false;
                }
                logger.trace("OAuth: Auth response: {}", response);
                if (!StringUtils.isEmpty(tokenResponse.error)) {
                    logger.warn("OAuth: Error refreshing tokens: {}:{}", tokenResponse.error,
                            tokenResponse.errorDescription);
                    if ("authorization_expired".equals(tokenResponse.error)) {
                        logger.warn("OAuth: Ecobee authorization has expired!!");
                        refreshToken = null;
                        accessToken = null;
                        if (initialTokenRequest) {
                            code = null;
                        }
                        saveOAuthProperties();
                    }
                    return false;
                } else {
                    refreshToken = tokenResponse.refreshToken;
                    accessToken = tokenResponse.accessToken;
                    accessTokenExpires = calculateExpiration(tokenResponse.expiresIn);
                    logger.debug("OAuth: Tokens successfully refreshed");
                    saveOAuthProperties();
                    return true;
                }
            } catch (IOException e) {
                logger.info("OAuth: Got exception: {}", e.getMessage());
                return false;
            }
        }

        private String calculateExpiration(Integer expiresIn) {
            return String.valueOf((System.currentTimeMillis() / 1000) + expiresIn.longValue());
        }

        /*
         * Only load the tokens if they were not saved with the app key used to create them (backwards
         * compatibility), or if the saved app key matches the current app key specified in openhab.cfg. This
         * properly ignores saved tokens when the app key has been changed.
         */
        @SuppressWarnings({ "null" })
        private void loadOAuthProperties() {
            Map<String, String> properties = readProperties();
            final String savedAppKey = properties.get(PROP_API_KEY);
            if (savedAppKey == null || savedAppKey.equals(this.apiKey)) {
                pin = properties.get(PROP_PIN);
                code = properties.get(PROP_CODE);
                refreshToken = properties.get(PROP_REFRESH_TOKEN);
                accessToken = properties.get(PROP_ACCESS_TOKEN);
                accessTokenExpires = properties.get(PROP_ACCESS_TOKEN_EXPIRES);
                logger.trace("OAuth: refreshToken={}, accessToken={}, accessTokenExpires={}", refreshToken, accessToken,
                        accessTokenExpires);
            }
        }

        private void saveOAuthProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(PROP_API_KEY, this.apiKey);
            putOrRemove(properties, PROP_PIN, pin);
            putOrRemove(properties, PROP_CODE, code);
            putOrRemove(properties, PROP_REFRESH_TOKEN, refreshToken);
            putOrRemove(properties, PROP_ACCESS_TOKEN, accessToken);
            putOrRemove(properties, PROP_ACCESS_TOKEN_EXPIRES, accessTokenExpires);
            writeProperties(properties);
        }

        private void putOrRemove(Map<String, String> properties, String property, @Nullable String value) {
            if (value != null) {
                properties.put(property, value);
            } else {
                properties.remove(property);
            }
        }

        @SuppressWarnings("unchecked")
        private Map<String, String> readProperties() {
            logger.debug("OAuth: Reading properties from {}", getEcobeeCacheFileName());
            Map<String, String> properties = new HashMap<>();
            try (FileReader reader = new FileReader(getEcobeeCacheFileName())) {
                properties = GSON.fromJson(reader, properties.getClass());
                logger.trace("OAuth: Read properties: {}", GSON.toJson(properties));
            } catch (FileNotFoundException e) {
                // File doesn't exist
            } catch (IOException e) {
                logger.warn("OAuth: IOException reading credentials file", e);
            } catch (JsonSyntaxException e) {
                logger.warn("OAuth: Credentials file not properly formatted");
            }
            return properties;
        }

        private void writeProperties(Map<String, String> properties) {
            logger.debug("OAuth: Writing properties to {}", getEcobeeCacheFileName());
            File file = new File(getEcobeeCacheFileName());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(properties, writer);
                logger.trace("OAuth: Wrote properties: {}", GSON.toJson(properties));
            } catch (IOException e) {
                logger.warn("OAuth: IOException writing credentials file", e);
            }
        }

        private String getEcobeeCacheFileName() {
            return (ConfigConstants.getUserDataFolder() + File.separator + "ecobee" + File.separator
                    + "ecobee-oauth.json");
        }
    }
}
