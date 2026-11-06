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
package org.openhab.binding.veluxactive.internal;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.veluxactive.internal.api.ModuleType;
import org.openhab.binding.veluxactive.internal.api.dto.ApiResponse;
import org.openhab.binding.veluxactive.internal.api.dto.HomeStatus;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.binding.veluxactive.internal.exception.ApiException;
import org.openhab.binding.veluxactive.internal.exception.ApiUnauthorizedException;

import com.google.gson.Gson;

/**
 * Verifies extended status fields and optional values against a representative response.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveHomeStatusTest {
    private final Gson gson = new Gson();

    @ParameterizedTest
    @CsvSource({ "NXG,GATEWAY", "NXS,INDOOR_SENSOR", "NXD,DEPARTURE_SWITCH", "NXO,OPENING" })
    void deserializesModuleTypes(String code, ModuleType expected) {
        var module = requireNonNull(gson.fromJson("{\"type\":\"" + code + "\"}", Module.class));
        assertThat(module.type, is(expected));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"type\":null}", "{\"type\":\"NXZ\"}", "{\"type\":\"\"}" })
    void preservesMissingOrUnknownModuleTypes(String json) {
        var module = requireNonNull(gson.fromJson(json, Module.class));
        assertThat(module.type, nullValue());
    }

    @Test
    void preservesModuleDiagnosticsAndRawRoomValues() {
        var response = requireNonNull(gson.fromJson("""
                {"status":"ok","time_server":1791635716,"body":{"home":{"id":"home","modules":[
                  {"id":"gateway","type":"NXG","busy":false,"calibrating":false,
                   "firmware_revision_netatmo":203,"firmware_revision_thirdparty":"0.1.2.0.43.0",
                   "hardware_version":236,"is_raining":false,"last_reset_type":"button_factory",
                   "last_seen":1791635715,"locked":true,"locking":false,"name":"VELUX gateway",
                   "pairing":"stop","secure":true,"wifi_strength":70,"wifi_state":"medium"},
                  {"id":"window","type":"NXO","current_position":0,"target_position":0,
                   "firmware_revision":53,"manufacturer":"Netatmo","mode":"algo_available",
                   "rain_position":50,"reachable":true,"secure_position":7,"silent":false,
                   "velux_type":"window","bridge":"gateway"},
                  {"id":"sensor","type":"NXS","battery_level":3883,"battery_percent":56,
                   "firmware_revision":16,"rf_strength":48,"battery_state":"medium","rf_state":"full"}
                ],"rooms":[{"id":"room","air_quality":1,"algo_status":304,"auto_close_ts":0,
                  "co2":715,"humidity":50,"lux":12,"max_comfort_co2":1150,
                  "max_comfort_humidity":70,"max_comfort_temperature":230,
                  "min_comfort_humidity":20,"min_comfort_temperature":180,"temperature":212},
                  {"id":"empty-room","algo_status":304,"auto_close_ts":0}]}}}
                """, ApiResponse.class));
        assertThat(response.timeServer, is(1791635716L));
        var status = requireNonNull(gson.fromJson(response.body, HomeStatus.class));
        var gateway = requireNonNull(status.module("gateway"));
        assertThat(gateway.firmwareRevisionThirdparty, is("0.1.2.0.43.0"));
        assertThat(gateway.isRaining, is(false));
        assertThat(gateway.reachable, nullValue());
        assertThat(gateway.wifiStrength, is(70));
        assertThat(gateway.lastResetType, is("button_factory"));
        var window = requireNonNull(status.module("window"));
        assertThat(window.currentPosition, is(0));
        assertThat(window.securePosition, is(7));
        assertThat(window.rainPosition, is(50));
        assertThat(window.veluxType, is("window"));
        assertThat(window.bridge, is("gateway"));
        var sensor = requireNonNull(status.module("sensor"));
        assertThat(sensor.batteryLevel, is(3883));
        assertThat(sensor.batteryPercent, is(56));
        assertThat(sensor.rfState, is("full"));
        var rooms = requireNonNull(requireNonNull(status.home).rooms);
        var room = requireNonNull(rooms.getFirst());
        assertThat(room.temperature, is(212));
        assertThat(room.maxComfortTemperature, is(230));
        assertThat(room.minComfortTemperature, is(180));
        assertThat(room.autoCloseTs, is(0L));
        assertThat(requireNonNull(rooms.get(1)).temperature, nullValue());
    }

    @Test
    void preservesErrorsWithMissingCodesAndMatchesTheirModule() {
        var status = requireNonNull(gson.fromJson("""
                {"errors":[null,{"id":"gateway","message":"Unavailable"}]}
                """, HomeStatus.class));
        assertThat(requireNonNull(status.error("gateway")).code, nullValue());
        assertThat(status.error("other"), nullValue());
    }

    @Test
    void distinguishesUnauthorizedFromOtherHttpFailures() {
        assertThrows(ApiUnauthorizedException.class, () -> VeluxActiveApi.parseResponse(401, ""));
        var error = assertThrows(ApiException.class, () -> VeluxActiveApi.parseResponse(503, ""));
        assertThat(error instanceof ApiUnauthorizedException, is(false));
    }
}
