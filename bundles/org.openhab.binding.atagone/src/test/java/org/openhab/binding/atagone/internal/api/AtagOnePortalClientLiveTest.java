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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;

/**
 * Read-only live test of {@link AtagOnePortalClient} against the real portal.
 * <p>
 * Run with: {@code mvnw.cmd test -pl :org.openhab.binding.atagone -DskipChecks
 * -Datag.portal.credentials=<file with email= and password=> [-Datag.host=<ip>]}
 * <p>
 * With {@code atag.host} it also reads the local API and prints both values side by side.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
@EnabledIfSystemProperty(named = "atag.portal.credentials", matches = ".+", disabledReason = "Only for manual execution against the real portal.")
class AtagOnePortalClientLiveTest {

    private static final String LOCAL_CLIENT_ID = "AA:BB:CC:DD:EE:FF";

    private static void row(String name, @Nullable Object remote, @Nullable Object local) {
        System.out.printf("LIVE | %-20s | remote=%-28s | local=%s%n", name, remote, local);
    }

    @Test
    void readsStateFromThePortal() throws Exception {
        Properties credentials = new Properties();
        try (Reader in = Files.newBufferedReader(Path.of(System.getProperty("atag.portal.credentials", "")),
                StandardCharsets.UTF_8)) {
            credentials.load(in);
        } catch (IOException e) {
            fail("Cannot read the credentials file: " + e.getMessage());
        }
        String email = credentials.getProperty("email", "").trim();
        String password = credentials.getProperty("password", "").trim();
        assertFalse(email.isEmpty() || password.isEmpty(), "email= and password= must both be filled in");

        HttpClient portalHttp = new HttpClient(new org.eclipse.jetty.util.ssl.SslContextFactory.Client());
        portalHttp.start();
        RetrieveReplyDTO remote;
        try {
            AtagOnePortalClient portalClient = new AtagOnePortalClient(portalHttp, email, password);
            remote = portalClient.retrieve();
        } finally {
            portalHttp.stop();
        }

        RetrieveReplyDTO local = null;
        String host = System.getProperty("atag.host");
        if (host != null && !host.isBlank()) {
            HttpClient localHttp = new HttpClient();
            localHttp.start();
            try {
                local = new AtagOneApiClient(localHttp, host, 10000, LOCAL_CLIENT_ID).retrieve();
            } catch (AtagOneCommunicationException e) {
                System.out.println("LIVE | local read failed: " + e.getMessage());
            } finally {
                localHttp.stop();
            }
        }

        row("report_time", AtagEpoch.toZonedDateTime(remote.report.report_time),
                local == null ? "-" : AtagEpoch.toZonedDateTime(local.report.report_time));
        row("room_temp", remote.report.room_temp, local == null ? "-" : local.report.room_temp);
        row("outside_temp", remote.report.outside_temp, local == null ? "-" : local.report.outside_temp);
        row("tout_avg", remote.report.tout_avg, local == null ? "-" : local.report.tout_avg);
        row("ch_water_temp", remote.report.ch_water_temp, local == null ? "-" : local.report.ch_water_temp);
        row("ch_return_temp", remote.report.ch_return_temp, local == null ? "-" : local.report.ch_return_temp);
        row("ch_water_pres", remote.report.ch_water_pres, local == null ? "-" : local.report.ch_water_pres);
        row("ch_setpoint", remote.report.ch_setpoint, local == null ? "-" : local.report.ch_setpoint);
        row("dhw_water_temp", remote.report.dhw_water_temp, local == null ? "-" : local.report.dhw_water_temp);
        row("burning_hours", remote.report.burning_hours, local == null ? "-" : local.report.burning_hours);
        row("boiler_status", remote.report.boiler_status, local == null ? "-" : local.report.boiler_status);
        row("rssi", remote.report.rssi, local == null ? "-" : local.report.rssi);
        row("voltage", remote.report.voltage, local == null ? "-" : local.report.voltage);
        row("resets", remote.report.resets, local == null ? "-" : local.report.resets);
        row("pcb_temp", remote.report.pcb_temp, local == null ? "-" : local.report.pcb_temp);
        row("dhw_flow_rate", remote.report.dhw_flow_rate, local == null ? "-" : local.report.dhw_flow_rate);
        row("ch_time_to_temp", remote.report.ch_time_to_temp, local == null ? "-" : local.report.ch_time_to_temp);
        row("device_errors", remote.report.device_errors, local == null ? "-" : local.report.device_errors);
        row("boiler_errors", remote.report.boiler_errors, local == null ? "-" : local.report.boiler_errors);
        row("dhw_water_pres", remote.report.dhw_water_pres, local == null ? "-" : local.report.dhw_water_pres);
        row("shown_set_temp", remote.report.shown_set_temp, local == null ? "-" : local.report.shown_set_temp);
        row("ch_mode", remote.control.ch_mode, local == null ? "-" : local.control.ch_mode);
        row("ch_mode_temp", remote.control.ch_mode_temp, local == null ? "-" : local.control.ch_mode_temp);
        row("dhw_temp_setp", remote.control.dhw_temp_setp, local == null ? "-" : local.control.dhw_temp_setp);
        row("weather_temp", remote.control.weather_temp, local == null ? "-" : local.control.weather_temp);
        row("time_zone", remote.configuration.time_zone, local == null ? "-" : local.configuration.time_zone);
        row("device_id", remote.status.device_id.isEmpty() ? "empty" : "present",
                local == null ? "-" : local.status.device_id.equals(remote.status.device_id));
        row("ch_schedule_days", Objects.requireNonNull(remote.schedules.ch_schedule).entries.length,
                local == null ? "-" : Objects.requireNonNull(local.schedules.ch_schedule).entries.length);
        row("dhw_schedule_days", Objects.requireNonNull(remote.schedules.dhw_schedule).entries.length,
                local == null ? "-" : Objects.requireNonNull(local.schedules.dhw_schedule).entries.length);
    }
}
