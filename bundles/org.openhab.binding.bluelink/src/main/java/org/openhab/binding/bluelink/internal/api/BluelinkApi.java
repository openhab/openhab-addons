/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.CLIENT_ID;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.CLIENT_SECRET;
import static org.openhab.binding.bluelink.internal.dto.ChargeLimitsRequest.PLUG_TYPE_AC;
import static org.openhab.binding.bluelink.internal.dto.ChargeLimitsRequest.PLUG_TYPE_DC;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bluelink.internal.dto.ChargeLimitsRequest;
import org.openhab.binding.bluelink.internal.dto.ClimateRequestEv;
import org.openhab.binding.bluelink.internal.dto.ClimateRequestIce;
import org.openhab.binding.bluelink.internal.dto.DoorCommandRequest;
import org.openhab.binding.bluelink.internal.dto.EnrollmentResponse;
import org.openhab.binding.bluelink.internal.dto.EnrollmentResponse.EnrolledVehicle;
import org.openhab.binding.bluelink.internal.dto.LoginRequest;
import org.openhab.binding.bluelink.internal.dto.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.dto.VehicleStatus;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * HTTP client for the Bluelink API.
 * <p>
 * Implementation based on [hyundai_kia_connect_api](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api)
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApi {

    private static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    private static final int HTTP_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(BluelinkApi.class);
    private final Gson gson = new GsonBuilder().create();
    private final HttpClient httpClient;
    private final String baseUrl;
    private final TimeZoneProvider timeZoneProvider;
    private final String username;
    private final String password;
    private final @Nullable String pin;

    private @Nullable TokenResponse token;
    private @Nullable Instant tokenExpiry;

    public BluelinkApi(final HttpClient httpClient, final String baseUrl, final TimeZoneProvider timeZoneProvider,
            final String username, final String password, final @Nullable String pin) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.timeZoneProvider = timeZoneProvider;
        this.username = username;
        this.password = password;
        this.pin = pin;
    }

    public boolean login() throws BluelinkApiException {
        final LoginRequest loginRequest = new LoginRequest(username, password);

        try {
            final String loginUrl = baseUrl + "/v2/ac/oauth/token";
            final Request request = httpClient.newRequest(loginUrl).method(HttpMethod.POST)
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .content(new StringContentProvider(gson.toJson(loginRequest)), APPLICATION_JSON);
            addStandardHeaders(request);

            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Login failed with status {}: {}", response.getStatus(), response.getContentAsString());
                final String msg = "Login failed: " + response.getStatus();
                if (isRetryableStatus(response.getStatus())) {
                    throw new RetryableRequestException(msg);
                } else {
                    throw new BluelinkApiException(msg);
                }
            }

            final TokenResponse tokenResponse = gson.fromJson(response.getContentAsString(), TokenResponse.class);
            if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.expiresIn() == null) {
                throw new BluelinkApiException("Invalid token response");
            }
            token = tokenResponse;
            tokenExpiry = Instant.now().plusSeconds(Integer.parseInt(tokenResponse.expiresIn()) - 60);
            logger.debug("Login successful, token valid until {}", tokenExpiry);
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Login interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Login failed", e);
        }
    }

    private boolean isAuthenticated() {
        final TokenResponse t = token;
        final Instant expiry = tokenExpiry;
        return t != null && expiry != null && Instant.now().isBefore(expiry);
    }

    /**
     * Ensure we have a valid token, refreshing if necessary.
     */
    private void ensureAuthenticated() throws BluelinkApiException {
        if (!isAuthenticated()) {
            login();
        }
    }

    /**
     * Get list of enrolled vehicles.
     */
    public List<VehicleInfo> getVehicles() throws BluelinkApiException {
        ensureAuthenticated();
        // One would expect the username (email address) to be URL-encoded, but
        // the API does not accept encoding the @ character so we omit encoding.
        final String url = baseUrl + "/ac/v2/enrollment/details/" + username;

        try {
            final Request request = httpClient.newRequest(url).timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            addStandardHeaders(request);
            addAuthHeaders(request);
            final ContentResponse response = checkStatus(request.send(), "get vehicles");
            final EnrollmentResponse enrollment = gson.fromJson(response.getContentAsString(),
                    EnrollmentResponse.class);
            if (enrollment == null) {
                return List.of();
            }
            final List<EnrolledVehicle> vehicles = enrollment.enrolledVehicleDetails();
            if (vehicles == null) {
                return List.of();
            }
            return vehicles.stream().map(EnrolledVehicle::vehicleDetails).filter(Objects::nonNull).toList();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Get vehicles interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Failed to get vehicles", e);
        }
    }

    public @Nullable VehicleStatus getVehicleStatus(final VehicleInfo vehicle, final boolean forceRefresh)
            throws BluelinkApiException {
        ensureAuthenticated();

        final String url = baseUrl + "/ac/v2/rcs/rvs/vehicleStatus";

        try {
            final Request request = httpClient.newRequest(url).timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .header("refresh", String.valueOf(forceRefresh));
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);
            final ContentResponse response = checkStatus(request.send(), "get vehicle status");
            return gson.fromJson(response.getContentAsString(), VehicleStatus.class);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Get vehicle status interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Failed to get vehicle status", e);
        }
    }

    public boolean lockVehicle(final VehicleInfo vehicle) throws BluelinkApiException {
        return sendDoorCommand(vehicle, "/ac/v2/rcs/rdo/off");
    }

    public boolean unlockVehicle(final VehicleInfo vehicle) throws BluelinkApiException {
        return sendDoorCommand(vehicle, "/ac/v2/rcs/rdo/on");
    }

    private boolean sendDoorCommand(final VehicleInfo vehicle, final String endpoint) throws BluelinkApiException {
        ensureAuthenticated();

        final String vin = vehicle.vin();
        if (vin == null) {
            throw new BluelinkApiException("VIN not available");
        }
        final String url = baseUrl + endpoint;
        final DoorCommandRequest doorRequest = new DoorCommandRequest(username, vin);

        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(HTTP_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);
            request.header("APPCLOUD-VIN", vin);
            request.content(new StringContentProvider(gson.toJson(doorRequest)), APPLICATION_JSON);
            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Door command failed: {} - {}", response.getStatus(), response.getContentAsString());
                return false;
            }

            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Door command interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Door command failed", e);
        }
    }

    public boolean climateStart(final VehicleInfo vehicle, final QuantityType<Temperature> temperature,
            final boolean heat, final boolean defrost) throws BluelinkApiException {
        ensureAuthenticated();
        final String url;
        final Object request;
        if (vehicle.isElectric()) {
            url = baseUrl + "/ac/v2/evc/fatc/start";
            request = ClimateRequestEv.create(temperature, heat, defrost);
        } else {
            if (vehicle.vin() == null) {
                throw new BluelinkApiException("VIN not available");
            }
            url = baseUrl + "/ac/v2/rcs/rsc/start";
            request = ClimateRequestIce.create(temperature, heat, defrost, username, vehicle.vin());
        }
        return sendClimateCommand(vehicle, url, request);
    }

    public boolean climateStop(final VehicleInfo vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, vehicle.isElectric() ? "/ac/v2/evc/fatc/stop" : "/ac/v2/rcs/rsc/stop");
    }

    private boolean sendClimateCommand(final VehicleInfo vehicle, final String url, final Object climateRequest)
            throws BluelinkApiException {
        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(HTTP_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);
            request.content(new StringContentProvider(gson.toJson(climateRequest)), APPLICATION_JSON);

            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Climate command failed: {} - {}", response.getStatus(), response.getContentAsString());
                return false;
            }

            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Climate command interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Climate command failed", e);
        }
    }

    public boolean startCharging(final VehicleInfo vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, "/ac/v2/evc/charge/start");
    }

    public boolean stopCharging(final VehicleInfo vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, "/ac/v2/evc/charge/stop");
    }

    public boolean setChargeLimitDC(final VehicleInfo vehicle, final int limit) throws BluelinkApiException {
        return setChargeLimit(vehicle, PLUG_TYPE_DC, limit);
    }

    public boolean setChargeLimitAC(final VehicleInfo vehicle, final int limit) throws BluelinkApiException {
        return setChargeLimit(vehicle, PLUG_TYPE_AC, limit);
    }

    private boolean setChargeLimit(final VehicleInfo vehicle, final int plugType, final int limit)
            throws BluelinkApiException {
        ensureAuthenticated();

        final String url = baseUrl + "/ac/v2/evc/charge/targetsoc/set";
        final ChargeLimitsRequest chargeLimitsRequest = new ChargeLimitsRequest(
                List.of(new ChargeLimitsRequest.TargetSOC(plugType, limit)));

        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .content(new StringContentProvider(gson.toJson(chargeLimitsRequest)), APPLICATION_JSON);
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);

            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Set charge limits failed: {} - {}", response.getStatus(), response.getContentAsString());
                return false;
            }

            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Set charge limits interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Set charge limits failed", e);
        }
    }

    private boolean sendSimplePostCommand(final VehicleInfo vehicle, final String endpoint)
            throws BluelinkApiException {
        final String url = baseUrl + endpoint;

        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .content(new StringContentProvider("{}"), APPLICATION_JSON);
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);

            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Command {} failed: {} - {}", endpoint, response.getStatus(),
                        response.getContentAsString());
                return false;
            }

            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Command interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Command failed", e);
        }
    }

    private void addStandardHeaders(final Request request) {
        request.header(HttpHeader.CONTENT_TYPE, APPLICATION_JSON)
                .header(HttpHeader.ACCEPT, "application/json, text/plain, */*")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate, br")
                .header(HttpHeader.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("from", "SPA").header("to", "ISS").header("language", "0").header("client_id", CLIENT_ID)
                .header("clientSecret", CLIENT_SECRET).header("brandIndicator", "H")
                .header("offset", String.valueOf(getTimeZoneOffset()));
    }

    private void addAuthHeaders(final Request request) {
        final TokenResponse t = token;
        if (t != null && t.accessToken() != null) {
            request.header("accessToken", t.accessToken());
        }
        request.header("username", username).header("blueLinkServicePin", pin);
    }

    private void addVehicleHeaders(final Request request, final VehicleInfo vehicle) {
        final String regId = vehicle.registrationId();
        final String gen = vehicle.vehicleGeneration();
        final String vin = vehicle.vin();

        if (regId != null) {
            request.header("registrationId", regId);
        }
        if (gen != null) {
            request.header("gen", gen);
        }
        if (vin != null) {
            request.header("vin", vin);
        }
    }

    private long getTimeZoneOffset() {
        return Duration.ofSeconds(timeZoneProvider.getTimeZone().getRules().getOffset(Instant.now()).getTotalSeconds())
                .toHours();
    }

    private ContentResponse checkStatus(final ContentResponse response, final String op) throws BluelinkApiException {
        if (response.getStatus() != HttpStatus.OK_200) {
            logger.debug("operation failed ({}): {} - {}", op, response.getStatus(), response.getContentAsString());
            final String msg = "operation failed: %s - %d".formatted(op, response.getStatus());
            if (isRetryableStatus(response.getStatus())) {
                throw new RetryableRequestException(msg);
            } else {
                throw new BluelinkApiException(msg);
            }
        }
        return response;
    }

    private static boolean isRetryableStatus(final int status) {
        return status >= 500 && status < 600;
    }
}
