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
package org.openhab.binding.atagone.internal.api;

import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.io.EOFException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.ReportDetailsDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Connection to the thermostat through the ATAG ONE portal.
 * <p>
 * The portal has no public API, so this client makes the calls the portal's own web pages make:
 * <ul>
 * <li>Login is the web form (anti-forgery token, then email and password) and leaves a session cookie. The data calls
 * under {@code /api/app/} also need the bearer token from {@code /Device/ApiAuthenticate}. Rejected credentials are
 * never retried, because repeated failures lock the account.</li>
 * <li>{@code /api/app/retrieve} takes the info bitmask of the local API. The binding asks for 79; bit 16 would return
 * the owner's name and address. The reply is a JSON string that contains the JSON, so it is parsed twice, and its
 * report has no {@code details} block.</li>
 * <li>Writes post the forms of the web pages (modes, settings form, schedules). Display brightness, language and the
 * two sensor offsets are not on those pages; the portal takes them on its {@code update} route.</li>
 * <li>Every write is checked against the portal's reply or by reading the value back.</li>
 * </ul>
 * Requests are spaced one second apart.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOnePortalClient implements AtagOneConnection {

    public static final String PORTAL_URL = "https://portal.atag-one.com";

    private static final int REQUEST_TIMEOUT_S = 20;
    private static final long MIN_INTERVAL_MS = 1_000L;
    // Control, schedules, configuration, report and status. Bit 16 would add the owner's address.
    private static final int PORTAL_INFO_BITMASK = 79;
    private static final int SCHEDULE_TYPE_CH = 0;
    private static final int SCHEDULE_TYPE_DHW = 1;
    private static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
            "Sunday" };
    private static final int VACATION_START_DELAY_MIN = 5;
    // The thermostat reports about every two minutes; the portal keeps serving the last report when it stops.
    private static final Duration MAX_REPORT_AGE = Duration.ofMinutes(10);
    private static final int MAX_LOGIN_ERROR_LENGTH = 200;
    private static final int MAX_REPLY_EXCERPT = 800;

    private static final Pattern FORM_TOKEN = Pattern
            .compile("name=\"__RequestVerificationToken\"[^>]+value=\"(.*?)\"");
    private static final Pattern DEVICE_ID = Pattern
            .compile("[0-9]{4}-[0-9]{4}-[0-9]{4}_[0-9]{2}-[0-9]{2}-[0-9]{3}-[0-9]{3}");
    private static final Pattern LOGIN_ERROR = Pattern.compile("<li class=\"text-error\"><span>(.*?)</span>",
            Pattern.DOTALL);
    private static final DateTimeFormatter VACATION_TIME = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    private static final Gson GSON = new Gson();

    // The portal's report has no details block; the handler publishes these as UNDEF.
    private static final Set<String> UNSUPPORTED_CHANNELS = Set.of(CHANNEL_MODULATION_LEVEL, CHANNEL_BOILER_TEMPERATURE,
            CHANNEL_BOILER_RETURN_TEMPERATURE);

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Berlin");

    private final Logger logger = LoggerFactory.getLogger(AtagOnePortalClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String email;
    private final String password;
    private final long minIntervalMs;
    private final Clock clock;

    private volatile @Nullable String deviceId;
    private volatile @Nullable String bearerToken;
    private volatile boolean credentialsRejected;
    private volatile @Nullable DeviceConfigDTO lastConfiguration;
    private volatile ZoneId deviceZone = DEFAULT_ZONE;
    private long lastRequestMs = 0;

    public AtagOnePortalClient(HttpClient httpClient, String email, String password) {
        this(httpClient, PORTAL_URL, email, password, MIN_INTERVAL_MS, Clock.systemUTC());
    }

    AtagOnePortalClient(HttpClient httpClient, String baseUrl, String email, String password, long minIntervalMs,
            Clock clock) {
        this.clock = clock;
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.email = email;
        this.password = password;
        this.minIntervalMs = minIntervalMs;
    }

    @Override
    public Set<String> unsupportedChannels() {
        return UNSUPPORTED_CHANNELS;
    }

    @Override
    public RetrieveReplyDTO retrieve() throws AtagOneCommunicationException {
        RetrieveReplyDTO reply = withSession(this::fetchState);
        Duration age = Duration.between(AtagEpoch.toZonedDateTime(reply.report.report_time).toInstant(),
                clock.instant());
        if (age.compareTo(MAX_REPORT_AGE) > 0) {
            throw new AtagOneCommunicationException(
                    "The thermostat is not reporting to the portal (last report " + age.toMinutes() + " min ago)");
        }
        return reply;
    }

    @Override
    public void updateControl(ControlUpdateDTO controlUpdate, @Nullable DeviceConfigUpdateDTO configUpdate)
            throws AtagOneCommunicationException {
        Set<String> control = setFields(controlUpdate);
        Set<String> config = configUpdate == null ? Set.of() : setFields(configUpdate);
        Integer mode = controlUpdate.ch_mode;
        Double setpoint = controlUpdate.ch_mode_temp;
        Long duration = controlUpdate.ch_mode_duration;
        boolean noConfig = config.isEmpty();

        if (noConfig && control.equals(Set.of("ch_mode_temp")) && setpoint != null) {
            runInSession(() -> setSetpoint(setpoint));
        } else if (noConfig && mode != null && mode == CH_MODE_MANUAL
                && (control.equals(Set.of("ch_mode")) || control.equals(Set.of("ch_mode", "ch_mode_temp")))) {
            runInSession(() -> setMode("manual"));
            if (setpoint != null) {
                runInSession(() -> setSetpoint(setpoint));
            }
        } else if (noConfig && mode != null && mode == CH_MODE_AUTO && (control.equals(Set.of("ch_mode"))
                || (control.equals(Set.of("ch_mode", "ch_mode_duration")) && duration != null && duration == 0))) {
            runInSession(() -> setMode("schedule_active"));
        } else if (mode != null && mode == CH_MODE_AUTO
                && control.equals(Set.of("ch_mode", "ch_mode_duration", "vacation_duration"))
                && config.equals(Set.of("start_vacation")) && isZero(controlUpdate.vacation_duration, duration)) {
            runInSession(this::clearVacation);
        } else if (noConfig && mode != null && mode == CH_MODE_EXTEND
                && control.equals(Set.of("ch_mode", "extend_duration")) && controlUpdate.extend_duration != null) {
            Long seconds = controlUpdate.extend_duration;
            runInSession(() -> setExtend(seconds));
        } else if (noConfig && mode != null && mode == CH_MODE_FIREPLACE
                && control.equals(Set.of("ch_mode", "ch_mode_duration", "fireplace_duration")) && duration != null) {
            runInSession(() -> setFireplace(duration));
        } else if (mode != null && mode == CH_MODE_HOLIDAY
                && control.equals(Set.of("ch_mode", "ch_mode_duration", "vacation_duration"))
                && config.equals(Set.of("start_vacation")) && duration != null) {
            runInSession(() -> setVacation(duration));
        } else if (configUpdate != null && !config.isEmpty() && !config.contains("start_vacation")
                && (control.isEmpty() || control.equals(Set.of("ch_control_mode")))) {
            Integer controlMode = controlUpdate.ch_control_mode;
            runInSession(() -> writeConfig(controlMode, configUpdate));
        } else {
            throw new AtagOneCommunicationException("Writing " + control + (config.isEmpty() ? "" : " with " + config)
                    + " through the portal is not supported");
        }
    }

    // The update DTOs leave unset fields null and serialize without them, so their keys are what is being written.
    private static Set<String> setFields(Object update) {
        return GSON.toJsonTree(update).getAsJsonObject().keySet();
    }

    private String sendConfigurationUpdate(Map<String, String> configuration) throws AtagOneCommunicationException {
        String token = bearerToken;
        if (token == null) {
            throw new SessionExpiredException();
        }
        Fields fields = new Fields();
        fields.put("update_message[seqnr]", "0");
        fields.put("update_message[device_id]", requireDeviceId());
        configuration.forEach((name, value) -> fields.put("update_message[configuration][" + name + "]", value));
        Request request = newRequest(HttpMethod.POST, "/api/app/update")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token).content(new FormContentProvider(fields));
        return checkSession(send(request)).getContentAsString();
    }

    // The portal's settings form does not have these; its update route takes them as single fields.
    static Map<String, String> directSettings(DeviceConfigUpdateDTO update, @Nullable DeviceConfigDTO current) {
        Map<String, String> settings = new LinkedHashMap<>();
        Integer language = update.language;
        if (language != null && (current == null || language != current.language)) {
            settings.put("language", language.toString());
        }
        Integer brightness = update.disp_brightness;
        if (brightness != null && (current == null || brightness != current.disp_brightness)) {
            settings.put("disp_brightness", brightness.toString());
        }
        Double inside = update.room_temp_offs;
        if (inside != null && (current == null || Math.abs(inside - current.room_temp_offs) > 0.001)) {
            settings.put("room_temp_offs", String.format(Locale.ROOT, "%.1f", inside));
        }
        Double outside = update.outs_temp_offs;
        if (outside != null && (current == null || Math.abs(outside - current.outs_temp_offs) > 0.001)) {
            settings.put("outs_temp_offs", String.format(Locale.ROOT, "%.1f", outside));
        }
        return settings;
    }

    private static boolean applied(Map<String, String> settings, DeviceConfigDTO stored) {
        String language = settings.get("language");
        String brightness = settings.get("disp_brightness");
        String inside = settings.get("room_temp_offs");
        String outside = settings.get("outs_temp_offs");
        return (language == null || language.equals(Integer.toString(stored.language)))
                && (brightness == null || brightness.equals(Integer.toString(stored.disp_brightness)))
                && (inside == null || Math.abs(Double.parseDouble(inside) - stored.room_temp_offs) < 0.01)
                && (outside == null || Math.abs(Double.parseDouble(outside) - stored.outs_temp_offs) < 0.01);
    }

    private void writeConfig(@Nullable Integer controlMode, DeviceConfigUpdateDTO update)
            throws AtagOneCommunicationException {
        String id = requireDeviceId();
        DeviceConfigDTO current = lastConfiguration;
        Map<String, String> direct = directSettings(update, current);
        if (!direct.isEmpty()) {
            sendConfigurationUpdate(direct);
            if (!applied(direct, fetchState().configuration)) {
                throw new AtagOneCommunicationException("Portal did not store " + direct.keySet());
            }
        }
        String page = checkSession(send(newRequest(HttpMethod.GET, "/Device/Edit?deviceId=" + id)))
                .getContentAsString();
        PortalDeviceForm form = PortalDeviceForm.parse(page);
        if (form.apply(controlMode, update, current)) {
            Request request = newRequest(HttpMethod.POST, "/Device/EditDevice")
                    .content(new FormContentProvider(form.toFields()));
            String reply = checkSession(send(request)).getContentAsString();
            if (!reply.contains("Changes saved successfully")) {
                throw rejected("Portal did not save the device settings", reply);
            }
        }
    }

    private static boolean isZero(@Nullable Long first, @Nullable Long second) {
        return first != null && first == 0 && second != null && second == 0;
    }

    @Override
    public void updateChSchedule(ScheduleDTO chSchedule) throws AtagOneCommunicationException {
        runInSession(() -> writeSchedule(SCHEDULE_TYPE_CH, chSchedule));
    }

    @Override
    public void updateDhwSchedule(ScheduleDTO dhwSchedule) throws AtagOneCommunicationException {
        runInSession(() -> writeSchedule(SCHEDULE_TYPE_DHW, dhwSchedule));
    }

    private void writeSchedule(int scheduleType, ScheduleDTO schedule) throws AtagOneCommunicationException {
        if (schedule.entries.length != DAYS.length) {
            throw new AtagOneCommunicationException("A schedule needs " + DAYS.length + " days");
        }
        String token = bearerToken;
        if (token == null) {
            throw new SessionExpiredException();
        }
        Fields form = scheduleForm(activeProgram(scheduleType), schedule);
        Request request = newRequest(HttpMethod.POST, "/api/app/EditSchedule")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token).content(new FormContentProvider(form));
        checkSession(send(request));
        if (!matches(activeProgram(scheduleType), schedule)) {
            throw new AtagOneCommunicationException("Portal did not store the schedule");
        }
    }

    // The portal edits the active program of each kind in place and expects the whole program.
    private JsonObject activeProgram(int scheduleType) throws AtagOneCommunicationException {
        String token = bearerToken;
        if (token == null) {
            throw new SessionExpiredException();
        }
        Request request = newRequest(HttpMethod.GET, "/api/app/GetAllSchedules?deviceId=" + requireDeviceId())
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token);
        String body = checkSession(send(request)).getContentAsString();
        try {
            for (JsonElement element : JsonParser.parseString(body).getAsJsonArray()) {
                JsonObject program = element.getAsJsonObject();
                if (member(program, "ScheduleType").getAsInt() == scheduleType
                        && member(program, "Active").getAsBoolean()) {
                    return program;
                }
            }
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException | NumberFormatException e) {
            throw new AtagOneCommunicationException("Malformed schedule list from portal", e);
        }
        throw new AtagOneCommunicationException("Portal has no active schedule of kind " + scheduleType);
    }

    static Fields scheduleForm(JsonObject program, ScheduleDTO schedule) throws AtagOneCommunicationException {
        Fields form = new Fields();
        try {
            for (String name : new String[] { "Key", "Type", "ProgramId", "DeviceId", "Name", "ScheduleType" }) {
                form.put(name, member(program, name).getAsString());
            }
        } catch (UnsupportedOperationException | IllegalStateException e) {
            throw new AtagOneCommunicationException("Unexpected program layout from portal", e);
        }
        form.put("Active", "true");
        form.put("BaseTemp", plain(schedule.base_temp));
        for (int day = 0; day < DAYS.length; day++) {
            double[][] periods = schedule.entries[day];
            for (int i = 0; periods != null && i < periods.length; i++) {
                String prefix = DAYS[day] + "[" + i + "][";
                form.put(prefix + "StartTime]", Long.toString((long) periods[i][0]));
                form.put(prefix + "EndTime]", Long.toString((long) periods[i][1]));
                form.put(prefix + "Temperature]", plain(periods[i][2]));
                form.put(prefix + "Type]", Integer.toString((day + 1) % DAYS.length));
            }
        }
        return form;
    }

    static boolean matches(JsonObject program, ScheduleDTO schedule) {
        try {
            if (Math.abs(member(program, "BaseTemp").getAsDouble() - schedule.base_temp) > 0.01) {
                return false;
            }
            for (int day = 0; day < DAYS.length; day++) {
                JsonArray stored = member(program, DAYS[day]).getAsJsonArray();
                double[][] wanted = schedule.entries[day] == null ? new double[0][] : schedule.entries[day];
                if (stored.size() != wanted.length) {
                    return false;
                }
                for (int i = 0; i < wanted.length; i++) {
                    JsonObject period = stored.get(i).getAsJsonObject();
                    if (member(period, "StartTime").getAsLong() != (long) wanted[i][0]
                            || member(period, "EndTime").getAsLong() != (long) wanted[i][1]
                            || Math.abs(member(period, "Temperature").getAsDouble() - wanted[i][2]) > 0.01) {
                        return false;
                    }
                }
            }
            return true;
        } catch (UnsupportedOperationException | IllegalStateException | NumberFormatException e) {
            return false;
        }
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private void runInSession(SessionAction action) throws AtagOneCommunicationException {
        withSession(() -> {
            action.run();
            return Boolean.TRUE;
        });
    }

    private <T> T withSession(SessionCall<T> call) throws AtagOneCommunicationException {
        if (credentialsRejected) {
            throw new AtagOneLoginException("Portal login was rejected, check the credentials");
        }
        String usedToken = bearerToken;
        try {
            if (usedToken == null) {
                login(null);
            }
            return runRetryingClosedConnection(call);
        } catch (SessionExpiredException e) {
            logger.debug("Portal rejected the token, renewing the session");
            login(usedToken);
            try {
                return runRetryingClosedConnection(call);
            } catch (SessionExpiredException e2) {
                throw new AtagOneCommunicationException("Portal rejected the session right after login");
            }
        }
    }

    // Every call sets a value or reads one, so repeating it after the portal closed a kept connection is safe.
    private <T> T runRetryingClosedConnection(SessionCall<T> call) throws AtagOneCommunicationException {
        try {
            return call.run();
        } catch (SessionExpiredException e) {
            throw e;
        } catch (AtagOneCommunicationException e) {
            if (!(e.getCause() instanceof ExecutionException && e.getCause().getCause() instanceof EOFException)) {
                throw e;
            }
            logger.debug("Portal closed a kept connection, trying once more");
            return call.run();
        }
    }

    private void setMode(String mode) throws AtagOneCommunicationException {
        String id = deviceId;
        if (id == null) {
            throw new SessionExpiredException();
        }
        Fields fields = new Fields();
        fields.put("__RequestVerificationToken", fetchFormToken());
        Request request = newRequest(HttpMethod.POST, "/Home/SetMode/?deviceId=" + id + "&mode=" + mode)
                .content(new FormContentProvider(fields));
        String reply = checkSession(send(request)).getContentAsString();
        if (!isSuccess(reply)) {
            throw rejected("Portal did not accept mode " + mode, reply);
        }
    }

    private String fetchFormToken() throws AtagOneCommunicationException {
        String token = firstGroup(FORM_TOKEN, checkSession(send(newRequest(HttpMethod.GET, "/"))).getContentAsString());
        if (token == null) {
            throw new AtagOneCommunicationException("Portal page has no anti-forgery token");
        }
        return token;
    }

    private void clearVacation() throws AtagOneCommunicationException {
        String id = requireDeviceId();
        String reply = checkSession(send(newRequest(HttpMethod.GET, "/Home/ClearVacation/?deviceId=" + id)))
                .getContentAsString();
        if (!isSuccess(reply)) {
            throw rejected("Portal did not clear the vacation", reply);
        }
    }

    private void setExtend(long seconds) throws AtagOneCommunicationException {
        if (seconds < 60) {
            throw new AtagOneCommunicationException("Extend needs at least one minute");
        }
        Fields extra = new Fields();
        extra.put("endDate", String.format(Locale.ROOT, "%02d:%02d", seconds / 3600, seconds % 3600 / 60));
        submitMode("/Home/SetExtendMode", "vacation_submit", "party", "setActiveMode('party'", extra);
    }

    private void setFireplace(long seconds) throws AtagOneCommunicationException {
        if (seconds < 3600 || seconds > 24 * 3600 || seconds % 1800 != 0) {
            throw new AtagOneCommunicationException(
                    "Fireplace duration must be 1 to 24 hours in steps of 30 minutes, got " + seconds + " s");
        }
        Fields extra = new Fields();
        extra.put("duration", BigDecimal.valueOf(seconds / 3600.0).stripTrailingZeros().toPlainString());
        submitMode("/Home/SetFireplaceMode", "fireplace_submit", "fireplace", "setActiveMode('fireplace'", extra);
    }

    private void setVacation(long seconds) throws AtagOneCommunicationException {
        ZonedDateTime start = ZonedDateTime.now(deviceZone).plusMinutes(VACATION_START_DELAY_MIN);
        Fields extra = new Fields();
        extra.put("startDate", VACATION_TIME.format(start));
        extra.put("endDate", VACATION_TIME.format(start.plusSeconds(seconds)));
        submitMode("/Home/SetVacationMode", "vacation_submit", "vacation", "retrieveAndUpdate(true)", extra);
    }

    private void submitMode(String path, String submitType, String mode, String successMarker, Fields extra)
            throws AtagOneCommunicationException {
        Fields fields = new Fields();
        fields.put("submit_type", submitType);
        fields.put("__RequestVerificationToken", fetchFormToken());
        fields.put("deviceId", requireDeviceId());
        fields.put("mode", mode);
        extra.forEach(field -> fields.put(field));
        fields.put("X-Requested-With", "XMLHttpRequest");
        String reply = checkSession(send(newRequest(HttpMethod.POST, path).content(new FormContentProvider(fields))))
                .getContentAsString();
        if (!reply.contains(successMarker)) {
            throw rejected("Portal did not accept mode " + mode, reply);
        }
    }

    private String requireDeviceId() throws AtagOneCommunicationException {
        String id = deviceId;
        if (id == null) {
            throw new SessionExpiredException();
        }
        return id;
    }

    private void setSetpoint(double temperature) throws AtagOneCommunicationException {
        String id = deviceId;
        if (id == null) {
            throw new SessionExpiredException();
        }
        Fields fields = new Fields();
        fields.put("__RequestVerificationToken", fetchFormToken());
        String value = BigDecimal.valueOf(temperature).stripTrailingZeros().toPlainString();
        Request request = newRequest(HttpMethod.POST, "/Home/DeviceSetSetpoint/" + id + "?temperature=" + value)
                .content(new FormContentProvider(fields));
        double confirmed = parseSetpointReply(checkSession(send(request)).getContentAsString());
        if (Math.abs(confirmed - temperature) > 0.01) {
            throw new AtagOneCommunicationException("Portal set " + confirmed + " instead of " + temperature);
        }
    }

    static double parseSetpointReply(String body) throws AtagOneCommunicationException {
        try {
            JsonElement outer = JsonParser.parseString(body);
            JsonElement inner = outer.isJsonPrimitive() ? JsonParser.parseString(outer.getAsString()) : outer;
            return member(inner.getAsJsonObject(), "ch_mode_temp").getAsDouble();
        } catch (JsonParseException | IllegalStateException | UnsupportedOperationException e) {
            throw new AtagOneCommunicationException("Portal did not confirm the setpoint", e);
        }
    }

    private synchronized void login(@Nullable String staleToken) throws AtagOneCommunicationException {
        String current = bearerToken;
        if (current != null && !current.equals(staleToken)) {
            return;
        }
        bearerToken = null;
        ContentResponse page = send(newRequest(HttpMethod.GET, "/Account/Login"));
        if (isSignedIn(page)) {
            // The session cookie outlives the bearer token, and the portal sends a signed-in visitor away from its
            // login page. Only a refused token means the session itself is gone.
            if (refreshBearerToken()) {
                logger.debug("Session still valid, got a new token");
                return;
            }
            logger.debug("Session gone, signing in with the credentials");
            httpClient.getCookieStore().removeAll();
            page = send(newRequest(HttpMethod.GET, "/Account/Login"));
        }
        String formToken = firstGroup(FORM_TOKEN, page.getContentAsString());
        if (page.getStatus() != 200 || formToken == null) {
            throw new AtagOneCommunicationException("Portal login page unavailable (HTTP " + page.getStatus() + ")");
        }

        Fields fields = new Fields();
        fields.put("__RequestVerificationToken", formToken);
        fields.put("Email", email);
        fields.put("Password", password);
        fields.put("RememberMe", "false");
        ContentResponse result = send(
                newRequest(HttpMethod.POST, "/Account/Login").content(new FormContentProvider(fields)));
        if (result.getStatus() != 302) {
            String message = firstGroup(LOGIN_ERROR, result.getContentAsString());
            if (result.getStatus() == 200 && message != null) {
                credentialsRejected = true;
                throw new AtagOneLoginException(sanitiseLoginError(message));
            }
            throw new AtagOneCommunicationException("Portal login failed (HTTP " + result.getStatus() + ")");
        }

        if (!refreshBearerToken()) {
            throw new AtagOneCommunicationException("Portal did not issue an API token");
        }
    }

    private static boolean isSignedIn(ContentResponse loginPage) {
        String location = loginPage.getHeaders().get(HttpHeader.LOCATION);
        return loginPage.getStatus() == 302 && location != null && !location.contains("/Account/Login");
    }

    /**
     * Finds the thermostat on the account if that has not happened yet, then asks for a new bearer token.
     *
     * @return {@code false} when the portal refuses the token because the session is gone
     */
    private boolean refreshBearerToken() throws AtagOneCommunicationException {
        if (deviceId == null) {
            ContentResponse home = send(newRequest(HttpMethod.GET, "/"));
            String found = firstMatch(DEVICE_ID, home.getContentAsString());
            if (found == null) {
                throw new AtagOneCommunicationException("No thermostat found on the portal account");
            }
            deviceId = found;
        }
        try {
            ContentResponse auth = send(newRequest(HttpMethod.GET, "/Device/ApiAuthenticate"));
            if (auth.getStatus() != 200) {
                return false;
            }
            bearerToken = parseBearerToken(auth.getContentAsString());
            return true;
        } catch (SessionExpiredException e) {
            return false;
        }
    }

    private RetrieveReplyDTO fetchState() throws AtagOneCommunicationException {
        String id = deviceId;
        String token = bearerToken;
        if (id == null || token == null) {
            throw new SessionExpiredException();
        }
        Fields fields = new Fields();
        fields.put("retrieve_message[seqnr]", "0");
        fields.put("retrieve_message[device_id]", id);
        fields.put("retrieve_message[info]", Integer.toString(PORTAL_INFO_BITMASK));
        Request retrieveRequest = newRequest(HttpMethod.POST, "/api/app/retrieve")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + token).content(new FormContentProvider(fields));
        RetrieveReplyDTO reply = parseRetrieve(checkSession(send(retrieveRequest)).getContentAsString());
        reply.status.device_id = id;
        deviceZone = zoneOf(reply.configuration.time_zone);
        lastConfiguration = reply.configuration;
        return reply;
    }

    private Request newRequest(HttpMethod method, String path) {
        return httpClient.newRequest(baseUrl + path).method(method).followRedirects(false)
                .header(HttpHeader.ACCEPT_LANGUAGE, "en").header("X-Requested-With", "XMLHttpRequest")
                .timeout(REQUEST_TIMEOUT_S, TimeUnit.SECONDS);
    }

    private synchronized ContentResponse send(Request request) throws AtagOneCommunicationException {
        long elapsed = System.currentTimeMillis() - lastRequestMs;
        if (elapsed < minIntervalMs) {
            try {
                Thread.sleep(minIntervalMs - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AtagOneCommunicationException("Interrupted while waiting for rate limit", e);
            }
        }
        try {
            lastRequestMs = System.currentTimeMillis();
            ContentResponse response = request.send();
            logger.trace("{} {} -> HTTP {}", request.getMethod(), request.getPath(), response.getStatus());
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AtagOneCommunicationException("Interrupted during portal request", e);
        } catch (ExecutionException e) {
            // Jetty rejects a 401 without WWW-Authenticate header before the response reaches us.
            if (e.getCause() instanceof HttpResponseException
                    && ((HttpResponseException) e.getCause()).getResponse().getStatus() == 401) {
                throw new SessionExpiredException();
            }
            throw new AtagOneCommunicationException("Portal not reachable: " + e.getMessage(), e);
        } catch (TimeoutException e) {
            throw new AtagOneCommunicationException("Portal not reachable: " + e.getMessage(), e);
        }
    }

    private ContentResponse checkSession(ContentResponse response) throws AtagOneCommunicationException {
        int status = response.getStatus();
        String location = response.getHeaders().get(HttpHeader.LOCATION);
        if (status == 401
                || (status >= 300 && status < 400 && location != null && location.contains("/Account/Login"))) {
            throw new SessionExpiredException();
        }
        if (status != 200) {
            throw new AtagOneCommunicationException("Portal answered HTTP " + status);
        }
        return response;
    }

    static String parseBearerToken(String json) throws AtagOneCommunicationException {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("success") && obj.get("success").getAsBoolean() && obj.has("token")) {
                return obj.get("token").getAsString();
            }
        } catch (JsonParseException | IllegalStateException e) {
            throw new AtagOneCommunicationException("Malformed portal token reply", e);
        }
        throw new AtagOneCommunicationException("Portal did not issue an API token");
    }

    // The portal returns the retrieve_reply JSON as a JSON string, so it is parsed twice.
    static RetrieveReplyDTO parseRetrieve(String body) throws AtagOneCommunicationException {
        try {
            JsonElement outer = JsonParser.parseString(body);
            JsonElement inner = outer.isJsonPrimitive() ? JsonParser.parseString(outer.getAsString()) : outer;
            JsonObject reply = inner.getAsJsonObject().getAsJsonObject("retrieve_reply");
            JsonObject report = reply == null ? null : reply.getAsJsonObject("report");
            if (report != null && report.has("ch_time_to_Temp")) {
                JsonElement timeToTarget = report.remove("ch_time_to_Temp");
                if (timeToTarget.isJsonPrimitive() && timeToTarget.getAsJsonPrimitive().isNumber()) {
                    report.addProperty("ch_time_to_temp", timeToTarget.getAsDouble());
                }
            }
            RetrieveReplyDTO dto = reply == null ? null : GSON.fromJson(reply, RetrieveReplyDTO.class);
            if (dto == null || dto.control == null || dto.schedules == null || dto.configuration == null
                    || dto.report == null || dto.status == null) {
                throw new AtagOneCommunicationException("Incomplete retrieve_reply from portal");
            }
            if (dto.report.details == null) {
                dto.report.details = new ReportDetailsDTO();
            }
            return dto;
        } catch (JsonParseException | IllegalStateException | NumberFormatException e) {
            throw new AtagOneCommunicationException("Malformed retrieve_reply from portal", e);
        }
    }

    // The portal's replies are the only evidence of what it did, so an unexpected one is logged for the next debugging.
    private AtagOneCommunicationException rejected(String message, String reply) {
        logger.debug("{}, it answered: {}", message, excerpt(reply));
        return new AtagOneCommunicationException(message);
    }

    static String excerpt(String reply) {
        String text = reply.replaceAll("<form[^>]*>", "<form>").replaceAll("<input[^>]*type=\"hidden\"[^>]*>", "")
                .replaceAll("value=\"[^\"]{20,}\"", "value=\"...\"").replaceAll("\\s+", " ").trim();
        return text.length() > MAX_REPLY_EXCERPT ? text.substring(0, MAX_REPLY_EXCERPT) : text;
    }

    // The text comes from the portal's page and ends up in the Thing status.
    static String sanitiseLoginError(String html) {
        String text = html.replaceAll("<[^>]*>", " ").replace('"', '\'').replace('\\', ' ').replaceAll("\\s+", " ")
                .trim();
        return text.length() > MAX_LOGIN_ERROR_LENGTH ? text.substring(0, MAX_LOGIN_ERROR_LENGTH) : text;
    }

    static boolean isSuccess(String body) {
        try {
            JsonElement success = JsonParser.parseString(body).getAsJsonObject().get("success");
            return success != null && success.isJsonPrimitive() && "true".equalsIgnoreCase(success.getAsString());
        } catch (JsonParseException | IllegalStateException e) {
            return false;
        }
    }

    private static JsonElement member(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null) {
            throw new IllegalStateException("Missing " + name);
        }
        return element;
    }

    static ZoneId zoneOf(int deviceTimeZone) {
        return TIME_ZONE_IDS.getOrDefault(deviceTimeZone, DEFAULT_ZONE);
    }

    private static @Nullable String firstGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private static @Nullable String firstMatch(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group() : null;
    }

    @FunctionalInterface
    private interface SessionAction {
        void run() throws AtagOneCommunicationException;
    }

    @FunctionalInterface
    private interface SessionCall<T> {
        T run() throws AtagOneCommunicationException;
    }

    private static class SessionExpiredException extends AtagOneCommunicationException {
        private static final long serialVersionUID = 1L;

        SessionExpiredException() {
            super("Portal session expired");
        }
    }
}
