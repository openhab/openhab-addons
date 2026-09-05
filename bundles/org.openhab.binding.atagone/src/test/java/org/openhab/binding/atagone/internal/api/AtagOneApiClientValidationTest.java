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
}
