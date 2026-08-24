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
import org.junit.jupiter.api.Test;
import org.openhab.binding.atagone.internal.dto.ControlDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigDTO;
import org.openhab.binding.atagone.internal.dto.ReportDTO;
import org.openhab.binding.atagone.internal.dto.ReportDetailsDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;

/**
 * Verifies that {@link AtagOneApiClient#validateComplete} rejects a reply with a missing section instead of
 * letting a DTO with null fields reach the handler, where it would NPE {@code updateChannels()} and — thrown
 * from a {@code scheduleWithFixedDelay} task — silently and permanently stop all future polls.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
class AtagOneApiClientValidationTest {

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
}
