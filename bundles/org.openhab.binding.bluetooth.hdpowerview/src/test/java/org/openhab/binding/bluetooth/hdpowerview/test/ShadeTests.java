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

package org.openhab.binding.bluetooth.hdpowerview.test;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.hdpowerview.internal.shade.ShadeDataWriter;
import org.openhab.core.util.HexUtils;

/**
 * Test of shade position calculations etc.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShadeTests {

    /**
     * Map of position command values as sniffed during testing with the HD Powerview App. The map keys are the target
     * position values (range 0..100%) set manually via the App, and the map values are the results sniffed as output
     * from the App.
     */
    private static final Map<Double, byte[]> HD_POWERVIEW_APP_OBSERVED_RESULTS = new TreeMap<>();
    static {
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.00, HexFormat.ofDelimiter(":").parseHex("5c:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.01, HexFormat.ofDelimiter(":").parseHex("5d:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.02, HexFormat.ofDelimiter(":").parseHex("5e:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.03, HexFormat.ofDelimiter(":").parseHex("5f:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.04, HexFormat.ofDelimiter(":").parseHex("58:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.05, HexFormat.ofDelimiter(":").parseHex("59:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.06, HexFormat.ofDelimiter(":").parseHex("5a:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.07, HexFormat.ofDelimiter(":").parseHex("5b:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.08, HexFormat.ofDelimiter(":").parseHex("54:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.09, HexFormat.ofDelimiter(":").parseHex("55:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.10, HexFormat.ofDelimiter(":").parseHex("56:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.11, HexFormat.ofDelimiter(":").parseHex("57:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.12, HexFormat.ofDelimiter(":").parseHex("50:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.13, HexFormat.ofDelimiter(":").parseHex("51:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.14, HexFormat.ofDelimiter(":").parseHex("52:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.15, HexFormat.ofDelimiter(":").parseHex("53:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.16, HexFormat.ofDelimiter(":").parseHex("4c:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.17, HexFormat.ofDelimiter(":").parseHex("4d:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.18, HexFormat.ofDelimiter(":").parseHex("4e:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.19, HexFormat.ofDelimiter(":").parseHex("4f:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.20, HexFormat.ofDelimiter(":").parseHex("48:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.30, HexFormat.ofDelimiter(":").parseHex("42:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.40, HexFormat.ofDelimiter(":").parseHex("74:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.50, HexFormat.ofDelimiter(":").parseHex("6e:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.60, HexFormat.ofDelimiter(":").parseHex("60:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.70, HexFormat.ofDelimiter(":").parseHex("1a:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.80, HexFormat.ofDelimiter(":").parseHex("0c:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(0.90, HexFormat.ofDelimiter(":").parseHex("06:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.00, HexFormat.ofDelimiter(":").parseHex("38:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.10, HexFormat.ofDelimiter(":").parseHex("32:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.20, HexFormat.ofDelimiter(":").parseHex("24:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.30, HexFormat.ofDelimiter(":").parseHex("de:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.40, HexFormat.ofDelimiter(":").parseHex("d0:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.50, HexFormat.ofDelimiter(":").parseHex("ca:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.60, HexFormat.ofDelimiter(":").parseHex("fc:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.70, HexFormat.ofDelimiter(":").parseHex("f6:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.80, HexFormat.ofDelimiter(":").parseHex("e8:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(1.90, HexFormat.ofDelimiter(":").parseHex("e2:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(2.00, HexFormat.ofDelimiter(":").parseHex("94:87"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(3.00, HexFormat.ofDelimiter(":").parseHex("70:86"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(4.00, HexFormat.ofDelimiter(":").parseHex("cc:86"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(5.00, HexFormat.ofDelimiter(":").parseHex("a8:86"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(6.00, HexFormat.ofDelimiter(":").parseHex("04:85"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(7.00, HexFormat.ofDelimiter(":").parseHex("e0:85"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(8.00, HexFormat.ofDelimiter(":").parseHex("7c:84"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(9.00, HexFormat.ofDelimiter(":").parseHex("d8:84"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(10.00, HexFormat.ofDelimiter(":").parseHex("b4:84"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(11.00, HexFormat.ofDelimiter(":").parseHex("10:83"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(12.00, HexFormat.ofDelimiter(":").parseHex("ec:83"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(13.00, HexFormat.ofDelimiter(":").parseHex("48:82"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(14.00, HexFormat.ofDelimiter(":").parseHex("24:82"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(15.00, HexFormat.ofDelimiter(":").parseHex("80:82"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(16.00, HexFormat.ofDelimiter(":").parseHex("1c:81"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(17.00, HexFormat.ofDelimiter(":").parseHex("f8:81"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(18.00, HexFormat.ofDelimiter(":").parseHex("54:80"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(19.00, HexFormat.ofDelimiter(":").parseHex("30:80"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.00, HexFormat.ofDelimiter(":").parseHex("8c:80"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.46, HexFormat.ofDelimiter(":").parseHex("a2:80"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.47, HexFormat.ofDelimiter(":").parseHex("a3:80"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.48, HexFormat.ofDelimiter(":").parseHex("5c:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.49, HexFormat.ofDelimiter(":").parseHex("5c:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(20.50, HexFormat.ofDelimiter(":").parseHex("5e:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(21.00, HexFormat.ofDelimiter(":").parseHex("68:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(22.00, HexFormat.ofDelimiter(":").parseHex("c4:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(23.00, HexFormat.ofDelimiter(":").parseHex("a0:8f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(24.00, HexFormat.ofDelimiter(":").parseHex("3c:8e"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(25.00, HexFormat.ofDelimiter(":").parseHex("98:8e"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(26.00, HexFormat.ofDelimiter(":").parseHex("74:8d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(27.00, HexFormat.ofDelimiter(":").parseHex("d0:8d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(28.00, HexFormat.ofDelimiter(":").parseHex("ac:8d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(29.00, HexFormat.ofDelimiter(":").parseHex("08:8c"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(30.00, HexFormat.ofDelimiter(":").parseHex("e4:8c"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(31.00, HexFormat.ofDelimiter(":").parseHex("40:8b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(32.00, HexFormat.ofDelimiter(":").parseHex("dc:8b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(33.00, HexFormat.ofDelimiter(":").parseHex("b8:8b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(34.00, HexFormat.ofDelimiter(":").parseHex("14:8a"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(35.00, HexFormat.ofDelimiter(":").parseHex("f0:8a"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(36.00, HexFormat.ofDelimiter(":").parseHex("4c:89"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(37.00, HexFormat.ofDelimiter(":").parseHex("28:89"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(38.00, HexFormat.ofDelimiter(":").parseHex("84:89"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(39.00, HexFormat.ofDelimiter(":").parseHex("60:88"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.00, HexFormat.ofDelimiter(":").parseHex("fc:88"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.94, HexFormat.ofDelimiter(":").parseHex("a2:88"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.95, HexFormat.ofDelimiter(":").parseHex("a3:88"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.96, HexFormat.ofDelimiter(":").parseHex("5c:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.97, HexFormat.ofDelimiter(":").parseHex("5d:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(40.98, HexFormat.ofDelimiter(":").parseHex("5e:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(41.00, HexFormat.ofDelimiter(":").parseHex("58:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(42.00, HexFormat.ofDelimiter(":").parseHex("34:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(43.00, HexFormat.ofDelimiter(":").parseHex("90:97"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(44.00, HexFormat.ofDelimiter(":").parseHex("6c:96"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(45.00, HexFormat.ofDelimiter(":").parseHex("c8:96"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(46.00, HexFormat.ofDelimiter(":").parseHex("a4:96"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(47.00, HexFormat.ofDelimiter(":").parseHex("00:95"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(48.00, HexFormat.ofDelimiter(":").parseHex("9c:95"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(49.00, HexFormat.ofDelimiter(":").parseHex("78:94"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(50.00, HexFormat.ofDelimiter(":").parseHex("d4:94"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(51.00, HexFormat.ofDelimiter(":").parseHex("b0:94"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(52.00, HexFormat.ofDelimiter(":").parseHex("0c:93"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(53.00, HexFormat.ofDelimiter(":").parseHex("e8:93"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(54.00, HexFormat.ofDelimiter(":").parseHex("44:92"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(55.00, HexFormat.ofDelimiter(":").parseHex("20:92"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(56.00, HexFormat.ofDelimiter(":").parseHex("bc:92"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(57.00, HexFormat.ofDelimiter(":").parseHex("18:91"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(58.00, HexFormat.ofDelimiter(":").parseHex("f4:91"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(59.00, HexFormat.ofDelimiter(":").parseHex("50:90"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(60.00, HexFormat.ofDelimiter(":").parseHex("2c:90"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.00, HexFormat.ofDelimiter(":").parseHex("88:90"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.42, HexFormat.ofDelimiter(":").parseHex("a2:90"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.43, HexFormat.ofDelimiter(":").parseHex("a3:90"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.44, HexFormat.ofDelimiter(":").parseHex("5c:9f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.45, HexFormat.ofDelimiter(":").parseHex("5d:9f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(61.46, HexFormat.ofDelimiter(":").parseHex("5e:9f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(62.00, HexFormat.ofDelimiter(":").parseHex("64:9f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(63.00, HexFormat.ofDelimiter(":").parseHex("c0:9f"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(64.00, HexFormat.ofDelimiter(":").parseHex("5c:9e"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(65.00, HexFormat.ofDelimiter(":").parseHex("38:9e"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(66.00, HexFormat.ofDelimiter(":").parseHex("94:9e"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(67.00, HexFormat.ofDelimiter(":").parseHex("70:9d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(68.00, HexFormat.ofDelimiter(":").parseHex("cc:9d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(69.00, HexFormat.ofDelimiter(":").parseHex("a8:9d"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(70.00, HexFormat.ofDelimiter(":").parseHex("04:9c"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(71.00, HexFormat.ofDelimiter(":").parseHex("e0:9c"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(72.00, HexFormat.ofDelimiter(":").parseHex("7c:9b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(73.00, HexFormat.ofDelimiter(":").parseHex("d8:9b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(74.00, HexFormat.ofDelimiter(":").parseHex("b4:9b"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(75.00, HexFormat.ofDelimiter(":").parseHex("10:9a"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(76.00, HexFormat.ofDelimiter(":").parseHex("ec:9a"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(77.00, HexFormat.ofDelimiter(":").parseHex("48:99"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(78.00, HexFormat.ofDelimiter(":").parseHex("24:99"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(79.00, HexFormat.ofDelimiter(":").parseHex("80:99"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(80.00, HexFormat.ofDelimiter(":").parseHex("1c:98"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.00, HexFormat.ofDelimiter(":").parseHex("f8:98"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.90, HexFormat.ofDelimiter(":").parseHex("a2:98"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.91, HexFormat.ofDelimiter(":").parseHex("a3:98"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.92, HexFormat.ofDelimiter(":").parseHex("5c:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.93, HexFormat.ofDelimiter(":").parseHex("5d:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(81.94, HexFormat.ofDelimiter(":").parseHex("5e:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(82.00, HexFormat.ofDelimiter(":").parseHex("54:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(83.00, HexFormat.ofDelimiter(":").parseHex("30:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(84.00, HexFormat.ofDelimiter(":").parseHex("8c:a7"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(85.00, HexFormat.ofDelimiter(":").parseHex("68:a6"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(86.00, HexFormat.ofDelimiter(":").parseHex("c4:a6"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(87.00, HexFormat.ofDelimiter(":").parseHex("a0:a6"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(88.00, HexFormat.ofDelimiter(":").parseHex("3c:a5"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(89.00, HexFormat.ofDelimiter(":").parseHex("98:a5"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(90.00, HexFormat.ofDelimiter(":").parseHex("74:a4"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(91.00, HexFormat.ofDelimiter(":").parseHex("d0:a4"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(92.00, HexFormat.ofDelimiter(":").parseHex("ac:a4"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(93.00, HexFormat.ofDelimiter(":").parseHex("08:a3"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(94.00, HexFormat.ofDelimiter(":").parseHex("e4:a3"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(95.00, HexFormat.ofDelimiter(":").parseHex("40:a2"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(96.00, HexFormat.ofDelimiter(":").parseHex("dc:a2"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(97.00, HexFormat.ofDelimiter(":").parseHex("b8:a2"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(98.00, HexFormat.ofDelimiter(":").parseHex("14:a1"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(99.00, HexFormat.ofDelimiter(":").parseHex("f0:a1"));
        HD_POWERVIEW_APP_OBSERVED_RESULTS.put(100.00, HexFormat.ofDelimiter(":").parseHex("4c:a0"));
    }

    private static final String TEST_KEY = "02c2efcbd4064d59409c980e627e2fc7"; // (or 9440bf8b334c2b6c8564d80548b67c00)

    /**
     * Compare the results of the binding {@code ShadeDataWriter} conversions against the results of the HD Powerview
     * App conversions, as sniffed over the air using a Bluetooth sniffer.
     */
    @Test
    void testCalculatedEqualsObserved() {
        for (Entry<Double, byte[]> observedResult : HD_POWERVIEW_APP_OBSERVED_RESULTS.entrySet()) {
            try {
                byte[] calculated = new ShadeDataWriter().withPrimary(observedResult.getKey()).getEncrypted(TEST_KEY);
                byte[] observed = observedResult.getValue();
                assertEquals(observed[0], calculated[4], 1); // allow error of 1 in LSB for rounding
                assertEquals(observed[1], calculated[5]);

            } catch (InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                fail(e);
            }
        }
    }

    /**
     * Test that {@code ShadeDataWriter} produces correct values.
     */
    @Test
    void testShadeDataWriter() {
        try {
            String actual;
            String expected;

            // test basic output
            actual = HexUtils.bytesToHex(new ShadeDataWriter().getEncrypted(TEST_KEY));
            expected = "1F70847E5C07AD03100E0FB3DA";
            assertTrue(expected.equals(actual));

            // test sequence number only
            actual = HexUtils.bytesToHex(new ShadeDataWriter().withSequence((byte) 1).getEncrypted(TEST_KEY));
            expected = "1F70857E5C07AD03100E0FB3DA";
            assertTrue(expected.equals(actual));

            // test primary position only
            actual = HexUtils.bytesToHex(new ShadeDataWriter().withPrimary(100).getEncrypted(TEST_KEY));
            expected = "1F70847E4CA0AD03100E0FB3DA";
            assertTrue(expected.equals(actual));

            // test tilt position only
            actual = HexUtils.bytesToHex(new ShadeDataWriter().withTilt(40).getEncrypted(TEST_KEY));
            expected = "1F70847E5C07AD03100E2733DA";
            assertTrue(expected.equals(actual));

            // test sequence number, plus primary position, plus secondary position
            expected = "1F70227EE48C4580100E0FB3DA";
            actual = HexUtils.bytesToHex(new ShadeDataWriter().withSequence((byte) 0xa6).withPrimary(30)
                    .withSecondary(10).getEncrypted(TEST_KEY));
            assertTrue(expected.equals(actual));

        } catch (InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            fail(e);
        }
    }
}
