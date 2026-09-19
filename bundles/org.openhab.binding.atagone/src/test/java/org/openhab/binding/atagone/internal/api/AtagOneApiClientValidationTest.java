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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atagone.internal.dto.ControlDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.ReportDTO;
import org.openhab.binding.atagone.internal.dto.ReportDetailsDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.openhab.binding.atagone.internal.dto.SchedulesDTO;

import com.google.gson.JsonObject;

/**
 * Verifies that {@link AtagOneApiClient#validateComplete} rejects a reply with a missing section instead of
 * letting a DTO with null fields reach the handler, where it would NPE {@code updateChannels()} and — thrown
 * from a {@code scheduleWithFixedDelay} task — silently and permanently stop all future polls.
 * <p>
 * Also verifies {@link AtagOneApiClient#parseReplyObject}, which guards {@code pair()}, {@code retrieve()},
 * and {@code updateControl()} against a malformed/non-JSON HTTP body — a realistic failure mode given the
 * device's documented HTTP/1.0 flakiness — throwing {@link AtagOneCommunicationException} instead of letting
 * an unchecked Gson exception escape into a caller (e.g. {@code doPair()}) that only catches the checked type.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagOneApiClientValidationTest {

    private final AtagOneApiClient client = new AtagOneApiClient(new HttpClient(), "localhost", 10000,
            "AA:BB:CC:DD:EE:FF");

    private RetrieveReplyDTO completeReply() {
        RetrieveReplyDTO dto = new RetrieveReplyDTO();
        dto.report = new ReportDTO();
        dto.report.details = new ReportDetailsDTO();
        dto.control = new ControlDTO();
        dto.schedules = new SchedulesDTO();
        dto.schedules.ch_schedule = new ScheduleDTO();
        dto.schedules.dhw_schedule = new ScheduleDTO();
        dto.configuration = new DeviceConfigDTO();
        return dto;
    }

    @Test
    void acceptsCompleteReply() {
        assertDoesNotThrow(() -> AtagOneApiClient.validateComplete(completeReply()));
    }

    @Test
    void rejectsMissingReport() {
        RetrieveReplyDTO dto = completeReply();
        dto.report = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingControl() {
        RetrieveReplyDTO dto = completeReply();
        dto.control = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingConfiguration() {
        RetrieveReplyDTO dto = completeReply();
        dto.configuration = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingReportDetails() {
        RetrieveReplyDTO dto = completeReply();
        dto.report.details = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingSchedules() {
        RetrieveReplyDTO dto = completeReply();
        dto.schedules = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingChSchedule() {
        RetrieveReplyDTO dto = completeReply();
        dto.schedules.ch_schedule = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void rejectsMissingDhwSchedule() {
        RetrieveReplyDTO dto = completeReply();
        dto.schedules.dhw_schedule = null;
        assertThrows(AtagOneCommunicationException.class, () -> AtagOneApiClient.validateComplete(dto));
    }

    @Test
    void parseReplyObjectAcceptsWellFormedResponse() throws AtagOneCommunicationException {
        JsonObject reply = client.parseReplyObject("{\"pair_reply\":{\"acc_status\":2}}", "pair_reply");
        assertEquals(2, reply.get("acc_status").getAsInt());
    }

    @Test
    void parseReplyObjectRejectsEmptyBody() {
        assertThrows(AtagOneCommunicationException.class, () -> client.parseReplyObject("", "pair_reply"));
    }

    @Test
    void parseReplyObjectRejectsMalformedJson() {
        assertThrows(AtagOneCommunicationException.class,
                () -> client.parseReplyObject("{not valid json", "pair_reply"));
    }

    @Test
    void parseReplyObjectRejectsNonObjectRoot() {
        assertThrows(AtagOneCommunicationException.class, () -> client.parseReplyObject("[1,2,3]", "pair_reply"));
    }

    @Test
    void parseReplyObjectRejectsMissingReplyKey() {
        assertThrows(AtagOneCommunicationException.class,
                () -> client.parseReplyObject("{\"something_else\":{}}", "pair_reply"));
    }

    /**
     * Confirmed live (2026-09-16): serializing {@code entries}' start/end as floats (Gson's default
     * for a {@code double[][][]}, e.g. {@code 0.0}/{@code 240.0}) makes the device silently wipe the
     * whole schedule to empty while still returning {@code acc_status:2} — no error, no {@code resets}
     * bump, nothing to distinguish it from a real success except an independent read-back. The device's
     * own wire format (every {@code /retrieve} reply, and every write that has ever actually applied)
     * uses bare integers for start/end, a float only for temp.
     */
    @Test
    void scheduleToJsonEmitsStartEndAsIntegersAndTempAsFloat() {
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = 22.5;
        schedule.entries = new double[][][] { { { 0, 240, 20.5 }, { 1230, 1440, 20.5 } } };

        String json = AtagOneApiClient.scheduleToJson(schedule).toString();

        assertTrue(json.contains("[0,240,20.5]"), () -> "expected integer start/end, float temp, got: " + json);
        assertFalse(json.contains("0.0"), () -> "start/end must not serialize with a decimal point: " + json);
    }

    @Test
    void scheduleToJsonHandlesEmptyAndNullDaysWithoutThrowing() {
        ScheduleDTO schedule = new ScheduleDTO();
        schedule.base_temp = 22.5;
        double[][][] entries = new double[3][][];
        entries[0] = new double[0][]; // explicitly empty day
        entries[1] = new double[][] { { 600, 900, 19.0 } };
        // entries[2] stays null — a defensively-possible shape, mirrored from ScheduleJson's own handling.
        schedule.entries = entries;

        String json = AtagOneApiClient.scheduleToJson(schedule).toString();

        assertTrue(json.contains("\"entries\":[[],[[600,900,19.0]],[]]"), () -> "unexpected shape: " + json);
    }
}
