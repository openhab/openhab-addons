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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bluelink.internal.api.LoginRejectedException.Reason;
import org.openhab.binding.bluelink.internal.dto.Token;
import org.openhab.binding.bluelink.internal.dto.ca.ChargeLimitsRequest;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestEV;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestICE;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestKiaEV;
import org.openhab.binding.bluelink.internal.dto.ca.ClimateRequestKiaEV9;
import org.openhab.binding.bluelink.internal.dto.ca.ListVehiclesResponse;
import org.openhab.binding.bluelink.internal.dto.ca.ListVehiclesResponse.Result.VehicleInfo;
import org.openhab.binding.bluelink.internal.dto.ca.LoginRequest;
import org.openhab.binding.bluelink.internal.dto.ca.MfaRequest;
import org.openhab.binding.bluelink.internal.dto.ca.MfaResponse;
import org.openhab.binding.bluelink.internal.dto.ca.PinRequest;
import org.openhab.binding.bluelink.internal.dto.ca.PinVerifyResponse;
import org.openhab.binding.bluelink.internal.dto.ca.ResponseError;
import org.openhab.binding.bluelink.internal.dto.ca.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.ca.Vehicle;
import org.openhab.binding.bluelink.internal.dto.ca.VehicleStatusResponse;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;

import com.google.gson.JsonSyntaxException;

/**
 * Client for the Kia Connect / Hyundai Bluelink / Genesis Connect API in Canada.
 * <p>
 * Implementation based on [hyundai_kia_connect_api](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api)
 *
 * @author Marcus Better - Initial contribution
 * @author Carlo Dischler - Login verification code
 */
@NonNullByDefault
public class BluelinkApiCA extends AbstractBluelinkApi<Vehicle> {
    private static final String CLIENT_ID = "HATAHSPACA0232141ED9722C67715A0B";
    private static final String VERIFICATION_REQUIRED = "7110";
    private static final String ACCOUNT_LOCKED = "7402";
    private static final String INVALID_CREDENTIALS = "7404";
    // expired, deleted by a newer login, issued to another IP address
    private static final Set<String> TOKEN_INVALID = Set.of("7403", "7602", "7606");

    private record PendingVerification(String otpKey, String email) {
    }

    private final Brand brand;
    private final String baseUrl;
    // the server asks for a verification code only once per device
    private final String deviceId;
    private volatile boolean verificationRequired;
    private volatile @Nullable PendingVerification pendingVerification;

