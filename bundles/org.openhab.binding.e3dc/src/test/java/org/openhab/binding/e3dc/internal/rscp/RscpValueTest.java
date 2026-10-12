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
package org.openhab.binding.e3dc.internal.rscp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests navigation and inspection of decoded RSCP trees.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class RscpValueTest {

    private static final int PV = 0x01800001;
    private static final int SOC = 0x01800008;
    private static final int SETTINGS = 0x0180008B;
    private static final int REQ_BAT_SOC = 0x01000008;

    /** Reads the integer one node of a lookup result must carry. */
    private static int intAt(List<RscpValue> values, int index) {
        return Objects.requireNonNull((Number) values.get(index).getValue()).intValue();
    }

    @Test
    void findLocatesNestedValues() {
        RscpValue tree = RscpValue.container(0, List.of(RscpValue.scalar(PV, RscpType.INT32, 4711),
                RscpValue.container(SETTINGS, List.of(RscpValue.scalar(SOC, RscpType.DOUBLE64, 55.5)))));
        Number number = Objects.requireNonNull(tree.findNumber(PV), "the nested int is found");
        Double nested = Objects.requireNonNull(tree.findDouble(SOC), "a value two levels deep is found");

        assertEquals(4711, number.intValue());
        assertEquals(55.5, nested, 0.0001);
        assertNull(tree.find(0x7FFFFFFF), "an absent tag yields null rather than a default");
    }

    @Test
    void findAllReturnsEveryOccurrenceInOrder() {
        RscpValue tree = RscpValue.container(0, List.of(RscpValue.scalar(SOC, RscpType.UCHAR8, 1), RscpValue.container(
                SETTINGS,
                List.of(RscpValue.scalar(SOC, RscpType.UCHAR8, 2), RscpValue.scalar(SOC, RscpType.UCHAR8, 3)))));

        List<RscpValue> all = tree.findAll(SOC);

        assertEquals(3, all.size());
        assertEquals(1, intAt(all, 0));
        assertEquals(3, intAt(all, 2));
    }

    @Test
    void aMissingValueDoesNotBecomeZero() {
        RscpValue tree = RscpValue.container(0, List.of(RscpValue.scalar(PV, RscpType.INT32, 1)));

        assertNull(tree.findNumber(SOC), "an absent value must stay absent, never default to zero");
        assertNull(tree.findDouble(SOC));
        assertNull(tree.findString(SOC));
    }

    @Test
    void aValueOfTheWrongKindIsNotCoerced() {
        RscpValue text = RscpValue.scalar(0x0A800019, RscpType.CSTRING, "1.0.16");

        assertNull(text.findNumber(0x0A800019), "a string is not a number");
        assertEquals("1.0.16", text.findString(0x0A800019));
    }

    @Test
    void errorItemsAreDetectedAnywhereInTheTree() {
        RscpValue warning = RscpValue.scalar(REQ_BAT_SOC, RscpType.ERROR, RscpError.UNKNOWN_TAG);
        RscpValue tree = RscpValue.container(0,
                List.of(RscpValue.scalar(PV, RscpType.INT32, 5), RscpValue.container(SETTINGS, List.of(warning))));

        assertTrue(tree.containsError());
        assertEquals(RscpError.UNKNOWN_TAG, tree.findError());
        assertFalse(RscpValue.scalar(PV, RscpType.INT32, 5).containsError());
    }

    @Test
    void anUnknownTagIsRenderedAsItsIdentifier() {
        RscpValue unknown = RscpValue.scalar(0x0BADC0DE, RscpType.INT32, 1);

        // an unknown tag is rendered by its numeric id, and the value is still shown
        assertEquals("UNKNOWN_0x0BADC0DE=1", unknown.toString());
    }

    @Test
    void aKnownTagIsRenderedByName() {
        assertEquals("EMS_POWER_PV=4711", RscpValue.scalar(RscpTag.EMS_POWER_PV, RscpType.INT32, 4711).toString());
    }

    @Test
    void aCStringIsRenderedAsText() {
        RscpValue value = RscpValue.scalar(0x0A800019, RscpType.CSTRING, "1.0.16");

        assertEquals("1.0.16", value.describeValue());
    }

    @Test
    void aByteArrayIsRenderedAsHex() {
        RscpValue value = RscpValue.scalar(0x0E042010, RscpType.BYTEARRAY, new byte[] { 0x01, 0x0A, (byte) 0xFF });

        assertEquals("0x010aff", value.describeValue());
    }

    @Test
    void aContainerWithoutValueIsStillNavigable() {
        RscpValue empty = RscpValue.container(SETTINGS, List.of());

        assertEquals(RscpType.CONTAINER, empty.getType());
        assertNull(empty.getValue());
        assertTrue(empty.getChildren().isEmpty());
        assertEquals("EMS_GET_POWER_SETTINGS[0]", empty.toString());
    }

    /**
     * The log needs the whole answer of a device, not only the size of its containers: a wrong request is
     * told apart from an unsupported data point by the tags and values that actually arrive.
     */
    @Test
    void theTreeRenderingNamesEveryLevelAndValue() {
        RscpValue tree = RscpValue.container(0,
                List.of(RscpValue.container(RscpTag.WB_DATA,
                        List.of(RscpValue.uchar8(RscpTag.WB_INDEX, 0), RscpValue.none(RscpTag.WB_REQ_SOC))),
                        RscpValue.scalar(RscpTag.WB_EXTERN_DATA, RscpType.BYTEARRAY, new byte[] { 0x00, 0x01 })));

        String separator = System.lineSeparator();
        assertEquals("UNKNOWN_0x00000000[2]" + separator //
                + "    WB_DATA[2]" + separator //
                + "        WB_INDEX=0" + separator //
                + "        WB_REQ_SOC=<none>" + separator //
                + "    WB_EXTERN_DATA=0x0001", tree.describeTree());
    }

    @Test
    void theErrorCodeOfAnItemIsExposed() {
        RscpValue error = RscpValue.scalar(0x01000008, RscpType.ERROR, RscpError.ACCESS_DENIED);

        assertTrue(error.isError());
        assertEquals(RscpError.ACCESS_DENIED, error.getError());
        assertFalse(RscpValue.scalar(0x01000008, RscpType.INT32, 1).isError());
        assertNull(RscpValue.scalar(0x01000008, RscpType.INT32, 1).getError());
    }

    @Test
    void errorCodesAreMappedFromTheProtocolNumbers() {
        assertEquals(RscpError.NOT_HANDLED, RscpError.fromCode(0x01));
        assertEquals(RscpError.ACCESS_DENIED, RscpError.fromCode(0x02));
        assertEquals(RscpError.NOT_AVAILABLE, RscpError.fromCode(0x06));
        assertEquals(RscpError.UNKNOWN_TAG, RscpError.fromCode(0x07));
        assertEquals(RscpError.UNKNOWN, RscpError.fromCode(0x7F), "an undocumented code must not throw");
    }

    @Test
    void unknownTypeCodesAreRejected() {
        assertEquals(RscpType.INT32, RscpType.fromCode(0x06));
        assertThrows(IllegalArgumentException.class, () -> RscpType.fromCode(0x42));
    }

    private static final int HISTORY_DAY = 0x06800100;
    private static final int SUM_CONTAINER = 0x06800010;
    private static final int AUTARKY = 0x0680000D;
    private static final int CONSUMED_PRODUCTION = 0x0680000C;
    private static final int DC_POWER = 0x06800004;

    /**
     * Builds the response of a daily history request: the period container holds the sum container,
     * which holds the values of that period.
     */
    private static RscpValue historyResponse() {
        return RscpValue.container(0,
                List.of(RscpValue.container(HISTORY_DAY,
                        List.of(RscpValue.container(SUM_CONTAINER,
                                List.of(RscpValue.scalar(AUTARKY, RscpType.DOUBLE64, 87.5),
                                        RscpValue.scalar(CONSUMED_PRODUCTION, RscpType.DOUBLE64, 62.0),
                                        RscpValue.scalar(DC_POWER, RscpType.DOUBLE64, 12500.0)))))));
    }

    /**
     * The values of the history data are three levels deep, so the path has to walk the period
     * container and the sum container before it reaches the value.
     */
    @Test
    void aTagPathWalksIntoTheHistorySumContainer() {
        RscpValue response = historyResponse();

        RscpValue autarky = response.find(List.of(HISTORY_DAY, SUM_CONTAINER, AUTARKY));
        assertNotNull(autarky, "the autarky value is reachable by its path");
        assertEquals(87.5, Objects.requireNonNull(autarky.asNumber()).doubleValue(), 0.0001);

        RscpValue consumed = response.find(List.of(HISTORY_DAY, SUM_CONTAINER, CONSUMED_PRODUCTION));
        assertNotNull(consumed);
        assertEquals(62.0, Objects.requireNonNull(consumed.asNumber()).doubleValue(), 0.0001);
    }

    /**
     * The lookup descends the whole subtree, so it also reaches a value whose container is not named in
     * the path. A mapping that names the value but not its container therefore stays silent only when
     * the value is absent from the response, not when the path is too short.
     */
    @Test
    void aRecursiveSearchReachesAValueWithoutNamingItsContainer() {
        RscpValue response = historyResponse();

        assertNotNull(response.find(AUTARKY), "the search descends into the sum container");
    }

    /**
     * The history response carries one period container per requested period. Taking the first is only
     * correct while the device returns them in the order they were asked for.
     */
    @Test
    void theFirstPeriodContainerIsTheOneAskedForFirst() {
        RscpValue today = RscpValue.container(HISTORY_DAY, List
                .of(RscpValue.container(SUM_CONTAINER, List.of(RscpValue.scalar(AUTARKY, RscpType.DOUBLE64, 87.5)))));
        RscpValue yesterday = RscpValue.container(HISTORY_DAY, List
                .of(RscpValue.container(SUM_CONTAINER, List.of(RscpValue.scalar(AUTARKY, RscpType.DOUBLE64, 42.0)))));
        RscpValue response = RscpValue.container(0, List.of(today, yesterday));

        List<RscpValue> periods = response.findAll(HISTORY_DAY);
        assertEquals(2, periods.size(), "both periods are present");
        RscpValue firstSum = Objects.requireNonNull(periods.get(0).find(SUM_CONTAINER));
        assertEquals(87.5, Objects.requireNonNull(firstSum.findNumber(AUTARKY)).doubleValue(), 0.0001,
                "the first period is the one requested first");
    }

    @Test
    void aPathWithAnUnknownStepYieldsNothing() {
        RscpValue response = historyResponse();

        assertNull(response.find(List.of(0x06800200, SUM_CONTAINER, AUTARKY)), "no history of that period");
        assertNull(response.find(List.of(HISTORY_DAY, 0x06800020, AUTARKY)), "no such nested container");
    }
}
