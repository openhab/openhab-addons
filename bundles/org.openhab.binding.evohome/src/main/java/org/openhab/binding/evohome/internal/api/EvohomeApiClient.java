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
package org.openhab.binding.evohome.internal.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.evohome.internal.api.models.v2.dto.request.HeatSetPoint;
import org.openhab.binding.evohome.internal.api.models.v2.dto.request.HeatSetPointBuilder;
import org.openhab.binding.evohome.internal.api.models.v2.dto.request.Mode;
import org.openhab.binding.evohome.internal.api.models.v2.dto.request.ModeBuilder;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Authentication;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.DomesticHotWater;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Gateway;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.HeatSetpointCapabilities;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Location;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.LocationStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Locations;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.LocationsStatus;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TemperatureControlSystem;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.TimeZone;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.UserAccount;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Zone;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneSchedule;
import org.openhab.binding.evohome.internal.configuration.EvohomeAccountConfiguration;

/**
 * Implementation of the evohome client V2 api
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - Authentication recovery and reliable state retrieval
 * @author Kovacs Istvan - Capability-aware overrides and controller reset
 * @author Kovacs Istvan - Temporary controller modes
 * @author Kovacs Istvan - Validated weekly program cache
 *
 */
@NonNullByDefault
public class EvohomeApiClient {

    private static final String APPLICATION_ID = "b013aa26-9724-4dbd-8897-048b9aada249";
    private static final String CLIENT_ID = "4a231089-d2b6-41bd-a5eb-16a0a422b999";
    private static final String CLIENT_SECRET = "1a15cdb8-42de-407b-add0-059f92c530cb";
    private static final Set<String> SYSTEM_MODES = Set.of("Auto", "AutoWithEco", "Away", "Custom", "DayOff",
            "HeatingOff");
    private static final Set<String> TEMPORARY_SYSTEM_MODES = Set.of("AutoWithEco", "Away", "Custom", "DayOff");
    private static final Pattern API_DURATION = Pattern
            .compile("(?:(\\d+)\\.)?(\\d{1,2}):(\\d{2}):(\\d{2})(?:\\.(\\d{1,9}))?");

    private final EvohomeAccountConfiguration configuration;
    private final ApiAccess apiAccess;

    private volatile Locations locations = new Locations();
    private @Nullable UserAccount useraccount;
    private volatile @Nullable LocationsStatus locationsStatus;
    private volatile boolean loggedIn;
    private volatile boolean closed;
    private final Map<String, CachedSchedule> zoneSchedules = new ConcurrentHashMap<>();
    private final Map<String, Instant> zoneScheduleRetryAfter = new ConcurrentHashMap<>();

    public record CachedSchedule(ZoneSchedule schedule, Instant updatedAt, @Nullable ZoneId timeZone) {
    }

    /**
     * Creates a new API client based on the V2 API interface
     *
     * @param configuration The configuration of the account to use
     */
    public EvohomeApiClient(EvohomeAccountConfiguration configuration, HttpClient httpClient) {
        this.configuration = configuration;
        apiAccess = new ApiAccess(httpClient);
        apiAccess.setApplicationId(APPLICATION_ID);
    }

    /**
     * Closes the current connection to the API
     */
    public void close() {
        // Do not wait for a network operation during handler disposal.
        closed = true;
        loggedIn = false;
        zoneSchedules.clear();
        zoneScheduleRetryAfter.clear();
    }

