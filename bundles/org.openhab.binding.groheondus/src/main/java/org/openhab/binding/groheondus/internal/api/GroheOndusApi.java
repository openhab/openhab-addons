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
package org.openhab.binding.groheondus.internal.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.FormRequestContent;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.groheondus.internal.api.dto.ApplianceDataResponse;
import org.openhab.binding.groheondus.internal.api.dto.ApplianceResponse;
import org.openhab.binding.groheondus.internal.api.dto.ApplianceStatusEntry;
import org.openhab.binding.groheondus.internal.api.dto.DashboardResponse;
import org.openhab.binding.groheondus.internal.api.dto.TokenResponse;
import org.openhab.core.i18n.TimeZoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Client for the GROHE ONDUS cloud REST API.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class GroheOndusApi {
    static final String BASE_URL = "https://idp2-apigw.cloud.grohe.com";
    private static final String API_URL = BASE_URL + "/v3/iot/";
    private static final long REQUEST_TIMEOUT_SECONDS = 20;
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final Pattern FORM_ACTION_PATTERN = Pattern.compile("action=\"([^\"]*)\"");
    private static final String WRONG_CREDENTIALS_MARKER = "Invalid email address or password";
    private static final String STATUS_TYPE_BATTERY = "battery";
    private static final String STATUS_TYPE_CONNECTION = "connection";

    // Timestamps without zone information, e.g. "2026-11-17 13:00:00"; hours, minutes and seconds may be unpadded
    private static final DateTimeFormatter SPACE_SEPARATED_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s");

    private final Logger logger = LoggerFactory.getLogger(GroheOndusApi.class);
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final Gson gson = new Gson();

    private volatile @Nullable String accessToken;

    public GroheOndusApi(HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
    }

    public boolean isAuthenticated() {
        return accessToken != null;
    }

    public void clearAuthentication() {
        accessToken = null;
    }

    /**
     * Obtains a new access token using a refresh token.
     *
     * @return the token response, containing a new refresh token
     * @throws GroheOndusAuthenticationException if the refresh token was rejected
     */
    public TokenResponse authenticateWithRefreshToken(String refreshToken) throws GroheOndusApiException {
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);
        Request request = httpClient.newRequest(API_URL + "oidc/refresh").method(HttpMethod.POST)
                .body(new StringRequestContent(CONTENT_TYPE_JSON, body.toString(), StandardCharsets.UTF_8));
        return acceptToken(sendForBody(request));
    }

    /**
     * Logs in through the web login form, which is the only way to authenticate with username and password.
     *
     * @return the token response, containing a refresh token for later logins
     * @throws GroheOndusAuthenticationException if the credentials were rejected
     */
    public TokenResponse authenticateWithCredentials(String username, String password) throws GroheOndusApiException {
        // The login form relies on a cookie session; stale cookies from an earlier login can break a new one
        httpClient.getHttpCookieStore().clear();

        String loginPage = sendForBody(httpClient.newRequest(API_URL + "oidc/login").method(HttpMethod.GET));
        String formAction = extractFormAction(loginPage);

        Fields fields = new Fields();
        fields.put("username", username);
        fields.put("password", password);
        ContentResponse response = execute(httpClient.newRequest(formAction).method(HttpMethod.POST)
                .followRedirects(false).headers(h -> h.put("X-Requested-With", "XMLHttpRequest")
                        .put(HttpHeader.REFERER, formAction).put(HttpHeader.ORIGIN, BASE_URL))
                .body(new FormRequestContent(fields)));

        // On success the form redirects to the app scheme; the same path served over https returns the tokens
        String location = response.getHeaders().get(HttpHeader.LOCATION);
        if (location == null || location.isBlank()) {
            if (response.getContentAsString().contains(WRONG_CREDENTIALS_MARKER)) {
                throw new GroheOndusAuthenticationException("Invalid username or password");
            }
            throw new GroheOndusCommunicationException(
                    "Unexpected login response with HTTP status " + response.getStatus(), response.getStatus());
        }
        String tokenUrl = location.replace("ondus://", "https://");
        return acceptToken(sendForBody(httpClient.newRequest(tokenUrl).method(HttpMethod.GET)));
    }

    public List<ApplianceInfo> getAppliances() throws GroheOndusApiException {
        DashboardResponse dashboard = parse(sendAuthorized(get(API_URL + "dashboard")), DashboardResponse.class);
        List<ApplianceInfo> result = new ArrayList<>();
        for (DashboardResponse.Location location : nonNull(dashboard.locations)) {
            for (DashboardResponse.Room room : nonNull(location.rooms)) {
                for (ApplianceResponse appliance : nonNull(room.appliances)) {
                    Integer locationId = location.id;
                    Integer roomId = room.id;
                    if (locationId != null && roomId != null && appliance.applianceId != null
                            && appliance.type != null) {
                        result.add(toApplianceInfo(new ApplianceRef(locationId, roomId, appliance.applianceId),
                                appliance));
                    }
                }
            }
        }
        return result;
    }

    /**
     * @throws GroheOndusNotFoundException if the appliance does not exist
     */
    public ApplianceInfo getAppliance(ApplianceRef ref) throws GroheOndusApiException {
        ApplianceResponse[] appliances = parse(sendAuthorized(get(applianceUrl(ref))), ApplianceResponse[].class);
        if (appliances.length == 0 || appliances[0] == null) {
            throw new GroheOndusNotFoundException("Appliance " + ref.applianceId() + " not found");
        }
        ApplianceResponse appliance = appliances[0];
        if (appliance.type == null) {
            throw new GroheOndusCommunicationException("Appliance response without type");
        }
        return toApplianceInfo(ref, appliance);
    }

    /**
     * @return the measurements in the given date range (both inclusive, in the API's local time), sorted by time
     */
    public List<SenseSample> getSenseSamples(ApplianceRef ref, LocalDate from, LocalDate to)
            throws GroheOndusApiException {
        ApplianceDataResponse.Data data = getApplianceData(ref, from, to);
        ZoneId zone = timeZoneProvider.getTimeZone();
        List<SenseSample> samples = new ArrayList<>();
        for (ApplianceDataResponse.Measurement measurement : nonNull(data.measurement)) {
            Instant timestamp = toTimestamp(measurement.date, zone);
            if (timestamp != null) {
                samples.add(new SenseSample(timestamp, measurement.temperature, measurement.humidity));
            }
        }
        samples.sort(Comparator.comparing(SenseSample::timestamp));
        return samples;
    }

    /**
     * @return the measurements and water withdrawals in the given date range, sorted by time
     */
    public GuardData getGuardData(ApplianceRef ref, LocalDate from, LocalDate to) throws GroheOndusApiException {
        ApplianceDataResponse.Data data = getApplianceData(ref, from, to);
        ZoneId zone = timeZoneProvider.getTimeZone();
        List<GuardSample> samples = new ArrayList<>();
        for (ApplianceDataResponse.Measurement measurement : nonNull(data.measurement)) {
            Instant timestamp = toTimestamp(measurement.date, zone);
            if (timestamp != null) {
                samples.add(new GuardSample(timestamp, measurement.flowrate, measurement.pressure,
                        measurement.temperatureGuard));
            }
        }
        samples.sort(Comparator.comparing(GuardSample::timestamp));

        List<WaterWithdrawal> withdrawals = new ArrayList<>();
        for (ApplianceDataResponse.Withdrawal withdrawal : nonNull(data.withdrawals)) {
            Instant timestamp = toTimestamp(withdrawal.date, zone);
            Double consumption = withdrawal.waterconsumption;
            if (timestamp != null && consumption != null) {
                withdrawals.add(new WaterWithdrawal(timestamp, consumption));
            }
        }
        withdrawals.sort(Comparator.comparing(WaterWithdrawal::timestamp));
        return new GuardData(samples, withdrawals);
    }

    public ApplianceStatus getStatus(ApplianceRef ref) throws GroheOndusApiException {
        ApplianceStatusEntry[] entries = parse(sendAuthorized(get(applianceUrl(ref) + "/status")),
                ApplianceStatusEntry[].class);
        Integer batteryLevel = null;
        Boolean connected = null;
        for (ApplianceStatusEntry entry : entries) {
            if (entry == null || entry.type == null) {
                continue;
            }
            Integer value = parseStatusValue(entry);
            if (STATUS_TYPE_BATTERY.equals(entry.type)) {
                batteryLevel = value;
            } else if (STATUS_TYPE_CONNECTION.equals(entry.type) && value != null) {
                connected = value != 0;
            }
        }
        return new ApplianceStatus(batteryLevel, connected);
    }

    private @Nullable Integer parseStatusValue(ApplianceStatusEntry entry) {
        String value = entry.value;
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            logger.debug("Unexpected value '{}' for status {}", value, entry.type);
            return null;
        }
    }

    /**
     * @return the valve state, or null if the appliance does not report it
     */
    public @Nullable Boolean isValveOpen(ApplianceRef ref) throws GroheOndusApiException {
        JsonObject command = getCommand(ref).getAsJsonObject("command");
        JsonElement valveOpen = command == null ? null : command.get("valve_open");
        return valveOpen == null || valveOpen.isJsonNull() ? null : valveOpen.getAsBoolean();
    }

    public void setValveOpen(ApplianceRef ref, boolean open) throws GroheOndusApiException {
        // The command endpoint expects the complete command object, so modify the current one
        JsonObject commandResponse = getCommand(ref);
        JsonObject command = commandResponse.getAsJsonObject("command");
        if (command == null) {
            throw new GroheOndusCommunicationException("Command response without command object");
        }
        command.addProperty("valve_open", open);
        sendAuthorized(httpClient.newRequest(applianceUrl(ref) + "/command").method(HttpMethod.POST)
                .body(new StringRequestContent(CONTENT_TYPE_JSON, commandResponse.toString(), StandardCharsets.UTF_8)));
    }

    /**
     * Pauses (snoozes) notifications of the appliance; a duration of 0 cancels an active pause.
     */
    public void setPause(ApplianceRef ref, int durationMinutes) throws GroheOndusApiException {
        String url = applianceUrl(ref) + "/snooze";
        if (durationMinutes == 0) {
            sendAuthorized(httpClient.newRequest(url).method(HttpMethod.DELETE));
        } else {
            JsonObject body = new JsonObject();
            body.addProperty("snooze_duration", durationMinutes);
            sendAuthorized(httpClient.newRequest(url).method(HttpMethod.PUT)
                    .body(new StringRequestContent(CONTENT_TYPE_JSON, body.toString(), StandardCharsets.UTF_8)));
        }
    }

    private JsonObject getCommand(ApplianceRef ref) throws GroheOndusApiException {
        return parse(sendAuthorized(get(applianceUrl(ref) + "/command")), JsonObject.class);
    }

    private ApplianceDataResponse.Data getApplianceData(ApplianceRef ref, LocalDate from, LocalDate to)
            throws GroheOndusApiException {
        String url = applianceUrl(ref) + "/data/aggregated?from=" + from + "&to=" + to;
        String body;
        try {
            body = sendAuthorized(get(url + "&groupBy=hour"));
        } catch (GroheOndusCommunicationException e) {
            if (e.getHttpStatus() != HttpStatus.BAD_REQUEST_400) {
                throw e;
            }
            // Only this request falls back; the rejection may depend on the requested range
            logger.debug("Hourly aggregation from {} to {} rejected for appliance {}, using daily values", from, to,
                    ref.applianceId());
            body = sendAuthorized(get(url));
        }
        ApplianceDataResponse response = parse(body, ApplianceDataResponse.class);
        ApplianceDataResponse.Data data = response.data;
        return data == null ? new ApplianceDataResponse.Data() : data;
    }

    private TokenResponse acceptToken(String json) throws GroheOndusApiException {
        TokenResponse token = parse(json, TokenResponse.class);
        String newAccessToken = token.accessToken;
        if (newAccessToken == null || newAccessToken.isBlank() || token.refreshToken == null
                || token.refreshToken.isBlank()) {
            throw new GroheOndusCommunicationException("Token response without access or refresh token");
        }
        accessToken = newAccessToken.trim();
        return token;
    }

    private Request get(String url) {
        return httpClient.newRequest(url).method(HttpMethod.GET);
    }

    private String sendAuthorized(Request request) throws GroheOndusApiException {
        String token = accessToken;
        if (token == null) {
            throw new GroheOndusAuthenticationException("Not authenticated");
        }
        return sendForBody(request.headers(h -> h.put(HttpHeader.AUTHORIZATION, "Bearer " + token)));
    }

    private String sendForBody(Request request) throws GroheOndusApiException {
        ContentResponse response = execute(request);
        int status = response.getStatus();
        if (status == HttpStatus.UNAUTHORIZED_401 || status == HttpStatus.FORBIDDEN_403) {
            throw new GroheOndusAuthenticationException("Request rejected with HTTP status " + status);
        }
        if (status == HttpStatus.NOT_FOUND_404) {
            throw new GroheOndusNotFoundException("Resource not found: " + request.getPath());
        }
        if (!HttpStatus.isSuccess(status)) {
            throw new GroheOndusCommunicationException(
                    "Request " + request.getPath() + " failed with HTTP status " + status, status);
        }
        String body = response.getContentAsString();
        String path = request.getPath();
        // Login and token responses contain credentials and must never be logged
        if (logger.isTraceEnabled() && path != null && !path.contains("/oidc/")) {
            logger.trace("Response for {}: {}", path, body);
        }
        return body;
    }

    private ContentResponse execute(Request request) throws GroheOndusCommunicationException {
        logger.trace("{} {}", request.getMethod(), request.getPath());
        try {
            return request.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GroheOndusCommunicationException("Request was interrupted", e);
        } catch (TimeoutException e) {
            throw new GroheOndusCommunicationException("Request to GROHE ONDUS timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new GroheOndusCommunicationException(
                    "Request to GROHE ONDUS failed: " + (cause != null ? cause.getMessage() : e.getMessage()), e);
        }
    }

    private <T> T parse(String json, Class<T> type) throws GroheOndusCommunicationException {
        try {
            @Nullable
            T result = gson.fromJson(json, type);
            if (result == null) {
                throw new GroheOndusCommunicationException("Empty response from GROHE ONDUS");
            }
            return result;
        } catch (JsonParseException e) {
            throw new GroheOndusCommunicationException("Malformed response from GROHE ONDUS", e);
        }
    }

    private ApplianceInfo toApplianceInfo(ApplianceRef ref, ApplianceResponse appliance) {
        ApplianceResponse.Config config = appliance.config;
        return new ApplianceInfo(ref, Objects.requireNonNullElse(appliance.name, ref.applianceId()),
                Objects.requireNonNull(appliance.type), config == null ? null : config.measurementTransmissionInterval,
                parseTimestamp(appliance.tdt, timeZoneProvider.getTimeZone()),
                decodeSerialNumber(appliance.serialNumber), appliance.version, appliance.installationDate,
                config == null ? null : config.measurementPeriod);
    }

    private static String applianceUrl(ApplianceRef ref) {
        return API_URL + "locations/" + ref.locationId() + "/rooms/" + ref.roomId() + "/appliances/"
                + URLEncoder.encode(ref.applianceId(), StandardCharsets.UTF_8);
    }

    private static <T> List<T> nonNull(@Nullable List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }

    private @Nullable Instant toTimestamp(@Nullable String date, ZoneId zone) {
        Instant timestamp = parseTimestamp(date, zone);
        if (timestamp == null) {
            logger.debug("Ignoring sample with unsupported timestamp '{}'", date);
        }
        return timestamp;
    }

    /**
     * The API reports the serial number hex-encoded, e.g. {@code 315a3136...} for {@code 1Z16...}.
     *
     * @return the decoded serial number, or the value unchanged if it is not hex-encoded printable ASCII
     */
    static @Nullable String decodeSerialNumber(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String text = value.trim();
        if (text.length() % 2 != 0 || !text.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            return text;
        }
        byte[] bytes = HexFormat.of().parseHex(text);
        for (byte b : bytes) {
            if (b < 0x20 || b > 0x7e) {
                return text;
            }
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    static String extractFormAction(String loginPage) throws GroheOndusCommunicationException {
        Matcher matcher = FORM_ACTION_PATTERN.matcher(loginPage);
        if (!matcher.find()) {
            throw new GroheOndusCommunicationException("Login form not found in GROHE ONDUS login page");
        }
        return unescapeHtml(matcher.group(1));
    }

    private static String unescapeHtml(String value) {
        return value.replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    /**
     * Parses an API timestamp. The API reports local time without zone information, either as date only (daily
     * aggregation) or as date and time; ISO timestamps with an offset are accepted as well.
     *
     * @return the timestamp, or null if it is missing or cannot be parsed
     */
    static @Nullable Instant parseTimestamp(@Nullable String value, ZoneId zone) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.length() == 10) {
                return LocalDate.parse(text).atStartOfDay(zone).toInstant();
            }
            if (text.indexOf('T') > 0) {
                TemporalAccessor parsed = DateTimeFormatter.ISO_DATE_TIME.parseBest(text, OffsetDateTime::from,
                        LocalDateTime::from);
                return parsed instanceof OffsetDateTime offsetDateTime ? offsetDateTime.toInstant()
                        : ((LocalDateTime) parsed).atZone(zone).toInstant();
            }
            return LocalDateTime.parse(text, SPACE_SEPARATED_DATE_TIME).atZone(zone).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
