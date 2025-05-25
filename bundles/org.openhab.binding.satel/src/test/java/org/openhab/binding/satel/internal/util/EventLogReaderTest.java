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
package org.openhab.binding.satel.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.command.ReadEventCommand;
import org.openhab.binding.satel.internal.command.ReadEventDescCommand;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.openhab.binding.satel.internal.util.EventLogReader.EventDescription;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
class EventLogReaderTest {

    private final SatelBridgeHandler bridgeHandler = mock(SatelBridgeHandler.class);

    private final DeviceNameResolver deviceNameResolver = mock(DeviceNameResolver.class);

    private final EventDispatcher eventDispatcher = mock(EventDispatcher.class);

    private final EventLogReader testSubject = new EventLogReader(bridgeHandler, deviceNameResolver);

    @BeforeEach
    void setUpBridgeHandler() {
        when(bridgeHandler.getEncoding()).thenReturn(StandardCharsets.US_ASCII);
    }

    @Test
    void readEventShouldReturnEmptyResultWhenCommandFailed() {
        when(bridgeHandler.sendCommand(isA(ReadEventCommand.class), eq(false))).thenReturn(false);

        Optional<EventDescription> result = testSubject.readEvent(0);

        assertFalse(result.isPresent());
    }

    @Test
    void readEventShouldReturnEmptyResultWhenInvalidIndexIsGiven() {
        setUpReadEventResponse(new byte[0]);

        Optional<EventDescription> result = testSubject.readEvent(0);

        assertFalse(result.isPresent());
    }

