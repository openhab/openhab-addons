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

import java.util.Objects;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Live integration tests for {@link AtagOneApiClient} against a real ATAG ONE thermostat.
 * <p>
 * Run with: {@code mvnw.cmd test -pl :org.openhab.binding.atagone -DskipChecks -Datag.host=<ip>}
 * <p>
 * The {@link EnabledIfSystemProperty} guard means normal builds skip this class silently;
 * it only executes when {@code -Datag.host} is supplied on the command line.
 *
 * @author Florian Lettner - Initial contribution
 */
@EnabledIfSystemProperty(named = "atag.host", matches = ".+", disabledReason = "Only for manual execution against a real device.")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AtagOneApiClientLiveTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtagOneApiClientLiveTest.class);

    /** Stable pseudo-MAC used as the client identifier for all test runs. */
    private static final String TEST_CLIENT_ID = "AA:BB:CC:DD:EE:FF";

    private static HttpClient httpClient;
    private static AtagOneApiClient apiClient;

    @BeforeAll
    static void setUp() throws Exception {
        String host = Objects.requireNonNull(System.getProperty("atag.host"));
        Integer portProp = Integer.getInteger("atag.port", 10000);
        int port = portProp != null ? portProp : 10000;
        LOGGER.info("=== ATAG ONE integration test  host={}  port={}", host, port);
        httpClient = new HttpClient();
        httpClient.start();
        apiClient = new AtagOneApiClient(httpClient, host, port, TEST_CLIENT_ID);
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (httpClient != null) {
            httpClient.stop();
        }
    }

    // ── Test 1: pairing ───────────────────────────────────────────────────────

    /**
     * pair() returns 1 (pending) if the user must press Accept, 2 (granted) if the device
     * auto-accepts. Some firmware versions return 0 for an open-LAN client — the raw JSON is
     * logged so it can be inspected. We assert only that the call completes without throwing;
     * retrieve() performs the authoritative auth check.
     */
    @Test
    @Order(1)
    void pairCompletesWithoutException() throws AtagOneCommunicationException {
        int accStatus = apiClient.pair();
        LOGGER.info("pair() → acc_status={} (1=pending, 2=granted, 3=denied, 0=unknown/open)", accStatus);
        if (accStatus == 1) {
            LOGGER.warn("acc_status=1 (pending) — press Accept on the thermostat display, then re-run the test");
        } else if (accStatus == 3) {
            fail("Device denied pairing (acc_status=3). Remove the binding's MAC from the device and retry.");
        }
        assertTrue(accStatus >= 0, "acc_status must be non-negative, got: " + accStatus);
    }

    // ── Test 2: retrieve ──────────────────────────────────────────────────────

    @Test
    @Order(2)
    void retrieveReturnsFullReport() throws AtagOneCommunicationException {
        RetrieveReplyDTO r = apiClient.retrieve();

        LOGGER.info("--- status ---");
        LOGGER.info("  device_id        : {}", r.status.device_id);
        LOGGER.info("  date_time        : {}", r.status.date_time);

        LOGGER.info("--- report ---");
        LOGGER.info("  room_temp        : {} °C", r.report.room_temp);
        LOGGER.info("  outside_temp     : {} °C", r.report.outside_temp);
        LOGGER.info("  ch_water_temp    : {} °C", r.report.ch_water_temp);
        LOGGER.info("  ch_return_temp   : {} °C", r.report.ch_return_temp);
        LOGGER.info("  ch_water_pres    : {} bar", r.report.ch_water_pres);
        LOGGER.info("  ch_setpoint      : {} °C", r.report.ch_setpoint);
        LOGGER.info("  boiler_status    : {} (0x{})", r.report.boiler_status,
                Integer.toHexString(r.report.boiler_status).toUpperCase());
        LOGGER.info("  flame            : {}", (r.report.boiler_status & 0x100) != 0);
        LOGGER.info("  burner_on        : {}", (r.report.boiler_status & 0x008) != 0);
        LOGGER.info("  ch_active        : {}", (r.report.boiler_status & 0x004) != 0);
        LOGGER.info("  dhw_active       : {}", (r.report.boiler_status & 0x010) != 0);
        LOGGER.info("  burning_hours    : {}", r.report.burning_hours);
        LOGGER.info("  dhw_water_temp   : {} °C", r.report.dhw_water_temp);
        LOGGER.info("  shown_set_temp   : {} °C", r.report.shown_set_temp);
        LOGGER.info("  tout_avg         : {} °C", r.report.tout_avg);
        LOGGER.info("  rssi             : {}", r.report.rssi);
        LOGGER.info("  power_cons       : {} W", r.report.power_cons);
        LOGGER.info("  voltage          : {}", r.report.voltage);
        LOGGER.info("  current          : {}", r.report.current);
        LOGGER.info("  dhw_flow_rate    : {} L/min", r.report.dhw_flow_rate);
        LOGGER.info("  resets           : {}", r.report.resets);
        LOGGER.info("  device_errors    : '{}'", r.report.device_errors);
        LOGGER.info("  boiler_errors    : '{}'", r.report.boiler_errors);
        LOGGER.info("  ch_time_to_temp  : {} s", r.report.ch_time_to_temp);

        LOGGER.info("--- report.details ---");
        LOGGER.info("  boiler_temp      : {} °C", r.report.details.boiler_temp);
        LOGGER.info("  boiler_return_temp: {} °C", r.report.details.boiler_return_temp);
        LOGGER.info("  rel_mod_level    : {}%", r.report.details.rel_mod_level);
        LOGGER.info("  min_mod_level    : {}%", r.report.details.min_mod_level);
        LOGGER.info("  max_boiler_temp  : {} °C", r.report.details.max_boiler_temp);
        LOGGER.info("  regulation_state : {}", r.report.details.regulation_state);
        LOGGER.info("  target_temp      : {} °C", r.report.details.target_temp);

        LOGGER.info("--- control ---");
        LOGGER.info("  ch_mode          : {}", r.control.ch_mode);
        LOGGER.info("  ch_control_mode  : {}", r.control.ch_control_mode);
        LOGGER.info("  ch_mode_temp     : {} °C", r.control.ch_mode_temp);
        LOGGER.info("  ch_mode_duration : {} s", r.control.ch_mode_duration);
        LOGGER.info("  dhw_temp_setp    : {} °C", r.control.dhw_temp_setp);
        LOGGER.info("  dhw_mode         : {}", r.control.dhw_mode);
        LOGGER.info("  weather_temp     : {} °C", r.control.weather_temp);
        LOGGER.info("  weather_status   : {}", r.control.weather_status);
        LOGGER.info("  vacation_duration: {} s", r.control.vacation_duration);
        LOGGER.info("  extend_duration  : {} s", r.control.extend_duration);
        LOGGER.info("  fireplace_duration: {} s", r.control.fireplace_duration);

        LOGGER.info("--- configuration ---");
        LOGGER.info("  ch_min_set       : {} °C", r.configuration.ch_min_set);
        LOGGER.info("  ch_max_set       : {} °C", r.configuration.ch_max_set);
        LOGGER.info("  dhw_min_set      : {} °C", r.configuration.dhw_min_set);
        LOGGER.info("  dhw_max_set      : {} °C", r.configuration.dhw_max_set);
        LOGGER.info("  ch_vacation_temp : {} °C", r.configuration.ch_vacation_temp);
        LOGGER.info("  frost_prot_enabled: {}", r.configuration.frost_prot_enabled);
        LOGGER.info("  frost_prot_temp_room: {} °C", r.configuration.frost_prot_temp_room);
        LOGGER.info("  summer_eco_mode  : {}", r.configuration.summer_eco_mode);
        LOGGER.info("  dhw_legion_enabled: {}", r.configuration.dhw_legion_enabled);
        LOGGER.info("  dhw_legion_day   : {}", r.configuration.dhw_legion_day);
        LOGGER.info("  disp_brightness  : {}", r.configuration.disp_brightness);
        LOGGER.info("  start_vacation   : {}", r.configuration.start_vacation);
        LOGGER.info("  ch_mode_vacation : {} s", r.configuration.ch_mode_vacation);
        LOGGER.info("  boiler_id        : {}", r.configuration.boiler_id);

        // ── Structural assertions ──────────────────────────────────────────────

        assertFalse(r.status.device_id.isEmpty(), "device_id must not be empty");

        assertTrue(r.report.room_temp >= 5 && r.report.room_temp <= 35,
                "room_temp out of plausible range: " + r.report.room_temp);

        assertTrue(r.report.outside_temp >= -30 && r.report.outside_temp <= 50,
                "outside_temp out of plausible range: " + r.report.outside_temp);

        assertTrue(r.report.ch_water_pres >= 0.3 && r.report.ch_water_pres <= 4.0,
                "ch_water_pres out of plausible range: " + r.report.ch_water_pres);

        assertTrue(r.report.burning_hours > 0, "burning_hours should be positive for a used boiler");

        assertTrue(r.report.details.rel_mod_level >= 0 && r.report.details.rel_mod_level <= 100,
                "rel_mod_level out of range: " + r.report.details.rel_mod_level);

        assertTrue(r.control.ch_mode >= 1 && r.control.ch_mode <= 6, "ch_mode out of range: " + r.control.ch_mode);

        assertTrue(r.control.dhw_temp_setp > 0, "dhw_temp_setp should be positive: " + r.control.dhw_temp_setp);

        assertTrue(r.configuration.ch_min_set > 0 && r.configuration.ch_min_set < r.configuration.ch_max_set,
                "ch_min_set/ch_max_set implausible: " + r.configuration.ch_min_set + "/" + r.configuration.ch_max_set);

        assertTrue(r.configuration.dhw_min_set > 0 && r.configuration.dhw_min_set < r.configuration.dhw_max_set,
                "dhw_min_set/dhw_max_set implausible: " + r.configuration.dhw_min_set + "/"
                        + r.configuration.dhw_max_set);
    }

    // ── Test 3: safe write ────────────────────────────────────────────────────

    @Test
    @Order(3)
    void updateControlRoundTrip() throws AtagOneCommunicationException {
        // Read current DHW setpoint, then write it back — no change to the boiler.
        RetrieveReplyDTO before = apiClient.retrieve();
        double currentDhwSetp = before.control.dhw_temp_setp;
        LOGGER.info("updateControl round-trip: dhw_temp_setp = {}", currentDhwSetp);

        ControlUpdateDTO update = new ControlUpdateDTO();
        update.dhw_temp_setp = currentDhwSetp;

        // Should complete without throwing
        assertDoesNotThrow(() -> apiClient.updateControl(update));

        // Read back and verify value is unchanged
        RetrieveReplyDTO after = apiClient.retrieve();
        assertEquals(currentDhwSetp, after.control.dhw_temp_setp, 0.5,
                "DHW setpoint changed unexpectedly after no-op write");

        LOGGER.info("updateControl round-trip: OK (dhw_temp_setp still {})", after.control.dhw_temp_setp);
    }
}
