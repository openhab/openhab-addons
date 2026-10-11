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

import java.time.LocalTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * Tests the text form of an idle period.
 *
 * <p>
 * The text is what a user types into the channel and what the binding writes back, so parsing and
 * rendering have to agree, and a malformed line has to be rejected rather than sent to the device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCIdlePeriodTest {

    @Test
    void aPeriodParsesIntoItsFields() {
        E3DCIdlePeriod period = E3DCIdlePeriod.parse("weekend:saturday,sunday:charge:true:08:00-18:00");

        assertNotNull(period);
        assertEquals("weekend", period.name());
        assertEquals(E3DCIdlePeriod.Type.CHARGE, period.type());
        assertTrue(period.active());
        assertEquals(LocalTime.of(8, 0), period.start());
        assertEquals(LocalTime.of(18, 0), period.stop());
        // saturday is bit 5 and sunday bit 6, counted from monday as the least significant bit
        assertEquals((1 << 5) | (1 << 6), period.weekdays());
    }

    @Test
    void theTextFormParsesBackToTheSamePeriod() {
        String text = "workdays:monday,tuesday,wednesday,thursday,friday:discharge:false:00:00-23:59";
        E3DCIdlePeriod period = E3DCIdlePeriod.parse(text);

        assertNotNull(period);
        assertEquals(text, period.toText(), "rendering has to reproduce the parsed text");
        assertEquals(E3DCIdlePeriod.Type.DISCHARGE, period.type());
        assertFalse(period.active());
    }

    @Test
    void aWholeDayListParsesAndRendersInOrder() {
        String text = "all:monday,tuesday,wednesday,thursday,friday,saturday,sunday:charge:true:06:30-21:15";
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.parseAll(text);

        assertNotNull(periods);
        assertEquals(1, periods.size());
        assertEquals(0x7F, periods.get(0).weekdays(), "all seven days");
        assertEquals(text, E3DCIdlePeriod.toText(periods));
    }

    @Test
    void severalPeriodsAreCarriedAsSeveralLines() {
        String text = "night:monday,tuesday:charge:true:00:00-06:00\n"
                + "evening:saturday,sunday:discharge:true:18:00-23:59";
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.parseAll(text);

        assertNotNull(periods);
        assertEquals(2, periods.size());
        assertEquals("night", periods.get(0).name());
        assertEquals("evening", periods.get(1).name());
        assertEquals(text, E3DCIdlePeriod.toText(periods));
    }

    @Test
    void anEmptyTextIsAnEmptyList() {
        List<E3DCIdlePeriod> periods = E3DCIdlePeriod.parseAll("");

        assertNotNull(periods);
        assertTrue(periods.isEmpty());
        assertEquals("", E3DCIdlePeriod.toText(periods));
    }

    @Test
    void malformedTextIsRejectedRatherThanSent() {
        // a day that does not exist
        assertNull(E3DCIdlePeriod.parse("x:moonday:charge:true:00:00-01:00"));
        // no day at all
        assertNull(E3DCIdlePeriod.parse("x::charge:true:00:00-01:00"));
        // an unknown mode
        assertNull(E3DCIdlePeriod.parse("x:monday:hold:true:00:00-01:00"));
        // a time that does not exist
        assertNull(E3DCIdlePeriod.parse("x:monday:charge:true:24:00-25:00"));
        assertNull(E3DCIdlePeriod.parse("x:monday:charge:true:aa:bb-cc:dd"));
        // too few fields
        assertNull(E3DCIdlePeriod.parse("x:monday:charge"));
        // a missing name
        assertNull(E3DCIdlePeriod.parse(":monday:charge:true:00:00-01:00"));
    }

    @Test
    void oneMalformedLineMakesTheWholeListInvalid() {
        assertNull(E3DCIdlePeriod.parseAll("good:monday:charge:true:00:00-01:00\nbroken"));
    }

    @Test
    void aSingleDigitHourIsAccepted() {
        E3DCIdlePeriod period = E3DCIdlePeriod.parse("x:monday:charge:true:8:00-9:30");

        assertNotNull(period);
        assertEquals(LocalTime.of(8, 0), period.start());
        assertEquals(LocalTime.of(9, 30), period.stop());
        assertEquals("x:monday:charge:true:08:00-09:30", period.toText(), "the text is rendered padded");
    }

    @Test
    void theIdleTypesUseTheCodesOfTheInterface() {
        assertEquals(0, E3DCIdlePeriod.Type.CHARGE.getCode());
        assertEquals(1, E3DCIdlePeriod.Type.DISCHARGE.getCode());
        assertEquals(E3DCIdlePeriod.Type.CHARGE, E3DCIdlePeriod.Type.fromCode(0));
        assertEquals(E3DCIdlePeriod.Type.DISCHARGE, E3DCIdlePeriod.Type.fromCode(1));
        assertNull(E3DCIdlePeriod.Type.fromCode(9), "an unknown code is not a type");
    }

    /**
     * The device reports the periods as one container each, and the text form has to reproduce what the
     * user could type back in.
     */
    @Test
    void thePeriodsOfAResponseAreRenderedAsText() {
        RscpValue period = RscpValue.container(0x0140027A,
                List.of(RscpValue.cstring(0x01800302, "weekend"), RscpValue.uchar8(0x01800083, 0),
                        RscpValue.uchar8(0x01800303, (1 << 5) | (1 << 6)),
                        RscpValue.of(0x01800301, RscpType.BOOL, Boolean.TRUE), RscpValue.int32(0x01800304, 8 * 3600),
                        RscpValue.int32(0x01800305, 18 * 3600)));
        RscpValue response = RscpValue.container(0x01800033, List.of(period));

        String text = E3DCIdlePeriod.toTextFromResponse(response);
        assertEquals("weekend:saturday,sunday:charge:true:08:00-18:00", text);
        // and the rendered text parses back to the same period
        List<E3DCIdlePeriod> parsed = E3DCIdlePeriod.parseAll(text);
        assertNotNull(parsed);
        assertEquals(1, parsed.size());
        assertEquals(0, parsed.get(0).type().getCode());
    }

    /**
     * A response without periods renders as empty text rather than as an invalid line.
     */
    @Test
    void aResponseWithoutPeriodsRendersAsEmptyText() {
        assertEquals("", E3DCIdlePeriod.toTextFromResponse(RscpValue.container(0x01800033, List.of())));
        // a container that lacks the fields of a period is skipped
        RscpValue incomplete = RscpValue.container(0x0140027A, List.of(RscpValue.cstring(0x01800302, "incomplete")));
        assertEquals("", E3DCIdlePeriod.toTextFromResponse(RscpValue.container(0x01800033, List.of(incomplete))));
    }
}
