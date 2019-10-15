package org.openhab.binding.teleinfo.internal.reader.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.openhab.binding.teleinfo.internal.reader.cbemm.evoicc.FrameCbemmEvolutionIccHcOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongBaseOption;
import org.openhab.binding.teleinfo.internal.reader.cbetm.FrameCbetmLongEjpOption;
import org.openhab.binding.teleinfo.internal.reader.common.FrameHcOption.Hhphc;
import org.openhab.binding.teleinfo.internal.reader.common.Ptec;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.binding.teleinfo.util.TestUtils;

public class TeleinfoInputStreamTest {

    @Test
    public void testReadNextFrameCbetmBase1()
            throws FileNotFoundException, IOException, InvalidFrameException, TimeoutException {

        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-base-option-1.raw")))) {
            Frame frame = in.readNextFrame();

            Assert.assertNotNull(frame);
            Assert.assertEquals(FrameCbetmLongBaseOption.class, frame.getClass());
            FrameCbetmLongBaseOption frameCbetmLongBaseOption = (FrameCbetmLongBaseOption) frame;
            Assert.assertEquals("XXXXXXXXXXXX", frameCbetmLongBaseOption.getAdco());
            Assert.assertEquals(20, frameCbetmLongBaseOption.getIsousc());
            Assert.assertEquals(1181243, frameCbetmLongBaseOption.getBase());
            Assert.assertEquals(Ptec.TH, frameCbetmLongBaseOption.getPtec());
            Assert.assertEquals(0, frameCbetmLongBaseOption.getIinst1());
            Assert.assertEquals(2, frameCbetmLongBaseOption.getIinst2());
            Assert.assertEquals(0, frameCbetmLongBaseOption.getIinst3());
            Assert.assertEquals(new Integer(26), frameCbetmLongBaseOption.getImax1());
            Assert.assertEquals(new Integer(18), frameCbetmLongBaseOption.getImax2());
            Assert.assertEquals(new Integer(27), frameCbetmLongBaseOption.getImax3());
            Assert.assertEquals(7990, frameCbetmLongBaseOption.getPmax());
            Assert.assertEquals(540, frameCbetmLongBaseOption.getPapp());
            Assert.assertEquals("00", frameCbetmLongBaseOption.getPpot());
        }
    }

    @Test
    public void testReadNextFrameCbemmEvoIccHc1()
            throws FileNotFoundException, IOException, InvalidFrameException, TimeoutException {

        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbemm-evo-icc-hc-option-1.raw")))) {
            Frame frame = in.readNextFrame();

            Assert.assertNotNull(frame);
            Assert.assertEquals(FrameCbemmEvolutionIccHcOption.class, frame.getClass());
            FrameCbemmEvolutionIccHcOption frameCbemmEvolutionIccHcOption = (FrameCbemmEvolutionIccHcOption) frame;
            Assert.assertEquals("XXXXXXXXXXXX", frameCbemmEvolutionIccHcOption.getAdco());
            Assert.assertEquals(30, frameCbemmEvolutionIccHcOption.getIsousc());
            Assert.assertEquals(6906827, frameCbemmEvolutionIccHcOption.getHchc());
            Assert.assertEquals(7617931, frameCbemmEvolutionIccHcOption.getHchp());
            Assert.assertEquals(Ptec.HP, frameCbemmEvolutionIccHcOption.getPtec());
            Assert.assertEquals(3, frameCbemmEvolutionIccHcOption.getIinst());
            Assert.assertEquals(new Integer(44), frameCbemmEvolutionIccHcOption.getImax());
            Assert.assertEquals(680, frameCbemmEvolutionIccHcOption.getPapp());
            Assert.assertNull(frameCbemmEvolutionIccHcOption.getAdps());
            Assert.assertEquals(Hhphc.A, frameCbemmEvolutionIccHcOption.getHhphc());
        }
    }

    @Test
    public void testReadNextFrameCbetmEjp1()
            throws FileNotFoundException, IOException, InvalidFrameException, TimeoutException {

        try (TeleinfoInputStream in = new TeleinfoInputStream(
                new FileInputStream(TestUtils.getTestFile("cbetm-ejp-option-1.raw")))) {
            Frame frame = in.readNextFrame();

            Assert.assertNotNull(frame);
            Assert.assertEquals(FrameCbetmLongEjpOption.class, frame.getClass());
            FrameCbetmLongEjpOption frameCbetmLongEjpOption = (FrameCbetmLongEjpOption) frame;
            Assert.assertEquals("XXXXXXXXXX", frameCbetmLongEjpOption.getAdco());
            Assert.assertEquals(30, frameCbetmLongEjpOption.getIsousc());
            Assert.assertEquals(1111111, frameCbetmLongEjpOption.getEjphn());
            Assert.assertEquals(2222222, frameCbetmLongEjpOption.getEjphpm());
            Assert.assertNull(frameCbetmLongEjpOption.getPejp());
            Assert.assertEquals(Ptec.HN, frameCbetmLongEjpOption.getPtec());
            Assert.assertEquals(10, frameCbetmLongEjpOption.getIinst1());
            Assert.assertEquals(5, frameCbetmLongEjpOption.getIinst2());
            Assert.assertEquals(8, frameCbetmLongEjpOption.getIinst3());
            Assert.assertEquals(new Integer(38), frameCbetmLongEjpOption.getImax1());
            Assert.assertEquals(new Integer(42), frameCbetmLongEjpOption.getImax2());
            Assert.assertEquals(new Integer(44), frameCbetmLongEjpOption.getImax3());
            Assert.assertEquals(17480, frameCbetmLongEjpOption.getPmax());
            Assert.assertEquals(5800, frameCbetmLongEjpOption.getPapp());
            Assert.assertEquals("00", frameCbetmLongEjpOption.getPpot());
        }
    }
}
