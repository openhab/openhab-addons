/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.pleos.internal.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenRequest;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenResponse;
import org.openhab.binding.pleos.internal.api.model.PleosAuthModels.TokenResponse.TokenData;
import org.openhab.binding.pleos.internal.api.model.PleosErrorResponse;
import org.openhab.binding.pleos.internal.api.model.PleosErrorResponse.ErrorBody;
import org.openhab.binding.pleos.internal.api.model.PleosErrorResponse.ErrorDetail;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse;
import org.openhab.binding.pleos.internal.model.Brand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Client for the Pleos API. Currently only available for European Car Owners.
 * Supports Hyundai / Kia and Genesis vehicles.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosApiClient {

    private final Logger logger = LoggerFactory.getLogger(PleosApiClient.class);

    private static final String PLEOS_API_BASE = "https://api.pleos.ai";
    private static final String PLEOS_AUTH_URL = "https://api.pleos.ai/v1/auth/personal-token";
    private static final int TIMEOUT_SECONDS = 10;

    // 55-minute fallback validity (3300 sec.) if Pleos does not provide an `expiresIn` value
    private static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 3300;
    // 5-minute safety buffer before the actual process begins
    private static final long EXPIRY_SAFETY_BUFFER_SECONDS = 300;

    // Error codes for which a new token resolves the issue
    private static final Set<String> RETRYABLE_TOKEN_CODES = Set.of("40003", "40004", "4011");

    // Error codes indicating incorrect access credentials or missing permissions
    private static final Set<String> FATAL_AUTH_CODES = Set.of("401", "403", "40001", "40404", "5006");

    private final HttpClient httpClient;
    private final Gson gson;

    private final String clientId;
    private final String clientSecret;
    private final Brand brand;

    private volatile @Nullable String accessToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    public PleosApiClient(HttpClient httpClient, String clientId, String clientSecret, Brand brand) {
        this.httpClient = httpClient;
        this.gson = new Gson();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.brand = brand;
    }

    private boolean isTokenExpired() {
        return accessToken == null || Instant.now().isAfter(tokenExpiry);
    }

    /**
     * Requests a new access token and stores it internally.
     */
    public synchronized void authenticate() throws PleosApiException {
        logger.debug("Requesting a new Pleos access token for brand '{}'...", brand);

        TokenRequest payload = new TokenRequest(clientId, clientSecret);
        String jsonPayload = gson.toJson(payload);

        Request request = httpClient.newRequest(PLEOS_AUTH_URL).method(HttpMethod.POST)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).header(HttpHeader.CONTENT_TYPE, "application/json")
                .header("Brand", brand.name().toLowerCase(Locale.ROOT))
                .content(new StringContentProvider(jsonPayload, StandardCharsets.UTF_8));

        try {
            ContentResponse response = request.send();
            int status = response.getStatus();
            String body = response.getContentAsString();

            if (status == HttpStatus.OK_200) {
                TokenResponse tokenResponse = gson.fromJson(body, TokenResponse.class);
                TokenData data = tokenResponse != null ? tokenResponse.data() : null;

                if (data != null && data.accessToken() != null) {
                    this.accessToken = data.accessToken();

                    // Dynamic calculation with a 5-minute buffer or a 55-minute fallback
                    Long expiresIn = data.expiresIn();
                    long validSeconds = (expiresIn != null && expiresIn > EXPIRY_SAFETY_BUFFER_SECONDS)
                            ? (expiresIn - EXPIRY_SAFETY_BUFFER_SECONDS)
                            : DEFAULT_TOKEN_LIFETIME_SECONDS;

                    this.tokenExpiry = Instant.now().plusSeconds(validSeconds);
                    logger.debug("Pleos access token successfully renewed (valid for {}s until {}).", validSeconds,
                            this.tokenExpiry);
                    return;
                }
                throw new PleosApiException("Token not found in the JSON response.");
            }

            throw buildExceptionFromErrorResponse(status, body, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PleosApiException("Token retrieval interrupted", e);
        } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new PleosApiException("Communication or parsing error during token retrieval: " + e.getMessage(), e);
        }
    }

    /**
     * CORE METHOD: Sends a GET request, refreshes the token if necessary,
     * and converts the JSON directly into the desired target record (<T>).
     */
    public <T> T sendGetRequest(String urlPath, Class<T> responseType) throws PleosApiException {
        return sendGetRequestInternal(urlPath, responseType, false);
    }

    private <T> T sendGetRequestInternal(String urlPath, Class<T> responseType, boolean isRetry)
            throws PleosApiException {
        if (isTokenExpired()) {
            authenticate();
        }

        Request request = httpClient.newRequest(PLEOS_API_BASE + urlPath).method(HttpMethod.GET)
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .header(HttpHeader.AUTHORIZATION, "Bearer " + this.accessToken)
                .header(HttpHeader.CONTENT_TYPE, "application/json")
                .header("Brand", brand.name().toLowerCase(Locale.ROOT));

        try {
            ContentResponse response = request.send();
            int status = response.getStatus();
            String body = response.getContentAsString();

            // Return API Result when HttpStatus.OK_200
            if (status == HttpStatus.OK_200) {
                @Nullable
                T result = gson.fromJson(body, responseType);
                if (result == null) {
                    throw new PleosApiException("Empty JSON response from " + urlPath);
                }
                return result;
            }

            // Build Exception From Error Response
            PleosApiException apiEx = buildExceptionFromErrorResponse(status, body, false);
            String errorCode = apiEx.getErrorCode();

            // Retryable Token Error try to get access token again
            if (!isRetry && errorCode != null && RETRYABLE_TOKEN_CODES.contains(errorCode)) {
                logger.debug("Token is invalid/expired according to the API (code {}). Renewing token...", errorCode);
                authenticate();
                return sendGetRequestInternal(urlPath, responseType, true);
            }

            // Else trow error reported from PLeosAPI
            throw apiEx;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PleosApiException("API request interrupted", e);
        } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new PleosApiException("Error during API request to " + urlPath + ": " + e.getMessage(), e);
        }
    }

    PleosApiException buildExceptionFromErrorResponse(int httpStatus, String body, boolean isLoginCall) {
        try {
            PleosErrorResponse errResp = gson.fromJson(body, PleosErrorResponse.class);
            ErrorBody err = errResp != null ? errResp.error() : null;
            if (err != null) {
                String code = err.code() != null ? err.code() : String.valueOf(httpStatus);
                String msg = err.message() != null ? err.message() : "Unknown API error";

                String detailText = "";
                List<ErrorDetail> details = err.details();
                if (details != null && !details.isEmpty()) {
                    detailText = " ["
                            + details.stream()
                                    .map(d -> (d.field() != null ? d.field() : "") + ": "
                                            + (d.issue() != null ? d.issue() : ""))
                                    .collect(Collectors.joining(", "))
                            + "]";
                }

                boolean isAuthFatal = isLoginCall || FATAL_AUTH_CODES.contains(code);
                return new PleosApiException("Pleos Error " + code + " (" + msg + ")" + detailText, code, isAuthFatal);
            }
        } catch (JsonSyntaxException ignored) {
            // Fallback in case no JSON was provided
        }
        return new PleosApiException("HTTP Error " + httpStatus + ": " + body, String.valueOf(httpStatus), isLoginCall);
    }

    // --- PLEOS API FUNCTIONAL ENDPOINTS ---

    public VehiclesResponse getVehicles() throws PleosApiException {
        return sendGetRequest("/v1/vehicles/consent", VehiclesResponse.class);
    }

    public BatteryResponse getBatteryStatus(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/batteries", BatteryResponse.class);
    }

    public PowertrainResponse getPowertrainStatus(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/powertrains", PowertrainResponse.class);
    }

    public LocationResponse getLocationInfo(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/locations", LocationResponse.class);
    }

    public DrivingResponse getOperationStatus(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/driving", DrivingResponse.class);
    }

    public VehicleStatusResponse getVehicleStatus(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/status", VehicleStatusResponse.class);
    }

    public FreezerResponse getFreezerStatus(String vin) throws PleosApiException {
        return sendGetRequest("/v1/vehicles/" + vin + "/freezer", FreezerResponse.class);
    }
}
