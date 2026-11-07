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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.atagone.internal.AtagOneBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests {@link AtagOnePortalClient} parsing against sanitised portal captures, and its session handling against a
 * local fake portal.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagOnePortalClientTest {

    private static final String DEVICE_ID = "1234-5678-9012_34-56-789-012";
    private static final String PASSWORD = "secret";
    private static final Instant REPORT_TIME = Instant.ofEpochSecond(846680715L + AtagEpoch.OFFSET);

    private final AtomicInteger loginPosts = new AtomicInteger();
    private final AtomicInteger retrieveCalls = new AtomicInteger();
    private volatile boolean rejectNextRetrieve;
    private volatile boolean dropNextRetrieve;
    private volatile boolean signedIn;
    private volatile int loginPostStatus;
    private volatile String loginErrorHtml = "";
    private volatile String setpointReply = "\"{\\\"ch_mode_temp\\\":22.5}\"";
    private volatile String lastSetpointRequest = "";
    private volatile String lastUpdateRequest = "";
    private volatile String retrieveReply = "";
    private volatile boolean ignoreUpdates;
    private volatile String fireplaceReply = "<script>setActiveMode('fireplace', true);</script>";
    private volatile String lastScheduleAuth = "";
    private volatile String programs = "[]";
    private volatile String programsAfterEdit = "[]";
    private volatile String saveReply = "<span>Changes saved successfully!</span>";

    private @Nullable HttpServer server;
    private @Nullable HttpClient httpClient;

    private static String fixture(String name) throws IOException {
        try (InputStream in = AtagOnePortalClientTest.class.getResourceAsStream("portal/" + name)) {
            assertNotNull(in, name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @BeforeEach
    void startServer() throws Exception {
        HttpServer fake = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fake.createContext("/", this::handle);
        fake.start();
        server = fake;
        HttpClient client = new HttpClient();
        client.start();
        httpClient = client;
    }

    @AfterEach
    void stopServer() throws Exception {
        HttpServer fake = server;
        if (fake != null) {
            fake.stop(0);
        }
        HttpClient client = httpClient;
        if (client != null) {
            client.stop();
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        int status = 200;
        String response;
        if ("/Account/Login".equals(path) && "GET".equals(method)) {
            if (signedIn) {
                status = 302;
                exchange.getResponseHeaders().add("Location", "/");
                response = "";
            } else {
                response = fixture("home.html");
            }
        } else if ("/Account/Login".equals(path)) {
            loginPosts.incrementAndGet();
            if (loginPostStatus > 0) {
                status = loginPostStatus;
                response = "";
            } else if (body.contains("Password=" + PASSWORD)) {
                signedIn = true;
                status = 302;
                exchange.getResponseHeaders().add("Location", "/");
                response = "";
            } else {
                response = loginErrorHtml.isEmpty() ? fixture("login-failed.html") : loginErrorHtml;
            }
        } else if ("/".equals(path)) {
            response = fixture("home.html");
        } else if ("/Device/ApiAuthenticate".equals(path)) {
            if (signedIn) {
                response = fixture("api-authenticate.json");
            } else {
                status = 401;
                response = "<Error><Message>Authorization has been denied for this request.</Message></Error>";
            }
        } else if ("/api/app/retrieve".equals(path)) {
            retrieveCalls.incrementAndGet();
            if (dropNextRetrieve) {
                dropNextRetrieve = false;
                exchange.close();
                return;
            }
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (rejectNextRetrieve || !"Bearer test-token".equals(auth)) {
                rejectNextRetrieve = false;
                status = 401;
                response = "<Error><Message>Authorization has been denied for this request.</Message></Error>";
            } else {
                response = retrieveReply.isEmpty() ? fixture("retrieve.json") : retrieveReply;
            }
        } else if (path.startsWith("/Home/DeviceSetSetpoint/")) {
            lastSetpointRequest = method + " " + exchange.getRequestURI() + " " + body;
            response = setpointReply;
        } else if ("/Home/SetMode/".equals(path)) {
            lastSetpointRequest = method + " " + exchange.getRequestURI() + " " + body;
            response = "{\"success\":\"true\"}";
        } else if ("/Home/SetExtendMode".equals(path)) {
            lastSetpointRequest = method + " " + path + " " + body;
            response = "<script>setActiveMode('party', true);</script>";
        } else if ("/Home/SetFireplaceMode".equals(path)) {
            lastSetpointRequest = method + " " + path + " " + body;
            response = fireplaceReply;
        } else if ("/Home/SetVacationMode".equals(path)) {
            lastSetpointRequest = method + " " + path + " " + body;
            response = "<script>retrieveAndUpdate(true);</script>";
        } else if ("/Home/ClearVacation/".equals(path)) {
            lastSetpointRequest = method + " " + exchange.getRequestURI();
            response = "{\"success\":true,\"mode\":\"schedule_active\"}";
        } else if ("/Device/Edit".equals(path)) {
            response = fixture("device-form.html");
        } else if ("/Device/EditDevice".equals(path)) {
            lastSetpointRequest = method + " " + path + " " + body;
            response = saveReply;
        } else if ("/api/app/update".equals(path)) {
            lastUpdateRequest = URLDecoder.decode(body, StandardCharsets.UTF_8);
            if (!ignoreUpdates) {
                applyConfigurationUpdate(lastUpdateRequest);
            }
            response = "{\"update_reply\":{\"seqnr\":0}}";
        } else if ("/api/app/GetAllSchedules".equals(path)) {
            response = programs;
        } else if ("/api/app/EditSchedule".equals(path)) {
            lastSetpointRequest = method + " " + path + " " + body;
            lastScheduleAuth = String.valueOf(exchange.getRequestHeaders().getFirst("Authorization"));
            programs = programsAfterEdit;
            response = "";
        } else {
            status = 404;
            response = "";
        }
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
        exchange.close();
    }

    private void applyConfigurationUpdate(String body) {
        try {
            String inner = Objects
                    .requireNonNull(new com.google.gson.Gson().fromJson(fixture("retrieve.json").trim(), String.class));
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(inner).getAsJsonObject();
            com.google.gson.JsonObject configuration = root.getAsJsonObject("retrieve_reply")
                    .getAsJsonObject("configuration");
            Matcher setting = Pattern.compile("update_message\\[configuration\\]\\[(\\w+)\\]=(-?[0-9.]+)")
                    .matcher(body);
            while (setting.find()) {
                configuration.addProperty(setting.group(1), Double.valueOf(setting.group(2)));
            }
            com.google.gson.JsonObject control = root.getAsJsonObject("retrieve_reply").getAsJsonObject("control");
            Matcher duration = Pattern.compile("update_message\\[control\\]\\[(\\w+)\\]=([0-9]+)").matcher(body);
            while (duration.find()) {
                control.addProperty(duration.group(1), Long.valueOf(duration.group(2)));
            }
            retrieveReply = new com.google.gson.Gson().toJson(root.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private AtagOnePortalClient client(String password) {
        return client(password, Clock.fixed(REPORT_TIME.plusSeconds(60), ZoneOffset.UTC));
    }

    private AtagOnePortalClient client(String password, Clock clock) {
        HttpServer fake = server;
        HttpClient http = httpClient;
        assertNotNull(fake);
        assertNotNull(http);
        return new AtagOnePortalClient(http, "http://127.0.0.1:" + fake.getAddress().getPort(), "me@example.com",
                password, 0, clock);
    }

    @Test
    void readsTheWholeReportFromTheRetrieveReply() throws Exception {
        RetrieveReplyDTO r = AtagOnePortalClient.parseRetrieve(fixture("retrieve.json"));

        assertEquals(DEVICE_ID, r.status.device_id);
        assertEquals(846680715L, r.report.report_time);
        assertEquals(22.8, r.report.room_temp);
        assertEquals(12.0, r.report.outside_temp);
        assertEquals(10.0, r.report.tout_avg);
        assertEquals(54.1, r.report.dhw_water_temp);
        assertEquals(1.2, r.report.ch_water_pres);
        assertEquals(10567.55, r.report.burning_hours);
        assertEquals(512, r.report.boiler_status);
        assertEquals(39, r.report.rssi);
        assertEquals(3581, r.report.voltage);
        assertEquals(7, r.report.resets);
        assertEquals(29.2, r.report.pcb_temp);
        assertEquals(0, r.report.ch_time_to_temp);
        assertEquals(1, r.control.ch_mode);
        assertEquals(21.5, r.control.ch_mode_temp);
        assertEquals(1, r.configuration.time_zone);
    }

    @Test
    void fillsTheMissingReportDetailsWithDefaults() throws Exception {
        RetrieveReplyDTO r = AtagOnePortalClient.parseRetrieve(fixture("retrieve.json"));

        assertNotNull(r.report.details);
        assertEquals(0, r.report.details.rel_mod_level);
    }

    @ParameterizedTest
    @ValueSource(strings = { "control", "schedules", "configuration", "report", "status" })
    void rejectsRetrieveReplyWithoutARequiredBlock(String block) throws Exception {
        String inner = Objects
                .requireNonNull(new com.google.gson.Gson().fromJson(fixture("retrieve.json").trim(), String.class));
        com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(inner).getAsJsonObject();
        root.getAsJsonObject("retrieve_reply").remove(block);
        String incomplete = root.toString();

        assertThrows(AtagOneCommunicationException.class, () -> AtagOnePortalClient.parseRetrieve(incomplete));
    }

    @Test
    void acceptsRetrieveReplyThatIsNotDoubleEncoded() throws Exception {
        String inner = fixture("retrieve.json").trim();
        String decoded = new com.google.gson.Gson().fromJson(inner, String.class);

        assertEquals(21.5, AtagOnePortalClient.parseRetrieve(Objects.requireNonNull(decoded)).control.ch_mode_temp);
    }

    @Test
    void reportsTheBurnerDetailsAsUnavailable() {
        AtagOnePortalClient portal = new AtagOnePortalClient(new HttpClient(), "me@example.com", PASSWORD);

        assertEquals(Set.of(CHANNEL_MODULATION_LEVEL, CHANNEL_BOILER_TEMPERATURE, CHANNEL_BOILER_RETURN_TEMPERATURE),
                portal.unsupportedChannels());
    }

    @Test
    void logsInOnceAndReadsState() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);

        RetrieveReplyDTO first = portal.retrieve();
        portal.retrieve();

        assertEquals(DEVICE_ID, first.status.device_id);
        assertEquals(22.8, first.report.room_temp);
        assertEquals(1, loginPosts.get());
        assertEquals(2, retrieveCalls.get());
    }

    @Test
    void logsInAgainWhenThePortalAnswers401() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();

        signedIn = false;
        rejectNextRetrieve = true;
        RetrieveReplyDTO reply = portal.retrieve();

        assertEquals(22.8, reply.report.room_temp);
        assertEquals(2, loginPosts.get());
    }

    @Test
    void asksForANewTokenWhenTheSessionCookieOutlivesIt() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();

        rejectNextRetrieve = true;
        RetrieveReplyDTO reply = portal.retrieve();

        assertEquals(22.8, reply.report.room_temp);
        assertEquals(1, loginPosts.get(), "the signed-in session is reused, no second login");
    }

    @Test
    void retriesOnceWhenThePortalClosedAnIdleConnection() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();

        dropNextRetrieve = true;
        RetrieveReplyDTO reply = portal.retrieve();

        assertEquals(22.8, reply.report.room_temp);
        assertEquals(1, loginPosts.get());
    }

    @Test
    void doesNotRetryAfterBadCredentials() {
        AtagOnePortalClient portal = client("wrong");

        AtagOneLoginException first = assertThrows(AtagOneLoginException.class, portal::retrieve);
        assertEquals("Invalid login attempt.", first.getMessage());
        assertThrows(AtagOneLoginException.class, portal::retrieve);

        assertEquals(1, loginPosts.get());
    }

    @Test
    void setsTheSetpointWithAFreshFormToken() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.ch_mode_temp = 22.5;

        portal.updateControl(update, null);

        assertEquals(
                "POST /Home/DeviceSetSetpoint/" + DEVICE_ID + "?temperature=22.5 __RequestVerificationToken=tok123",
                lastSetpointRequest);
    }

    @Test
    void sendsWholeDegreesWithoutDecimals() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.ch_mode_temp = 23.0;
        setpointReply = "\"{\\\"ch_mode_temp\\\":23.0}\"";

        portal.updateControl(update, null);

        assertTrue(lastSetpointRequest.contains("?temperature=23 "), lastSetpointRequest);
    }

    @Test
    void failsWhenThePortalConfirmsAnotherSetpoint() {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.ch_mode_temp = 21.0;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(update, null));
    }

    @Test
    void switchesToManualAndAuto() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO manual = new ControlUpdateDTO();
        manual.ch_mode = CH_MODE_MANUAL;
        ControlUpdateDTO auto = new ControlUpdateDTO();
        auto.ch_mode = CH_MODE_AUTO;
        auto.ch_mode_duration = 0L;

        portal.updateControl(manual, null);
        assertEquals("POST /Home/SetMode/?deviceId=" + DEVICE_ID + "&mode=manual __RequestVerificationToken=tok123",
                lastSetpointRequest);
        portal.updateControl(auto, null);
        assertEquals(
                "POST /Home/SetMode/?deviceId=" + DEVICE_ID + "&mode=schedule_active __RequestVerificationToken=tok123",
                lastSetpointRequest);
    }

    @Test
    void manualWithTemperatureAlsoSetsTheSetpoint() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO manual = new ControlUpdateDTO();
        manual.ch_mode = CH_MODE_MANUAL;
        manual.ch_mode_temp = 22.5;

        portal.updateControl(manual, null);

        assertTrue(lastSetpointRequest.contains("DeviceSetSetpoint/" + DEVICE_ID + "?temperature=22.5 "),
                lastSetpointRequest);
    }

    @Test
    void extendsByTheRequestedTime() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO extend = new ControlUpdateDTO();
        extend.ch_mode = CH_MODE_EXTEND;
        extend.extend_duration = 5400L;

        portal.updateControl(extend, null);

        assertTrue(lastSetpointRequest.startsWith("POST /Home/SetExtendMode "), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("mode=party"), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("endDate=01%3A30"), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("submit_type=vacation_submit"), lastSetpointRequest);
    }

    @Test
    void startsFireplaceInHalfHourSteps() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO fireplace = new ControlUpdateDTO();
        fireplace.ch_mode = CH_MODE_FIREPLACE;
        fireplace.ch_mode_duration = 5400L;
        fireplace.fireplace_duration = 5400L;

        portal.updateControl(fireplace, null);
        assertTrue(lastSetpointRequest.contains("duration=1.5"), lastSetpointRequest);

        fireplace.ch_mode_duration = 7200L;
        fireplace.fireplace_duration = 7200L;
        portal.updateControl(fireplace, null);
        assertTrue(lastSetpointRequest.contains("duration=2&") || lastSetpointRequest.contains("duration=2 "),
                lastSetpointRequest);

        fireplace.ch_mode_duration = 1000L;
        fireplace.fireplace_duration = 1000L;
        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(fireplace, null));

        fireplace.ch_mode_duration = 25 * 3600L;
        fireplace.fireplace_duration = 25 * 3600L;
        AtagOneCommunicationException tooLong = assertThrows(AtagOneCommunicationException.class,
                () -> portal.updateControl(fireplace, null));
        assertTrue(tooLong.getMessage().contains("1 to 24 hours"), tooLong.getMessage());
    }

    @Test
    void rejectsAFireplaceReplyWithoutTheConfirmation() {
        AtagOnePortalClient portal = client(PASSWORD);
        fireplaceReply = "<div class=\"validation-message error\">Duration is not allowed</div>";
        ControlUpdateDTO fireplace = new ControlUpdateDTO();
        fireplace.ch_mode = CH_MODE_FIREPLACE;
        fireplace.ch_mode_duration = 3600L;
        fireplace.fireplace_duration = 3600L;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(fireplace, null));
    }

    @Test
    void logsTheMessageOfAReplyWithoutTheFormBoilerplate() {
        String reply = "<form action=\"/Home/SetFireplaceMode\" data-ajax=\"true\" id=\"SetModeForm\">\n"
                + "  <input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"" + "x".repeat(120)
                + "\" /> <input id=\"deviceId\" name=\"deviceId\" type=\"hidden\" value=\"1234-5678-9012_34-56-789-012\" />\n"
                + "  <input name=\"owner\" value=\"1234-5678-9012_34-56-789-012\" />\n"
                + "<div class=\"validation-message error\">Duration is not allowed</div><p>" + "y".repeat(1000)
                + "</p></form>";

        String shown = AtagOnePortalClient.excerpt(reply);

        assertTrue(shown.contains("Duration is not allowed"), shown);
        assertFalse(shown.contains("xxxxx") || shown.contains("1234-5678") || shown.contains("data-ajax"), shown);
        assertFalse(shown.contains("\n"));
        assertEquals(800, shown.length());
    }

    @Test
    void startsHolidayFiveMinutesAheadForTheRequestedDays() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO holiday = new ControlUpdateDTO();
        holiday.ch_mode = CH_MODE_HOLIDAY;
        holiday.ch_mode_duration = 7 * 86400L;
        holiday.vacation_duration = 7 * 86400L;
        DeviceConfigUpdateDTO config = new DeviceConfigUpdateDTO();
        config.start_vacation = 1L;

        portal.updateControl(holiday, config);

        Matcher dates = Pattern.compile("startDate=([^&]+)&endDate=([^&]+)").matcher(lastSetpointRequest);
        assertTrue(dates.find(), lastSetpointRequest);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        LocalDateTime start = LocalDateTime.parse(URLDecoder.decode(dates.group(1), StandardCharsets.UTF_8), format);
        LocalDateTime end = LocalDateTime.parse(URLDecoder.decode(dates.group(2), StandardCharsets.UTF_8), format);
        assertEquals(7, java.time.Duration.between(start, end).toDays());
        long minutesAhead = java.time.Duration.between(LocalDateTime.now(ZoneId.of("Europe/Berlin")), start)
                .toMinutes();
        assertTrue(minutesAhead >= 3 && minutesAhead <= 6, "minutes ahead: " + minutesAhead);
    }

    @Test
    void clearsTheVacation() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO cancel = new ControlUpdateDTO();
        cancel.ch_mode = CH_MODE_AUTO;
        cancel.ch_mode_duration = 0L;
        cancel.vacation_duration = 0L;
        DeviceConfigUpdateDTO config = new DeviceConfigUpdateDTO();
        config.start_vacation = 0L;

        portal.updateControl(cancel, config);

        assertEquals("GET /Home/ClearVacation/?deviceId=" + DEVICE_ID, lastSetpointRequest);
    }

    @Test
    void savesTheDeviceFormWithOnlyTheChangeApplied() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.ch_vacation_temp = 14.5;

        portal.updateControl(new ControlUpdateDTO(), update);

        assertTrue(lastSetpointRequest.startsWith("POST /Device/EditDevice "), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("vacation_mode_temp=14.5"), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("Location.Street=Teststra%C3%9Fe"), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("__RequestVerificationToken=tok123"), lastSetpointRequest);
        assertTrue(lastSetpointRequest.contains("deviceId=" + DEVICE_ID), lastSetpointRequest);
    }

    @Test
    void sendsOffsetsAndBrightnessAsSingleFieldsThroughTheUpdateRoute() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.room_temp_offs = -0.5;
        update.outs_temp_offs = 0.5;
        update.disp_brightness = 40;
        update.language = 0;

        portal.updateControl(new ControlUpdateDTO(), update);

        assertTrue(lastUpdateRequest.contains("update_message[configuration][room_temp_offs]=-0.5"), lastUpdateRequest);
        assertTrue(lastUpdateRequest.contains("update_message[configuration][outs_temp_offs]=0.5"), lastUpdateRequest);
        assertTrue(lastUpdateRequest.contains("update_message[configuration][disp_brightness]=40"), lastUpdateRequest);
        assertTrue(lastUpdateRequest.contains("update_message[configuration][language]=0"), lastUpdateRequest);
        assertTrue(lastUpdateRequest.contains("update_message[device_id]=" + DEVICE_ID), lastUpdateRequest);
        assertEquals("", lastSetpointRequest, "the settings form is not posted for these settings");
    }

    @ParameterizedTest
    @CsvSource({ "extend_duration, 900", "fireplace_duration, 7200", "vacation_duration, 86400" })
    void storesAStandaloneDurationThroughTheUpdateRoute(String field, long seconds) throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        ControlUpdateDTO update = new ControlUpdateDTO();
        switch (field) {
            case "extend_duration" -> update.extend_duration = seconds;
            case "fireplace_duration" -> update.fireplace_duration = seconds;
            default -> update.vacation_duration = seconds;
        }

        portal.updateControl(update, null);

        assertTrue(lastUpdateRequest.contains("update_message[control][" + field + "]=" + seconds), lastUpdateRequest);
        assertFalse(lastUpdateRequest.contains("ch_mode"), "a stored duration must not start a mode");
        assertEquals("", lastSetpointRequest, "no web route is called");
        RetrieveReplyDTO stored = portal.retrieve();
        long value = switch (field) {
            case "extend_duration" -> stored.control.extend_duration;
            case "fireplace_duration" -> stored.control.fireplace_duration;
            default -> stored.control.vacation_duration;
        };
        assertEquals(seconds, value);
    }

    @Test
    void failsWhenThePortalDoesNotStoreTheDuration() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        ignoreUpdates = true;
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.extend_duration = 900L;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(update, null));
    }

    @Test
    void rejectsTwoDurationsWrittenTogether() {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO update = new ControlUpdateDTO();
        update.extend_duration = 900L;
        update.fireplace_duration = 3600L;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(update, null));
        assertEquals("", lastUpdateRequest);
    }

    @Test
    void failsWhenThePortalDoesNotStoreTheDirectSetting() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        ignoreUpdates = true;
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.room_temp_offs = -0.5;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(new ControlUpdateDTO(), update));
    }

    @Test
    void doesNotPostTheFormWhenNothingDiffers() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.ch_vacation_temp = 14.0;

        portal.updateControl(new ControlUpdateDTO(), update);

        assertEquals("", lastSetpointRequest);
        assertEquals("", lastUpdateRequest);
    }

    @Test
    void failsWhenThePortalDoesNotConfirmTheSave() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        portal.retrieve();
        saveReply = "<div class=\"validation-message error\">Invalid</div>";
        DeviceConfigUpdateDTO update = new DeviceConfigUpdateDTO();
        update.ch_vacation_temp = 14.5;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(new ControlUpdateDTO(), update));
    }

    private static ScheduleDTO schedule(double base, double[][] weekday) {
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = base;
        schedule.entries = new double[7][][];
        for (int day = 0; day < 7; day++) {
            schedule.entries[day] = weekday;
        }
        return schedule;
    }

    private static String program(String id, int type, boolean active, ScheduleDTO schedule) {
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        StringBuilder json = new StringBuilder(
                "{\"Key\":\"Program_" + DEVICE_ID + "_" + id + "\",\"Type\":\"Program\"");
        json.append(",\"ProgramId\":\"").append(id).append("\",\"DeviceId\":\"").append(DEVICE_ID).append("\"");
        json.append(",\"Name\":\"20-04-2026\",\"ScheduleType\":").append(type).append(",\"Active\":").append(active);
        json.append(",\"BaseTemp\":").append(schedule.base_temp);
        for (int day = 0; day < 7; day++) {
            json.append(",\"").append(days[day]).append("\":[");
            double[][] periods = schedule.entries[day];
            for (int i = 0; i < periods.length; i++) {
                json.append(i == 0 ? "" : ",").append("{\"StartTime\":").append((long) periods[i][0])
                        .append(",\"EndTime\":").append((long) periods[i][1]).append(",\"Temperature\":")
                        .append(periods[i][2]).append(",\"Type\":").append((day + 1) % 7).append("}");
            }
            json.append("]");
        }
        return json.append("}").toString();
    }

    @Test
    void writesTheActiveProgramInPlace() throws Exception {
        ScheduleDTO current = schedule(22.5, new double[][] { { 0, 240, 20.5 }, { 1230, 1440, 20.5 } });
        ScheduleDTO wanted = schedule(22.5,
                new double[][] { { 0, 240, 20.5 }, { 240, 1230, 23 }, { 1230, 1440, 20.5 } });
        ScheduleDTO other = schedule(18, new double[][] { { 0, 1440, 18 } });
        programs = "[" + program("old-1", 0, false, other) + "," + program("active-ch", 0, true, current) + ","
                + program("active-dhw", 1, true, other) + "]";
        programsAfterEdit = "[" + program("old-1", 0, false, other) + "," + program("active-ch", 0, true, wanted) + ","
                + program("active-dhw", 1, true, other) + "]";
        AtagOnePortalClient portal = client(PASSWORD);

        portal.updateChSchedule(wanted);

        String sent = URLDecoder.decode(lastSetpointRequest, StandardCharsets.UTF_8);
        assertTrue(sent.startsWith("POST /api/app/EditSchedule "), sent);
        assertTrue(sent.contains("ProgramId=active-ch"), sent);
        assertFalse(sent.contains("old-1") || sent.contains("active-dhw"), sent);
        assertTrue(sent.contains("ScheduleType=0&Active=true&BaseTemp=22.5"), sent);
        assertTrue(sent.contains("Monday[1][StartTime]=240&Monday[1][EndTime]=1230&Monday[1][Temperature]=23"), sent);
        assertTrue(sent.contains("Saturday[0][Type]=6"), sent);
        assertTrue(sent.contains("Sunday[2][Type]=0"), sent);
        assertEquals("Bearer test-token", lastScheduleAuth);
    }

    @Test
    void writesTheHotWaterProgramOfTheOtherKind() throws Exception {
        ScheduleDTO current = schedule(55, new double[][] { { 0, 1440, 45 } });
        ScheduleDTO wanted = schedule(55, new double[][] { { 0, 360, 45 }, { 360, 1440, 50 } });
        programs = "[" + program("active-ch", 0, true, current) + "," + program("active-dhw", 1, true, current) + "]";
        programsAfterEdit = "[" + program("active-ch", 0, true, current) + "," + program("active-dhw", 1, true, wanted)
                + "]";
        AtagOnePortalClient portal = client(PASSWORD);

        portal.updateDhwSchedule(wanted);

        String sent = URLDecoder.decode(lastSetpointRequest, StandardCharsets.UTF_8);
        assertTrue(sent.contains("ProgramId=active-dhw"), sent);
        assertTrue(sent.contains("ScheduleType=1&Active=true&BaseTemp=55"), sent);
    }

    @Test
    void failsWhenThePortalDoesNotStoreTheSchedule() {
        ScheduleDTO current = schedule(22.5, new double[][] { { 0, 1440, 20 } });
        ScheduleDTO wanted = schedule(22.5, new double[][] { { 0, 1440, 21 } });
        programs = "[" + program("active-ch", 0, true, current) + "]";
        programsAfterEdit = programs;
        AtagOnePortalClient portal = client(PASSWORD);

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateChSchedule(wanted));
    }

    @Test
    void refusesAScheduleThatIsNotAWholeWeek() {
        AtagOnePortalClient portal = client(PASSWORD);
        ScheduleDTO threeDays = new ScheduleDTO();
        threeDays.entries = new double[3][][];

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateChSchedule(threeDays));
        assertEquals("", lastSetpointRequest);
    }

    @Test
    void aPortalErrorOnLoginIsNotMistakenForBadCredentials() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        loginPostStatus = 503;

        AtagOneCommunicationException failure = assertThrows(AtagOneCommunicationException.class, portal::retrieve);
        assertFalse(failure instanceof AtagOneLoginException, failure.getMessage());

        loginPostStatus = 0;
        assertEquals(22.8, portal.retrieve().report.room_temp);
        assertEquals(2, loginPosts.get());
    }

    @Test
    void theLoginErrorFromThePortalIsShownWithoutMarkup() {
        loginErrorHtml = "<ul><li class=\"text-error\"><span><b>Bad</b>  login</span></li></ul>";
        AtagOnePortalClient portal = client("wrong");

        AtagOneLoginException failure = assertThrows(AtagOneLoginException.class, portal::retrieve);

        assertEquals("Bad login", failure.getMessage());
    }

    @Test
    void sanitisesAndCapsTheLoginError() {
        String raw = "<script>x</script> \"quoted\" back\\slash " + "y".repeat(300);

        String shown = AtagOnePortalClient.sanitiseLoginError(raw);

        assertEquals(200, shown.length());
        assertFalse(shown.contains("<") || shown.contains(">") || shown.contains("\"") || shown.contains("\\"), shown);
    }

    @Test
    void ignoresANullTimeToTarget() throws Exception {
        String inner = Objects
                .requireNonNull(new com.google.gson.Gson().fromJson(fixture("retrieve.json").trim(), String.class));
        com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(inner).getAsJsonObject();
        root.getAsJsonObject("retrieve_reply").getAsJsonObject("report").add("ch_time_to_Temp",
                com.google.gson.JsonNull.INSTANCE);

        assertEquals(0, AtagOnePortalClient.parseRetrieve(root.toString()).report.ch_time_to_temp);
    }

    @Test
    void readsTheSuccessFlagOfModeReplies() {
        assertTrue(AtagOnePortalClient.isSuccess("{\"success\":\"true\"}"));
        assertTrue(AtagOnePortalClient.isSuccess("{\"success\":true,\"mode\":\"schedule_active\"}"));
        assertFalse(AtagOnePortalClient.isSuccess("{\"success\":\"false\"}"));
        assertFalse(AtagOnePortalClient.isSuccess("{\"message\":\"true\"}"));
        assertFalse(AtagOnePortalClient.isSuccess("not json"));
    }

    @Test
    void failsWhenTheThermostatStoppedReportingToThePortal() {
        AtagOnePortalClient portal = client(PASSWORD, Clock.fixed(REPORT_TIME.plusSeconds(11 * 60), ZoneOffset.UTC));

        AtagOneCommunicationException failure = assertThrows(AtagOneCommunicationException.class, portal::retrieve);

        assertFalse(failure instanceof AtagOneLoginException);
        assertTrue(failure.getMessage().contains("not reporting"), failure.getMessage());
    }

    @Test
    void acceptsAReportFromTheLastTenMinutes() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD, Clock.fixed(REPORT_TIME.plusSeconds(9 * 60), ZoneOffset.UTC));

        assertEquals(22.8, portal.retrieve().report.room_temp);
    }

    @Test
    void twoCallersShareOneLogin() throws Exception {
        AtagOnePortalClient portal = client(PASSWORD);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<RetrieveReplyDTO> first = pool.submit(portal::retrieve);
            Future<RetrieveReplyDTO> second = pool.submit(portal::retrieve);
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, loginPosts.get());
    }

    @Test
    void rejectsWriteShapesThePortalHasNoCallFor() {
        AtagOnePortalClient portal = client(PASSWORD);
        ControlUpdateDTO holiday = new ControlUpdateDTO();
        holiday.ch_mode = CH_MODE_HOLIDAY;
        ControlUpdateDTO vacation = new ControlUpdateDTO();
        vacation.ch_mode = CH_MODE_AUTO;
        vacation.vacation_duration = 0L;
        ControlUpdateDTO autoWithTemp = new ControlUpdateDTO();
        autoWithTemp.ch_mode = CH_MODE_AUTO;
        autoWithTemp.ch_mode_temp = 22.5;

        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(holiday, null));
        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(vacation, null));
        assertThrows(AtagOneCommunicationException.class, () -> portal.updateControl(autoWithTemp, null));
        assertEquals("", lastSetpointRequest);
    }
}
