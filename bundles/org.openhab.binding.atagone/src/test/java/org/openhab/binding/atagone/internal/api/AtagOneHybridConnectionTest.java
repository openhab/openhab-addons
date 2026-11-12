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
import static org.mockito.Mockito.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.ReportDTO;
import org.openhab.binding.atagone.internal.dto.ReportDetailsDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;

/**
 * Tests {@link AtagOneHybridConnection}.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class AtagOneHybridConnectionTest {

    private static final Set<String> PORTAL_GAPS = Set.of("heating#modulation-level");

    private @Mock @NonNullByDefault({}) AtagOneConnection portal;
    private @Mock @NonNullByDefault({}) AtagOneConnection local;
    private @NonNullByDefault({}) AtagOneHybridConnection hybrid;

    @BeforeEach
    void setUp() {
        hybrid = new AtagOneHybridConnection(portal);
    }

    private static RetrieveReplyDTO reply(int modulation) {
        RetrieveReplyDTO reply = new RetrieveReplyDTO();
        reply.report = new ReportDTO();
        reply.report.room_temp = 22.5;
        reply.report.details = new ReportDetailsDTO();
        reply.report.details.rel_mod_level = modulation;
        return reply;
    }

    @Test
    void withoutALocalSourceThePortalAnswersAlone() throws Exception {
        when(portal.retrieve()).thenReturn(reply(0));
        when(portal.unsupportedChannels()).thenReturn(PORTAL_GAPS);

        assertEquals(0, hybrid.retrieve().report.details.rel_mod_level);
        assertEquals(PORTAL_GAPS, hybrid.unsupportedChannels());
    }

    @Test
    void takesTheReportDetailsFromTheLocalSource() throws Exception {
        when(portal.retrieve()).thenReturn(reply(0));
        when(local.retrieve()).thenReturn(reply(37));
        hybrid.setDetailsSource(local);

        RetrieveReplyDTO result = hybrid.retrieve();

        assertEquals(37, result.report.details.rel_mod_level);
        assertEquals(22.5, result.report.room_temp);
        assertEquals(Set.of(), hybrid.unsupportedChannels());
    }

    @Test
    void aFailingLocalReadKeepsThePollAndTheGaps() throws Exception {
        when(portal.retrieve()).thenReturn(reply(0));
        when(portal.unsupportedChannels()).thenReturn(PORTAL_GAPS);
        when(local.retrieve()).thenThrow(new AtagOneCommunicationException("unreachable"));
        hybrid.setDetailsSource(local);

        RetrieveReplyDTO result = hybrid.retrieve();

        assertEquals(0, result.report.details.rel_mod_level);
        assertEquals(PORTAL_GAPS, hybrid.unsupportedChannels());
    }

    @Test
    void theGapsComeBackWhenTheLocalSourceStopsAnswering() throws Exception {
        when(portal.retrieve()).thenReturn(reply(0), reply(0));
        when(portal.unsupportedChannels()).thenReturn(PORTAL_GAPS);
        when(local.retrieve()).thenReturn(reply(37)).thenThrow(new AtagOneCommunicationException("unreachable"));
        hybrid.setDetailsSource(local);

        hybrid.retrieve();
        assertEquals(Set.of(), hybrid.unsupportedChannels());
        hybrid.retrieve();
        assertEquals(PORTAL_GAPS, hybrid.unsupportedChannels());
    }

    @Test
    void aPortalFailureIsNotHiddenByTheLocalSource() throws Exception {
        when(portal.retrieve()).thenThrow(new AtagOneLoginException("rejected"));
        hybrid.setDetailsSource(local);

        assertThrows(AtagOneLoginException.class, hybrid::retrieve);
        verify(local, never()).retrieve();
    }

    @Test
    void writesGoThroughThePortal() throws Exception {
        hybrid.setDetailsSource(local);
        ControlUpdateDTO control = new ControlUpdateDTO();
        ScheduleDTO schedule = new ScheduleDTO();

        hybrid.updateControl(control, null);
        hybrid.updateChSchedule(schedule);
        hybrid.updateDhwSchedule(schedule);

        verify(portal).updateControl(control, null);
        verify(portal).updateChSchedule(schedule);
        verify(portal).updateDhwSchedule(schedule);
        verifyNoInteractions(local);
    }
}