    public synchronized void login() throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        loggedIn = false;
        zoneSchedules.clear();
        zoneScheduleRetryAfter.clear();
        updateAuthentication();
        useraccount = requestUserAccount();
        locations = requestLocations();
        checkOpen();
        loggedIn = true;
    }

    public boolean isLoggedIn() {
        return loggedIn && !closed;
    }

    public void logout() {
        close();
    }

    public synchronized void update() throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        if (!loggedIn) {
            throw new EvohomeApiClientException("Installation has not been loaded");
        }
        LocationsStatus status = requestLocationsStatus();
        checkOpen();
        locationsStatus = status;
    }

    public Locations getInstallationInfo() {
        return locations;
    }

    public @Nullable LocationsStatus getInstallationStatus() {
        return locationsStatus;
    }

    public synchronized void setTcsMode(String tcsId, String mode) throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        org.openhab.binding.evohome.internal.api.models.v2.dto.response.Mode capability = systemModeCapability(tcsId,
                mode);
        if (capability != null && Boolean.FALSE.equals(capability.getCanBePermanent())) {
            throw new IllegalArgumentException("This system mode cannot be set permanently");
        }
        String url = String.format(EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_MODE, tcsId);
        Mode modeCommand = new ModeBuilder().setMode(mode).build();
        put(url, modeCommand);
    }

    public synchronized void setTemporaryTcsMode(String tcsId, String mode, Instant until)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        org.openhab.binding.evohome.internal.api.models.v2.dto.response.Mode capability = systemModeCapability(tcsId,
                mode);
        Boolean canBeTemporary = capability == null ? null : capability.getCanBeTemporary();
        if (Boolean.FALSE.equals(canBeTemporary) || canBeTemporary == null && !TEMPORARY_SYSTEM_MODES.contains(mode)) {
            throw new IllegalArgumentException("This system mode cannot be set temporarily");
        }
        until = until.truncatedTo(ChronoUnit.SECONDS);
        Instant now = Instant.now();
        if (!until.isAfter(now)) {
            throw new IllegalArgumentException("The system mode expiry must be in the future");
        }
        String limit = capability == null ? null : capability.getMaxDuration();
        if (limit != null && !limit.isBlank()) {
            Duration maxDuration = parseDuration(limit);
            if (maxDuration != null && Duration.between(now, until).compareTo(maxDuration) > 0) {
                throw new IllegalArgumentException("The system mode exceeds the controller's maximum duration");
            }
        }
        String url = String.format(EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_MODE, tcsId);
        put(url, new ModeBuilder().setTemporaryMode(mode, until).build());
    }

    private org.openhab.binding.evohome.internal.api.models.v2.dto.response.@Nullable Mode systemModeCapability(
            String tcsId, String mode) {
        TemperatureControlSystem system = findTemperatureControlSystem(tcsId);
        if (system != null && !system.getAllowedSystemModes().isEmpty()) {
            return system.getAllowedSystemModes().stream().filter(entry -> mode.equals(entry.getSystemMode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("System mode is not supported by this controller"));
        }
        if (!SYSTEM_MODES.contains(mode)) {
            throw new IllegalArgumentException("Unknown or unadvertised system mode");
        }
        return null;
    }

    public synchronized void setHeatingZoneOverride(String zoneId, double setPoint)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        setPoint = validateTemperature(zoneId, setPoint, "PermanentOverride");
        HeatSetPoint setPointCommand = new HeatSetPointBuilder().setSetPoint(setPoint).build();
        setHeatingZoneOverride(zoneId, setPointCommand);
    }

    public synchronized void cancelHeatingZoneOverride(String zoneId)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        validateZoneMode(zoneId, "FollowSchedule");
        HeatSetPoint setPointCommand = new HeatSetPointBuilder().setCancelSetPoint().build();
        setHeatingZoneOverride(zoneId, setPointCommand);
    }

    public synchronized void setHeatingZoneTemporaryOverride(String zoneId, double setPoint, Instant until)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        setPoint = validateTemperature(zoneId, setPoint, "TemporaryOverride");
        until = until.truncatedTo(ChronoUnit.SECONDS);
        Instant now = Instant.now();
        if (!until.isAfter(now)) {
            throw new IllegalArgumentException("The override expiry must be in the future");
        }
        Zone zone = findZone(zoneId);
        HeatSetpointCapabilities capabilities = zone == null ? null : zone.getHeatSetpointCapabilities();
        String limit = capabilities == null ? null : capabilities.getMaxDuration();
        if (limit != null && !limit.isBlank()) {
            Duration maxDuration = parseDuration(limit);
            if (maxDuration != null && Duration.between(now, until).compareTo(maxDuration) > 0) {
                throw new IllegalArgumentException("The override exceeds the zone's maximum duration");
            }
        }
        HeatSetPoint command = new HeatSetPointBuilder().setTemporarySetPoint(setPoint, until).build();
        setHeatingZoneOverride(zoneId, command);
    }

    public synchronized Instant setHeatingZoneTemporaryOverrideUntilNextSwitchpoint(String zoneId, double setPoint)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        validateTemperature(zoneId, setPoint, "TemporaryOverride");
        Instant until = getNextSwitchpoint(zoneId);
        setHeatingZoneTemporaryOverride(zoneId, setPoint, until);
        return until;
    }

    public synchronized Instant getNextSwitchpoint(String zoneId) throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        Location location = findZoneLocation(zoneId);
        if (location == null) {
            throw new IllegalArgumentException("The zone is not present in the installation");
        }
        ZoneId timeZone = locationTimeZone(location);
        String url = String.format(EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_SCHEDULE,
                "temperatureZone", zoneId);
        ZoneSchedule schedule = get(url, ZoneSchedule.class);
        try {
            return schedule.nextSwitchpoint(Instant.now(), timeZone);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("The zone schedule has an invalid switchpoint time", e);
        }
    }

    public synchronized CachedSchedule readZoneSchedule(String zoneId)
            throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        Location location = findZoneLocation(zoneId);
        if (location == null) {
            throw new IllegalArgumentException("The zone is not present in the installation");
        }
        String url = String.format(EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_SCHEDULE,
                "temperatureZone", zoneId);
        ZoneSchedule schedule = get(url, ZoneSchedule.class);
        checkOpen();
        try {
            schedule.validate();
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("The zone schedule has an invalid switchpoint time", e);
        }
        ZoneId timeZone = null;
        try {
            timeZone = locationTimeZone(location);
        } catch (IllegalArgumentException e) {
            // The local weekly program remains readable without an absolute schedule time zone.
        }
        CachedSchedule cached = new CachedSchedule(schedule, Instant.now(), timeZone);
        checkOpen();
        zoneSchedules.put(zoneId, cached);
        zoneScheduleRetryAfter.remove(zoneId);
        if (closed) {
            zoneSchedules.remove(zoneId, cached);
            checkOpen();
        }
        return cached;
    }

    public @Nullable CachedSchedule getCachedZoneSchedule(String zoneId) {
        return closed ? null : zoneSchedules.get(zoneId);
    }

    public boolean isZoneScheduleDue(String zoneId, Instant now, int intervalMinutes) {
        if (closed) {
            return false;
        }
        Instant retryAfter = zoneScheduleRetryAfter.get(zoneId);
        if (retryAfter != null && now.isBefore(retryAfter)) {
            return false;
        }
        CachedSchedule cached = zoneSchedules.get(zoneId);
        return cached == null || !now.isBefore(cached.updatedAt().plus(intervalMinutes, ChronoUnit.MINUTES));
    }

    public void deferZoneScheduleRefresh(String zoneId, long seconds) {
        if (!closed) {
            Instant retryAfter;
            try {
                retryAfter = Instant.now().plusSeconds(Math.max(0, seconds));
            } catch (DateTimeException | ArithmeticException e) {
                retryAfter = Instant.MAX;
            }
            zoneScheduleRetryAfter.put(zoneId, retryAfter);
        }
    }

    public synchronized void resetTcs(String tcsId) throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        TemperatureControlSystem system = findTemperatureControlSystem(tcsId);
        if (system == null) {
            throw new IllegalArgumentException("The controller is not present in the installation");
        }
        if (system.getAllowedSystemModes().stream().anyMatch(mode -> "AutoWithReset".equals(mode.getSystemMode())
                && !Boolean.FALSE.equals(mode.getCanBePermanent()))) {
            setTcsMode(tcsId, "AutoWithReset");
            return;
        }
        for (Zone zone : system.getZones()) {
            validateZoneMode(zone.getZoneId(), "FollowSchedule");
        }
        setTcsMode(tcsId, "Auto");
        for (Zone zone : system.getZones()) {
            cancelHeatingZoneOverride(zone.getZoneId());
        }
        DomesticHotWater dhw = system.getDhw();
        if (dhw != null && dhw.getDhwId() != null && !dhw.getDhwId().isBlank()) {
            String url = String.format(EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_HOT_WATER,
                    dhw.getDhwId());
            put(url, Map.of("mode", "FollowSchedule"));
        }
    }

    public @Nullable Zone findZone(String zoneId) {
        for (Location location : locations) {
            for (Gateway gateway : location.getGateways()) {
                for (TemperatureControlSystem system : gateway.getTemperatureControlSystems()) {
                    for (Zone zone : system.getZones()) {
                        if (zoneId.equals(zone.getZoneId())) {
                            return zone;
                        }
                    }
                }
            }
        }
        return null;
    }

    public @Nullable TemperatureControlSystem findTemperatureControlSystem(String tcsId) {
        for (Location location : locations) {
            for (Gateway gateway : location.getGateways()) {
                for (TemperatureControlSystem system : gateway.getTemperatureControlSystems()) {
                    if (tcsId.equals(system.getSystemId())) {
                        return system;
                    }
                }
            }
        }
        return null;
    }

    private @Nullable Location findZoneLocation(String zoneId) {
        for (Location location : locations) {
            for (Gateway gateway : location.getGateways()) {
                for (TemperatureControlSystem system : gateway.getTemperatureControlSystems()) {
                    if (system.getZones().stream().anyMatch(zone -> zoneId.equals(zone.getZoneId()))) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    private ZoneId locationTimeZone(Location location) {
        TimeZone timeZone = location.getLocationInfo().getTimeZone();
        String id = timeZone == null ? null : timeZone.getTimeZoneId();
        if (timeZone == null || id == null) {
            throw new IllegalArgumentException("The location has no schedule time zone");
        }
        if (Boolean.FALSE.equals(location.getLocationInfo().getUseDaylightSaveSwitching())) {
            Integer offsetMinutes = timeZone.getOffsetMinutes();
            if (offsetMinutes != null) {
                try {
                    return ZoneOffset.ofTotalSeconds(Math.multiplyExact(offsetMinutes, 60));
                } catch (DateTimeException | ArithmeticException e) {
                    throw new IllegalArgumentException("The location has an invalid standard UTC offset", e);
                }
            }
        }
        // TCC also returns Windows IDs without spaces or periods. Preserve IANA IDs in the fallback.
        // CLDR windowsZones.xml global mappings preserve regional DST rules, unlike currentOffsetMinutes.
        String mapped = switch (id.replace(" ", "").replace(".", "")) {
            case "UTC" -> "Etc/UTC";
            case "GMTStandardTime" -> "Europe/London";
            case "GreenwichStandardTime" -> "Atlantic/Reykjavik";
            case "WEuropeStandardTime" -> "Europe/Berlin";
            case "CentralEuropeStandardTime" -> "Europe/Budapest";
            case "RomanceStandardTime" -> "Europe/Paris";
            case "CentralEuropeanStandardTime" -> "Europe/Warsaw";
            case "WCentralAfricaStandardTime" -> "Africa/Lagos";
            case "FLEStandardTime" -> "Europe/Kiev";
            case "GTBStandardTime" -> "Europe/Bucharest";
            case "EEuropeStandardTime" -> "Europe/Chisinau";
            case "TurkeyStandardTime" -> "Europe/Istanbul";
            case "RussianStandardTime" -> "Europe/Moscow";
            case "SouthAfricaStandardTime" -> "Africa/Johannesburg";
            case "ArabStandardTime" -> "Asia/Riyadh";
            case "ArabianStandardTime" -> "Asia/Dubai";
            default -> id;
        };
        try {
            return ZoneId.of(mapped);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Unsupported location schedule time zone: " + id, e);
        }
    }

    private @Nullable HeatSetpointCapabilities validateZoneMode(String zoneId, String mode) {
        Zone zone = findZone(zoneId);
        HeatSetpointCapabilities capabilities = zone == null ? null : zone.getHeatSetpointCapabilities();
        if (capabilities != null) {
            List<String> modes = capabilities.getAllowedSetpointModes();
            if (!modes.isEmpty() && !modes.contains(mode)) {
                throw new IllegalArgumentException("Setpoint mode is not supported by this zone");
            }
        }
        return capabilities;
    }

    private double validateTemperature(String zoneId, double setPoint, String mode) {
        HeatSetpointCapabilities capabilities = validateZoneMode(zoneId, mode);
        Double minimum = capabilities == null ? null : capabilities.getMinHeatSetpoint();
        Double maximum = capabilities == null ? null : capabilities.getMaxHeatSetpoint();
        Double valueResolution = capabilities == null ? null : capabilities.getValueResolution();
        double min = minimum == null ? 5 : minimum;
        double max = maximum == null ? 35 : maximum;
        double resolution = valueResolution == null || !Double.isFinite(valueResolution) || valueResolution <= 0 ? 0.5
                : valueResolution;
        if (!Double.isFinite(setPoint) || !Double.isFinite(min) || !Double.isFinite(max) || min > max || setPoint < min
                || setPoint > max) {
            throw new IllegalArgumentException("The setpoint must be within the zone's temperature limits");
        }
        double steps = Math.min(Math.round((setPoint - min) / resolution), Math.floor((max - min) / resolution));
        return min + steps * resolution;
    }

    private @Nullable Duration parseDuration(String value) {
        Matcher matcher = API_DURATION.matcher(value);
        if (!matcher.matches()) {
            return null;
        }
        try {
            long days = matcher.group(1) == null ? 0 : Long.parseLong(matcher.group(1));
            Duration duration = Duration.ofDays(days).plusHours(Long.parseLong(matcher.group(2)))
                    .plusMinutes(Long.parseLong(matcher.group(3))).plusSeconds(Long.parseLong(matcher.group(4)));
            String fraction = matcher.group(5);
            return fraction == null ? duration
                    : duration.plusNanos(Long.parseLong((fraction + "000000000").substring(0, 9)));
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
    }

    private void setHeatingZoneOverride(String zoneId, HeatSetPoint heatSetPoint)
            throws TimeoutException, EvohomeApiClientException {
        String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_HEAT_SETPOINT;
        url = String.format(url, zoneId);
        put(url, heatSetPoint);
    }

    private UserAccount requestUserAccount() throws TimeoutException, EvohomeApiClientException {
        String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_ACCOUNT;
        UserAccount account = get(url, UserAccount.class);
        if (account.getUserId() == null || account.getUserId().isBlank()) {
            throw new EvohomeApiClientException("Missing user account ID");
        }
        return account;
    }

    private Locations requestLocations() throws TimeoutException, EvohomeApiClientException {
        UserAccount localAccount = useraccount;
        if (localAccount != null) {
            String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_INSTALLATION_INFO;
            url = String.format(url, localAccount.getUserId());

            return get(url, Locations.class);
        }
        throw new EvohomeApiClientException("Missing user account");
    }

    private LocationsStatus requestLocationsStatus() throws TimeoutException, EvohomeApiClientException {
        LocationsStatus locationsStatus = new LocationsStatus();

        for (Location location : locations) {
            String url = EvohomeApiConstants.URL_V2_BASE + EvohomeApiConstants.URL_V2_LOCATION_STATUS;
            url = String.format(url, location.getLocationInfo().getLocationId());
            LocationStatus status = get(url, LocationStatus.class);
            locationsStatus.add(status);
        }

        return locationsStatus;
    }

    private void authenticate(String credentials, String grantType) throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        String data = credentials + "&" + "Host=rs.alarmnet.com%2F&" + "Pragma=no-cache&"
                + "Cache-Control=no-store+no-cache&"
                + "scope=EMEA-V1-Basic+EMEA-V1-Anonymous+EMEA-V1-Get-Current-User-Account&" + "grant_type=" + grantType
                + "&" + "Content-Type=application%2Fx-www-form-urlencoded%3B+charset%3Dutf-8&"
                + "Connection=Keep-Alive";

        Map<String, String> headers = new HashMap<>();
        String basicAuth = Base64.getEncoder()
                .encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
        headers.put("Authorization", "Basic " + basicAuth);
        headers.put("Accept", "application/json, application/xml, text/json, text/x-json, text/javascript, text/xml");

        Authentication authentication = apiAccess.doRequest(HttpMethod.POST, EvohomeApiConstants.URL_V2_AUTH, headers,
                data, "application/x-www-form-urlencoded", Authentication.class);
        if (authentication == null || authentication.getAccessToken() == null
                || authentication.getAccessToken().isBlank() || authentication.getExpiresIn() <= 0) {
            throw new EvohomeApiClientException("Invalid authentication response");
        }
        checkOpen();
        authentication.setSystemTime(System.currentTimeMillis() / 1000);
        apiAccess.setAuthentication(authentication);
    }

    private void authenticateWithUsername() throws TimeoutException, EvohomeApiClientException {
        String credentials = "Username=" + URLEncoder.encode(configuration.username, StandardCharsets.UTF_8) + "&"
                + "Password=" + URLEncoder.encode(configuration.password, StandardCharsets.UTF_8);
        authenticate(credentials, "password");
    }

    private void authenticateWithToken(String refreshToken) throws TimeoutException, EvohomeApiClientException {
        String credentials = "refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        authenticate(credentials, "refresh_token");
    }

    private void updateAuthentication() throws TimeoutException, EvohomeApiClientException {
        checkOpen();
        Authentication authentication = apiAccess.getAuthentication();
        if (authentication == null) {
            authenticateWithUsername();
        } else {
            long currentTime = System.currentTimeMillis() / 1000;
            long expiration = authentication.getSystemTime() + authentication.getExpiresIn();
            // Long polling intervals must not cause every poll to refresh a newly issued token.
            expiration -= Math.min(60, authentication.getExpiresIn() / 10);

            // Update the access token just before it expires, but fall back to username and password
            // when it fails (i.e. refresh token had been invalidated)
            if (currentTime >= expiration) {
                String refreshToken = authentication.getRefreshToken();
                if (refreshToken == null || refreshToken.isBlank()) {
                    authenticateWithUsername();
                } else {
                    try {
                        authenticateWithToken(refreshToken);
                    } catch (EvohomeApiClientException e) {
                        if (e.getStatusCode() != 400 && e.getStatusCode() != 401) {
                            throw e;
                        }
                        authenticateWithUsername();
                    }
                }
            }
        }
    }

    private <T> T get(String url, Class<T> type) throws TimeoutException, EvohomeApiClientException {
        updateAuthentication();
        @Nullable
        T result;
        try {
            result = apiAccess.doAuthenticatedGet(url, type);
        } catch (EvohomeApiClientException e) {
            if (e.getStatusCode() != 401) {
                throw e;
            }
            apiAccess.setAuthentication(null);
            updateAuthentication();
            result = apiAccess.doAuthenticatedGet(url, type);
        }
        if (result == null) {
            throw new EvohomeApiClientException("Empty API response");
        }
        return result;
    }

    private void put(String url, Object command) throws TimeoutException, EvohomeApiClientException {
        updateAuthentication();
        try {
            apiAccess.doAuthenticatedPut(url, command);
        } catch (EvohomeApiClientException e) {
            if (e.getStatusCode() != 401) {
                throw e;
            }
            apiAccess.setAuthentication(null);
            updateAuthentication();
            apiAccess.doAuthenticatedPut(url, command);
        }
    }

    private void checkOpen() throws EvohomeApiClientException {
        if (closed) {
            throw new EvohomeApiClientException("API client is closed");
        }
    }
}
