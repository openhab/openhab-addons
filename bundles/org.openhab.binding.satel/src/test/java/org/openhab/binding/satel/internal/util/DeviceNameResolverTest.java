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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand.DeviceType;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.openhab.binding.satel.internal.types.IntegraType;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
class DeviceNameResolverTest {

    private final SatelBridgeHandler bridgeHandler = mock(SatelBridgeHandler.class);

    private final EventDispatcher eventDispatcher = mock(EventDispatcher.class);

    private final DeviceNameResolver testSubject = new DeviceNameResolver(bridgeHandler);

    @BeforeEach
    void setUpBridgeHandler() {
        when(bridgeHandler.getEncoding()).thenReturn(StandardCharsets.US_ASCII);
    }

    @Test
    void resolveShouldReadDeviceName() {
        setUpResponse("partition name");

        String result = testSubject.resolve(DeviceType.PARTITION, 1);
        assertEquals("partition: partition name", result);
    }

    @Test
    void resolveShouldCacheDevice() {
        setUpResponse("partition name");
        testSubject.resolve(DeviceType.PARTITION, 1);

        String result = testSubject.resolve(DeviceType.PARTITION, 1);

        assertEquals("partition: partition name", result);
        verify(bridgeHandler, times(1)).sendCommand(any(), eq(false));
    }

    @Test
    void resolveShouldReturnDeviceNumberWhenNameNotAvailable() {
        when(bridgeHandler.sendCommand(any(), eq(false))).thenReturn(false);

        String result = testSubject.resolve(DeviceType.PARTITION, 1);

        assertEquals("partition: 1", result);
    }

    @Test
    void clearCacheShouldRemoveCachedName() {
        setUpResponse("partition name");

        testSubject.resolve(DeviceType.PARTITION, 1);
        testSubject.clearCache();
        testSubject.resolve(DeviceType.PARTITION, 1);

        verify(bridgeHandler, times(2)).sendCommand(any(), eq(false));
    }

    @Test
    void resolveOutputExpanderShouldReturnMainboardWhenDeviceNumberIsZero() {
        assertEquals("mainboard", testSubject.resolveOutputExpander(0, false));
    }

    @Test
    void resolveOutputExpanderShouldReturnOutputName() {
        setUpResponse("output name");

        String result = testSubject.resolveOutputExpander(1, false);

        assertEquals("output: output name", result);
    }

    @Test
    void resolveOutputExpanderShouldReturnResolveUpperOutputName() {
        when(bridgeHandler.sendCommand(any(), eq(false))).thenReturn(false);

        String result = testSubject.resolveOutputExpander(1, true);

        assertEquals("output: 129", result);
    }

    @Test
    void resolveOutputExpanderShouldReturnExpanderName() {
        setUpResponse("expander name");

        String result = testSubject.resolveOutputExpander(129, false);

        assertEquals("expander: expander name", result);
    }

    @Test
    void resolveOutputExpanderShouldHandleInvalidDeviceNumber() {
        String result = testSubject.resolveOutputExpander(193, false);

        assertEquals("invalid output|expander device: 193", result);
    }

    @Test
    void resolveZoneExpanderKeypadShouldReturnMainboardWhenDeviceNumberIsZero() {
        assertEquals("mainboard", testSubject.resolveZoneExpanderKeypad(0, false));
    }

    @Test
    void resolveZoneExpanderKeypadShouldReturnZoneName() {
        setUpResponse("zone name");

        String result = testSubject.resolveZoneExpanderKeypad(1, false);

        assertEquals("zone: zone name", result);
    }

    @Test
    void resolveZoneExpanderKeypadShouldReturnUpperZoneName() {
        when(bridgeHandler.sendCommand(any(), eq(false))).thenReturn(false);

        String result = testSubject.resolveZoneExpanderKeypad(1, true);

        assertEquals("zone: 129", result);
    }

    @Test
    void resolveZoneExpanderKeypadShouldReturnExpanderName() {
        setUpResponse("expander name");

        String result = testSubject.resolveZoneExpanderKeypad(129, false);

        assertEquals("expander: expander name", result);
    }

    @Test
    void resolveZoneExpanderKeypadShouldReturnKeypadName() {
        setUpResponse("keypad name");

        String result = testSubject.resolveZoneExpanderKeypad(193, false);

        assertEquals("keypad: keypad name", result);
    }

    @Test
    void resolvePartitionKeypadShouldReturnPartitionKeypadNameForSecondBus() {
        setUpResponse("keypad name");

        String result = testSubject.resolvePartitionKeypad(33);

        assertEquals("expander: keypad name", result);
    }

    @Test
    void resolvePartitionKeypadShouldReturnPartitionKeypadNameForWrlLeonBoard() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I128_LEON);
        setUpResponse("keypad name");

        String result = testSubject.resolvePartitionKeypad(1);

        assertEquals("expander: keypad name", result);
    }

    @Test
    void resolvePartitionKeypadShouldReturnMainboardForWrlLeonBoard() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I128_LEON);

        String result = testSubject.resolvePartitionKeypad(33);

        assertEquals("mainboard", result);
    }

    @Test
    void resolvePartitionKeypadShouldReturnMainboardForWrlSim300Board() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I128_SIM300);

        String result = testSubject.resolvePartitionKeypad(33);

        assertEquals("mainboard", result);
    }

    @Test
    void resolveUserShouldReturnUserName() {
        setUpResponse("user name");

        String result = testSubject.resolveUser(1);

        assertEquals("user: user name", result);
    }

    @ParameterizedTest
    @CsvSource({ "0,user: unknown", "249,INT-AV", "250,ACCO NET", "251,SMS", "252,timer", "253,function zone",
            "254,Quick arm", "255,service" })
    void resolveUserShouldReturnStaticNameForSpecificDeviceNumber(int deviceNumber, String expectedResult) {
        String result = testSubject.resolveUser(deviceNumber);

        assertEquals(expectedResult, result);
    }

    @Test
    void resolveDataBusShouldReturnDataBusName() {
        assertEquals("data bus: 1", testSubject.resolveDataBus(1));
    }

    @Test
    void resolveTelephoneShouldReturnTelephoneName() {
        setUpResponse("telephone name");

        String result = testSubject.resolveTelephone(1);

        assertEquals("telephone: telephone name", result);
    }

    @Test
    void resolveTelephoneShouldReturnUnknownTelephoneWhenDeviceNumberIs0() {
        String result = testSubject.resolveTelephone(0);

        assertEquals("telephone: unknown", result);
    }

    @Test
    void resolveTelephoneRelayShouldReturnTelephoneRelayName() {
        assertEquals("telephone relay: 1", testSubject.resolveTelephoneRelay(1));
    }

    void setUpResponse(String deviceName) {
        when(bridgeHandler.sendCommand(isA(ReadDeviceInfoCommand.class), eq(false))).thenAnswer(invocationOnMock -> {
            ReadDeviceInfoCommand cmd = invocationOnMock.getArgument(0);
            byte[] payload = new byte[19];
            byte[] nameBytes = deviceName.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(nameBytes, 0, payload, 3, nameBytes.length);
            cmd.handleResponse(eventDispatcher, new SatelMessage(ReadDeviceInfoCommand.COMMAND_CODE, payload));
            return true;
        });
    }
}
