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
package org.openhab.binding.bluelink.internal.api;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bluelink.internal.dto.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.VehicleStatusMapper;
import org.openhab.binding.bluelink.internal.dto.eu.BaseResponse;
import org.openhab.binding.bluelink.internal.dto.eu.RegistrationRequest;
import org.openhab.binding.bluelink.internal.dto.eu.RegistrationResponse;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleInfoEU;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleStatusEU;
import org.openhab.binding.bluelink.internal.dto.eu.VehiclesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * EU implementation of the {@link BluelinkApi}.
 * <p>
 * Implementation based on
 * <a href="https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api">hyundai_kia_connect_api</a> and
 * <a href="https://github.com/Hacksore/bluelinky">bluelinky</a>.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiEU implements BluelinkApi {
    private static final String HTTP_USER_AGENT = "okhttp/3.12.0";
    private static final int HTTP_TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(BluelinkApiEU.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final BrandConfig brandConfig;
    private final String refreshToken;

    private @Nullable TokenResponse token;
    private @Nullable Instant tokenExpiry;
    private @Nullable UUID deviceId;

    public enum Brand {
        HYUNDAI,
        KIA,
        GENESIS
    }

    public BluelinkApiEU(final HttpClient httpClient, final Map<String, String> properties, final Brand brand,
            final String refreshToken) {
        this.httpClient = httpClient;
        String deviceId = properties.get("deviceId");
        if (deviceId != null && !deviceId.isBlank()) {
            this.deviceId = UUID.fromString(deviceId);
        }
        this.brandConfig = BrandConfig.forBrand(brand);
        this.refreshToken = refreshToken;
    }

    /**
     * Constructor for unit test use only.
     */
    public BluelinkApiEU(final HttpClient httpClient, final BrandConfig brandConfig, final String refreshToken) {
        this.httpClient = httpClient;
        this.brandConfig = brandConfig;
        this.refreshToken = refreshToken;
    }

    @Override
    public Map<String, String> getProperties() {
        UUID deviceId = this.deviceId;
        if (deviceId != null) {
            return Map.of("deviceId", deviceId.toString());
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean login() throws BluelinkApiException {
        authenticate();
        UUID deviceId = this.deviceId;
        if (deviceId == null) {
            registerDevice();
        } else {
            logger.debug("Device already registered, device registrationId: {}", deviceId);
        }
        return true;
    }

    /**
     * Authenticate to Bluelink API EU using refresh_token and get an access_token.
     * 
     * @throws BluelinkApiException
     */
    private void authenticate() throws BluelinkApiException {
        String url = brandConfig.loginBaseUrl + "/auth/api/v2/user/oauth2/token";
        String formBody = "grant_type=refresh_token" + "&refresh_token=" + this.refreshToken + "&client_id="
                + brandConfig.ccspServiceId + "&client_secret=" + brandConfig.clientSecret;

        Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded").agent(HTTP_USER_AGENT)
                .content(new StringContentProvider(formBody));

        try {
            ContentResponse response = request.send();

            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Login failed with status {}: {}", response.getStatus(), response.getContentAsString());
                if (response.getStatus() == HttpStatus.BAD_REQUEST_400) {
                    throw new BluelinkApiException("Login failed: Invalid refresh token");
                }
                throw new BluelinkApiException("Login failed with status " + response.getStatus());
            }

            TokenResponse tokenResponse = gson.fromJson(response.getContentAsString(), TokenResponse.class);
            if (tokenResponse == null || tokenResponse.tokenType() == null || tokenResponse.accessToken() == null) {
                throw new BluelinkApiException("Invalid token response");
            }
            this.token = tokenResponse;
            tokenExpiry = Instant.now().plusSeconds(Integer.parseInt(tokenResponse.expiresIn()) - 60);
            logger.debug("Login successful, token valid until {}", tokenExpiry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Login interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Login failed", e);
        }
    }

    /**
     * Whether access_token is valid (i.e. not expired).
     * 
     * @return access_token is valid
     */
    private boolean isAuthenticated() {
        final TokenResponse t = token;
        final Instant expiry = tokenExpiry;
        return t != null && expiry != null && Instant.now().isBefore(expiry);
    }

    /**
     * Ensure access_token is valid, refresh if expired.
     * 
     * @throws BluelinkApiException
     */
    private void ensureAuthenticated() throws BluelinkApiException {
        if (!isAuthenticated()) {
            login();
        }
    }

    /**
     * Register using a random UUID to get a valid <code>deviceId</code> required for API calls.
     * 
     * @throws BluelinkApiException
     */
    private void registerDevice() throws BluelinkApiException {
        ensureAuthenticated();
        String url = brandConfig.apiBaseUrl + "/api/v1/spa/notifications/register";

        RegistrationRequest payload = new RegistrationRequest(
                // ThreadLocalRandom is good enough, we don't need cryptographically secure randomness
                String.format("%064x", ThreadLocalRandom.current().nextLong()), brandConfig.pushType,
                UUID.randomUUID());

        Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(payload)), "application/json;charset=UTF-8");
        addStandardHeaders(request);
        addAuthHeaders(request);

        ContentResponse response;
        try {
            response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Device registration failed with status {}: {}", response.getStatus(),
                        response.getContentAsString());
                throw new BluelinkApiException("Device registration failed with status " + response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Device registration interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Device registration failed", e);
        }

        Type type = new TypeToken<BaseResponse<RegistrationResponse>>() {
        }.getType();
        BaseResponse<RegistrationResponse> registration = gson.fromJson(response.getContentAsString(), type);
        if (registration != null) {
            this.deviceId = registration.resultMessage().deviceId();
        }
        logger.debug("Device registration successful, deviceId '{}'", deviceId);
    }

    @Override
    public List<Vehicle> getVehicles() throws BluelinkApiException {
        ensureAuthenticated();
        String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles";

        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(HTTP_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);

        ContentResponse response = doRequest(request);

        Type type = new TypeToken<BaseResponse<VehiclesResponse>>() {
        }.getType();
        BaseResponse<VehiclesResponse> vehicles = gson.fromJson(response.getContentAsString(), type);
        if (vehicles == null) {
            return Collections.emptyList();
        }

        return vehicles.resultMessage().vehicles().stream().map(VehicleInfoEU::mapToVehicle).toList();
    }

    @Override
    public @Nullable VehicleStatus getVehicleStatus(Vehicle vehicle, boolean forceRefresh) throws BluelinkApiException {
        ensureAuthenticated();

        String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicle.registrationId();

        if (forceRefresh) {
            url += "/status";
        } else {
            url += vehicle.ccs2ProtocolSupport() ? "/ccs2/carstatus/latest" : "/status/latest";
        }

        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(HTTP_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);

        ContentResponse response = doRequest(request);

        logger.debug("Vehicle status request successful for vehicle {}: {}", vehicle.getDisplayName(),
                response.getContentAsString());

        if (vehicle.ccs2ProtocolSupport()) {
            throw new BluelinkApiException(
                    "CCU CCS protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        }

        if (forceRefresh) {
            Type type = new TypeToken<BaseResponse<VehicleStatusEU.VehicleStatusData>>() {
            }.getType();
            BaseResponse<VehicleStatusEU.VehicleStatusData> response1 = gson.fromJson(response.getContentAsString(),
                    type);
            if (response1 == null) {
                return null;
            }
            return VehicleStatusMapper.map(
                    new VehicleStatusEU(new VehicleStatusEU.VehicleStatusInfo(null, response1.resultMessage(), null)));
        } else {
            Type type = new TypeToken<BaseResponse<VehicleStatusEU>>() {
            }.getType();
            BaseResponse<VehicleStatusEU> response1 = gson.fromJson(response.getContentAsString(), type);
            if (response1 == null) {
                return null;
            }
            return VehicleStatusMapper.map(response1.resultMessage());
        }
    }

    private void addStandardHeaders(Request request) {
        String stamp = generateStamp();

        request.header("ccsp-service-id", brandConfig.ccspServiceId).header("ccsp-application-id", brandConfig.appId)
                .header("Stamp", stamp).header(HttpHeader.USER_AGENT, HTTP_USER_AGENT);
    }

    private void addAuthHeaders(Request request) {
        TokenResponse token = this.token;
        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION, token.tokenType() + " " + token.accessToken());
        }
        UUID deviceId = this.deviceId;
        if (deviceId != null) {
            request.header("ccsp-device-id", deviceId.toString());
        }
    }

    private ContentResponse doRequest(Request request) throws BluelinkApiException {
        try {
            ContentResponse response = request.send();
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                return response;
            } else {
                logger.debug("API request failed with status {}: {}", response.getStatus(),
                        response.getContentAsString());
                throw new BluelinkApiException("API request failed with status " + response.getStatus());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("API request interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("API request failed", e);
        }
    }

    private String generateStamp() {
        long timestamp = Instant.now().getEpochSecond();
        String rawData = brandConfig.appId + ":" + timestamp;
        byte[] rawBytes = rawData.getBytes(StandardCharsets.UTF_8);
        byte[] cfbBytes = Base64.getDecoder().decode(brandConfig.cfb);

        int length = Math.min(rawBytes.length, cfbBytes.length);
        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) {
            result[i] = (byte) (cfbBytes[i] ^ rawBytes[i]);
        }

        return Base64.getEncoder().encodeToString(result);
    }

    public static class BrandConfig {
        final String apiBaseUrl;
        final String loginBaseUrl;
        final String ccspServiceId;
        final String appId;
        final String clientSecret;
        final String cfb;
        final String pushType;

        public BrandConfig(String apiBaseUrl, String loginBaseUrl, String ccspServiceId, String appId,
                String clientSecret, String cfg, String pushType) {
            this.apiBaseUrl = apiBaseUrl;
            this.loginBaseUrl = loginBaseUrl;
            this.ccspServiceId = ccspServiceId;
            this.appId = appId;
            this.clientSecret = clientSecret;
            this.cfb = cfg;
            this.pushType = pushType;
        }

        static BrandConfig forBrand(Brand brand) {
            return switch (brand) {
                case HYUNDAI -> new BrandConfig("https://prd.eu-ccapi.hyundai.com:8080",
                        "https://idpconnect-eu.hyundai.com", "6d477c38-3ca4-4cf3-9557-2a1929a94654",
                        "014d2225-8495-4735-812d-2616334fd15d", "KUy49XxPzLpLuoK0xhBC77W6VXhmtQR9iQhmIFjjoY4IpxsV",
                        "RFtoRq/vDXJmRndoZaZQyfOot7OrIqGVFj96iY2WL3yyH5Z/pUvlUhqmCxD2t+D65SQ=", "GCM");
                case KIA -> new BrandConfig("https://prd.eu-ccapi.kia.com:8080", "https://idpconnect-eu.kia.com",
                        "fdc85c00-0a2f-4c64-bcb4-2cfb1500730a", "a2b8469b-30a3-4361-8e13-6fceea8fbe74", "secret",
                        "wLTVxwidmH8CfJYBWSnHD6E0huk0ozdiuygB4hLkM5XCgzAL1Dk5sE36d/bx5PFMbZs=", "APNS");
                case GENESIS ->
                    new BrandConfig("https://prd-eu-ccapi.genesis.com:8080", "https://idpconnect-eu.genesis.com",
                            "3020afa2-30ff-412a-aa51-d28fbe901e10", "f11f2b86-e0e7-4851-90df-5600b01d8b70", "secret",
                            "RFtoRq/vDXJmRndoZaZQyYo3/qFLtVReW8P7utRPcc0ZxOzOELm9mexvviBk/qqIp4A=", "GCM");
            };
        }
    }
}
