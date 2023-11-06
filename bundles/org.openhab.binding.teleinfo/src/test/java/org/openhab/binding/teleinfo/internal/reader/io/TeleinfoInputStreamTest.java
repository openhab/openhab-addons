/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.reader.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.time.Month;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.data.FrameType;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.binding.teleinfo.internal.serial.TeleinfoTicMode;
import org.openhab.binding.teleinfo.util.TestUtils;
import org.openhab.core.library.types.DateTimeType;

/**
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoInputStreamTest {

    @Test
    public void testReadNextFrameCbetmBase1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-base-option-1.raw")), false,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameType.CBETM_LONG_BASE, frame.getType());
            assertEquals("XXXXXXXXXXXX", frame.get(Label.ADCO));
            assertEquals(20, frame.getAsInt(Label.ISOUSC));
            assertEquals(1181243, frame.getAsInt(Label.BASE));
            assertEquals("TH..", frame.get(Label.PTEC));
            assertEquals(0, frame.getAsInt(Label.IINST1));
            assertEquals(2, frame.getAsInt(Label.IINST2));
            assertEquals(0, frame.getAsInt(Label.IINST3));
            assertEquals(26, frame.getAsInt(Label.IMAX1));
            assertEquals(18, frame.getAsInt(Label.IMAX2));
            assertEquals(27, frame.getAsInt(Label.IMAX3));
            assertEquals(7990, frame.getAsInt(Label.PMAX));
            assertEquals(540, frame.getAsInt(Label.PAPP));
            assertEquals("00", frame.get(Label.PPOT));
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccHc1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-hc-option-1.raw")), false,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameType.CBEMM_ICC_HC, frame.getType());
            assertEquals("XXXXXXXXXXXX", frame.get(Label.ADCO));
            assertEquals(30, frame.getAsInt(Label.ISOUSC));
            assertEquals(6906827, frame.getAsInt(Label.HCHC));
            assertEquals(7617931, frame.getAsInt(Label.HCHP));
            assertEquals("HP..", frame.get(Label.PTEC));
            assertEquals(3, frame.getAsInt(Label.IINST));
            assertEquals(44, frame.getAsInt(Label.IMAX));
            assertEquals(680, frame.getAsInt(Label.PAPP));
            assertNull(frame.get(Label.ADPS));
            assertEquals("A", frame.get(Label.HHPHC));
        }
    }

    @Test
    public void testReadNextFrameCbetmEjp1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-ejp-option-1.raw")), false,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameType.CBETM_LONG_EJP, frame.getType());
            assertEquals("XXXXXXXXXX", frame.get(Label.ADCO));
            assertEquals(30, frame.getAsInt(Label.ISOUSC));
            assertEquals(1111111, frame.getAsInt(Label.EJPHN));
            assertEquals(2222222, frame.getAsInt(Label.EJPHPM));
            assertNull(frame.get(Label.PEJP));
            assertEquals("HN..", frame.get(Label.PTEC));
            assertEquals(10, frame.getAsInt(Label.IINST1));
            assertEquals(5, frame.getAsInt(Label.IINST2));
            assertEquals(8, frame.getAsInt(Label.IINST3));
            assertEquals(38, frame.getAsInt(Label.IMAX1));
            assertEquals(42, frame.getAsInt(Label.IMAX2));
            assertEquals(44, frame.getAsInt(Label.IMAX3));
            assertEquals(17480, frame.getAsInt(Label.PMAX));
            assertEquals(5800, frame.getAsInt(Label.PAPP));
            assertEquals("00", frame.get(Label.PPOT));
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccTempo1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-tempo-option-1.raw")), false,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameType.CBEMM_ICC_TEMPO, frame.getType());
            assertEquals("XXXXXXXXXXXX", frame.get(Label.ADCO));
            assertEquals(45, frame.getAsInt(Label.ISOUSC));
            assertEquals(2697099, frame.getAsInt(Label.BBRHCJB));
            assertEquals(3494559, frame.getAsInt(Label.BBRHPJB));
            assertEquals(41241, frame.getAsInt(Label.BBRHCJW));
            assertEquals(194168, frame.getAsInt(Label.BBRHPJW));
            assertEquals(0, frame.getAsInt(Label.BBRHCJR));
            assertEquals(89736, frame.getAsInt(Label.BBRHPJR));
            assertEquals("HPJR", frame.get(Label.PTEC));
            assertEquals("----", frame.get(Label.DEMAIN));
            assertEquals(3, frame.getAsInt(Label.IINST));
            assertEquals(37, frame.getAsInt(Label.IMAX));
            assertEquals(620, frame.getAsInt(Label.PAPP));
            assertFalse(frame.getLabelToValues().containsKey(Label.ADPS));
            assertNull(frame.get(Label.ADPS));
            assertEquals("Y", frame.get(Label.HHPHC));
            assertEquals("B", frame.getProgrammeCircuit1());
            assertEquals("P2", frame.getProgrammeCircuit2());
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccBase1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-base-option-1.raw")), false,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();
            assertNotNull(frame);
            assertEquals(FrameType.CBEMM_ICC_BASE, frame.getType());
            assertEquals("031762120162", frame.get(Label.ADCO));
            assertEquals(30, frame.getAsInt(Label.ISOUSC));
            assertEquals(190575, frame.getAsInt(Label.BASE));
            assertEquals("TH..", frame.get(Label.PTEC));
            assertEquals(1, frame.getAsInt(Label.IINST));
            assertEquals(90, frame.getAsInt(Label.IMAX));
            assertEquals(270, frame.getAsInt(Label.PAPP));
            assertNull(frame.get(Label.ADPS));
        }
    }

    @Test
    public void testInvalidADPSgrouplineWithAutoRepairActivated() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("invalid-adps-groupline.raw")), true,
                TeleinfoTicMode.HISTORICAL)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(Frame.class, frame.getClass());
            assertEquals(37, frame.getAsInt(Label.ADPS));
        }
    }

    @Test
    public void testReadNextFrameLinkyTICModeStandardThreePhaseProd() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("linky-tic-mode-standard-three-phase-prod.raw")), false,
                TeleinfoTicMode.STANDARD, true)) {
            Frame frame = in.readNextFrame();
            assertNotNull(frame);
            assertEquals(FrameType.LSMT_PROD, frame.getType());
            assertEquals("123456789012", frame.get(Label.ADSC));
            assertEquals("02", frame.get(Label.VTIC));
            assertEquals("", frame.get(Label.DATE));
            assertEquals("     TEMPO      ", frame.get(Label.NGTF));
            assertEquals("    HP  BLEU    ", frame.get(Label.LTARF));
            assertEquals(11604109, frame.getAsInt(Label.EAST));
            assertEquals(2741488, frame.getAsInt(Label.EASF01));
            assertEquals(18, frame.getAsInt(Label.PCOUP));
            assertEquals("2021-04-14T08:26:25", frame.getAsDateTime(Label.DATE));
            DateTimeType dateTime = DateTimeType.valueOf(frame.getAsDateTime(Label.DATE));
            assertEquals(2021, dateTime.getZonedDateTime().getYear());
            assertEquals(Month.APRIL, dateTime.getZonedDateTime().getMonth());
            assertEquals(14, dateTime.getZonedDateTime().getDayOfMonth());
            assertEquals(8, dateTime.getZonedDateTime().getHour());
            assertEquals(26, dateTime.getZonedDateTime().getMinute());
            assertEquals(25, dateTime.getZonedDateTime().getSecond());

            assertNotEquals(TeleinfoBindingConstants.NOT_A_CHANNEL, Label.CCASN.getTimestampChannelName());
            assertEquals("2021-04-14T08:00:00", frame.getAsDateTime(Label.CCASN));
            dateTime = DateTimeType.valueOf(frame.getAsDateTime(Label.CCASN));
            assertEquals(2021, dateTime.getZonedDateTime().getYear());
            assertEquals(Month.APRIL, dateTime.getZonedDateTime().getMonth());
            assertEquals(14, dateTime.getZonedDateTime().getDayOfMonth());
            assertEquals(8, dateTime.getZonedDateTime().getHour());
            assertEquals(0, dateTime.getZonedDateTime().getMinute());
            assertEquals(0, dateTime.getZonedDateTime().getSecond());
        }
    }

    @Test
    public void testReadNextFrameLinkyTICModeStandardSinglePhaseProd() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("linky-tic-mode-standard-single-phase-prod.raw")),
                TeleinfoTicMode.STANDARD, true)) {
            Frame frame = in.readNextFrame();
            assertNotNull(frame);
            assertEquals(FrameType.LSMM_PROD, frame.getType());
            assertEquals("123456789012", frame.get(Label.ADSC));
            assertEquals("02", frame.get(Label.VTIC));
            assertEquals("", frame.get(Label.DATE));
            assertEquals("PRODUCTEUR", frame.get(Label.NGTF));
            assertEquals("INDEX NON CONSO ", frame.get(Label.LTARF));
            assertEquals(0, frame.getAsInt(Label.EAST));
            assertEquals(0, frame.getAsInt(Label.EASF01));
            assertEquals(32781, frame.getAsInt(Label.EAIT));
        }
    }
}
