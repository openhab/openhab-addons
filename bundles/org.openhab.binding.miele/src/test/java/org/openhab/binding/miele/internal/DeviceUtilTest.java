/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.miele.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.types.UnDefType;

/**
 * This class provides test cases for {@link
 * org.openhab.binding.miele.internal.DeviceUtil}
 *
 * @author Jacob Laursen - Initial contribution
 */

public class DeviceUtilTest extends JavaTest {

    @Test
    public void bytesToHexWhenTopBitIsUsedReturnsCorrectString() {
        String actual = DeviceUtil.bytesToHex(new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef });
        assertEquals("DEADBEEF", actual);
    }

    /**
     * This test guards that the UTF-16 returned by the RPC-JSON API will be
     * considered as a sequence of 8-bit characters and converted into bytes
     * accordingly. Default behaviour of String.getBytes() assumes UTF-8
     * and adds a 0xc2 byte before any character out of ASCII range.
     */
    @Test
    public void stringToBytesWhenTopBitIsUsedReturnsSingleByte() {
        byte[] expected = new byte[] { (byte) 0x00, (byte) 0x80, (byte) 0x00 };
        byte[] actual = DeviceUtil.stringToBytes("\u0000\u0080\u0000");
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getTemperatureStateWellFormedValueReturnsQuantityType() throws NumberFormatException {
        assertEquals(new QuantityType<>(42, SIUnits.CELSIUS), DeviceUtil.getTemperatureState("42"));
    }

    @Test
    public void getTemperatureStateMagicValueReturnsUndefined() throws NumberFormatException {
        assertEquals(UnDefType.UNDEF, DeviceUtil.getTemperatureState("32768"));
    }

    @Test
    public void getTemperatureStateColdValueReturns10Degrees() throws NumberFormatException {
        assertEquals(new QuantityType<>(10, SIUnits.CELSIUS), DeviceUtil.getTemperatureState("-32760"));
    }

    @Test
    public void getTemperatureStateNonNumericValueThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> DeviceUtil.getTemperatureState("A"));
    }

    @Test
    public void getTemperatureStateNullValueThrowsNumberFormatException() {
        assertThrows(NumberFormatException.class, () -> DeviceUtil.getTemperatureState(null));
    }

    @Test
    public void getStateTextStateProviderHasPrecedence() {
        assertEquals("I brug", this.getStateTextState("5", "Running", "miele.state.running", "I brug"));
    }

    @Test
    public void getStateTextStateGatewayTextIsReturnedWhenKeyIsUnknown() {
        assertEquals("Running", this.getStateTextState("-1", "Running", "key.unknown", "I brug"));
    }

    @Test
    public void getStateTextStateKeyIsReturnedWhenUnknownByGatewayAndProvider() {
        assertEquals("state.99", this.getStateTextState("99", null, "key.unknown", "I brug"));
    }

    private String getStateTextState(String value, String localizedValue, String mockedKey, String mockedValue) {
        var metaData = new DeviceMetaData();
        metaData.LocalizedValue = localizedValue;
        var translationProvider = mock(MieleTranslationProvider.class);
        when(translationProvider.getText(mockedKey, metaData.LocalizedValue)).thenReturn(mockedValue);

        return DeviceUtil.getStateTextState(value, metaData, translationProvider).toString();
    }
}
