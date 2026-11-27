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
 * Verifies {@link AtagOneApiClient#validateComplete} and {@link AtagOneApiClient#parseReplyObject}.
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
        schedule.entries = entries;

        String json = AtagOneApiClient.scheduleToJson(schedule).toString();

        assertTrue(json.contains("\"entries\":[[],[[600,900,19.0]],[]]"), () -> "unexpected shape: " + json);
    }
}
