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
package org.openhab.binding.myvaillant.internal.api;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.ObjIntConsumer;
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
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Client for the myVAILLANT cloud API (the API of the myVAILLANT app), ported from
 * <a href="https://github.com/signalkraft/myPyllant">myPyllant</a>, which the Home Assistant integration uses.
 *
 * <p>
 * Only systems with the control identifier {@code tli} (sensoCOMFORT / VRC720 with VR921 gateway) are supported.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class MyVaillantApi {

    /** Control identifier of systems with a sensoCOMFORT (VRC720), the only supported one */
    public static final String CONTROL_IDENTIFIER_TLI = "tli";

    private static final String IDENTITY_BASE_URL = "https://identity.vaillant-group.com";
    private static final String AUTH_BASE_URL = IDENTITY_BASE_URL + "/auth/realms/";
    private static final String ALTCHA_CHALLENGE_URL = IDENTITY_BASE_URL + "/api/altcha/challenge";
    private static final String API_BASE_URL = "https://api.vaillant-group.com/service-connected-control/end-user-app-api/v1";
    private static final String CLIENT_ID = "myvaillant";
    private static final String REDIRECT_URI = "enduservaillant.page.link://login";
    // Public key of the myVAILLANT app, also used by myPyllant
    private static final String SUBSCRIPTION_KEY = "1e0a2f3511fb4c5bbb1c7f9fedd20b1c";

    private static final int TIMEOUT_SECONDS = 30;
    private static final Duration TOKEN_REFRESH_MARGIN = Duration.ofMinutes(3);
    private static final Duration HOMES_CACHE_DURATION = Duration.ofHours(6);
    private static final Duration DEFAULT_QUOTA_PAUSE = Duration.ofHours(1);
    private static final Pattern QUOTA_DURATION = Pattern
            .compile("Quota will be replenished in (\\d{2}):(\\d{2}):(\\d{2})");
    private static final Pattern CODE_PARAMETER = Pattern.compile("[?&#]code=([^&#]+)");
    private static final Pattern LOGIN_ERROR = Pattern.compile("id=\"input-error[^\"]*\"[^>]*>([^<]*)<");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x?)([0-9a-fA-F]+);");
    private static final DateTimeFormatter API_DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter BUCKET_QUERY_DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);

    private final Logger logger = LoggerFactory.getLogger(MyVaillantApi.class);
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final String username;
    private final String password;
    private final String realm;
    private final ObjIntConsumer<LocalDate> requestCountListener;

    private final Object tokenLock = new Object();
    private @Nullable Token token;

    private volatile @Nullable Instant blockedUntil;
    private final Map<String, String> controlIdentifiers = new ConcurrentHashMap<>();
    private final Map<String, ZoneId> timeZones = new ConcurrentHashMap<>();
    private volatile @Nullable JsonArray homes;
    private volatile Instant homesFetched = Instant.MIN;

    private final Object requestCountLock = new Object();
    private LocalDate requestCountDate;
    private int requestCount;

    private record Token(String accessToken, @Nullable String refreshToken, Instant expires) {
    }

    /**
     * @param httpClient started HTTP client with its own cookie store, the login keeps cookies between requests
     * @param timeZoneProvider time zone of openHAB, the requests are counted per day in this zone
     * @param brand brand of the account, e.g. {@code vaillant}
     * @param country country of the account, e.g. {@code germany}, not used by every brand
     * @param requestsToday requests already sent today, e.g. before a restart, the count continues from there
     * @param requestCountListener receives the day and the number of requests sent on it after each request
     */
    public MyVaillantApi(HttpClient httpClient, TimeZoneProvider timeZoneProvider, String username, String password,
            String brand, @Nullable String country, int requestsToday, ObjIntConsumer<LocalDate> requestCountListener) {
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        this.requestCountDate = LocalDate.now(timeZoneProvider.getTimeZone());
        this.requestCount = requestsToday;
        this.username = username;
        this.password = password;
        this.realm = getRealm(brand, country);
        this.requestCountListener = requestCountListener;
    }

    /**
     * Vaillant and Saunier Duval use {@code brand-country-b2c} as realm, the other brands {@code brand-b2c}.
     */
    static String getRealm(String brand, @Nullable String country) {
        return switch (brand) {
            case "bulex", "glow-worm", "demirdokum" -> brand + "-b2c";
            default -> country == null || country.isBlank() ? brand + "-b2c" : brand + "-" + country + "-b2c";
        };
    }

    /** Time until which the exceeded call volume quota blocks all requests, {@code null} if it is not exceeded */
    public @Nullable Instant getBlockedUntil() {
        Instant until = blockedUntil;
        return until != null && until.isAfter(Instant.now()) ? until : null;
    }

    /**
     * Logs in with username and password (OpenID Connect authorization code flow with PKCE, the login form is posted
     * directly instead of showing it in a browser).
     */
    public void login() throws MyVaillantException, InterruptedException {
        synchronized (tokenLock) {
            token = null;
            String codeVerifier = StringUtils.getRandomAlphanumeric(128);
            String code = getAuthorizationCode(codeVerifier);
            Fields fields = new Fields();
            fields.put("grant_type", "authorization_code");
            fields.put("client_id", CLIENT_ID);
            fields.put("code", code);
            fields.put("code_verifier", codeVerifier);
            fields.put("redirect_uri", REDIRECT_URI);
            token = requestToken(fields);
            logger.debug("Logged in to myVAILLANT realm {}", realm);
        }
    }

    /** Logs in or renews the access token if needed */
    public void ensureLoggedIn() throws MyVaillantException, InterruptedException {
        getAccessToken(false);
    }

    private String getAuthorizationCode(String codeVerifier) throws MyVaillantException, InterruptedException {
        String authorizeUrl = AUTH_BASE_URL + realm + "/protocol/openid-connect/auth?response_type=code&client_id="
                + CLIENT_ID + "&code=code_challenge&redirect_uri=" + encode(REDIRECT_URI)
                + "&code_challenge_method=S256&code_challenge=" + codeChallenge(codeVerifier);
        ContentResponse loginPage = send(newRequest(HttpMethod.GET, authorizeUrl));
        // A still valid identity provider session redirects directly with a code
        String code = getCodeFromRedirect(loginPage);
        if (code != null) {
            return code;
        }
        if (loginPage.getStatus() == HttpStatus.NOT_FOUND_404) {
            throw new MyVaillantAuthenticationException("Realm " + realm + " does not exist, check brand and country");
        }
        if (loginPage.getStatus() != HttpStatus.OK_200) {
            throw new MyVaillantException("Login page returned HTTP " + loginPage.getStatus());
        }

        Matcher loginUrlMatcher = Pattern
                .compile(Pattern.quote(AUTH_BASE_URL + realm + "/login-actions/authenticate") + "\\?[^\"]*")
                .matcher(loginPage.getContentAsString());
        if (!loginUrlMatcher.find()) {
            throw new MyVaillantException("Could not find the login form on the login page");
        }
        String loginUrl = unescapeHtml(loginUrlMatcher.group());

        Fields form = new Fields();
        form.put("username", username);
        form.put("password", password);
        form.put("credentialId", "");
        String altcha = solveAltchaChallenge();
        if (altcha != null) {
            form.put("altcha", altcha);
        }
        ContentResponse loginResponse = send(newRequest(HttpMethod.POST, loginUrl).body(new FormRequestContent(form)));
        code = getCodeFromRedirect(loginResponse);
        if (code == null) {
            // Keycloak shows the login form again with an error below the input fields
            Matcher error = LOGIN_ERROR.matcher(loginResponse.getContentAsString());
            if (error.find()) {
                throw new MyVaillantAuthenticationException(
                        "Login failed: " + unescapeHtml(error.group(1).trim()) + " Check username and password.");
            }
            throw new MyVaillantException("Login failed, got HTTP " + loginResponse.getStatus() + " without a code");
        }
        return code;
    }

    /**
     * The login form only accepts the login with a solved ALTCHA proof-of-work challenge. Without a challenge (e.g. if
     * Vaillant removes it again) the login is tried without it.
     */
    private @Nullable String solveAltchaChallenge() throws InterruptedException {
        try {
            ContentResponse response = send(newRequest(HttpMethod.GET, ALTCHA_CHALLENGE_URL));
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("No ALTCHA challenge (HTTP {}), logging in without it", response.getStatus());
                return null;
            }
            long start = System.nanoTime();
            String solution = AltchaSolver
                    .solve(JsonUtil.requireObject(JsonParser.parseString(response.getContentAsString()), "ALTCHA"));
            logger.debug("Solved ALTCHA challenge in {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            return solution;
        } catch (MyVaillantException | JsonParseException e) {
            logger.debug("Could not solve the ALTCHA challenge, logging in without it: {}", e.getMessage());
            return null;
        }
    }

    private @Nullable String getCodeFromRedirect(ContentResponse response) {
        String location = response.getHeaders().get(HttpHeader.LOCATION);
        if (location == null) {
            return null;
        }
        Matcher matcher = CODE_PARAMETER.matcher(location);
        return matcher.find() ? URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8) : null;
    }

    private Token requestToken(Fields fields) throws MyVaillantException, InterruptedException {
        ContentResponse response = send(
                newRequest(HttpMethod.POST, AUTH_BASE_URL + realm + "/protocol/openid-connect/token")
                        .body(new FormRequestContent(fields)));
        if (response.getStatus() != HttpStatus.OK_200) {
            // The body is an OAuth error without credentials, e.g. {"error":"invalid_grant"}
            throw new MyVaillantException(
                    "Token request failed with HTTP " + response.getStatus() + ": " + response.getContentAsString());
        }
        try {
            JsonObject json = JsonUtil.requireObject(JsonParser.parseString(response.getContentAsString()), "token");
            Integer expiresIn = JsonUtil.getInteger(json, "expires_in");
            return new Token(JsonUtil.requireString(json, "access_token"), JsonUtil.getString(json, "refresh_token"),
                    Instant.now().plusSeconds(expiresIn == null ? 300 : expiresIn));
        } catch (JsonParseException e) {
            throw new MyVaillantException("Invalid token response", e);
        }
    }

    private String getAccessToken(boolean renew) throws MyVaillantException, InterruptedException {
        synchronized (tokenLock) {
            Token current = token;
            if (current != null && !renew && current.expires().minus(TOKEN_REFRESH_MARGIN).isAfter(Instant.now())) {
                return current.accessToken();
            }
            String refreshToken = current == null ? null : current.refreshToken();
            if (refreshToken != null) {
                Fields fields = new Fields();
                fields.put("grant_type", "refresh_token");
                fields.put("client_id", CLIENT_ID);
                fields.put("refresh_token", refreshToken);
                try {
                    Token refreshed = requestToken(fields);
                    token = refreshed;
                    return refreshed.accessToken();
                } catch (MyVaillantException e) {
                    logger.debug("Token refresh failed, logging in again: {}", e.getMessage());
                }
            }
            login();
            Token loggedIn = token;
            if (loggedIn == null) {
                throw new MyVaillantException("Login did not return a token");
            }
            return loggedIn.accessToken();
        }
    }

    private static String codeChallenge(String codeVerifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /**
     * Unescapes text of the login page. {@link StringUtils#unEscapeXml(String)} only knows the named entities, Keycloak
     * also writes numeric character references, e.g. {@code &#39;} or {@code &#61;}.
     */
    static String unescapeHtml(String value) {
        Matcher matcher = NUMERIC_ENTITY.matcher(Objects.requireNonNull(StringUtils.unEscapeXml(value)));
        StringBuilder decoded = new StringBuilder();
        while (matcher.find()) {
            int codePoint = Integer.parseInt(matcher.group(2), matcher.group(1).isEmpty() ? 10 : 16);
            matcher.appendReplacement(decoded, Matcher.quoteReplacement(Character.toString(codePoint)));
        }
        matcher.appendTail(decoded);
        return decoded.toString();
    }

    /** Homes (one per system) of the account, cached for some hours as they rarely change */
    public JsonArray getHomes() throws MyVaillantException, InterruptedException {
        JsonArray cached = homes;
        if (cached != null && homesFetched.plus(HOMES_CACHE_DURATION).isAfter(Instant.now())) {
            return cached;
        }
        JsonArray fetched = JsonUtil.requireArray(get(API_BASE_URL + "/homes"), "homes");
        homes = fetched;
        homesFetched = Instant.now();
        return fetched;
    }

    /** The home of a system from the cached homes, {@code null} if the account has no such system */
    public @Nullable JsonObject getHome(String systemId) throws MyVaillantException, InterruptedException {
        for (JsonElement home : getHomes()) {
            if (home.isJsonObject() && systemId.equals(JsonUtil.getString(home.getAsJsonObject(), "system_id"))) {
                return home.getAsJsonObject();
            }
        }
        return null;
    }

    /** Controller family of the system, e.g. {@code tli} or {@code vrc700}. Cached, as it never changes. */
    public String getControlIdentifier(String systemId) throws MyVaillantException, InterruptedException {
        String cached = controlIdentifiers.get(systemId);
        if (cached != null) {
            return cached;
        }
        JsonObject json = JsonUtil.requireObject(
                get(API_BASE_URL + "/systems/" + encode(systemId) + "/meta-info/control-identifier"),
                "control identifier");
        String controlIdentifier = JsonUtil.requireString(json, "control_identifier");
        controlIdentifiers.put(systemId, controlIdentifier);
        return controlIdentifier;
    }

    /** Time zone of the system (cached), the time zone of openHAB if the API reports an unknown one */
    public ZoneId getTimeZone(String systemId) throws MyVaillantException, InterruptedException {
        ZoneId cached = timeZones.get(systemId);
        if (cached != null) {
            return cached;
        }
        JsonObject json = JsonUtil.requireObject(
                get(API_BASE_URL + "/systems/" + encode(systemId) + "/meta-info/time-zone"), "time zone");
        ZoneId zone;
        try {
            zone = ZoneId.of(JsonUtil.requireString(json, "time_zone"));
        } catch (java.time.DateTimeException e) {
            logger.debug("Unknown time zone of system {}, using the time zone of openHAB: {}", systemId,
                    e.getMessage());
            zone = timeZoneProvider.getTimeZone();
        }
        timeZones.put(systemId, zone);
        return zone;
    }

    /** Configuration, state and properties of the system, its zones, circuits and hot water */
    public JsonObject getSystem(String systemId) throws MyVaillantException, InterruptedException {
        return JsonUtil.requireObject(get(systemUrl(systemId)), "system");
    }

    /** The devices of the system (heat generators, backup heater, ...) and the energy data they report */
    public JsonObject getCurrentSystem(String systemId) throws MyVaillantException, InterruptedException {
        return JsonUtil.requireObject(get(API_BASE_URL + "/emf/v2/" + encode(systemId) + "/currentSystem"),
                "current system");
    }

    /** Operation time and on/off cycles per device */
    public JsonObject getRts(String systemId) throws MyVaillantException, InterruptedException {
        return JsonUtil.requireObject(get(API_BASE_URL + "/rts/" + encode(systemId) + "/devices"), "rts");
    }

    /** Current power consumption per device */
    public JsonObject getMpc(String systemId) throws MyVaillantException, InterruptedException {
        return JsonUtil.requireObject(get(API_BASE_URL + "/hem/" + encode(systemId) + "/mpc"), "mpc");
    }

    /** Whether the gateway of the system is connected to the cloud, {@code null} if the API does not tell */
    public @Nullable Boolean getConnectionStatus(String systemId) throws MyVaillantException, InterruptedException {
        JsonElement json = get(API_BASE_URL + "/systems/" + encode(systemId) + "/meta-info/connection-status");
        return json.isJsonObject() ? JsonUtil.getBoolean(json.getAsJsonObject(), "connected") : null;
    }

    /** Diagnostic trouble codes, one entry per device with its {@code codes} */
    public JsonArray getDiagnosticTroubleCodes(String systemId) throws MyVaillantException, InterruptedException {
        return JsonUtil.requireArray(get(API_BASE_URL + "/systems/" + encode(systemId) + "/diagnostic-trouble-codes"),
                "diagnostic trouble codes");
    }

    /**
     * Energy values of one data type of a device.
     *
     * @param resolution {@code HOUR}, {@code DAY} or {@code MONTH}
     * @param operationMode e.g. {@code HEATING}
     * @param valueType e.g. {@code CONSUMED_ELECTRICAL_ENERGY}
     */
    public JsonObject getDeviceBuckets(String systemId, String deviceUuid, String resolution, String operationMode,
            String valueType, ZonedDateTime from, ZonedDateTime to) throws MyVaillantException, InterruptedException {
        String url = API_BASE_URL + "/emf/v2/" + encode(systemId) + "/devices/" + encode(deviceUuid)
                + "/buckets?resolution=" + encode(resolution) + "&operationMode=" + encode(operationMode)
                + "&energyType=" + encode(valueType) + "&startDate=" + encode(from.format(BUCKET_QUERY_DATE_TIME))
                + "&endDate=" + encode(to.format(BUCKET_QUERY_DATE_TIME));
        return JsonUtil.requireObject(get(url), "device buckets");
    }

    /** @param mode {@code MANUAL}, {@code TIME_CONTROLLED} or {@code OFF} */
    public void setZoneHeatingOperationMode(String systemId, int zone, String mode)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("operationMode", mode);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/heating-operation-mode", body);
    }

    /** @param mode {@code MANUAL}, {@code TIME_CONTROLLED} or {@code OFF} */
    public void setZoneCoolingOperationMode(String systemId, int zone, String mode)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("operationMode", mode);
        body.addProperty("type", "COOLING");
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/operation-mode", body);
    }

    /** @param type {@code HEATING} or {@code COOLING} */
    public void setZoneManualModeSetpoint(String systemId, int zone, double setpoint, String type)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("setpoint", setpoint);
        body.addProperty("type", type);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/manual-mode-setpoint", body);
    }

    /** Sets the target temperature for cooling in °C */
    public void setZoneCoolingSetpoint(String systemId, int zone, double setpoint)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("setpoint", setpoint);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/setpoint-cooling", body);
    }

    /** Sets the temperature in °C the zone is heated to outside of the time windows of its time program */
    public void setZoneSetBackTemperature(String systemId, int zone, double temperature)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("setBackTemperature", temperature);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/set-back-temperature", body);
    }

    /** Overrides the target temperature of the zone (in °C) for the given duration */
    public void startZoneQuickVeto(String systemId, int zone, double temperature, double durationHours)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("desiredRoomTemperatureSetpoint", temperature);
        body.addProperty("duration", durationHours);
        send(HttpMethod.POST, systemUrl(systemId) + "/zones/" + zone + "/quick-veto", body);
    }

    /** Changes a running quick veto, a {@code null} value stays unchanged */
    public void updateZoneQuickVeto(String systemId, int zone, @Nullable Double temperature,
            @Nullable Double durationHours) throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        if (temperature != null) {
            body.addProperty("desiredRoomTemperatureSetpoint", temperature);
        }
        if (durationHours != null) {
            body.addProperty("duration", durationHours);
        }
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/quick-veto", body);
    }

    /** Ends the quick veto of the zone */
    public void cancelZoneQuickVeto(String systemId, int zone) throws MyVaillantException, InterruptedException {
        send(HttpMethod.DELETE, systemUrl(systemId) + "/zones/" + zone + "/quick-veto", null);
    }

    /**
     * @param timeProgram time program in the format of the API (weekdays with {@code startTime}, {@code endTime} and
     *            {@code setpoint}) and {@code type} ({@code heating} or {@code cooling})
     */
    public void setZoneTimeProgram(String systemId, int zone, JsonObject timeProgram)
            throws MyVaillantException, InterruptedException {
        send(HttpMethod.PATCH, systemUrl(systemId) + "/zones/" + zone + "/time-windows", timeProgram);
    }

    /** Sets away mode (holidays) for all zones of the system */
    public void setHoliday(String systemId, Instant start, Instant end)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("startDateTime", API_DATE_TIME.format(start));
        body.addProperty("endDateTime", API_DATE_TIME.format(end));
        send(HttpMethod.POST, systemUrl(systemId) + "/away-mode", body);
    }

    /** Ends away mode */
    public void cancelHoliday(String systemId) throws MyVaillantException, InterruptedException {
        send(HttpMethod.DELETE, systemUrl(systemId) + "/away-mode", null);
    }

    /** Cools manually between start and end */
    public void setCoolingForDays(String systemId, Instant start, Instant end)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("startDateTime", API_DATE_TIME.format(start));
        body.addProperty("endDateTime", API_DATE_TIME.format(end));
        send(HttpMethod.POST, systemUrl(systemId) + "/cooling-for-days", body);
    }

    /** Ends manual cooling */
    public void cancelCoolingForDays(String systemId) throws MyVaillantException, InterruptedException {
        send(HttpMethod.DELETE, systemUrl(systemId) + "/cooling-for-days", null);
    }

    /** Starts the ventilation boost of the system */
    public void setVentilationBoost(String systemId) throws MyVaillantException, InterruptedException {
        send(HttpMethod.POST, systemUrl(systemId) + "/ventilation-boost", new JsonObject());
    }

    /** Ends the ventilation boost */
    public void cancelVentilationBoost(String systemId) throws MyVaillantException, InterruptedException {
        send(HttpMethod.DELETE, systemUrl(systemId) + "/ventilation-boost", null);
    }

    /** Sets the slope of the heating curve */
    public void setCircuitHeatingCurve(String systemId, int circuit, double heatingCurve)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("heatingCurve", heatingCurve);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/circuit/" + circuit + "/heating-curve", body);
    }

    /** Sets the outdoor temperature in °C above which the circuit has no heat demand */
    public void setCircuitHeatDemandLimitedByOutsideTemperature(String systemId, int circuit, double temperature)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("heatDemandLimitedByOutsideTemperature", temperature);
        send(HttpMethod.POST,
                systemUrl(systemId) + "/circuit/" + circuit + "/heat-demand-limited-by-outside-temperature", body);
    }

    /** Sets the minimum flow temperature of the circuit in °C */
    public void setCircuitMinFlowTemperatureSetpoint(String systemId, int circuit, double temperature)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("minFlowTemperatureSetpoint", temperature);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/circuit/" + circuit + "/min-flow-temperature-setpoint", body);
    }

    /** The API only accepts whole degrees */
    public void setDhwTemperature(String systemId, int dhw, int setpoint)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("setpoint", setpoint);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/temperature", body);
    }

    /** @param mode {@code MANUAL}, {@code TIME_CONTROLLED} or {@code OFF} */
    public void setDhwOperationMode(String systemId, int dhw, String mode)
            throws MyVaillantException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("operationMode", mode);
        send(HttpMethod.PATCH, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/operation-mode", body);
    }

    /** Heats the cylinder once now, independent of operation mode and time program */
    public void startDhwBoost(String systemId, int dhw) throws MyVaillantException, InterruptedException {
        send(HttpMethod.POST, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/boost", new JsonObject());
    }

    /** Ends a running boost */
    public void cancelDhwBoost(String systemId, int dhw) throws MyVaillantException, InterruptedException {
        send(HttpMethod.DELETE, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/boost", null);
    }

    /**
     * @param timeProgram time program in the format of the API (weekdays with {@code startTime} and {@code endTime})
     */
    public void setDhwTimeProgram(String systemId, int dhw, JsonObject timeProgram)
            throws MyVaillantException, InterruptedException {
        send(HttpMethod.PATCH, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/time-windows", timeProgram);
    }

    /**
     * @param timeProgram time program in the format of the API (weekdays with {@code startTime} and {@code endTime})
     */
    public void setDhwCirculationTimeProgram(String systemId, int dhw, JsonObject timeProgram)
            throws MyVaillantException, InterruptedException {
        send(HttpMethod.PATCH, systemUrl(systemId) + "/domestic-hot-water/" + dhw + "/circulation-pump-time-windows",
                timeProgram);
    }

    private static String systemUrl(String systemId) {
        return API_BASE_URL + "/systems/" + encode(systemId) + "/" + CONTROL_IDENTIFIER_TLI;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** GET with keys converted to snake_case */
    private JsonElement get(String url) throws MyVaillantException, InterruptedException {
        return JsonUtil.toSnakeCase(send(HttpMethod.GET, url, null));
    }

    private JsonElement send(HttpMethod method, String url, @Nullable JsonObject body)
            throws MyVaillantException, InterruptedException {
        Instant until = getBlockedUntil();
        if (until != null) {
            throw new MyVaillantQuotaExceededException(until);
        }
        ContentResponse response = send(authorizedRequest(method, url, body, getAccessToken(false)));
        if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            logger.debug("Access token rejected, renewing it");
            response = send(authorizedRequest(method, url, body, getAccessToken(true)));
        }

        int status = response.getStatus();
        String content = response.getContentAsString();
        String path = url.substring(API_BASE_URL.length()).replaceFirst("\\?.*", "");
        if (status == HttpStatus.FORBIDDEN_403 || status == HttpStatus.TOO_MANY_REQUESTS_429) {
            // The quota is only reported as text, e.g. "Out of call volume quota. Quota will be replenished in
            // 00:29:12."
            String lowerContent = content.toLowerCase(Locale.ROOT);
            if (lowerContent.contains("quota exceeded") || lowerContent.contains("out of call volume quota")) {
                Instant blocked = Instant.now().plus(getQuotaPause(content, response.getHeaders().get("Retry-After")));
                blockedUntil = blocked;
                throw new MyVaillantQuotaExceededException(blocked);
            }
        }
        if (status < 200 || status >= 300) {
            throw new MyVaillantApiException(method.asString(), path, status, content);
        }
        if (content.isBlank()) {
            return JsonNull.INSTANCE;
        }
        try {
            return JsonParser.parseString(content);
        } catch (JsonParseException e) {
            throw new MyVaillantException("Invalid JSON from " + path, e);
        }
    }

    static Duration getQuotaPause(String content, @Nullable String retryAfter) {
        if (retryAfter != null) {
            try {
                return Duration.ofSeconds(Long.parseLong(retryAfter.trim()));
            } catch (NumberFormatException e) {
                // Retry-After can also be an HTTP date, fall back to the message
            }
        }
        Matcher matcher = QUOTA_DURATION.matcher(content);
        if (matcher.find()) {
            return Duration.ofHours(Long.parseLong(matcher.group(1))).plusMinutes(Long.parseLong(matcher.group(2)))
                    .plusSeconds(Long.parseLong(matcher.group(3)) + 10);
        }
        return DEFAULT_QUOTA_PAUSE;
    }

    /**
     * A request with the settings of all requests: an explicit timeout, and no redirects, as the login reads the code
     * from the redirect URL.
     */
    private Request newRequest(HttpMethod method, String url) {
        return httpClient.newRequest(url).method(method).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .followRedirects(false);
    }

    private Request authorizedRequest(HttpMethod method, String url, @Nullable JsonObject body, String accessToken) {
        Request request = newRequest(method, url) //
                .headers(headers -> headers.put(HttpHeader.AUTHORIZATION, "Bearer " + accessToken) //
                        .put("x-app-identifier", "VAILLANT") //
                        .put(HttpHeader.ACCEPT_LANGUAGE, "en-GB") //
                        .put(HttpHeader.ACCEPT, "application/json, text/plain, */*") //
                        .put("x-client-locale", "en-GB") //
                        .put("x-idm-identifier", "KEYCLOAK") //
                        .put("ocp-apim-subscription-key", SUBSCRIPTION_KEY)) //
                .agent("okhttp/4.9.2");
        if (body != null) {
            request.body(new StringRequestContent("application/json", body.toString(), StandardCharsets.UTF_8));
        }
        return request;
    }

    private ContentResponse send(Request request) throws MyVaillantException, InterruptedException {
        countRequest();
        try {
            return request.send();
        } catch (TimeoutException | ExecutionException e) {
            throw new MyVaillantException(
                    "Request to " + request.getHost() + request.getPath() + " failed: " + e.getMessage(), e);
        }
    }

    private void countRequest() {
        int count;
        LocalDate today = LocalDate.now(timeZoneProvider.getTimeZone());
        synchronized (requestCountLock) {
            if (!today.equals(requestCountDate)) {
                requestCountDate = today;
                requestCount = 0;
            }
            count = ++requestCount;
        }
        requestCountListener.accept(today, count);
    }
}
