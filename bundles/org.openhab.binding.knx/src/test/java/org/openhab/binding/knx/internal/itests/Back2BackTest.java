/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.itests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.client.DummyKNXNetworkLink;
import org.openhab.binding.knx.internal.client.DummyProcessListener;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.dpt.ValueDecoder;
import org.openhab.binding.knx.internal.dpt.ValueEncoder;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Type;
import org.openhab.core.util.ColorUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;

/**
 * Integration test to check conversion from raw KNX frame data to OH data types and back.
 *
 * This test checks
 * <ul>
 * <li>if OH can properly decode raw data payload from KNX frames using {@link ValueDecoder#decode()},
 * <li>if OH can properly encode the data for handover to Calimero using {@link ValueEncoder#encode()},
 * <li>if Calimero supports and correctly handles the data conversion to raw bytes for sending.
 * </ul>
 *
 * In addition, it checks if newly integrated releases of Calimero introduce new DPT types not yet
 * handled by this test. However, new subtypes are not detected.
 *
 * @see DummyKNXNetworkLink
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
public class Back2BackTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(Back2BackTest.class);
    static Set<Integer> dptTested = new HashSet<>();
    static final byte[] F32_MINUS_ONE = new byte[] { (byte) 0xbf, (byte) 0x80, 0, 0 };

    /**
     * helper method for integration tests
     *
     * @param dpt DPT type, e.g. "251.600", see 03_07_02-Datapoint-Types-v02.02.01-AS.pdf
     * @param rawData byte array containing raw data, known content
     * @param ohReferenceData OpenHAB data type, initialized to known good value
     * @param maxDistance byte array containing maximal deviations when comparing byte arrays (rawData against created
     *            KNX frame), may be empty if no deviation is considered
     * @param bitmask to mask certain bits in the raw to raw comparison, required for multivalued KNX frames
     */
    void helper(String dpt, byte[] rawData, Type ohReferenceData, byte[] maxDistance, byte[] bitmask) {
        try {
            DummyKNXNetworkLink link = new DummyKNXNetworkLink();
            ProcessCommunicator pc = new ProcessCommunicatorImpl(link);
            DummyProcessListener processListener = new DummyProcessListener();
            pc.addProcessListener(processListener);

            GroupAddress groupAddress = new GroupAddress(2, 4, 6);
            Datapoint datapoint = new CommandDP(groupAddress, "dummy GA", 0,
                    DPTUtil.NORMALIZED_DPT.getOrDefault(dpt, dpt));

            // 0) check usage of helper()
            assertTrue(rawData.length > 0);
            if (maxDistance.length == 0) {
                maxDistance = new byte[rawData.length];
            }
            assertEquals(rawData.length, maxDistance.length, "incorrect length of maxDistance array");
            if (bitmask.length == 0) {
                bitmask = new byte[rawData.length];
                Arrays.fill(bitmask, (byte) 0xff);
            }
            assertEquals(rawData.length, bitmask.length, "incorrect length of bitmask array");
            int mainType = Integer.parseUnsignedInt(dpt.substring(0, dpt.indexOf('.')));
            dptTested.add(mainType);
            // check if OH would be able to send out a frame, given the type
            Set<Integer> knownWorking = Set.of(1, 3, 5);
            if (!knownWorking.contains(mainType)) {
                Set<Class<? extends Type>> allowedTypes = DPTUtil.getAllowedTypes("" + mainType);
                if (!allowedTypes.contains(ohReferenceData.getClass())) {
                    LOGGER.warn(
                            "test for DPT {} uses type {} which is not contained in DPT_TYPE_MAP, sending may not be allowed",
                            dpt, ohReferenceData.getClass());
                }
            }

            // 1) check if the decoder works (rawData to known good type ohReferenceData)
            //
            // This test is based on known raw data. The mapping to openHAB type is known and confirmed.
            // In this test, only ValueDecoder.decode() is involved.

            // raw data of the DPT on application layer, without all headers from the layers below
            // see 03_07_02-Datapoint-Types-v02.02.01-AS.pdf
            Type ohData = (Type) ValueDecoder.decode(dpt, rawData, ohReferenceData.getClass());
            assertNotNull(ohData, "could not decode frame data for DPT " + dpt);
            if ((ohReferenceData instanceof HSBType hsbReferenceData) && (ohData instanceof HSBType hsbData)) {
                assertTrue(hsbReferenceData.closeTo(hsbData, 0.001),
                        "comparing reference to decoded value for DPT " + dpt);
            } else {
                assertEquals(ohReferenceData, ohData, "comparing reference to decoded value: failed for DPT " + dpt
                        + ", check ValueEncoder.decode()");
            }

            // 2) check the encoding (ohData to raw data)
            //
            // Test approach is to
            // a) encode the value into String format using ValueEncoder.encode(),
            // b) pass it to Calimero for conversion into a raw representation, and
            // c) finally grab raw data bytes from a custom KNXNetworkLink implementation
            String enc = ValueEncoder.encode(ohData, dpt);
            pc.write(datapoint, enc);

            byte[] frame = link.getLastFrame();
            assertNotNull(frame);
            // remove header; for compact frames extract data byte from header
            frame = DataUnitBuilder.extractASDU(frame);
            assertEquals(rawData.length, frame.length,
                    "unexpected length of KNX frame: " + HexUtils.bytesToHex(frame, " "));
            for (int i = 0; i < rawData.length; i++) {
                assertEquals(rawData[i] & bitmask[i] & 0xff, frame[i] & bitmask[i] & 0xff, maxDistance[i],
                        "unexpected content in encoded data, " + i);
            }

            // 3) Check date provided by Calimero library as input via loopback, it should match the initial data
            //
            // Deviations in some bytes of the frame may be possible due to data conversion, e.g. for HSBType.
            // This is why maxDistance is used.
            byte[] input = processListener.getLastFrame();
            LOGGER.info("loopback {}", HexUtils.bytesToHex(input, " "));
            assertNotNull(input);
            assertEquals(rawData.length, input.length, "unexpected length of loopback frame");
            for (int i = 0; i < rawData.length; i++) {
                assertEquals(rawData[i] & bitmask[i] & 0xff, input[i] & bitmask[i] & 0xff, maxDistance[i],
                        "unexpected content in loopback data, " + i);
            }

            pc.close();
        } catch (KNXException e) {
            LOGGER.warn("exception occurred: {}", e.toString());
            assertEquals("", e.toString());
        }
    }

    void helper(String dpt, byte[] rawData, Type ohReferenceData) {
        helper(dpt, rawData, ohReferenceData, new byte[0], new byte[0]);
    }

    @Test
    void testDpt1() {
        helper("1.001", new byte[] { 0 }, OnOffType.OFF);
        helper("1.001", new byte[] { 1 }, OnOffType.ON);
        helper("1.001", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.001", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.002", new byte[] { 0 }, OnOffType.OFF);
        helper("1.002", new byte[] { 1 }, OnOffType.ON);
        helper("1.002", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.002", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.003", new byte[] { 0 }, OnOffType.OFF);
        helper("1.003", new byte[] { 1 }, OnOffType.ON);
        helper("1.003", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.003", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.004", new byte[] { 0 }, OnOffType.OFF);
        helper("1.004", new byte[] { 1 }, OnOffType.ON);
        helper("1.004", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.004", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.005", new byte[] { 0 }, OnOffType.OFF);
        helper("1.005", new byte[] { 1 }, OnOffType.ON);
        helper("1.005", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.005", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.006", new byte[] { 0 }, OnOffType.OFF);
        helper("1.006", new byte[] { 1 }, OnOffType.ON);
        helper("1.006", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.006", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.007", new byte[] { 0 }, OnOffType.OFF);
        helper("1.007", new byte[] { 1 }, OnOffType.ON);
        helper("1.007", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.007", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.008", new byte[] { 0 }, UpDownType.UP);
        helper("1.008", new byte[] { 1 }, UpDownType.DOWN);
        // NOTE: This is how DPT 1.009 is defined: 0: open, 1: closed
        // For historical reasons it is defined the other way on OH
        helper("1.009", new byte[] { 0 }, OnOffType.OFF);
        helper("1.009", new byte[] { 1 }, OnOffType.ON);
        helper("1.009", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.009", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.010", new byte[] { 0 }, StopMoveType.STOP);
        helper("1.010", new byte[] { 1 }, StopMoveType.MOVE);
        helper("1.011", new byte[] { 0 }, OnOffType.OFF);
        helper("1.011", new byte[] { 1 }, OnOffType.ON);
        helper("1.011", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.011", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.012", new byte[] { 0 }, OnOffType.OFF);
        helper("1.012", new byte[] { 1 }, OnOffType.ON);
        helper("1.012", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.012", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.013", new byte[] { 0 }, OnOffType.OFF);
        helper("1.013", new byte[] { 1 }, OnOffType.ON);
        helper("1.013", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.013", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.014", new byte[] { 0 }, OnOffType.OFF);
        helper("1.014", new byte[] { 1 }, OnOffType.ON);
        helper("1.014", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.014", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.015", new byte[] { 0 }, OnOffType.OFF);
        helper("1.015", new byte[] { 1 }, OnOffType.ON);
        helper("1.015", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.015", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.016", new byte[] { 0 }, OnOffType.OFF);
        helper("1.016", new byte[] { 1 }, OnOffType.ON);
        helper("1.016", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.016", new byte[] { 1 }, OpenClosedType.OPEN);
        // DPT 1.017 is a special case, "trigger" has no "value", both 0 and 1 shall trigger
        helper("1.017", new byte[] { 0 }, OnOffType.OFF);
        helper("1.017", new byte[] { 0 }, OpenClosedType.CLOSED);
        // Calimero maps it always to 0
        // helper("1.017", new byte[] { 1 }, OnOffType.ON);
        helper("1.018", new byte[] { 0 }, OnOffType.OFF);
        helper("1.018", new byte[] { 1 }, OnOffType.ON);
        helper("1.018", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.018", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.019", new byte[] { 0 }, OnOffType.OFF);
        helper("1.019", new byte[] { 1 }, OnOffType.ON);
        helper("1.019", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.019", new byte[] { 1 }, OpenClosedType.OPEN);

        helper("1.021", new byte[] { 0 }, OnOffType.OFF);
        helper("1.021", new byte[] { 1 }, OnOffType.ON);
        helper("1.021", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.021", new byte[] { 1 }, OpenClosedType.OPEN);
        // DPT 1.022 is mapped to decimal, Calimero does not follow the recommendation
        // from KNX spec to add offset 1
        helper("1.022", new byte[] { 0 }, DecimalType.valueOf("0"));
        helper("1.022", new byte[] { 1 }, DecimalType.valueOf("1"));
        helper("1.023", new byte[] { 0 }, OnOffType.OFF);
        helper("1.023", new byte[] { 1 }, OnOffType.ON);
        helper("1.023", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.023", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.024", new byte[] { 0 }, OnOffType.OFF);
        helper("1.024", new byte[] { 1 }, OnOffType.ON);
        helper("1.024", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.024", new byte[] { 1 }, OpenClosedType.OPEN);

        helper("1.100", new byte[] { 0 }, OnOffType.OFF);
        helper("1.100", new byte[] { 1 }, OnOffType.ON);
        helper("1.100", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.100", new byte[] { 1 }, OpenClosedType.OPEN);

        helper("1.1200", new byte[] { 0 }, OnOffType.OFF);
        helper("1.1200", new byte[] { 1 }, OnOffType.ON);
        helper("1.1200", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.1200", new byte[] { 1 }, OpenClosedType.OPEN);
        helper("1.1201", new byte[] { 0 }, OnOffType.OFF);
        helper("1.1201", new byte[] { 1 }, OnOffType.ON);
        helper("1.1201", new byte[] { 0 }, OpenClosedType.CLOSED);
        helper("1.1201", new byte[] { 1 }, OpenClosedType.OPEN);
    }

    @Test
    void testDpt2() {
        for (int subType = 1; subType <= 12; subType++) {
            helper("2." + String.format("%03d", subType), new byte[] { (byte) (subType % 4) },
                    new DecimalType(subType % 4));
        }
    }

    @Test
    void testDpt3() {
        // DPT 3.007 and DPT 3.008 consist of a control bit (1 bit) and stepsize (3 bit)
        // if stepsize is 0, OH will ignore the command
        byte controlBit = 1 << 3;
        // loop all other step sizes and check only the control bit
        for (byte i = 1; i < 8; i++) {
            helper("3.007", new byte[] { i }, IncreaseDecreaseType.DECREASE, new byte[0], new byte[] { controlBit });
            helper("3.007", new byte[] { (byte) (i + controlBit) }, IncreaseDecreaseType.INCREASE, new byte[0],
                    new byte[] { controlBit });
            helper("3.008", new byte[] { i }, UpDownType.UP, new byte[0], new byte[] { controlBit });
            helper("3.008", new byte[] { (byte) (i + controlBit) }, UpDownType.DOWN, new byte[0],
                    new byte[] { controlBit });
        }

        // check if OH ignores incoming frames with mask 0 (mapped to UndefType)
        Assertions.assertFalse(ValueDecoder.decode("3.007", new byte[] { 0 },
                IncreaseDecreaseType.class) instanceof IncreaseDecreaseType);
        Assertions.assertFalse(ValueDecoder.decode("3.007", new byte[] { controlBit },
                IncreaseDecreaseType.class) instanceof IncreaseDecreaseType);
        Assertions.assertFalse(ValueDecoder.decode("3.008", new byte[] { 0 }, UpDownType.class) instanceof UpDownType);
        Assertions.assertFalse(
                ValueDecoder.decode("3.008", new byte[] { controlBit }, UpDownType.class) instanceof UpDownType);
    }

    @Test
    void testDpt5() {
        helper("5.001", new byte[] { 0 }, new QuantityType<>("0 %"));
        helper("5.001", new byte[] { (byte) 0xff }, new QuantityType<>("100 %"));
        // fallback: PercentType
        helper("5.001", new byte[] { 0 }, new PercentType(0));
        helper("5.001", new byte[] { (byte) 0x80 }, new PercentType(50));
        helper("5.001", new byte[] { (byte) 0xff }, new PercentType(100));

        helper("5.003", new byte[] { 0 }, new QuantityType<>("0 °"));
        helper("5.003", new byte[] { (byte) 0xff }, new QuantityType<>("360 °"));
        helper("5.004", new byte[] { 0 }, new QuantityType<>("0 %"));
        helper("5.004", new byte[] { (byte) 0x64 }, new QuantityType<>("100 %"));
        helper("5.004", new byte[] { (byte) 0xff }, new QuantityType<>("255 %"));
        // PercentType cannot encode values >100%, not supported for 5.004
        helper("5.005", new byte[] { 42 }, new DecimalType(42));
        helper("5.005", new byte[] { (byte) 0xff }, new DecimalType(255));
        helper("5.006", new byte[] { 0 }, new DecimalType(0));
        helper("5.006", new byte[] { 42 }, new DecimalType(42));
        helper("5.006", new byte[] { (byte) 0xfe }, new DecimalType(254));

        helper("5.010", new byte[] { 42 }, new DecimalType(42));
        helper("5.010", new byte[] { (byte) 0xff }, new DecimalType(255));
    }

    @Test
    void testDpt6() {
        helper("6.001", new byte[] { 0 }, new QuantityType<>("0 %"));
        helper("6.001", new byte[] { (byte) 0x7f }, new QuantityType<>("127 %"));
        helper("6.001", new byte[] { (byte) 0xff }, new QuantityType<>("-1 %"));
        // PercentType cannot encode values >100% or <0%, not supported for 6.001

        helper("6.010", new byte[] { 0 }, new DecimalType(0));
        helper("6.010", new byte[] { (byte) 0x7f }, new DecimalType(127));
        helper("6.010", new byte[] { (byte) 0xff }, new DecimalType(-1));

        helper("6.020", new byte[] { 9 }, StringType.valueOf("0/0/0/0/1 0"));
    }

    @Test
    void testDpt7() {
        helper("7.001", new byte[] { 0, 42 }, new DecimalType(42));
        helper("7.001", new byte[] { (byte) 0xff, (byte) 0xff }, new DecimalType(65535));
        helper("7.002", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 ms"));
        helper("7.002", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("7.002", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 ms"));
        helper("7.003", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("7.003", new byte[] { (byte) 0x00, (byte) 0x64 }, new QuantityType<>("1000 ms"));
        helper("7.003", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("2550 ms"));
        helper("7.003", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("655350 ms"));
        helper("7.004", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("7.004", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("25500 ms"));
        helper("7.004", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("6553500 ms"));
        helper("7.005", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 s"));
        helper("7.005", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 s"));
        helper("7.005", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 s"));
        helper("7.006", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 min"));
        helper("7.006", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 min"));
        helper("7.006", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 min"));
        helper("7.006", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("3932100 s"));
        helper("7.007", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 h"));
        helper("7.007", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 h"));
        helper("7.007", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("918000 s"));
        helper("7.007", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 h"));

        helper("7.010", new byte[] { 0, 42 }, new DecimalType(42));
        helper("7.010", new byte[] { (byte) 0xff, (byte) 0xff }, new DecimalType(65535));
        helper("7.011", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 mm"));
        helper("7.011", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 mm"));
        helper("7.011", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 mm"));
        helper("7.012", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 mA"));
        helper("7.012", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 mA"));
        helper("7.012", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 mA"));
        helper("7.013", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 lx"));
        helper("7.013", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 lx"));
        helper("7.013", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 lx"));

        helper("7.600", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 K"));
        helper("7.600", new byte[] { (byte) 0x00, (byte) 0xff }, new QuantityType<>("255 K"));
        helper("7.600", new byte[] { (byte) 0xff, (byte) 0xff }, new QuantityType<>("65535 K"));
    }

    @Test
    void testDpt8() {
        helper("8.001", new byte[] { (byte) 0x7f, (byte) 0xff }, new DecimalType(32767));
        helper("8.001", new byte[] { (byte) 0x80, (byte) 0x00 }, new DecimalType(-32768));
        helper("8.002", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 ms"));
        helper("8.002", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 ms"));
        helper("8.002", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("8.003", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-327680 ms"));
        helper("8.003", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("327670 ms"));
        helper("8.003", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("8.004", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-3276800 ms"));
        helper("8.004", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("3276700 ms"));
        helper("8.004", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ms"));
        helper("8.005", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 s"));
        helper("8.005", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 s"));
        helper("8.005", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 s"));
        helper("8.006", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 min"));
        helper("8.006", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 min"));
        helper("8.006", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 min"));
        helper("8.007", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 h"));
        helper("8.007", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 h"));
        helper("8.007", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 h"));

        helper("8.010", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-327.68 %"));
        helper("8.011", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 °"));
        helper("8.011", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 °"));
        helper("8.011", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 °"));
        helper("8.012", new byte[] { (byte) 0x80, (byte) 0x00 }, new QuantityType<>("-32768 m"));
        helper("8.012", new byte[] { (byte) 0x7f, (byte) 0xff }, new QuantityType<>("32767 m"));
        helper("8.012", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 m"));
    }

    @Test
    void testDpt9() {
        // special float with sign, 4-bit exponent, and mantissa in two's complement notation
        // ref: KNX spec, 03_07_02-Datapoint-Types
        // FIXME according to spec, value 0x7fff shall be regarded as "invalid data"
        // TODO add tests for clipping at lower boundary (e.g. absolute zero)
        helper("9.001", new byte[] { (byte) 0x00, (byte) 0x64 }, new QuantityType<>("1 °C"));
        helper("9.001", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 °C"));
        helper("9.001", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 °C"));
        helper("9.001", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 °C"));
        // lower values than absolute zero will be set to abs. zero (-273 °C)
        // helper("9.001", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-273 °C"));
        helper("9.002", new byte[] { (byte) 0x00, (byte) 0x64 }, new QuantityType<>("1 K"));
        helper("9.002", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 K"));
        helper("9.002", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 K"));
        helper("9.002", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 K"));
        helper("9.002", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 K"));
        helper("9.003", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 K/h"));
        helper("9.003", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 K/h"));
        helper("9.003", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 K/h"));
        helper("9.003", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 K/h"));
        helper("9.004", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 lx"));
        helper("9.004", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 lx"));
        helper("9.004", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 lx"));
        // no negative values allowed for DPTs 9.004-9.008
        helper("9.005", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 m/s"));
        helper("9.005", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 m/s"));
        helper("9.005", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 m/s"));
        helper("9.005", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 m/s"));
        helper("9.005", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 m/s"));
        helper("9.005", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 m/s"));
        helper("9.006", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 Pa"));
        helper("9.006", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 Pa"));
        helper("9.006", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 Pa"));
        helper("9.007", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 %"));
        helper("9.007", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 %"));
        helper("9.007", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 %"));
        helper("9.008", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 ppm"));
        helper("9.008", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 ppm"));
        helper("9.008", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 ppm"));
        helper("9.009", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 m³/h"));
        helper("9.009", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 m³/h"));
        helper("9.009", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 m³/h"));
        helper("9.009", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 m³/h"));
        helper("9.010", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 s"));
        helper("9.010", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 s"));
        helper("9.010", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 s"));
        helper("9.010", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 s"));
        helper("9.011", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 ms"));
        helper("9.011", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 ms"));
        helper("9.011", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 ms"));
        helper("9.011", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 ms"));

        helper("9.020", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 mV"));
        helper("9.020", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 mV"));
        helper("9.020", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 mV"));
        helper("9.020", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 mV"));
        helper("9.021", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 mA"));
        helper("9.021", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 mA"));
        helper("9.021", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 mA"));
        helper("9.021", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 mA"));
        helper("9.022", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 W/m²"));
        helper("9.022", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 W/m²"));
        helper("9.022", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 W/m²"));
        helper("9.022", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 W/m²"));
        helper("9.023", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 K/%"));
        helper("9.023", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 K/%"));
        helper("9.023", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 K/%"));
        helper("9.023", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 K/%"));
        helper("9.024", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 kW"));
        helper("9.024", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 kW"));
        helper("9.024", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 kW"));
        helper("9.024", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 kW"));
        helper("9.025", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 l/h"));
        helper("9.025", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 l/h"));
        helper("9.025", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 l/h"));
        helper("9.025", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 l/h"));
        helper("9.026", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 l/m²"));
        helper("9.026", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 l/m²"));
        helper("9.026", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 l/m²"));
        helper("9.026", new byte[] { (byte) 0xf8, (byte) 0x00 }, new QuantityType<>("-671088.64 l/m²"));
        helper("9.027", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 °F"));
        helper("9.027", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 °F"));
        helper("9.027", new byte[] { (byte) 0x87, (byte) 0x9c }, new QuantityType<>("-1 °F"));
        // lower values than absolute zero will be set to abs. zero (-459.6 °F)
        helper("9.028", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 km/h"));
        helper("9.028", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 km/h"));
        helper("9.028", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 km/h"));
        // no negative values allowed for DPTs 9.028-9.030
        helper("9.029", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 g/m³"));
        helper("9.029", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 g/m³"));
        helper("9.029", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 g/m³"));
        helper("9.030", new byte[] { (byte) 0x07, (byte) 0xff }, new QuantityType<>("20.47 µg/m³"));
        helper("9.030", new byte[] { (byte) 0x7f, (byte) 0xfe }, new QuantityType<>("670433.28 µg/m³"));
        helper("9.030", new byte[] { (byte) 0x00, (byte) 0x00 }, new QuantityType<>("0 µg/m³"));
    }

    @Test
    void testDpt10() {
        // TODO check handling of DPT10: date is not set to current date, but 1970-01-01 + offset if day is given
        // maybe we should change the semantics and use current date + offset if day is given

        // note: local timezone is set when creating DateTimeType, for example "1970-01-01Thh:mm:ss.000+0100"

        // no-day
        assertTrue(Objects
                .toString(ValueDecoder.decode("10.001", new byte[] { (byte) 0x11, (byte) 0x1e, 0 }, DecimalType.class))
                .startsWith("1970-01-01T17:30:00.000+"));
        // Thursday, this is correct for 1970-01-01
        assertTrue(Objects
                .toString(ValueDecoder.decode("10.001", new byte[] { (byte) 0x91, (byte) 0x1e, 0 }, DecimalType.class))
                .startsWith("1970-01-01T17:30:00.000+"));
        // Monday -> 1970-01-05
        assertTrue(Objects
                .toString(ValueDecoder.decode("10.001", new byte[] { (byte) 0x31, (byte) 0x1e, 0 }, DecimalType.class))
                .startsWith("1970-01-05T17:30:00.000+"));

        // Thursday, otherwise first byte of encoded data will not match
        helper("10.001", new byte[] { (byte) 0x91, (byte) 0x1e, (byte) 0x0 }, new DateTimeType("17:30:00"));
        helper("10.001", new byte[] { (byte) 0x11, (byte) 0x1e, (byte) 0x0 }, new DateTimeType("17:30:00"), new byte[0],
                new byte[] { (byte) 0x1f, (byte) 0xff, (byte) 0xff });
    }

    @Test
    void testDpt11() {
        // note: local timezone and dst is set when creating DateTimeType, for example "2019-06-12T00:00:00.000+0200"
        helper("11.001", new byte[] { (byte) 12, 6, 19 }, new DateTimeType("2019-06-12"));
    }

    @Test
    void testDpt12() {
        helper("12.001", new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe },
                new DecimalType("4294967294"));
        helper("12.100", new byte[] { 0, 0, 0, 60 }, new QuantityType<>("60 s"));
        helper("12.100", new byte[] { 0, 0, 0, 60 }, new QuantityType<>("1 min"));
        helper("12.101", new byte[] { 0, 0, 0, 60 }, new QuantityType<>("60 min"));
        helper("12.101", new byte[] { 0, 0, 0, 60 }, new QuantityType<>("1 h"));
        helper("12.102", new byte[] { 0, 0, 0, 1 }, new QuantityType<>("1 h"));
        helper("12.102", new byte[] { 0, 0, 0, 1 }, new QuantityType<>("60 min"));

        helper("12.1200", new byte[] { 0, 0, 0, 1 }, new QuantityType<>("1 l"));
        helper("12.1200", new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe },
                new QuantityType<>("4294967294 l"));
        helper("12.1201", new byte[] { 0, 0, 0, 1 }, new QuantityType<>("1 m³"));
        helper("12.1201", new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe },
                new QuantityType<>("4294967294 m³"));
    }

    @Test
    void testDpt13() {
        helper("13.001", new byte[] { 0, 0, 0, 0 }, new DecimalType(0));
        helper("13.001", new byte[] { 0, 0, 0, 42 }, new DecimalType(42));
        helper("13.001", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new DecimalType(2147483647));
        // KNX representation typically uses two's complement
        helper("13.001", new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff }, new DecimalType(-1));
        helper("13.001", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 }, new DecimalType(-2147483648));
        helper("13.002", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 m³/h"));
        helper("13.002", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 m³/h"));
        helper("13.002", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 m³/h"));

        helper("13.010", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 Wh"));
        helper("13.010", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 Wh"));
        helper("13.010", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 Wh"));
        helper("13.011", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 VAh"));
        helper("13.011", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 VAh"));
        helper("13.011", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 VAh"));
        helper("13.012", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 varh"));
        helper("13.012", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 varh"));
        helper("13.012", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 varh"));
        helper("13.013", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 kWh"));
        helper("13.013", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 kWh"));
        helper("13.013", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 kWh"));
        helper("13.014", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 VAh"));
        helper("13.014", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648000 VAh"));
        helper("13.014", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647000 VAh"));
        helper("13.015", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 kvarh"));
        helper("13.015", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 kvarh"));
        helper("13.015", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 kvarh"));
        helper("13.016", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 MWh"));
        helper("13.016", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 MWh"));
        helper("13.016", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 MWh"));

        helper("13.100", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 s"));
        helper("13.100", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 s"));
        helper("13.100", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 s"));

        helper("13.1200", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 l"));
        helper("13.1200", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 l"));
        helper("13.1200", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 l"));
        helper("13.1201", new byte[] { 0, 0, 0, 0 }, new QuantityType<>("0 m³"));
        helper("13.1201", new byte[] { (byte) 0x80, (byte) 0x0, (byte) 0x0, (byte) 0x0 },
                new QuantityType<>("-2147483648 m³"));
        helper("13.1201", new byte[] { (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff },
                new QuantityType<>("2147483647 m³"));
    }

    @Test
    void testDpt14() {
        helper("14.000", new byte[] { (byte) 0x3f, (byte) 0x80, 0, 0 }, new QuantityType<>("1 m/s²"));
        helper("14.000", F32_MINUS_ONE, new QuantityType<>("-1 m/s²"));
        helper("14.001", F32_MINUS_ONE, new QuantityType<>("-1 rad/s²"));
        helper("14.002", F32_MINUS_ONE, new QuantityType<>("-1 J/mol"));
        helper("14.003", F32_MINUS_ONE, new QuantityType<>("-1 /s"));
        helper("14.004", F32_MINUS_ONE, new QuantityType<>("-1 mol"));
        helper("14.005", F32_MINUS_ONE, new DecimalType("-1"));
        helper("14.006", F32_MINUS_ONE, new QuantityType<>("-1 rad"));
        helper("14.007", F32_MINUS_ONE, new QuantityType<>("-1 °"));
        helper("14.008", F32_MINUS_ONE, new QuantityType<>("-1 J*s"));
        helper("14.009", F32_MINUS_ONE, new QuantityType<>("-1 rad/s"));
        helper("14.010", F32_MINUS_ONE, new QuantityType<>("-1 m²"));
        helper("14.011", F32_MINUS_ONE, new QuantityType<>("-1 F"));
        helper("14.012", F32_MINUS_ONE, new QuantityType<>("-1 C/m²"));
        helper("14.013", F32_MINUS_ONE, new QuantityType<>("-1 C/m³"));
        helper("14.014", F32_MINUS_ONE, new QuantityType<>("-1 m²/N"));
        helper("14.015", F32_MINUS_ONE, new QuantityType<>("-1 S"));
        helper("14.016", F32_MINUS_ONE, new QuantityType<>("-1 S/m"));
        helper("14.017", F32_MINUS_ONE, new QuantityType<>("-1 kg/m³"));
        helper("14.018", F32_MINUS_ONE, new QuantityType<>("-1 C"));
        helper("14.019", F32_MINUS_ONE, new QuantityType<>("-1 A"));
        helper("14.020", F32_MINUS_ONE, new QuantityType<>("-1 A/m²"));
        helper("14.021", F32_MINUS_ONE, new QuantityType<>("-1 C*m"));
        helper("14.022", F32_MINUS_ONE, new QuantityType<>("-1 C/m²"));
        helper("14.023", F32_MINUS_ONE, new QuantityType<>("-1 V/m"));
        helper("14.024", F32_MINUS_ONE, new QuantityType<>("-1 V*m")); // SI unit is Vm
        helper("14.025", F32_MINUS_ONE, new QuantityType<>("-1 C/m²"));
        helper("14.026", F32_MINUS_ONE, new QuantityType<>("-1 C/m²"));
        helper("14.027", F32_MINUS_ONE, new QuantityType<>("-1 V"));
        helper("14.028", F32_MINUS_ONE, new QuantityType<>("-1 V"));
        helper("14.029", F32_MINUS_ONE, new QuantityType<>("-1 A*m²"));
        helper("14.030", F32_MINUS_ONE, new QuantityType<>("-1 V"));
        helper("14.031", F32_MINUS_ONE, new QuantityType<>("-1 J"));
        helper("14.032", F32_MINUS_ONE, new QuantityType<>("-1 N"));
        helper("14.033", F32_MINUS_ONE, new QuantityType<>("-1 /s"));
        helper("14.034", F32_MINUS_ONE, new QuantityType<>("-1 rad/s"));
        helper("14.035", F32_MINUS_ONE, new QuantityType<>("-1 J/K"));
        helper("14.036", F32_MINUS_ONE, new QuantityType<>("-1 W"));
        helper("14.037", F32_MINUS_ONE, new QuantityType<>("-1 J"));
        helper("14.038", F32_MINUS_ONE, new QuantityType<>("-1 Ohm"));
        helper("14.039", F32_MINUS_ONE, new QuantityType<>("-1 m"));
        helper("14.040", F32_MINUS_ONE, new QuantityType<>("-1 J"));
        helper("14.041", F32_MINUS_ONE, new QuantityType<>("-1 cd/m²"));
        helper("14.042", F32_MINUS_ONE, new QuantityType<>("-1 lm"));
        helper("14.043", F32_MINUS_ONE, new QuantityType<>("-1 cd"));
        helper("14.044", F32_MINUS_ONE, new QuantityType<>("-1 A/m"));
        helper("14.045", F32_MINUS_ONE, new QuantityType<>("-1 Wb"));
        helper("14.046", F32_MINUS_ONE, new QuantityType<>("-1 T"));
        helper("14.047", F32_MINUS_ONE, new QuantityType<>("-1 A*m²"));
        helper("14.048", F32_MINUS_ONE, new QuantityType<>("-1 T"));
        helper("14.049", F32_MINUS_ONE, new QuantityType<>("-1 A/m"));
        helper("14.050", F32_MINUS_ONE, new QuantityType<>("-1 A"));
        helper("14.051", F32_MINUS_ONE, new QuantityType<>("-1 kg"));
        helper("14.052", F32_MINUS_ONE, new QuantityType<>("-1 kg/s"));
        helper("14.053", F32_MINUS_ONE, new QuantityType<>("-1 N/s"));
        helper("14.054", F32_MINUS_ONE, new QuantityType<>("-1 rad"));
        helper("14.055", F32_MINUS_ONE, new QuantityType<>("-1 °"));
        helper("14.056", F32_MINUS_ONE, new QuantityType<>("-1 W"));
        helper("14.057", F32_MINUS_ONE, new DecimalType("-1"));
        helper("14.058", F32_MINUS_ONE, new QuantityType<>("-1 Pa"));
        helper("14.059", F32_MINUS_ONE, new QuantityType<>("-1 Ohm"));
        helper("14.060", F32_MINUS_ONE, new QuantityType<>("-1 Ohm"));
        helper("14.061", F32_MINUS_ONE, new QuantityType<>("-1 Ohm*m"));
        helper("14.062", F32_MINUS_ONE, new QuantityType<>("-1 H"));
        helper("14.063", F32_MINUS_ONE, new QuantityType<>("-1 sr"));
        helper("14.064", F32_MINUS_ONE, new QuantityType<>("-1 W/m²"));
        helper("14.065", F32_MINUS_ONE, new QuantityType<>("-1 m/s"));
        helper("14.066", F32_MINUS_ONE, new QuantityType<>("-1 Pa"));
        helper("14.067", F32_MINUS_ONE, new QuantityType<>("-1 N/m"));
        helper("14.068", new byte[] { (byte) 0x3f, (byte) 0x80, 0, 0 }, new QuantityType<>("1 °C"));
        helper("14.068", F32_MINUS_ONE, new QuantityType<>("-1 °C"));
        helper("14.069", F32_MINUS_ONE, new QuantityType<>("-1 K"));
        helper("14.070", F32_MINUS_ONE, new QuantityType<>("-1 K"));
        helper("14.071", F32_MINUS_ONE, new QuantityType<>("-1 J/K"));
        helper("14.072", F32_MINUS_ONE, new QuantityType<>("-1 W/m/K"));
        helper("14.073", F32_MINUS_ONE, new QuantityType<>("-1 V/K"));
        helper("14.074", F32_MINUS_ONE, new QuantityType<>("-1 s"));
        helper("14.075", F32_MINUS_ONE, new QuantityType<>("-1 N*m"));
        helper("14.076", F32_MINUS_ONE, new QuantityType<>("-1 m³"));
        helper("14.077", F32_MINUS_ONE, new QuantityType<>("-1 m³/s"));
        helper("14.078", F32_MINUS_ONE, new QuantityType<>("-1 N"));
        helper("14.079", F32_MINUS_ONE, new QuantityType<>("-1 J"));
        helper("14.080", F32_MINUS_ONE, new QuantityType<>("-1 VA"));

        helper("14.1200", F32_MINUS_ONE, new QuantityType<>("-1 m³/h"));
        helper("14.1201", F32_MINUS_ONE, new QuantityType<>("-1 l/s"));
    }

    @Test
    void testDpt16() {
        helper("16.000", new byte[] { (byte) 0x4B, (byte) 0x4E, 0x58, 0x20, 0x69, 0x73, 0x20, (byte) 0x4F, (byte) 0x4B,
                0x0, 0x0, 0x0, 0x0, 0x0 }, new StringType("KNX is OK"));
        helper("16.001", new byte[] { (byte) 0x4B, (byte) 0x4E, 0x58, 0x20, 0x69, 0x73, 0x20, (byte) 0x4F, (byte) 0x4B,
                0x0, 0x0, 0x0, 0x0, 0x0 }, new StringType("KNX is OK"));
    }

    @Test
    void testDpt17() {
        helper("17.001", new byte[] { 0 }, new DecimalType(0));
        helper("17.001", new byte[] { 42 }, new DecimalType(42));
        helper("17.001", new byte[] { 63 }, new DecimalType(63));
    }

    @Test
    void testDpt18() {
        // scene, activate 0..63
        helper("18.001", new byte[] { 0 }, new DecimalType(0));
        helper("18.001", new byte[] { 42 }, new DecimalType(42));
        helper("18.001", new byte[] { 63 }, new DecimalType(63));
        // scene, learn += 0x80
        helper("18.001", new byte[] { (byte) (0x80 + 0) }, new DecimalType(0x80));
        helper("18.001", new byte[] { (byte) (0x80 + 42) }, new DecimalType(0x80 + 42));
        helper("18.001", new byte[] { (byte) (0x80 + 63) }, new DecimalType(0x80 + 63));
    }

    @Test
    void testDpt19() {
        // 2019-01-15 17:30:00
        helper("19.001", new byte[] { (byte) (2019 - 1900), 1, 15, 17, 30, 0, (byte) 0x25, (byte) 0x00 },
                new DateTimeType("2019-01-15T17:30:00"));
        helper("19.001", new byte[] { (byte) (2019 - 1900), 1, 15, 17, 30, 0, (byte) 0x24, (byte) 0x00 },
                new DateTimeType("2019-01-15T17:30:00"));
        // 2019-07-15 17:30:00
        helper("19.001", new byte[] { (byte) (2019 - 1900), 7, 15, 17, 30, 0, (byte) 0x25, (byte) 0x00 },
                new DateTimeType("2019-07-15T17:30:00"), new byte[0], new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 });
        helper("19.001", new byte[] { (byte) (2019 - 1900), 7, 15, 17, 30, 0, (byte) 0x24, (byte) 0x00 },
                new DateTimeType("2019-07-15T17:30:00"), new byte[0], new byte[] { 0, 0, 0, 0, 0, 0, 0, 1 });
        // TODO add tests for incompletly filled frames (e.g. containing only date or time)
    }

    @Test
    void testDpt20() {
        // test default String representation of enum (incomplete)
        helper("20.001", new byte[] { 0 }, new StringType("autonomous"));
        helper("20.001", new byte[] { 1 }, new StringType("slave"));
        helper("20.001", new byte[] { 2 }, new StringType("master"));

        helper("20.002", new byte[] { 0 }, new StringType("building in use"));
        helper("20.002", new byte[] { 1 }, new StringType("building not used"));
        helper("20.002", new byte[] { 2 }, new StringType("building protection"));

        // test DecimalType representation of enum
        int[] subTypes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 13, 14, 17, 20, 21, 22, 100, 101, 102, 103, 104,
                105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 120, 121, 122, 600, 601, 602, 603, 604, 605, 606,
                607, 608, 609, 610, 611, 612, 613, 801, 802, 803, 804, 1000, 1001, 1002, 1003, 1004, 1005, 1200, 1202,
                1203, 1204, 1205, 1206, 1207, 1208, 1209 };
        for (int subType : subTypes) {
            helper("20." + String.format("%03d", subType), new byte[] { 1 }, new DecimalType(1));
        }
        // once these DPTs are available in Calimero, add to check above
        int[] unsupportedSubTypes = new int[] {};
        for (int subType : unsupportedSubTypes) {
            assertNull(ValueDecoder.decode("20." + String.format("%03d", subType), new byte[] { 0 }, StringType.class));
        }
    }

    @Test
    void testDpt21() {
        // test default String representation of bitfield (incomplete)
        helper("21.001", new byte[] { 5 }, new StringType("overridden, out of service"));

        // test DecimalType representation of bitfield
        int[] subTypes = new int[] { 1, 2, 100, 101, 102, 103, 104, 105, 106, 601, 1000, 1001, 1002, 1010, 1200, 1201 };
        for (int subType : subTypes) {
            helper("21." + String.format("%03d", subType), new byte[] { 1 }, new DecimalType(1));
        }
    }

    @Test
    void testDpt22() {
        // test default String representation of bitfield (incomplete)
        helper("22.101", new byte[] { 1, 0 }, new StringType("heating mode"));
        helper("22.101", new byte[] { 1, 2 }, new StringType("heating mode, heating eco mode"));

        // test DecimalType representation of bitfield
        helper("22.100", new byte[] { 0, 2 }, new DecimalType(2));
        helper("22.101", new byte[] { 0, 2 }, new DecimalType(2));
        helper("22.1000", new byte[] { 0, 2 }, new DecimalType(2));
        helper("22.1010", new byte[] { 0, 2 }, new DecimalType(2));
    }

    @Test
    void testDpt28() {
        // null terminated strings, UTF8
        helper("28.001", new byte[] { 0x31, 0x32, 0x33, 0x34, 0x0 }, new StringType("1234"));
        helper("28.001", new byte[] { (byte) 0xce, (byte) 0xb5, 0x34, 0x0 }, new StringType("\u03b54"));
    }

    @Test
    void testDpt29() {
        helper("29.010", new byte[] { 0, 0, 0, 0, 0, 0, 0, 42 }, new QuantityType<>("42 Wh"));
        helper("29.010", new byte[] { (byte) 0x80, 0, 0, 0, 0, 0, 0, 0 },
                new QuantityType<>("-9223372036854775808 Wh"));
        helper("29.010", new byte[] { (byte) 0xff, 0, 0, 0, 0, 0, 0, 0 }, new QuantityType<>("-72057594037927936 Wh"));
        helper("29.010", new byte[] { 0, 0, 0, 0, 0, 0, 0, 42 }, new QuantityType<>("42 Wh"));
        helper("29.011", new byte[] { 0, 0, 0, 0, 0, 0, 0, 42 }, new QuantityType<>("42 VAh"));
        helper("29.012", new byte[] { 0, 0, 0, 0, 0, 0, 0, 42 }, new QuantityType<>("42 varh"));
    }

    @Test
    void testDpt229() {
        // special DPT for metering, allows several units and different scaling
        // -> Calimero uses scaling, but always encodes as dimensionless value
        final int dimensionlessCounter = 0b10111010;
        helper("229.001", new byte[] { 0, 0, 0, 0, (byte) dimensionlessCounter, 0 }, new DecimalType(0));
    }

    @Test
    void testColorDpts() {
        // HSB
        helper("232.600", new byte[] { 123, 45, 67 }, ColorUtil.rgbToHsb(new int[] { 123, 45, 67 }));
        // RGB, MDT specific
        helper("232.60000", new byte[] { 123, 45, 67 }, new HSBType("173.6, 17.6, 26.3"));

        // xyY
        int x = (int) (14.65 * 65535.0 / 100.0);
        int y = (int) (11.56 * 65535.0 / 100.0);
        // encoding is always xy and brightness (C+B, 0x03), do not test other combinations
        helper("242.600", new byte[] { (byte) ((x >> 8) & 0xff), (byte) (x & 0xff), (byte) ((y >> 8) & 0xff),
                (byte) (y & 0xff), (byte) 0x28, 0x3 }, new HSBType("220,90,50"), new byte[] { 0, 8, 0, 8, 0, 0 },
                new byte[0]);
        // TODO check brightness

        // RGBW, only RGB part
        helper("251.600", new byte[] { 0x26, 0x2b, 0x31, 0x00, 0x00, 0x0e }, new HSBType("207, 23, 19"),
                new byte[] { 1, 1, 1, 0, 0, 0 }, new byte[0]);
        // RGBW, only RGB part
        helper("251.600", new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x0e },
                new HSBType("0, 0, 100"), new byte[] { 1, 1, 1, 0, 0, 0 }, new byte[0]);
        // RGBW, only W part
        helper("251.600", new byte[] { 0x0, 0x0, 0x0, 0x1A, 0x00, 0x01 }, new PercentType("10.2"));
        // RGBW, all
        helper("251.60600", new byte[] { (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0xff, 0x00, 0x0f },
                new HSBType("0, 0, 100"), new byte[] { 1, 1, 1, 2, 0, 0 }, new byte[0]);
        // RGBW, mixed
        int[] rgbw = new int[] { 240, 0x0, 0x0, 0x0f };
        HSBType hsb = ColorUtil.rgbToHsb(rgbw);
        helper("251.60600", new byte[] { (byte) rgbw[0], (byte) rgbw[1], (byte) rgbw[2], (byte) rgbw[3], 0x00, 0x0f },
                hsb, new byte[] { 2, 2, 2, 2, 0, 0 }, new byte[0]);
    }

    @Test
    void testColorTransitionDpts() {
        // DPT 243.600 DPT_Colour_Transition_xyY
        // time(2) y(2) x(2), %brightness(1), flags(1)
        helper("243.600", new byte[] { 0, 5, 0x7F, 0, (byte) 0xfe, 0, 42, 3 },
                new StringType("(0.9922, 0.4961) 16.5 % 500 ms"));
        helper("243.600", new byte[] { (byte) 0x02, (byte) 0x00, 0x7F, 0, (byte) 0xfe, 0, 42, 3 },
                new StringType("(0.9922, 0.4961) 16.5 % 51200 ms"));
        helper("243.600", new byte[] { (byte) 0x40, (byte) 0x00, 0x7F, 0, (byte) 0xfe, 0, 42, 3 },
                new StringType("(0.9922, 0.4961) 16.5 % 1638400 ms"));
        helper("243.600", new byte[] { (byte) 0xff, (byte) 0xff, 0x7F, 0, (byte) 0xfe, 0, 42, 3 },
                new StringType("(0.9922, 0.4961) 16.5 % 6553500 ms"));
        // DPT 249.600 DPT_Brightness_Colour_Temperature_Transition
        // time(2) colortemp(2), brightness(1), flags(1)
        helper("249.600", new byte[] { 0, 5, 0, 40, 127, 7 }, new StringType("49.8 % 40 K 0.5 s"));
        helper("249.600", new byte[] { (byte) 0xff, (byte) 0xfa, 0, 40, 127, 7 }, new StringType("49.8 % 40 K 6553 s"));
        helper("249.600", new byte[] { (byte) 0xff, (byte) 0xff, 0, 40, 127, 7 },
                new StringType("49.8 % 40 K 6553.5 s"));
        // DPT 250.600 DPT_Brightness_Colour_Temperature_Control
        // cct(1) cb(1) flags(1)
        helper("250.600", new byte[] { 0x0f, 0x0e, 3 }, new StringType("CT increase 7 steps BRT increase 6 steps"));
        // DPT 252.600 DPT_Relative_Control_RGBW
        // r(1) g(1) b(1) w(1) flags(1)
        helper("252.600", new byte[] { 0x0f, 0x0e, 0x0d, 0x0c, 0x0f },
                new StringType("R increase 7 steps G increase 6 steps B increase 5 steps W increase 4 steps"));
        // DPT 253.600 DPT_Relative_Control_xyY
        // cs(1) ct(1) cb(1) flags(1)
        helper("253.600", new byte[] { 0x0f, 0x0e, 0x0d, 0x7 },
                new StringType("x increase 7 steps y increase 6 steps Y increase 5 steps"));
        // DPT 254.600 DPT_Relative_Control_RGB
        // cr(1) cg(1) cb(1)
        helper("254.600", new byte[] { 0x0f, 0x0e, 0x0d },
                new StringType("R increase 7 steps G increase 6 steps B increase 5 steps"));
    }

    @Test
    @AfterAll
    static void checkForMissingMainTypes() {
        // checks if we have itests for all main DPT types supported by Calimero library,
        // data is collected within method helper()
        var wrapper = new Object() {
            boolean testsMissing = false;
        };
        TranslatorTypes.getAllMainTypes().forEach((i, t) -> {
            if (!dptTested.contains(i)) {
                LOGGER.warn("missing tests for main DPT type {}", i);
                wrapper.testsMissing = true;
            }
        });
        assertFalse(wrapper.testsMissing, "add tests for new DPT main types");
    }
}
