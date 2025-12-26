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

import static org.openhab.binding.bluelink.internal.dto.ca.ChargeLimitsRequest.PLUG_TYPE_AC;
import static org.openhab.binding.bluelink.internal.dto.ca.ChargeLimitsRequest.PLUG_TYPE_DC;

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
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.bluelink.internal.dto.ca.ChargeLimitsRequest;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestEV;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestICE;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestKiaEV;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestKiaEV9;
import org.openhab.binding.bluelink.internal.dto.ca.ListVehiclesResponse;
import org.openhab.binding.bluelink.internal.dto.ca.ListVehiclesResponse.Result.VehicleInfo;
import org.openhab.binding.bluelink.internal.dto.ca.LoginRequest;
import org.openhab.binding.bluelink.internal.dto.ca.PinRequest;
import org.openhab.binding.bluelink.internal.dto.ca.PinVerifyResponse;
import org.openhab.binding.bluelink.internal.dto.ca.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.ca.Vehicle;
import org.openhab.binding.bluelink.internal.dto.ca.VehicleStatusResponse;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;

/**
 * Client for the Kia Connect / Hyundai Bluelink / Genesis Connect API in Canada.
 * <p>
 * Implementation based on [hyundai_kia_connect_api](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api)
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiCA extends AbstractBluelinkApi<Vehicle> {
    private static final String CLIENT_ID = "HATAHSPACA0232141ED9722C67715A0B";

    private final Brand brand;
    private final String baseUrl;

    public BluelinkApiCA(final HttpClient httpClient, final Brand brand, final Optional<String> optBaseUrl,
            final TimeZoneProvider timeZoneProvider, final String username, final String password,
            final @Nullable String pin) {
        super(httpClient, timeZoneProvider, username, password, pin);
        this.brand = brand;
        this.baseUrl = optBaseUrl.map(url -> url.endsWith("/") ? url : url + "/")
                .orElseGet(() -> getApiHostname(brand));
    }

    private static String getApiHostname(final Brand brand) {
        assert brand != Brand.UNKNOWN;
        return switch (brand) {
            case HYUNDAI -> "https://mybluelink.ca/tods/api/";
            case KIA -> "https://kiaconnect.ca/tods/api/";
            case GENESIS -> "https://genesisconnect.ca/tods/api/";
            case UNKNOWN -> throw new IllegalArgumentException("brand not configured"); // not reached
        };
    }

    @Override
    public boolean login() throws BluelinkApiException {
        final LoginRequest loginRequest = new LoginRequest(username, password);
        final String loginUrl = baseUrl + "v2/login";
        return doLogin(loginUrl, loginRequest, TokenResponse.class,
                t -> t.result() != null ? t.result().token() : null);
    }

    @Override
    public List<Vehicle> getVehicles() throws BluelinkApiException {
        ensureAuthenticated();
        final String url = baseUrl + "vhcllst";
        final Request request = httpClient.newRequest(url).timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);
        final ListVehiclesResponse listVehiclesResponse = sendRequest(request, ListVehiclesResponse.class,
                "get vehicles");
        if (listVehiclesResponse.result() == null || listVehiclesResponse.result().vehicles() == null) {
            return List.of();
        }
        return listVehiclesResponse.result().vehicles().stream().filter(Objects::nonNull).filter(v -> v.vin() != null)
                .map(BluelinkApiCA::toVehicle).toList();
    }

    @Override
    public boolean getVehicleStatus(final IVehicle vehicle, final boolean forceRefresh, final VehicleStatusCallback cb)
            throws BluelinkApiException {
        ensureAuthenticated();
        final String endpoint = forceRefresh ? "rltmvhclsts" : "lstvhclsts";
        final String url = baseUrl + endpoint;
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider("{}"), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);
        final VehicleStatusResponse response = sendRequest(request, VehicleStatusResponse.class, "get vehicle status");
        if (response.result() == null || response.result().status() == null) {
            return false;
        }
        final var data = response.result().status();
        cb.acceptStatus(data);

        if (data.lastStatusDate() != null) {
            try {
                cb.acceptLastUpdateTimestamp(
                        DateTimeFormatter.RFC_1123_DATE_TIME.parse(data.lastStatusDate(), Instant::from));
            } catch (final DateTimeParseException e) {
                try {
                    cb.acceptLastUpdateTimestamp(
                            DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(data.lastStatusDate(), Instant::from));
                } catch (final DateTimeParseException e2) {
                    logger.warn("unexpected lastStatusDate: {}", data.lastStatusDate());
                }
            }
        }
        return true;
    }

    @Override
    public boolean lockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        return sendPinCommand(vehicle, "drlck");
    }

    @Override
    public boolean unlockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        return sendPinCommand(vehicle, "drulck");
    }

    @Override
    public boolean climateStart(final IVehicle vehicle, final QuantityType<Temperature> temperature, final boolean heat,
            final boolean defrost, final @Nullable Integer igniOnDuration) throws BluelinkApiException {
        final String endpoint;
        final Object climateRequest;
        final int igniOn = igniOnDuration != null ? igniOnDuration : 10;
        if (vehicle.isElectric()) {
            endpoint = "evc/rfon";
            if (brand == Brand.KIA) {
                if ("EV9".equals(vehicle.model())) {
                    climateRequest = ClimateRequestKiaEV9.create(vehicle, pin, temperature, heat, defrost, igniOn);
                } else {
                    climateRequest = ClimateRequestKiaEV.create(vehicle, pin, temperature, heat, defrost, igniOn);
                }
            } else {
                climateRequest = ClimateRequestEV.create(vehicle, pin, temperature, heat, defrost);
            }
        } else {
            endpoint = "rmtstrt";
            climateRequest = ClimateRequestICE.create(vehicle, pin, temperature, heat, defrost, igniOn);
        }
        return simplePostWithPin(vehicle, endpoint, climateRequest);
    }

    @Override
    public boolean climateStop(final IVehicle vehicle) throws BluelinkApiException {
        final String endpoint;
        if (vehicle.engineType() == Vehicle.EngineType.EV) {
            endpoint = "evc/rfoff";
        } else {
            endpoint = "rmtstp";
        }
        return sendPinCommand(vehicle, endpoint);
    }

    @Override
    public boolean startCharging(final IVehicle vehicle) throws BluelinkApiException {
        return sendPinCommand(vehicle, "evc/rcstrt");
    }

    @Override
    public boolean stopCharging(final IVehicle vehicle) throws BluelinkApiException {
        return sendPinCommand(vehicle, "evc/rcstp");
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
        final var payload = new ChargeLimitsRequest(pin, List.of(new ChargeLimitsRequest.TargetSOC(plugType, limit)));
        final String url = baseUrl + "evc/setsoc";
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(payload)), APPLICATION_JSON).header("from", "SPA")
                .header("offset", "-8").header("priority", "u=1, i")
                .header(HttpHeader.REFERER, "https://kiaconnect.ca/remote/");
        addStandardHeaders(request);
        addAuthHeaders(request);
        addPinAuthHeader(request, vehicle);
        addVehicleHeaders(request, vehicle);
        sendRequest(request, "evc/setsoc");
        return true;
    }

    private String getPinToken(final IVehicle vehicle) throws BluelinkApiException {
        final String pin = this.pin;
        if (pin == null || pin.isBlank()) {
            throw new BluelinkApiException("PIN is required for this operation");
        }
        final String url = baseUrl + "vrfypin";
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(new PinRequest(pin))), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addVehicleHeaders(request, vehicle);

        final PinVerifyResponse pinResponse = sendRequest(request, PinVerifyResponse.class, "PIN verify");
        if (pinResponse.result() == null || pinResponse.result().pAuth() == null) {
            throw new BluelinkApiException("Invalid PIN verification response");
        }
        logger.debug("PIN verified");
        return pinResponse.result().pAuth();
    }

    private boolean sendPinCommand(final IVehicle vehicle, final String endpoint) throws BluelinkApiException {
        return simplePostWithPin(vehicle, endpoint, new PinRequest(pin));
    }

    private boolean simplePostWithPin(final IVehicle vehicle, final String endpoint, final Object payload)
            throws BluelinkApiException {
        ensureAuthenticated();
        final String url = baseUrl + endpoint;
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(payload)), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        addPinAuthHeader(request, vehicle);
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
                        "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.101 Mobile Safari/537.36")
                .header("from", "CWP").header("language", "0").header("client_id", CLIENT_ID)
                .header("clientSecret", "CLISCR01AHSPA").header("offset", String.valueOf(getTimeZoneOffset()));
    }

    private void addAuthHeaders(final Request request) {
        final String token = accessToken;
        if (token != null) {
            request.header("accessToken", token);
        }
    }

    private void addPinAuthHeader(final Request request, final IVehicle vehicle) throws BluelinkApiException {
        final String token = getPinToken(vehicle);
        request.header("pAuth", token);
    }

    private void addVehicleHeaders(final Request request, final IVehicle vehicle) {
        final String id = vehicle.id();
        if (id != null) {
            request.header("vehicleId", id);
        }
    }

    private static Vehicle toVehicle(final VehicleInfo v) {
        int modelYear = 0;
        if (v.modelYear() != null) {
            try {
                modelYear = Integer.parseInt(v.modelYear());
            } catch (final NumberFormatException e) {
                // ignore
            }
        }
        final var engineType = switch (v.fuelKindCode()) {
            case "G" -> Vehicle.EngineType.ICE;
            case "E" -> Vehicle.EngineType.EV;
            case "P" -> Vehicle.EngineType.PHEV;
            default -> Vehicle.EngineType.UNKNOWN;
        };
        return new Vehicle(v.vehicleId(), v.vin(), v.nickName(), engineType, v.modelName(), modelYear);
    }
}
