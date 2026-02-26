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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
import org.openhab.binding.bluelink.internal.dto.DrivingRange;
import org.openhab.binding.bluelink.internal.dto.TokenResponse;
import org.openhab.binding.bluelink.internal.dto.eu.AirTemperature;
import org.openhab.binding.bluelink.internal.dto.eu.BaseResponse;
import org.openhab.binding.bluelink.internal.dto.eu.ChargeLimitsRequest;
import org.openhab.binding.bluelink.internal.dto.eu.ControlRequest;
import org.openhab.binding.bluelink.internal.dto.eu.RegistrationRequest;
import org.openhab.binding.bluelink.internal.dto.eu.RegistrationResponse;
import org.openhab.binding.bluelink.internal.dto.eu.Vehicle;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleStatusResponse;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleStatusResponse.VehicleStatusData;
import org.openhab.binding.bluelink.internal.dto.eu.VehicleStatusResponse.VehicleStatusInfo;
import org.openhab.binding.bluelink.internal.dto.eu.VehiclesResponse;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;

import com.google.gson.reflect.TypeToken;

/**
 * Client for the Kia Connect / Hyundai Bluelink / Genesis Connect API in Europe.
 * <p>
 * Implementation based on
 * <a href="https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api">hyundai_kia_connect_api</a> and
 * <a href="https://github.com/Hacksore/bluelinky">bluelinky</a>.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class BluelinkApiEU extends AbstractBluelinkApi<Vehicle> {
    private static final String HTTP_USER_AGENT = "okhttp/3.12.0";
    private static final DateTimeFormatter EU_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BrandConfig brandConfig;
    private final String refreshToken;
    private @Nullable UUID deviceId;

    public BluelinkApiEU(final HttpClient httpClient, final Brand brand, final Map<String, String> properties,
            final @Nullable String baseUrl, final TimeZoneProvider timeZoneProvider, final String refreshToken) {
        super(httpClient, timeZoneProvider, "", refreshToken, null);
        this.refreshToken = refreshToken;
        final BrandConfig baseBrandConfig = BrandConfig.forBrand(brand);
        if (baseUrl == null) {
            this.brandConfig = baseBrandConfig;
        } else {
            this.brandConfig = new BrandConfig(baseUrl, baseUrl, baseBrandConfig.ccspServiceId, baseBrandConfig.appId,
                    baseBrandConfig.clientSecret, baseBrandConfig.cfb, baseBrandConfig.pushType);
        }
        final String storedDeviceId = properties.get("deviceId");
        if (storedDeviceId != null && !storedDeviceId.isBlank()) {
            this.deviceId = UUID.fromString(storedDeviceId);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        final UUID id = deviceId;
        return id != null ? Map.of("deviceId", id.toString()) : Map.of();
    }

    @Override
    public boolean login() throws BluelinkApiException {
        authenticate();
        if (this.deviceId == null) {
            registerDevice();
        } else {
            logger.debug("Device already registered, deviceId: {}", deviceId);
        }
        return true;
    }

    /**
     * Authenticate to the Bluelink EU API using refresh_token and get an access_token.
     * 
     * @throws BluelinkApiException
     */
    private void authenticate() throws BluelinkApiException {
        final String loginUrl = brandConfig.loginBaseUrl + "/auth/api/v2/user/oauth2/token";
        final String formBody = "grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id="
                + brandConfig.ccspServiceId + "&client_secret=" + brandConfig.clientSecret;
        final Request request = httpClient.newRequest(loginUrl).method(HttpMethod.POST)
                .header(HttpHeader.USER_AGENT, HTTP_USER_AGENT)
                .content(new StringContentProvider(formBody), "application/x-www-form-urlencoded");
        doLogin(request, TokenResponse.class, t -> t);
    }

    /**
     * Register using a random UUID to get a valid <code>deviceId</code> required for API calls.
     * 
     * @throws BluelinkApiException
     */
    private void registerDevice() throws BluelinkApiException {
        ensureAuthenticated();
        final String url = brandConfig.apiBaseUrl + "/api/v1/spa/notifications/register";
        final RegistrationRequest payload = new RegistrationRequest(
                // ThreadLocalRandom is good enough, we don't need cryptographically secure randomness
                String.format("%064x", ThreadLocalRandom.current().nextLong()), brandConfig.pushType,
                UUID.randomUUID().toString());

        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(payload)), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);

        final BaseResponse<RegistrationResponse> registration = sendRequest(request, new TypeToken<>() {
        }, "register device");
        final var result = registration.result();
        if (result != null) {
            this.deviceId = result.deviceId();
        }
        logger.debug("Device registration successful, deviceId '{}'", deviceId);
    }

    @Override
    public List<Vehicle> getVehicles() throws BluelinkApiException {
        ensureAuthenticated();
        final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles";
        final Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(HTTP_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        addStandardHeaders(request);
        addAuthHeaders(request);

        final BaseResponse<VehiclesResponse> response = sendRequest(request, new TypeToken<>() {
        }, "get vehicles");
        final VehiclesResponse result = response.result();
        if (result == null || result.vehicles() == null) {
            return List.of();
        }
        return result.vehicles().stream().filter(Objects::nonNull).filter(v -> v.vin() != null)
                .map(BluelinkApiEU::toVehicle).toList();
    }

    /**
     * Whether the vehicle supports the CCU/CCS2 protocol.
     * 
     * @param vehicle the vehicle to check
     * @return true if the vehicle supports CCU/CCS2 protocol, false otherwise
     */
    private boolean isCcsProtocol(final IVehicle vehicle) {
        return vehicle instanceof Vehicle euVehicle && euVehicle.ccs2ProtocolSupport();
    }

    @Override
    public boolean getVehicleStatus(final IVehicle vehicle, final boolean forceRefresh, final VehicleStatusCallback cb)
            throws BluelinkApiException {
        ensureAuthenticated();

        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        }

        final String vehicleId = vehicle.id();
        if (vehicleId == null) {
            throw new BluelinkApiException("Vehicle ID is missing");
        }

        final @Nullable VehicleStatusData data;
        if (forceRefresh) {
            final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicleId + "/status";
            final Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(HTTP_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            addStandardHeaders(request);
            addAuthHeaders(request);

            final BaseResponse<VehicleStatusData> response = sendRequest(request, new TypeToken<>() {
            }, "get vehicle status (force refresh)");
            data = response.result();
        } else {
            final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicleId + "/status/latest";
            final Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(HTTP_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
            addStandardHeaders(request);
            addAuthHeaders(request);

            final BaseResponse<VehicleStatusResponse> response = sendRequest(request, new TypeToken<>() {
            }, "get vehicle status");
            final VehicleStatusResponse result = response.result();
            if (result == null || result.vehicleStatusInfo() == null) {
                return false;
            }

            final VehicleStatusInfo statusInfo = result.vehicleStatusInfo();
            final var location = statusInfo.vehicleLocation();
            if (location != null && location.coord() != null) {
                final var coord = location.coord();
                cb.acceptLocation(new PointType(new DecimalType(coord.lat()), new DecimalType(coord.lon()),
                        new DecimalType(coord.alt())));
            }

            final DrivingRange odometer = statusInfo.odometer();
            if (odometer != null) {
                final var range = odometer.getRange();
                if (range instanceof QuantityType<?> qt) {
                    @SuppressWarnings("unchecked")
                    final QuantityType<javax.measure.quantity.Length> length = (QuantityType<javax.measure.quantity.Length>) qt;
                    cb.acceptOdometer(length);
                }
            }

            data = statusInfo.vehicleStatus();
        }
        if (data == null) {
            return false;
        }
        cb.acceptStatus(data);

        if (data.time() != null) {
            try {
                final ZoneId tz = timeZoneProvider.getTimeZone();
                final LocalDateTime ldt = LocalDateTime.parse(data.time(), EU_DATETIME_FORMAT);
                cb.acceptLastUpdateTimestamp(ldt.atZone(tz).toInstant());
            } catch (final DateTimeParseException e) {
                logger.warn("unexpected time format: {}", data.time());
            }
        }

        cb.acceptSmartKeyBatteryWarning(data.smartKeyBatteryWarning());

        return true;
    }

    /**
     * Send a control action request for legacy protocol vehicles.
     * 
     * @param url the URL to send the request to
     * @param payload the payload to send
     * @return true on success
     * @throws BluelinkApiException
     */
    private boolean sendControlAction(final String url, final ControlRequest payload) throws BluelinkApiException {
        ensureAuthenticated();

        final String payloadJson = gson.toJson(payload);
        logger.debug("send control action request: {}", payloadJson);
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header(HttpHeader.USER_AGENT, HTTP_USER_AGENT)
                .content(new StringContentProvider(payloadJson), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);

        final BaseResponse<?> response = sendRequest(request, new TypeToken<>() {
        }, "send control action");
        if (!response.retCode().equals("S")) {
            throw new BluelinkApiException("Failed to send control action: " + response);
        }
        return true;
    }

    /**
     * Send a control action request for legacy protocol vehicles.
     *
     * @param vehicle the vehicle to send the request for
     * @param endpoint the endpoint to send the request to
     * @param action the action to send
     * @return true on success
     * @throws BluelinkApiException
     */
    private boolean sendControlAction(final IVehicle vehicle, final String endpoint, final String action)
            throws BluelinkApiException {
        final String vehicleId = vehicle.id();
        if (vehicleId == null) {
            throw new BluelinkApiException("Vehicle ID is missing");
        }

        final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicleId + "/control/" + endpoint;
        final ControlRequest payload = new ControlRequest(this.deviceId, action, null, null, null, null);
        return sendControlAction(url, payload);
    }

    @Override
    public boolean lockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            return sendControlAction(vehicle, "door", "close");
        }
    }

    @Override
    public boolean unlockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            return sendControlAction(vehicle, "door", "open");
        }
    }

    @Override
    public boolean climateStart(final IVehicle vehicle, final QuantityType<Temperature> temperature, final boolean heat,
            final boolean defrost, final @Nullable Integer igniOnDuration) throws BluelinkApiException {
        final String vehicleId = vehicle.id();
        if (vehicleId == null) {
            throw new BluelinkApiException("Vehicle ID is missing");
        }

        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicleId + "/control/temperature";
            final AirTemperature airTemperature = AirTemperature.of(vehicle, temperature);
            final ControlRequest payload = new ControlRequest(deviceId, "start", 0,
                    new ControlRequest.Options(defrost, heat ? 1 : 0), airTemperature.value(), "C");
            return sendControlAction(url, payload);
        }
    }

    @Override
    public boolean climateStop(final IVehicle vehicle) throws BluelinkApiException {
        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            return sendControlAction(vehicle, "temperature", "stop");
        }
    }

    @Override
    public boolean startCharging(final IVehicle vehicle) throws BluelinkApiException {
        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            return sendControlAction(vehicle, "charge", "start");
        }
    }

    @Override
    public boolean stopCharging(final IVehicle vehicle) throws BluelinkApiException {
        if (isCcsProtocol(vehicle)) {
            throw new BluelinkApiException(
                    "CCU/CCS2 protocol support hasn't been implemented yet. Report this on GitHub and provide debug logs.");
        } else {
            return sendControlAction(vehicle, "charge", "stop");
        }
    }

    /**
     * Send a charge limit request to the Bluelink EU API both for legacy and CCU/CCS2 protocol.
     *
     * @param vehicle
     * @param plugType
     * @param limit
     * @return
     * @throws BluelinkApiException
     */
    private boolean sendChargeLimitRequest(final IVehicle vehicle, int plugType, int limit)
            throws BluelinkApiException {
        ensureAuthenticated();

        final String vehicleId = vehicle.id();
        if (vehicleId == null) {
            throw new BluelinkApiException("Vehicle ID is missing");
        }
        boolean ccuCcs2ProtocolSupport = isCcsProtocol(vehicle);

        final String url = brandConfig.apiBaseUrl + "/api/v1/spa/vehicles/" + vehicleId + "/charge/target";
        final ChargeLimitsRequest payload = new ChargeLimitsRequest(
                List.of(new ChargeLimitsRequest.ChargeLimit(plugType, limit)));
        final String payloadJson = gson.toJson(payload);
        logger.debug("send charge limit request: {}", payloadJson);
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .header(HttpHeader.USER_AGENT, HTTP_USER_AGENT)
                .content(new StringContentProvider(payloadJson), APPLICATION_JSON);
        addStandardHeaders(request);
        addAuthHeaders(request);
        if (ccuCcs2ProtocolSupport) {
            addCcuCcs2Headers(request);
        }

        final BaseResponse<?> response = sendRequest(request, new TypeToken<>() {
        }, "send charge limit");
        if (!response.retCode().equals("S")) {
            throw new BluelinkApiException("Failed to set charge limit: " + response);
        }
        return true;
    }

    @Override
    public boolean setChargeLimitDC(final IVehicle vehicle, final int limit) throws BluelinkApiException {
        return sendChargeLimitRequest(vehicle, 0, limit);
    }

    @Override
    public boolean setChargeLimitAC(final IVehicle vehicle, final int limit) throws BluelinkApiException {
        return sendChargeLimitRequest(vehicle, 1, limit);
    }

    @Override
    public void addStandardHeaders(final Request request) {
        request.header("ccsp-service-id", brandConfig.ccspServiceId).header("ccsp-application-id", brandConfig.appId)
                .header("Stamp", generateStamp()).header(HttpHeader.USER_AGENT, HTTP_USER_AGENT);
    }

    private void addCcuCcs2Headers(final Request request) {
        request.header("Ccuccs2protocolsupport", "1");
    }

    private void addAuthHeaders(final Request request) {
        final String token = accessToken;
        if (token != null) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        }
        final UUID id = this.deviceId;
        if (id != null) {
            request.header("ccsp-device-id", id.toString());
        }
    }

    private String generateStamp() {
        final long timestamp = Instant.now().getEpochSecond();
        final String rawData = brandConfig.appId + ":" + timestamp;
        final byte[] rawBytes = rawData.getBytes(StandardCharsets.UTF_8);
        final byte[] cfbBytes = Base64.getDecoder().decode(brandConfig.cfb);

        final int length = Math.min(rawBytes.length, cfbBytes.length);
        final byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (cfbBytes[i] ^ rawBytes[i]);
        }
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    protected boolean isRetryable(final ContentResponse response) {
        if (super.isRetryable(response)) {
            return true;
        }
        if (response.getStatus() == HttpStatus.BAD_REQUEST_400) {
            // retry on HTTP 400: resCode = 4004; resMsg = Duplicate request
            final BaseResponse<?> parsed = gson.fromJson(response.getContentAsString(), BaseResponse.class);
            return parsed != null && "4004".equals(parsed.resCode());
        }
        return false;
    }

    private static Vehicle toVehicle(final VehiclesResponse.VehicleInfo info) {
        final IVehicle.EngineType engineType = switch (info.type() != null ? info.type() : "") {
            case "EV" -> IVehicle.EngineType.EV;
            case "PHEV" -> IVehicle.EngineType.PHEV;
            case "ICE" -> IVehicle.EngineType.ICE;
            default -> IVehicle.EngineType.UNKNOWN;
        };
        return new Vehicle(info.vehicleId(), info.vin(), info.nickname(), engineType, info.vehicleName(), 0,
                info.ccuCCS2ProtocolSupport() != 0);
    }

    record BrandConfig(String apiBaseUrl, String loginBaseUrl, String ccspServiceId, String appId, String clientSecret,
            String cfb, String pushType) {

        static BrandConfig forBrand(final Brand brand) {
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
                case UNKNOWN -> throw new IllegalArgumentException("brand not configured");
            };
        }
    }
}
