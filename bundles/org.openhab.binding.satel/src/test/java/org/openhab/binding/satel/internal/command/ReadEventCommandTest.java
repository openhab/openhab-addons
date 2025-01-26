/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.satel.internal.command.ReadEventCommand.COMMAND_CODE;
import static org.openhab.binding.satel.internal.command.ReadEventCommand.EventClass;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.protocol.SatelMessage;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
class ReadEventCommandTest {

    private final EventDispatcher eventDispatcher = new EventDispatcher();

    private final Clock clock = mock(Clock.class);

    private final ReadEventCommand testSubject = new ReadEventCommand(-1, clock);

    @BeforeEach
    void setupClock() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(Instant.parse("2012-01-01T00:00:00Z"));
    }

    @Test
    void handleResponseShouldFailIfResponseHasWrongSize() {
        SatelMessage response = new SatelMessage(COMMAND_CODE, new byte[13]);

        assertFalse(testSubject.handleResponse(eventDispatcher, response));
    }

    @Test
    void handleResponseShouldSucceedIfResponseHasCorrectSize() {
        SatelMessage response = new SatelMessage(COMMAND_CODE, new byte[14]);

        assertTrue(testSubject.handleResponse(eventDispatcher, response));
    }

    @Test
    void isEmptyShouldReturnTrueForEmptyResponse() {
        SatelMessage response = new SatelMessage(COMMAND_CODE, new byte[14]);
        testSubject.handleResponse(eventDispatcher, response);

        assertTrue(testSubject.isEmpty());
    }

    @Test
    void isEmptyShouldReturnFalseForNonEmptyResponse() {
        SatelMessage response = createMessageWithBytes(0, 0x20);
        testSubject.handleResponse(eventDispatcher, response);

        assertFalse(testSubject.isEmpty());
    }

    @Test
    void isPresentShouldReturnFalseForEmptyResponse() {
        SatelMessage response = new SatelMessage(COMMAND_CODE, new byte[14]);
        testSubject.handleResponse(eventDispatcher, response);

        assertFalse(testSubject.isEventPresent());
    }

    @Test
    void isPresentShouldReturnTrueForNonEmptyResponse() {
        SatelMessage response = createMessageWithBytes(0, 0x10);
        testSubject.handleResponse(eventDispatcher, response);

        assertTrue(testSubject.isEventPresent());
    }

    @Test
    void getTimestampShouldReturnCorrectDateAndTime1() {
        SatelMessage response = createMessageWithBytes(0, 0xc0, 0x1f, 0x74, 0xc7);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(LocalDateTime.of(2011, 7, 31, 20, 23), testSubject.getTimestamp());
    }

    @Test
    void getTimestampShouldReturnCorrectDateAndTime2() {
        SatelMessage response = createMessageWithBytes(0, 0x00, 0x07, 0xc0, 0xff);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(LocalDateTime.of(2012, 12, 7, 4, 15), testSubject.getTimestamp());
    }

    @Test
    void getEventClassShouldReturnEventClass() {
        SatelMessage response = createMessageWithBytes(1, 0xa0);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(EventClass.TROUBLES, testSubject.getEventClass());
    }

    @Test
    void getPartitionShouldReturnPartitionNumber() {
        SatelMessage response = createMessageWithBytes(4, 0xf8);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(32, testSubject.getPartition());
    }

    @Test
    void getPartitionKeypadShouldReturnPartitionKeypadNumber() {
        SatelMessage response = createMessageWithBytes(4, 0x7c);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(48, testSubject.getPartitionKeypad());
    }

    @Test
    void getEventCodeShouldReturnEventCode() {
        SatelMessage response = createMessageWithBytes(4, 0x3, 0xff);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(0x3ff, testSubject.getEventCode());
    }

    @Test
    void isRestoreShouldReturnFalseForEmptyResponse() {
        SatelMessage response = new SatelMessage(COMMAND_CODE, new byte[14]);
        testSubject.handleResponse(eventDispatcher, response);

        assertFalse(testSubject.isRestore());
    }

    @Test
    void isRestoreShouldReturnRestoreFlagSet() {
        SatelMessage response = createMessageWithBytes(4, 0x4);
        testSubject.handleResponse(eventDispatcher, response);

        assertTrue(testSubject.isRestore());
    }

    @Test
    void getSourceShouldReturnSourceNumber() {
        SatelMessage response = createMessageWithBytes(6, 0xff);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(255, testSubject.getSource());
    }

    @Test
    void getObjectShouldReturnObjectNumber() {
        SatelMessage response = createMessageWithBytes(7, 0xe0);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(7, testSubject.getObject());
    }

    @Test
    void getUserControlNumberShouldReturnUserControlNumber() {
        SatelMessage response = createMessageWithBytes(7, 0x1f);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(31, testSubject.getUserControlNumber());
    }

    @Test
    void getNextIndexShouldReturnNextIndex() {
        SatelMessage response = createMessageWithBytes(8, 0xff, 0xff, 0xff);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(0xffffff, testSubject.getNextIndex());
    }

    @Test
    void getCurrentIndexShouldReturnCurrentIndex() {
        SatelMessage response = createMessageWithBytes(11, 0xff, 0xff, 0xff);
        testSubject.handleResponse(eventDispatcher, response);

        assertEquals(0xffffff, testSubject.getCurrentIndex());
    }

    private SatelMessage createMessageWithBytes(int offset, int... data) {
        byte[] payload = new byte[14];
        for (int i = 0; i < data.length; ++i) {
            payload[offset + i] = (byte) data[i];
        }
        return new SatelMessage(COMMAND_CODE, payload);
    }
}
