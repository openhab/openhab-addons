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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for the address styles, data type ids and individual addresses of the project model.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxValueTypesTest {

    @Test
    void threeLevelStyle() {
        KnxGroupAddressStyle style = KnxGroupAddressStyle.THREE_LEVEL;
        assertEquals("0/0/0", style.format(0));
        assertEquals("1/2/3", style.format((1 << 11) | (2 << 8) | 3));
        assertEquals("31/7/255", style.format(0xFFFF));
    }

    @Test
    void twoLevelStyle() {
        KnxGroupAddressStyle style = KnxGroupAddressStyle.TWO_LEVEL;
        assertEquals("0/0", style.format(0));
        assertEquals("1/515", style.format((1 << 11) | 515));
        assertEquals("31/2047", style.format(0xFFFF));
    }

    @Test
    void freeStyle() {
        KnxGroupAddressStyle style = KnxGroupAddressStyle.FREE;
        assertEquals("0", style.format(0));
        assertEquals("2563", style.format(2563));
        assertEquals("65535", style.format(0xFFFF));
    }

    @Test
    void styleNamesOfTheProjectFile() {
        assertEquals(KnxGroupAddressStyle.THREE_LEVEL, KnxGroupAddressStyle.fromProjectValue("ThreeLevel"));
        assertEquals(KnxGroupAddressStyle.TWO_LEVEL, KnxGroupAddressStyle.fromProjectValue("TwoLevel"));
        assertEquals(KnxGroupAddressStyle.FREE, KnxGroupAddressStyle.fromProjectValue("Free"));
        assertNull(KnxGroupAddressStyle.fromProjectValue("FourLevel"));
        assertNull(KnxGroupAddressStyle.fromProjectValue(null));
    }

    private static List<KnxDatapointType> parse(@Nullable String value) throws KnxProjectException {
        return KnxDatapointType.parseList(value, new ReadBudget(ReadLimits.DEFAULT));
    }

    @Test
    void datapointTypeFromItsTextForm() {
        assertEquals(new KnxDatapointType(9, 1), KnxDatapointType.parse("9.001"));
        assertEquals(new KnxDatapointType(232, 600), KnxDatapointType.parse(" 232.600 "));
        assertEquals(new KnxDatapointType(14, 1200), KnxDatapointType.parse("14.1200"));
        assertEquals(new KnxDatapointType(9, null), KnxDatapointType.parse("9"));
        assertNull(KnxDatapointType.parse("9.1"));
        assertNull(KnxDatapointType.parse("DPST-9-1"));
        assertNull(KnxDatapointType.parse("1234.001"));
        assertNull(KnxDatapointType.parse(""));
        assertNull(KnxDatapointType.parse(null));
    }

    @Test
    void datapointTypeWithAndWithoutSubNumber() throws KnxProjectException {
        assertEquals(List.of(new KnxDatapointType(9, 1)), parse("DPST-9-1"));
        assertEquals(List.of(new KnxDatapointType(9, null)), parse("DPT-9"));
        assertEquals("9.001", new KnxDatapointType(9, 1).toString());
        assertEquals("14.1200", new KnxDatapointType(14, 1200).toString());
        assertEquals("9", new KnxDatapointType(9, null).toString());
    }

    @Test
    void subNumberIsPaddedWithAsciiDigitsOnly() {
        assertEquals("001", KnxDatapointType.padSub(1));
        assertEquals("010", KnxDatapointType.padSub(10));
        assertEquals("600", KnxDatapointType.padSub(600));
        assertEquals("12345", KnxDatapointType.padSub(12345));
        assertEquals("1.008", new KnxDatapointType(1, 8).toString());
    }

    @Test
    void severalDatapointTypesOnOneValue() throws KnxProjectException {
        assertEquals(List.of(new KnxDatapointType(5, 1), new KnxDatapointType(5, 4), new KnxDatapointType(7, null)),
                parse("DPST-5-1  DPST-5-4\tDPT-7"));
    }

    @Test
    void unknownDatapointTypeFormsAreSkipped() throws KnxProjectException {
        assertEquals(List.of(), parse(null));
        assertEquals(List.of(), parse(""));
        assertEquals(List.of(new KnxDatapointType(1, 1)),
                parse("DPST-x-1 DPT- 1.001 DPST-1-1 DPT-99999999999 DPST-1-1-1"));
    }

    @Test
    void individualAddressText() {
        assertEquals("15.15.255", new KnxIndividualAddress(15, 15, 255).toString());
        assertEquals("0.0.0", new KnxIndividualAddress(0, 0, 0).toString());
    }
}
