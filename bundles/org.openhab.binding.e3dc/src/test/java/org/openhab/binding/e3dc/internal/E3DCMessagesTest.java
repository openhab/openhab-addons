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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * Tests the reading of the stored messages of the energy manager.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCMessagesTest {

    private static RscpValue message(int type, String source, String text, int code) {
        return RscpValue.container(RscpTag.EMS_ERROR_CONTAINER, List.of(RscpValue.uchar8(RscpTag.EMS_ERROR_TYPE, type),
                RscpValue.cstring(RscpTag.EMS_ERROR_SOURCE, source), RscpValue.cstring(RscpTag.EMS_ERROR_MESSAGE, text),
                RscpValue.int32(RscpTag.EMS_ERROR_CODE, code)));
    }

    private static RscpValue response(RscpValue... messages) {
        return RscpValue.container(0x00000000,
                List.of(RscpValue.container(RscpTag.EMS_STORED_ERRORS, List.of(messages))));
    }

    @Test
    void readsEveryMessageInTheOrderOfTheResponse() {
        List<E3DCMessages.Message> messages = E3DCMessages
                .fromResponse(response(message(1, "EMS", "A schedule is being executed! (AI360)", 32803),
                        message(1, "SmgwEmsManipul", "Smart meter gateway lost connection", 49154)));
        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals(32803, messages.get(0).code());
        assertEquals("SmgwEmsManipul", messages.get(1).source());
    }

    @Test
    void noMessagesGiveAnEmptyListAndAnEmptyArray() {
        List<E3DCMessages.Message> messages = E3DCMessages.fromResponse(response());
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
        assertEquals("[]", E3DCMessages.toJson(messages));
    }

    @Test
    void aResponseWithoutTheAnswerSaysNothingAboutTheMessages() {
        assertNull(E3DCMessages
                .fromResponse(RscpValue.container(0, List.of(RscpValue.uchar8(RscpTag.EMS_ERROR_TYPE, 1)))));
    }

    @Test
    void writesTheFieldsAsJson() {
        List<E3DCMessages.Message> messages = E3DCMessages
                .fromResponse(response(message(1, "EMS", "A schedule is being executed! (AI360)", 32803)));
        assertNotNull(messages);
        assertEquals(
                "[{\"type\":1,\"source\":\"EMS\",\"code\":32803,\"message\":\"A schedule is being executed! (AI360)\"}]",
                E3DCMessages.toJson(messages));
    }

    @Test
    void escapesQuotesAndControlCharactersInTheText() {
        List<E3DCMessages.Message> messages = E3DCMessages
                .fromResponse(response(message(2, "EMS", "say \"hi\"\n\\ done", 1)));
        assertNotNull(messages);
        assertEquals("[{\"type\":2,\"source\":\"EMS\",\"code\":1,\"message\":\"say \\\"hi\\\"\\n\\\\ done\"}]",
                E3DCMessages.toJson(messages));
    }

    @Test
    void leavesOutWhatTheDeviceDidNotReport() {
        RscpValue onlyCode = RscpValue.container(RscpTag.EMS_ERROR_CONTAINER,
                List.of(RscpValue.int32(RscpTag.EMS_ERROR_CODE, 7)));
        List<E3DCMessages.Message> messages = E3DCMessages.fromResponse(response(onlyCode));
        assertNotNull(messages);
        assertEquals("[{\"code\":7}]", E3DCMessages.toJson(messages));
    }

    @Test
    void skipsAContainerWithoutTextAndCode() {
        RscpValue empty = RscpValue.container(RscpTag.EMS_ERROR_CONTAINER, List.of());
        List<E3DCMessages.Message> messages = E3DCMessages.fromResponse(response(empty));
        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    @Test
    void readsTheTimestampAsAnInstant() {
        RscpValue stamped = RscpValue.container(RscpTag.EMS_ERROR_CONTAINER,
                List.of(RscpValue.cstring(RscpTag.EMS_ERROR_MESSAGE, "x"),
                        RscpValue.timestamp(RscpTag.EMS_ERROR_TIMESTAMP, Instant.ofEpochSecond(1_761_775_200L))));
        List<E3DCMessages.Message> messages = E3DCMessages.fromResponse(response(stamped));
        assertNotNull(messages);
        assertEquals("[{\"message\":\"x\",\"timestamp\":\"2025-10-29T22:00:00Z\"}]", E3DCMessages.toJson(messages));
    }
}