    public BluelinkApiCA(final HttpClient httpClient, final Brand brand, final Map<String, String> properties,
            final @Nullable String baseUrl, final TimeZoneProvider timeZoneProvider, final String username,
            final String password, final @Nullable String pin) {
        super(httpClient, timeZoneProvider, username, password, pin);
        this.brand = brand;
        if (baseUrl == null) {
            this.baseUrl = getApiHostname(brand);
        } else {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        }
        final String storedDeviceId = properties.get("deviceId");
        this.deviceId = storedDeviceId != null && !storedDeviceId.isBlank() ? storedDeviceId
                : Base64.getEncoder().encodeToString(
                        UUID.randomUUID().toString().replace("-", "").getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.of("deviceId", deviceId);
    }

    private static String getApiHostname(final Brand brand) {
        return switch (brand) {
            case HYUNDAI -> "https://mybluelink.ca/tods/api/";
            case KIA -> "https://kiaconnect.ca/tods/api/";
            case GENESIS -> "https://genesisconnect.ca/tods/api/";
            case UNKNOWN -> throw new IllegalArgumentException("brand not configured");
        };
    }

    // a login deletes the previous token, so a token that is still valid must not be replaced
    @Override
    protected synchronized void ensureAuthenticated() throws BluelinkApiException {
        super.ensureAuthenticated();
    }

    @Override
    public synchronized boolean login() throws BluelinkApiException {
        if (isAuthenticated()) {
            return true;
        }
        // the user requests the code, until then further logins would only be rejected again
        if (verificationRequired) {
            final PendingVerification pending = pendingVerification;
            if (pending != null) {
                throw new LoginRejectedException(Reason.VERIFICATION_CODE_SENT, "verification code sent",
                        pending.email());
            }
            throw verificationRequired();
        }
        final TokenResponse response = postLogin("v2/login", new LoginRequest(username, password), TokenResponse.class,
                "login");
        final ResponseError error = response.error();
        if (error != null && VERIFICATION_REQUIRED.equals(error.errorCode())) {
            verificationRequired = true;
            throw verificationRequired();
        }
        checkError(error, "login");
        applyToken(response.result() != null ? response.result().token() : null);
        return true;
    }

    /**
     * Complete a login that needs a verification code with the code the user received by e-mail.
     *
     * @param code the verification code
     * @return false if no code was requested
     * @throws BluelinkApiException if the code is not accepted
     */
    public synchronized boolean verifyCode(final String code) throws BluelinkApiException {
        final PendingVerification pending = pendingVerification;
        if (pending == null) {
            return false;
        }
        final MfaResponse validation = postLogin("mfa/validateotp",
                new MfaRequest.ValidateCode(code.trim(), username, pending.otpKey(), MfaRequest.API_CODE),
                MfaResponse.class, "verify code");
        checkError(validation.error(), "verify code");
        final MfaResponse.Result result = validation.result();
        if (result == null || !Boolean.TRUE.equals(result.verifiedOtp()) || result.otpValidationKey() == null) {
            throw new LoginRejectedException(Reason.REJECTED, "verify code: code not accepted", "code not accepted");
        }
        final MfaResponse tokenResponse = postLogin("mfa/genmfatkn", new MfaRequest.GenerateToken(username,
                pending.email(), MfaRequest.API_CODE, result.otpValidationKey(), "Y"), MfaResponse.class,
                "verified login");
        checkError(tokenResponse.error(), "verified login");
        applyToken(tokenResponse.result() != null ? tokenResponse.result().token() : null);
        pendingVerification = null;
        verificationRequired = false;
        logger.debug("Login verified, deviceId: {}", deviceId);
        return true;
    }

    /**
     * Send a verification code to the e-mail address of the account.
     *
     * @return the e-mail address, or null if the login does not need a code
     * @throws BluelinkApiException if the code cannot be sent
     */
    public synchronized @Nullable String requestVerificationCode() throws BluelinkApiException {
        if (!verificationRequired) {
            return null;
        }
        final MfaResponse methods = postLogin("mfa/selverifmeth",
                new MfaRequest.SelectMethod(MfaRequest.API_CODE, username), MfaResponse.class, "verification methods");
        checkError(methods.error(), "verification methods");
        final MfaResponse.Result result = methods.result();
        if (result == null || result.userInfoUuid() == null) {
            throw new BluelinkApiException("verification methods: invalid response");
        }
        final List<String> emails = result.emailList();
        final String email = emails != null && !emails.isEmpty() ? emails.getFirst() : username;
        final MfaResponse sent = postLogin("mfa/sendotp",
                new MfaRequest.SendCode("E", MfaRequest.API_CODE, email, "", result.userInfoUuid()), MfaResponse.class,
                "send code");
        checkError(sent.error(), "send code");
        if (sent.result() == null || sent.result().otpKey() == null) {
            throw new BluelinkApiException("send code: invalid response");
        }
        logger.debug("Verification code sent to {}", email);
        pendingVerification = new PendingVerification(sent.result().otpKey(), email);
        return email;
    }

    @Override
    protected void checkResponse(final Request request, final ContentResponse response, final String op)
            throws BluelinkApiException {
        final ResponseError error;
        try {
            final ResponseError.Envelope envelope = gson.fromJson(response.getContentAsString(),
                    ResponseError.Envelope.class);
            error = envelope != null ? envelope.error() : null;
        } catch (final JsonSyntaxException e) {
            return;
        }
        if (error != null && TOKEN_INVALID.contains(error.errorCode())) {
            invalidateToken(request.getHeaders().get("accessToken"));
            throw new TokenInvalidException(op + ": " + error.errorDesc());
        }
        checkError(error, op);
    }

    private synchronized void invalidateToken(final @Nullable String usedToken) {
        // another request may have logged in again already
        if (usedToken != null && usedToken.equals(accessToken)) {
            accessToken = null;
        }
    }

    // the token expires early when it is not used and is deleted by a login from elsewhere, so log in once more
    private <T> T withLogin(final Call<T> call) throws BluelinkApiException {
        try {
            return call.run();
        } catch (final TokenInvalidException e) {
            logger.debug("{}, logging in again", e.getMessage());
            return call.run();
        }
    }

    @FunctionalInterface
    private interface Call<T> {
        T run() throws BluelinkApiException;
    }

    private static class TokenInvalidException extends BluelinkApiException {
        private static final long serialVersionUID = 1L;

        TokenInvalidException(final String message) {
            super(message);
        }
    }

    private static LoginRejectedException verificationRequired() {
        return new LoginRejectedException(Reason.VERIFICATION_REQUIRED, "verification required");
    }

    private void checkError(final @Nullable ResponseError error, final String op) throws BluelinkApiException {
        if (error == null || error.errorCode() == null) {
            return;
        }
        final String description = error.errorDesc() != null ? error.errorDesc() : error.errorCode();
        logger.debug("{} failed: {} - {}", op, error.errorCode(), description);
        throw switch (error.errorCode()) {
            case INVALID_CREDENTIALS ->
                new LoginRejectedException(Reason.INVALID_CREDENTIALS, op + ": invalid username or password");
            case ACCOUNT_LOCKED -> new LoginRejectedException(Reason.REJECTED, op + ": " + description, description);
            default -> new BluelinkApiException(op + ": " + description);
        };
    }

    private void applyToken(final @Nullable Token token) throws BluelinkApiException {
        if (token == null || token.accessToken() == null || token.expiresIn() == null) {
            throw new BluelinkApiException("Invalid token response");
        }
        try {
            setAccessToken(token.accessToken(), Instant.now().plusSeconds(Long.parseLong(token.expiresIn())));
        } catch (final NumberFormatException e) {
            throw new BluelinkApiException("Invalid token response", e);
        }
    }

    // the login requests carry the device id, a known device does not need a verification code
    private <T> T postLogin(final String endpoint, final Object payload, final Class<T> type, final String op)
            throws BluelinkApiException {
        final Request request = httpClient.newRequest(baseUrl + endpoint).method(HttpMethod.POST)
                .content(new StringContentProvider(gson.toJson(payload)), APPLICATION_JSON)
                .header("Deviceid", deviceId);
        addStandardHeaders(request);
        final ContentResponse response = send(request, op);
        final int status = response.getStatus();
        if (status != HttpStatus.OK_200) {
            logger.debug("operation failed ({}): {} - {}", op, status, response.getContentAsString());
            throw operationFailed(op, status, isServerError(status));
        }
        try {
            final @Nullable T result = gson.fromJson(response.getContentAsString(), type);
            if (result == null) {
                throw new BluelinkApiException(op + ": empty response");
            }
            return result;
        } catch (final JsonSyntaxException e) {
            throw new BluelinkApiException(op + ": invalid response", e);
        }
    }

    @Override
    public List<Vehicle> getVehicles() throws BluelinkApiException {
        return withLogin(() -> {
            ensureAuthenticated();
            final String url = baseUrl + "vhcllst";
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .content(new StringContentProvider("{}"), APPLICATION_JSON);
            addStandardHeaders(request);
            addAuthHeaders(request);
            final ListVehiclesResponse listVehiclesResponse = sendRequest(request, ListVehiclesResponse.class,
                    "get vehicles");
            if (listVehiclesResponse.result() == null || listVehiclesResponse.result().vehicles() == null) {
                return List.of();
            }
            return listVehiclesResponse.result().vehicles().stream().filter(Objects::nonNull)
                    .filter(v -> v.vin() != null).map(BluelinkApiCA::toVehicle).toList();
        });
    }

    @Override
    public boolean getVehicleStatus(final IVehicle vehicle, final boolean forceRefresh, final VehicleStatusCallback cb)
            throws BluelinkApiException {
        return withLogin(() -> {
            ensureAuthenticated();
            final String endpoint = forceRefresh ? "rltmvhclsts" : "lstvhclsts";
            final String url = baseUrl + endpoint;
            final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                    .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .content(new StringContentProvider("{}"), APPLICATION_JSON);
            addStandardHeaders(request);
            addAuthHeaders(request);
            addVehicleHeaders(request, vehicle);
            final VehicleStatusResponse response = sendRequest(request, VehicleStatusResponse.class,
                    "get vehicle status");
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
        });
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
        final int igniOn = igniOnDuration != null ? igniOnDuration : DEFAULT_HEAT_DURATION_MINUTES;
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
        return withLogin(() -> {
            ensureAuthenticated();
            final var payload = new ChargeLimitsRequest(pin,
                    List.of(new ChargeLimitsRequest.TargetSOC(plugType, limit)));
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
        });
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
        return withLogin(() -> {
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
        });
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