    @Test
    void readEventShouldReturnEventCodeWhenReadDescriptionFailed() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x00, 0x01 });

        Optional<EventDescription> result = testSubject.readEvent(0);

        assertTrue(result.isPresent());
        EventDescription eventDescription = result.get();
        assertEquals(0, eventDescription.getKind());
        assertEquals("event #1", eventDescription.getText());
    }

    @Test
    void readEventShouldReturnEventCodeWithRestoreFlagWhenReadDescriptionFailed() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x04, 0x01 });

        Optional<EventDescription> result = testSubject.readEvent(0);

        assertTrue(result.isPresent());
        EventDescription eventDescription = result.get();
        assertEquals(0, eventDescription.getKind());
        assertEquals("event #1 (restore)", eventDescription.getText());
    }

    @Test
    void readEventShouldReturnEventDescription() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x04, 0x01 });
        setUpReadEventDescResponse(new byte[] { 0x00, 0x00, 0x55, 0x00, 0x00, 'A', 'r', 'm' });

        Optional<EventDescription> result = testSubject.readEvent(0);

        assertTrue(result.isPresent());
        EventDescription eventDescription = result.get();
        assertEquals(0x55, eventDescription.getKind());
        assertEquals("Arm", eventDescription.getText());
    }

    @Test
    void readEventShouldCacheEventDescription() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x04, 0x01 });
        setUpReadEventDescResponse(new byte[0]);

        testSubject.readEvent(0);
        testSubject.readEvent(0);

        verify(bridgeHandler).sendCommand(isA(ReadEventDescCommand.class), eq(false));
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind0() {
        String result = testSubject.buildDetails(createEventDescription(0));

        assertEquals("", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind1() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolveZoneExpanderKeypad(70, false)).thenReturn("zone|expander|keypad");

        String result = testSubject.buildDetails(createEventDescription(1));

        assertEquals("partition, zone|expander|keypad", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind2() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolveUser(70)).thenReturn("user");

        String result = testSubject.buildDetails(createEventDescription(2));

        assertEquals("partition, user", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind3() {
        when(deviceNameResolver.resolvePartitionKeypad(50)).thenReturn("partition keypad");
        when(deviceNameResolver.resolveUser(70)).thenReturn("user");

        String result = testSubject.buildDetails(createEventDescription(3));

        assertEquals("partition keypad, user", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind4() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I256_PLUS);
        when(deviceNameResolver.resolveZoneExpanderKeypad(70, true)).thenReturn("zone|expander|keypad");

        String result = testSubject.buildDetails(createEventDescription(4));

        assertEquals("zone|expander|keypad", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind5() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");

        String result = testSubject.buildDetails(createEventDescription(5));

        assertEquals("partition", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind6() {
        when(deviceNameResolver.resolve(DeviceType.KEYPAD, 40)).thenReturn("keypad");
        when(deviceNameResolver.resolveUser(70)).thenReturn("user");

        String result = testSubject.buildDetails(createEventDescription(6));

        assertEquals("keypad, user", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind7() {
        when(deviceNameResolver.resolveUser(70)).thenReturn("user");

        String result = testSubject.buildDetails(createEventDescription(7));

        assertEquals("user", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind8() {
        when(deviceNameResolver.resolve(DeviceType.EXPANDER, 70)).thenReturn("expander");

        String result = testSubject.buildDetails(createEventDescription(8));

        assertEquals("expander", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind9() {
        when(deviceNameResolver.resolveTelephone(70)).thenReturn("telephone");

        String result = testSubject.buildDetails(createEventDescription(9));

        assertEquals("telephone", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind10() {
        when(deviceNameResolver.resolveTelephoneRelay(70)).thenReturn("telephone relay");

        String result = testSubject.buildDetails(createEventDescription(10));

        assertEquals("telephone relay", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind11() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolveDataBus(70)).thenReturn("data bus");

        String result = testSubject.buildDetails(createEventDescription(11));

        assertEquals("partition, data bus", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind12() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I256_PLUS);
        when(deviceNameResolver.resolveOutputExpander(5, false)).thenReturn("output|expander");

        String result = testSubject.buildDetails(createEventDescription(12, 0, 5));

        assertEquals("output|expander", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind12WithPartition() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I256_PLUS);
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolveOutputExpander(70, true)).thenReturn("output|expander");

        String result = testSubject.buildDetails(createEventDescription(12));

        assertEquals("partition, output|expander", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind13() {
        when(deviceNameResolver.resolveOutputExpander(70, false)).thenReturn("output|expander");

        String result = testSubject.buildDetails(createEventDescription(13));

        assertEquals("output|expander", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind13WithPartition() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolveOutputExpander(130, false)).thenReturn("output|expander");

        String result = testSubject.buildDetails(createEventDescription(13, 0, 130));

        assertEquals("partition, output|expander", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind14() {
        when(deviceNameResolver.resolveTelephone(40)).thenReturn("telephone");
        when(deviceNameResolver.resolveUser(70)).thenReturn("user");

        String result = testSubject.buildDetails(createEventDescription(14));

        assertEquals("telephone, user", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind15() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolve(DeviceType.TIMER, 70)).thenReturn("timer");

        String result = testSubject.buildDetails(createEventDescription(15));

        assertEquals("partition, timer", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind30() {
        when(deviceNameResolver.resolve(DeviceType.KEYPAD, 40)).thenReturn("keypad");

        String result = testSubject.buildDetails(createEventDescription(30));

        assertEquals("keypad, ip: 70.62", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind31() {
        String result = testSubject.buildDetails(createEventDescription(31));

        assertEquals(".70.62", result);
    }

    @Test
    void buildDetailsShouldReturnDetailsForKind32() {
        when(deviceNameResolver.resolve(DeviceType.PARTITION, 40)).thenReturn("partition");
        when(deviceNameResolver.resolve(DeviceType.ZONE, 70)).thenReturn("zone");

        String result = testSubject.buildDetails(createEventDescription(32));

        assertEquals("partition, zone", result);
    }

    @Test
    void buildDetailsShouldReturnDefaultDetailsForKind33() {
        String result = testSubject.buildDetails(createEventDescription(33));

        assertEquals("kind=33, partition=40, source=70, object=1, ucn=30", result);
    }

    @Test
    void buildDetailsShouldReadEventForKind31() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x14, 0x01, (byte) 192, (byte) 168 });
        setUpReadEventDescResponse(new byte[] { 0x00, 0x00, 30, 0x00, 0x00, 'A', 'r', 'm' });
        when(deviceNameResolver.resolve(DeviceType.KEYPAD, 3)).thenReturn("keypad");
        EventDescription eventDescription = createEventDescription(31);

        String result = testSubject.buildDetails(eventDescription);

        assertEquals("keypad, ip: 192.168.70.62", result);
        assertEquals(0, eventDescription.getNextIndex());
    }

    @Test
    void buildDetailsShouldSkipNextEventForKind31() {
        setUpReadEventResponse(new byte[] { 0x30, 0x01, 0x10, 0x00, 0x14, 0x01, (byte) 192, (byte) 168 });
        setUpReadEventDescResponse(new byte[] { 0x00, 0x00, 29, 0x00, 0x00, 'A', 'r', 'm' });
        EventDescription eventDescription = createEventDescription(31);

        String result = testSubject.buildDetails(eventDescription);

        assertEquals(".70.62", result);
        assertEquals(20, eventDescription.getNextIndex());
    }

    @Test
    void clearCacheShouldClearDeviceNameCache() {
        testSubject.clearCache();

        verify(deviceNameResolver).clearCache();
    }

    private void setUpReadEventResponse(byte[] responseBytes) {
        when(bridgeHandler.sendCommand(isA(ReadEventCommand.class), eq(false))).thenAnswer(invocationOnMock -> {
            ReadEventCommand cmd = invocationOnMock.getArgument(0);
            byte[] payload = new byte[14];
            System.arraycopy(responseBytes, 0, payload, 0, responseBytes.length);
            cmd.handleResponse(eventDispatcher, new SatelMessage(ReadEventCommand.COMMAND_CODE, payload));
            return true;
        });
    }

    private void setUpReadEventDescResponse(byte[] responseBytes) {
        when(bridgeHandler.sendCommand(isA(ReadEventDescCommand.class), eq(false))).thenAnswer(invocationOnMock -> {
            ReadEventDescCommand cmd = invocationOnMock.getArgument(0);
            byte[] payload = new byte[51];
            System.arraycopy(responseBytes, 0, payload, 0, responseBytes.length);
            cmd.handleResponse(eventDispatcher, new SatelMessage(ReadEventDescCommand.COMMAND_CODE, payload));
            return true;
        });
    }

    private EventDescription createEventDescription(int descKind) {
        return createEventDescription(descKind, 30, 70);
    }

    private EventDescription createEventDescription(int descKind, int userControlNumber, int source) {
        return testSubject.new EventDescription(mockReadEventCommand(userControlNumber, source), "", descKind);
    }

    private ReadEventCommand mockReadEventCommand(int userControlNumber, int source) {
        ReadEventCommand result = mock(ReadEventCommand.class);
        when(result.getCurrentIndex()).thenReturn(10);
        when(result.getNextIndex()).thenReturn(20);
        when(result.getUserControlNumber()).thenReturn(userControlNumber);
        when(result.getPartition()).thenReturn(40);
        when(result.getPartitionKeypad()).thenReturn(50);
        when(result.getObject()).thenReturn(1);
        when(result.getSource()).thenReturn(source);
        when(result.getTimestamp()).thenReturn(LocalDateTime.parse("2020-03-12T12:34:56"));
        return result;
    }
}
