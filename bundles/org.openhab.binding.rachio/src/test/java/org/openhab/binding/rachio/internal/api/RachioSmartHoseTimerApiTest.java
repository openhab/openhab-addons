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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStationListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgramListResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests Smart Hose Timer API payload and DTO helpers.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioSmartHoseTimerApiTest {
    @Test
    void baseStationListResponseParsesWrappedList() {
        String json = """
                {
                  "baseStations": [
                    {"id":"base-station-id","name":"Hub","serialNumber":"BS123","online":true}
                  ]
                }
                """;

        RachioBaseStationListResponse response = RachioBaseStationListResponse.fromJson(json);

        assertThat(response.baseStations.size(), is(1));
        assertThat(response.baseStations.get(0).id, is("base-station-id"));
        assertThat(response.baseStations.get(0).getThingName(), is("Hub"));
        assertThat(response.baseStations.get(0).isOnline(), is(true));
    }

    @Test
    void valveListResponseParsesValveStateMatches() {
        String json = """
                {
                  "valves": [
                    {
                      "id":"valve-id",
                      "baseStationId":"base-station-id",
                      "name":"Garden",
                      "serialNumber":"V123",
                      "defaultRuntimeSeconds":600,
                      "batteryLevel":87,
                      "state":{"matches":false,"flowDetected":true}
                    }
                  ]
                }
                """;

        RachioValveListResponse response = RachioValveListResponse.fromJson(json);

        assertThat(response.valves.size(), is(1));
        RachioValve valve = response.valves.get(0);
        assertThat(valve.id, is("valve-id"));
        assertThat(valve.baseStationId, is("base-station-id"));
        assertThat(valve.getDefaultRuntimeSeconds(), is(600));
        assertThat(valve.stateMatches(), is(false));
        assertThat(valve.flowDetected(), is(true));
    }

    @Test
    void getValveResponseParsesNestedValveObject() {
        String json = """
                {
                  "valve": {
                    "id":"valve-id",
                    "name":"Front Yard",
                    "valveState":{"matches":true}
                  }
                }
                """;

        RachioValve valve = RachioSmartHoseTimerGsonDTO.parseValve(json);

        assertThat(valve.id, is("valve-id"));
        assertThat(valve.getThingName(), is("Front Yard"));
        assertThat(valve.stateMatches(), is(true));
    }

    @Test
    void setDefaultRuntimePayloadContainsDocumentedFields() {
        JsonObject json = JsonParser.parseString(RachioApi.buildValveDefaultRuntimePayload("valve-id", 900))
                .getAsJsonObject();

        assertThat(json.get("valveId").getAsString(), is("valve-id"));
        assertThat(json.get("defaultRuntimeSeconds").getAsInt(), is(900));
    }

    @Test
    void startWateringPayloadContainsDocumentedFields() {
        JsonObject json = JsonParser.parseString(RachioApi.buildValveStartWateringPayload("valve-id", 300))
                .getAsJsonObject();

        assertThat(json.get("valveId").getAsString(), is("valve-id"));
        assertThat(json.get("durationSeconds").getAsInt(), is(300));
    }

    @Test
    void stopWateringPayloadContainsValveId() {
        JsonObject json = JsonParser.parseString(RachioApi.buildValveStopWateringPayload("valve-id")).getAsJsonObject();

        assertThat(json.get("valveId").getAsString(), is("valve-id"));
    }

    @Test
    void valveProgramListResponseParsesV2Programs() {
        String json = """
                {
                  "programs": [
                    {
                      "id":"program-id",
                      "name":"Morning Hose",
                      "enabled":true,
                      "programType":"FIXED",
                      "resourceId":{"valveId":"valve-id","baseStationId":"base-station-id"},
                      "durationSeconds":900,
                      "daysOfWeek":["MONDAY","WEDNESDAY"]
                    }
                  ]
                }
                """;

        RachioValveProgramListResponse response = RachioValveProgramListResponse.fromJson(json);

        assertThat(response.programs.size(), is(1));
        RachioValveProgram program = response.programs.get(0);
        assertThat(program.id, is("program-id"));
        assertThat(program.getThingName(), is("Morning Hose"));
        assertThat(program.getValveId(), is("valve-id"));
        assertThat(program.getBaseStationId(), is("base-station-id"));
        assertThat(program.getDurationSeconds(), is(900));
        assertThat(program.getDaysOfWeek(), is("[\"MONDAY\",\"WEDNESDAY\"]"));
    }

    @Test
    void valveDayViewsResponseFindsUpcomingSkippedAndCompletedRuns() {
        String yesterday = Instant.now().minusSeconds(86400).toString();
        String tomorrow = Instant.now().plusSeconds(86400).toString();
        String nextWeek = Instant.now().plusSeconds(604800).toString();
        String json = """
                {
                  "dayViews": [
                    {
                      "plannedRuns": [
                        {
                          "plannedRunId":"planned-next",
                          "programId":"program-id",
                          "valveId":"valve-id",
                          "plannedRunStartTime":"%s",
                          "durationSeconds":600,
                          "skipped":false
                        },
                        {
                          "plannedRunId":"planned-skipped",
                          "programId":"program-id",
                          "valveId":"valve-id",
                          "plannedRunStartTime":"%s",
                          "durationSeconds":300,
                          "skipped":true
                        }
                      ],
                      "completedRuns": [
                        {
                          "plannedRunId":"completed-run",
                          "programId":"program-id",
                          "valveId":"valve-id",
                          "startTime":"%s",
                          "durationSeconds":120,
                          "status":"COMPLETED"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(tomorrow, nextWeek, yesterday);

        RachioValveDayViewsResponse response = RachioValveDayViewsResponse.fromJson(json);

        assertThat(response.dayViews.size(), is(1));
        assertThat(response.findNextPlannedRun().orElseThrow().getPlannedRunId(), is("planned-next"));
        assertThat(response.findNextSkippedRun().orElseThrow().getPlannedRunId(), is("planned-skipped"));
        assertThat(response.findLastCompletedRun().orElseThrow().getPlannedRunId(), is("completed-run"));
    }

    @Test
    void valveDayViewsPayloadContainsResourceIdAndDateWindow() {
        JsonObject json = JsonParser.parseString(RachioApi.buildValveDayViewsPayload("valve-id",
                LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-08"))).getAsJsonObject();

        assertThat(json.getAsJsonObject("start").get("date").getAsString(), is("2026-05-01"));
        assertThat(json.getAsJsonObject("end").get("date").getAsString(), is("2026-05-08"));
        assertThat(json.getAsJsonObject("resourceId").get("valveId").getAsString(), is("valve-id"));
    }

    @Test
    void skipOverridePayloadsContainDocumentedIdentifiers() {
        JsonObject programSkip = JsonParser
                .parseString(RachioApi.buildProgramSkipOverridePayload("program-id", "2026-05-20T06:00:00Z"))
                .getAsJsonObject();
        JsonObject plannedRunSkip = JsonParser
                .parseString(RachioApi.buildPlannedRunSkipOverridePayload("planned-run-id", "2026-05-20"))
                .getAsJsonObject();

        assertThat(programSkip.get("programId").getAsString(), is("program-id"));
        assertThat(programSkip.get("timestamp").getAsString(), is("2026-05-20T06:00:00Z"));
        assertThat(plannedRunSkip.get("plannedRunId").getAsString(), is("planned-run-id"));
        assertThat(plannedRunSkip.get("date").getAsString(), is("2026-05-20"));
    }
}
