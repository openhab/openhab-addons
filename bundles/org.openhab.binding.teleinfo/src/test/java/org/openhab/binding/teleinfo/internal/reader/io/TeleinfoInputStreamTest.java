/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.teleinfo.internal.dto.Frame;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccBaseOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccHcOption;
import org.openhab.binding.teleinfo.internal.dto.cbemm.evoicc.FrameCbemmEvolutionIccTempoOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongBaseOption;
import org.openhab.binding.teleinfo.internal.dto.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption.ProgrammeCircuit1;
import org.openhab.binding.teleinfo.internal.dto.common.FrameTempoOption.ProgrammeCircuit2;
import org.openhab.binding.teleinfo.internal.dto.common.Hhphc;
import org.openhab.binding.teleinfo.internal.dto.common.Ptec;
import org.openhab.binding.teleinfo.util.TestUtils;

/**
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoInputStreamTest {
    private static final int TIMEOUT_US = 5000000;

    @Test
    public void testReadNextFrameCbetmBase1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-base-option-1.raw")), TIMEOUT_US, TIMEOUT_US, false)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameCbetmLongBaseOption.class, frame.getClass());
            FrameCbetmLongBaseOption frameCbetmLongBaseOption = (FrameCbetmLongBaseOption) frame;
            assertEquals("XXXXXXXXXXXX", frameCbetmLongBaseOption.getAdco());
            assertEquals(20, frameCbetmLongBaseOption.getIsousc());
            assertEquals(1181243, frameCbetmLongBaseOption.getBase());
            assertEquals(Ptec.TH, frameCbetmLongBaseOption.getPtec());
            assertEquals(0, frameCbetmLongBaseOption.getIinst1());
            assertEquals(2, frameCbetmLongBaseOption.getIinst2());
            assertEquals(0, frameCbetmLongBaseOption.getIinst3());
            assertEquals(26, frameCbetmLongBaseOption.getImax1().intValue());
            assertEquals(18, frameCbetmLongBaseOption.getImax2().intValue());
            assertEquals(27, frameCbetmLongBaseOption.getImax3().intValue());
            assertEquals(7990, frameCbetmLongBaseOption.getPmax());
            assertEquals(540, frameCbetmLongBaseOption.getPapp());
            assertEquals("00", frameCbetmLongBaseOption.getPpot());
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccHc1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-hc-option-1.raw")), TIMEOUT_US, TIMEOUT_US,
                false)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameCbemmEvolutionIccHcOption.class, frame.getClass());
            FrameCbemmEvolutionIccHcOption frameCbemmEvolutionIccHcOption = (FrameCbemmEvolutionIccHcOption) frame;
            assertEquals("XXXXXXXXXXXX", frameCbemmEvolutionIccHcOption.getAdco());
            assertEquals(30, frameCbemmEvolutionIccHcOption.getIsousc());
            assertEquals(6906827, frameCbemmEvolutionIccHcOption.getHchc());
            assertEquals(7617931, frameCbemmEvolutionIccHcOption.getHchp());
            assertEquals(Ptec.HP, frameCbemmEvolutionIccHcOption.getPtec());
            assertEquals(3, frameCbemmEvolutionIccHcOption.getIinst());
            assertEquals(44, frameCbemmEvolutionIccHcOption.getImax().intValue());
            assertEquals(680, frameCbemmEvolutionIccHcOption.getPapp());
            assertNull(frameCbemmEvolutionIccHcOption.getAdps());
            assertEquals(Hhphc.A, frameCbemmEvolutionIccHcOption.getHhphc());
        }
    }

    @Test
    public void testReadNextFrameCbetmEjp1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-ejp-option-1.raw")), TIMEOUT_US, TIMEOUT_US, false)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameCbetmLongEjpOption.class, frame.getClass());
            FrameCbetmLongEjpOption frameCbetmLongEjpOption = (FrameCbetmLongEjpOption) frame;
            assertEquals("XXXXXXXXXX", frameCbetmLongEjpOption.getAdco());
            assertEquals(30, frameCbetmLongEjpOption.getIsousc());
            assertEquals(1111111, frameCbetmLongEjpOption.getEjphn());
            assertEquals(2222222, frameCbetmLongEjpOption.getEjphpm());
            assertNull(frameCbetmLongEjpOption.getPejp());
            assertEquals(Ptec.HN, frameCbetmLongEjpOption.getPtec());
            assertEquals(10, frameCbetmLongEjpOption.getIinst1());
            assertEquals(5, frameCbetmLongEjpOption.getIinst2());
            assertEquals(8, frameCbetmLongEjpOption.getIinst3());
            assertEquals(38, frameCbetmLongEjpOption.getImax1().intValue());
            assertEquals(42, frameCbetmLongEjpOption.getImax2().intValue());
            assertEquals(44, frameCbetmLongEjpOption.getImax3().intValue());
            assertEquals(17480, frameCbetmLongEjpOption.getPmax());
            assertEquals(5800, frameCbetmLongEjpOption.getPapp());
            assertEquals("00", frameCbetmLongEjpOption.getPpot());
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccTempo1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-tempo-option-1.raw")), TIMEOUT_US, TIMEOUT_US,
                false)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameCbemmEvolutionIccTempoOption.class, frame.getClass());
            FrameCbemmEvolutionIccTempoOption frameCbemmEvolutionIccTempoOption = (FrameCbemmEvolutionIccTempoOption) frame;
            assertEquals("XXXXXXXXXXXX", frameCbemmEvolutionIccTempoOption.getAdco());
            assertEquals(45, frameCbemmEvolutionIccTempoOption.getIsousc());
            assertEquals(2697099, frameCbemmEvolutionIccTempoOption.getBbrhcjb());
            assertEquals(3494559, frameCbemmEvolutionIccTempoOption.getBbrhpjb());
            assertEquals(41241, frameCbemmEvolutionIccTempoOption.getBbrhcjw());
            assertEquals(194168, frameCbemmEvolutionIccTempoOption.getBbrhpjw());
            assertEquals(0, frameCbemmEvolutionIccTempoOption.getBbrhcjr());
            assertEquals(89736, frameCbemmEvolutionIccTempoOption.getBbrhpjr());
            assertEquals(Ptec.HPJR, frameCbemmEvolutionIccTempoOption.getPtec());
            assertNull(frameCbemmEvolutionIccTempoOption.getDemain());
            assertEquals(3, frameCbemmEvolutionIccTempoOption.getIinst());
            assertEquals(37, frameCbemmEvolutionIccTempoOption.getImax().intValue());
            assertEquals(620, frameCbemmEvolutionIccTempoOption.getPapp());
            assertNull(frameCbemmEvolutionIccTempoOption.getAdps());
            assertEquals(Hhphc.Y, frameCbemmEvolutionIccTempoOption.getHhphc());
            assertEquals(ProgrammeCircuit1.B, frameCbemmEvolutionIccTempoOption.getProgrammeCircuit1());
            assertEquals(ProgrammeCircuit2.P2, frameCbemmEvolutionIccTempoOption.getProgrammeCircuit2());
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccBase1() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-base-option-1.raw")), TIMEOUT_US, TIMEOUT_US,
                false)) {
            Frame frame = in.readNextFrame();
            assertNotNull(frame);
            assertEquals(FrameCbemmEvolutionIccBaseOption.class, frame.getClass());
            FrameCbemmEvolutionIccBaseOption frameCbemmEvolutionIccBaseOption = (FrameCbemmEvolutionIccBaseOption) frame;
            assertEquals("031762120162", frameCbemmEvolutionIccBaseOption.getAdco());
            assertEquals(30, frameCbemmEvolutionIccBaseOption.getIsousc());
            assertEquals(190575, frameCbemmEvolutionIccBaseOption.getBase());
            assertEquals(Ptec.TH, frameCbemmEvolutionIccBaseOption.getPtec());
            assertEquals(1, frameCbemmEvolutionIccBaseOption.getIinst());
            assertEquals(90, frameCbemmEvolutionIccBaseOption.getImax().intValue());
            assertEquals(270, frameCbemmEvolutionIccBaseOption.getPapp());
            assertNull(frameCbemmEvolutionIccBaseOption.getAdps());
        }
    }

    @Test
    public void testInvalidADPSgrouplineWithAutoRepairActivated() throws Exception {
        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("invalid-adps-groupline.raw")), TIMEOUT_US, TIMEOUT_US,
                true)) {
            Frame frame = in.readNextFrame();

            assertNotNull(frame);
            assertEquals(FrameCbemmEvolutionIccBaseOption.class, frame.getClass());
            FrameCbemmEvolutionIccBaseOption frameCbemmEvolutionIccBaseOption = (FrameCbemmEvolutionIccBaseOption) frame;
            assertEquals(37, frameCbemmEvolutionIccBaseOption.getAdps().intValue());
        }
    }
}
