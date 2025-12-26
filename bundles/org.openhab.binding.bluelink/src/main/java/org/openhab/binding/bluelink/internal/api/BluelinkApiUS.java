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

import static org.openhab.binding.bluelink.internal.dto.us.bluelink.ChargeLimitsRequest.PLUG_TYPE_AC;
import static org.openhab.binding.bluelink.internal.dto.us.bluelink.ChargeLimitsRequest.PLUG_TYPE_DC;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.ChargeLimitsRequest;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.ClimateRequestEV;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.ClimateRequestICE;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.DoorCommandRequest;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.EnrollmentResponse;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.EnrollmentResponse.EnrolledVehicle;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.ErrorResponse;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.LoginRequest;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.Vehicle;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.VehicleLocation;
import org.openhab.binding.bluelink.internal.dto.us.bluelink.VehicleStatusResponse.VehicleStatus;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.binding.bluelink.internal.model.IVehicle.EngineType;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;

/**
 * Client for the Bluelink API (US).
 * <p>
 * Implementation based on [hyundai_kia_connect_api](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api)
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiUS extends AbstractBluelinkApi<Vehicle> {

    private static final String API_ENDPOINT = "https://api.telematics.hyundaiusa.com";
    private static final String CLIENT_ID = "m66129Bb-em93-SPAHYN-bZ91-am4540zp19920";
    private static final String CLIENT_SECRET = "v558o935-6nne-423i-baa8";

    private final String baseUrl;

    public BluelinkApiUS(final HttpClient httpClient, final Optional<String> baseUrl,
            final TimeZoneProvider timeZoneProvider, final String username, final String password,
            final @Nullable String pin) {
        super(httpClient, timeZoneProvider, username, password, pin);
        this.baseUrl = baseUrl.orElseGet(() -> API_ENDPOINT);
    }

    @Override
    public boolean login() throws BluelinkApiException {
        final LoginRequest loginRequest = new LoginRequest(username, password);
        final String loginUrl = baseUrl + "/v2/ac/oauth/token";
        return doLogin(loginUrl, loginRequest, TokenResponse.class, t -> t);
    }

    @Override
    public List<Vehicle> getVehicles() throws BluelinkApiException {
        ensureAuthenticated();
        // One would expect the username (email address) to be URL-encoded, but
        // the API does not accept encoding the @ character so we omit encoding.
        final String url = baseUrl + "/ac/v2/enrollment/details/" + username;

        final Request request = httpClient.newRequest(url).timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);
        final EnrollmentResponse enrollment = sendRequest(request, EnrollmentResponse.class, "get vehicles");
        if (enrollment.enrolledVehicleDetails() == null) {
            return List.of();
        }
        return enrollment.enrolledVehicleDetails().stream().map(EnrolledVehicle::vehicleDetails)
                .filter(Objects::nonNull).filter(v -> v.vin() != null).map(BluelinkApiUS::toVehicle).toList();
    }

    @Override
    public boolean getVehicleStatus(final IVehicle vehicle, final boolean forceRefresh, final VehicleStatusCallback cb)
            throws BluelinkApiException {
        ensureAuthenticated();
        final String url = baseUrl + "/ac/v2/rcs/rvs/vehicleStatus";
        final Request request = httpClient.newRequest(url).timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .header("refresh", String.valueOf(forceRefresh));
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        final VehicleStatus status = sendRequest(request, VehicleStatus.class, "get vehicle status");
        final var data = status.vehicleStatus();
        if (data == null) {
            return false;
        }
        cb.acceptStatus(data);

        if (data.dateTime() != null) {
            try {
                final Instant lastUpdate = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(data.dateTime(), Instant::from);
                cb.acceptLastUpdateTimestamp(lastUpdate);
            } catch (final DateTimeParseException e) {
                logger.warn("unexpected dateTime: {}", data.dateTime());
            }
        }
        if (data.vehicleLocation() != null) {
            VehicleLocation.Coordinates coord = data.vehicleLocation().coord();
            if (coord != null) {
                cb.acceptLocation(new PointType(new DecimalType(coord.latitude()), new DecimalType(coord.longitude()),
                        new DecimalType(coord.altitude())));
            }
        }
        cb.acceptSmartKeyBatteryWarning(data.smartKeyBatteryWarning());

        getVehicles().stream().filter(v -> v.vin().equals(vehicle.vin())).findFirst()
                .ifPresent(v -> cb.acceptOdometer(new QuantityType<>(v.odometer(), ImperialUnits.MILE)));
        return true;
    }

    @Override
    public boolean lockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        return sendDoorCommand(vehicle, "/ac/v2/rcs/rdo/off");
    }

    @Override
    public boolean unlockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        return sendDoorCommand(vehicle, "/ac/v2/rcs/rdo/on");
    }

    private boolean sendDoorCommand(final IVehicle vehicle, final String endpoint) throws BluelinkApiException {
        ensureAuthenticated();
        final String url = baseUrl + endpoint;
        final DoorCommandRequest doorRequest = new DoorCommandRequest(username, vehicle.vin());

        final Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(HTTP_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        request.header("APPCLOUD-VIN", vehicle.vin());
        request.content(new StringContentProvider(gson.toJson(doorRequest)), APPLICATION_JSON);
        sendRequest(request, "door command");
        return true;
    }

    @Override
    public boolean climateStart(final IVehicle vehicle, final QuantityType<Temperature> temperature, final boolean heat,
            final boolean defrost, final @Nullable Integer igniOnDuration) throws BluelinkApiException {
        ensureAuthenticated();
        final String url;
        final Object climateRequest;
        if (vehicle.isElectric()) {
            url = baseUrl + "/ac/v2/evc/fatc/start";
            climateRequest = ClimateRequestEV.create(temperature, heat, defrost);
        } else {
            url = baseUrl + "/ac/v2/rcs/rsc/start";
            climateRequest = ClimateRequestICE.create(temperature, heat, defrost, username, vehicle.vin());
        }
        return sendClimateCommand(vehicle, url, climateRequest);
    }

    @Override
    public boolean climateStop(final IVehicle vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, vehicle.isElectric() ? "/ac/v2/evc/fatc/stop" : "/ac/v2/rcs/rsc/stop");
    }

    private boolean sendClimateCommand(final IVehicle vehicle, final String url, final Object climateRequest)
            throws BluelinkApiException {
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(HTTP_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        request.content(new StringContentProvider(gson.toJson(climateRequest)), APPLICATION_JSON);
        sendRequest(request, "climate command");
        return true;
    }

    @Override
    public boolean startCharging(final IVehicle vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, "/ac/v2/evc/charge/start");
    }

    @Override
    public boolean stopCharging(final IVehicle vehicle) throws BluelinkApiException {
        ensureAuthenticated();
        return sendSimplePostCommand(vehicle, "/ac/v2/evc/charge/stop");
    }

    @Override
    public boolean setChargeLimitDC(final IVehicle vehicle, final int limit) throws BluelinkApiException {
        return setChargeLimit(vehicle, PLUG_TYPE_DC, limit);
    }

    @Override
    public boolean setChargeLimitAC(final IVehicle vehicle, final int limit) throws BluelinkApiException {
        return setChargeLimit(vehicle, PLUG_TYPE_AC, limit);
    }

    private boolean setChargeLimit(final IVehicle vehicle, final int plugType, final int limit)
            throws BluelinkApiException {
        ensureAuthenticated();

        final String url = baseUrl + "/ac/v2/evc/charge/targetsoc/set";
        final ChargeLimitsRequest chargeLimitsRequest = new ChargeLimitsRequest(
                List.of(new ChargeLimitsRequest.TargetSOC(plugType, limit)));
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(chargeLimitsRequest)), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        sendRequest(request, "set charge limits");
        return true;
    }

    private boolean sendSimplePostCommand(final IVehicle vehicle, final String endpoint) throws BluelinkApiException {
        final String url = baseUrl + endpoint;
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider("{}"), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        sendRequest(request, endpoint);
        return true;
    }

    @Override
    public void addStandardHeaders(final Request request) {
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
        final String token = accessToken;
        if (token != null) {
            request.header("accessToken", token);
        }
        request.header("username", username).header("blueLinkServicePin", pin);
    }

    private void addVehicleHeaders(final Request request, final IVehicle vehicle) {
        final String regId = vehicle.id();
        final String vin = vehicle.vin();

        if (regId != null) {
            request.header("registrationId", regId);
        }
        if (vehicle instanceof Vehicle v && v.generation() != null) {
            request.header("gen", v.generation());
        }
        request.header("vin", vin);
    }

    private static Vehicle toVehicle(final EnrolledVehicle.VehicleInfo info) {
        assert info.vin() != null;
        int modelYear = 0;
        if (info.modelYear() != null) {
            try {
                modelYear = Integer.parseInt(info.modelYear());
            } catch (final NumberFormatException e) {
                // ignore
            }
        }
        return new Vehicle(info.registrationId(), info.vin(), info.nickName(), switch (info.evStatus()) {
            case "E" -> EngineType.EV;
            case "N" -> EngineType.ICE;
            default -> EngineType.UNKNOWN;
        }, info.modelCode(), modelYear, info.vehicleGeneration(), info.odometer());
    }

    @Override
    protected boolean isRetryable(final ContentResponse response) {
        if (response.getStatus() == 502) {
            final ErrorResponse err = gson.fromJson(response.getContentAsString(), ErrorResponse.class);
            if (err != null && "HT_534".equals(err.errorSubCode())) {
                // do not retry if rate-limited
                logger.warn("[{}] Rate limit exceeded, consider increasing refreshInterval", err.functionName());
                return false;
            }
        }
        return super.isRetryable(response);
    }
}
