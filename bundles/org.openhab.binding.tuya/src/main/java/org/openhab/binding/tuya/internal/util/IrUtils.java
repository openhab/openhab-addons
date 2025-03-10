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
package org.openhab.binding.tuya.internal.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IrUtils} is a support class for decode/encode infra-red codes
 * <p>
 * Based on https://github.com/jasonacox/tinytuya/blob/master/tinytuya/Contrib/IRRemoteControlDevice.py
 *
 * @author Dmitry Pyatykh - Initial contribution
 */
@NonNullByDefault
public class IrUtils {
    private static final Logger logger = LoggerFactory.getLogger(IrUtils.class);

    private IrUtils() {
    }

    /**
     * Convert Base64 code format from Tuya to nec-format.
     *
     * @param base64Code the base64 code format from Tuya
     * @return the nec-format code
     */
    public static String base64ToNec(String base64Code) {
        List<Integer> pulses = base64ToPulse(base64Code);
        if (!pulses.isEmpty()) {
            List<String> res = pulsesToNec(pulses);
            if (!res.isEmpty()) {
                return res.get(0);
            }
        }
        throw new IllegalArgumentException("No pulses found or conversion result is empty.");
    }

    /**
     * Convert Base64 code format from Tuya to samsung-format.
     *
     * @param base64Code the base64 code format from Tuya
     * @return the samsung-format code
     */
    public static String base64ToSamsung(String base64Code) {
        List<Integer> pulses = base64ToPulse(base64Code);
        if (!pulses.isEmpty()) {
            List<String> res = pulsesToSamsung(pulses);
            if (!res.isEmpty()) {
                return res.get(0);
            }
        }
        throw new IllegalArgumentException("No pulses found or conversion result is empty.");
    }

    private static List<Integer> base64ToPulse(String base64Code) {
        List<Integer> pulses = new ArrayList<>();
        String key = (base64Code.length() % 4 == 1 && base64Code.startsWith("1")) ? base64Code.substring(1)
                : base64Code;
        byte[] raw_bytes = Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8));

        int i = 0;
        try {
            while (i < raw_bytes.length) {
                int word = ((raw_bytes[i] & 0xFF) + (raw_bytes[i + 1] & 0xFF) * 256) & 0xFFFF;
                pulses.add(word);
                i += 2;

                // dirty hack because key not aligned by 4 byte ?
                if (i >= raw_bytes.length) {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.warn("Failed to convert base64 key code to pulses: {}", e.getMessage());
        }
        return pulses;
    }

    private static List<Long> pulsesToWidthEncoded(List<Integer> pulses, Integer startMark) {
        List<Long> ret = new ArrayList<>();
        if (pulses.size() < 68) {
            throw new IllegalArgumentException("Not enough pulses");
        }

        while (pulses.size() >= 68 && (pulses.get(0) < (startMark * 0.75) || pulses.get(0) > (startMark * 1.25))) {
            pulses.remove(0);
        }

        while (pulses.size() >= 68) {
            if (pulses.get(0) < startMark * 0.75 || pulses.get(0) > startMark * 1.25) {
                throw new IllegalArgumentException(
                        "Pulse length is less than 3/4 startMark or more than 5/4 startMark");
            }

            // remove two first elements
            pulses.remove(0);
            pulses.remove(0);

            int res = 0;
            long x = 0L;

            for (int i = 31; i >= 0; i--) {
                res = pulses.get(1) >= (Integer) 1125 ? 1 : 0;

                x |= (long) (res) << i;

                // remove two first elements
                pulses.remove(0);
                pulses.remove(0);
            }

            if (!ret.contains(x)) {
                ret.add(x);
            }
        }

        return ret;
    }

    private static List<Long> widthEncodedToPulses(long data, PulseParams param) {
        List<Long> pulses = new ArrayList<>();
        pulses.add(param.startMark);
        pulses.add(param.startSpace);

        for (int i = 31; i >= 0; i--) {
            if ((data & (1L << i)) > 0L) {
                pulses.add(param.pulseOne);
                pulses.add(param.spaceOne);
            } else {
                pulses.add(param.pulseZero);
                pulses.add(param.spaceZero);
            }
        }
        pulses.add(param.trailingPulse);
        pulses.add(param.trailingSpace);
        return pulses;
    }

    private static long mirrorBits(long data) {
        int shift = 8 - 1;
        long out = 0;

        for (int i = 0; i < 8; i++) {
            if ((data & (1L << i)) > 0L) {
                out |= 1L << shift;
            }
            shift -= 1;
        }
        return out & 0xFF;
    }

    private static List<String> pulsesToNec(List<Integer> pulses) {
        List<String> ret = new ArrayList<>();
        List<Long> res = pulsesToWidthEncoded(pulses, 9000);
        if (res.isEmpty()) {
            throw new IllegalArgumentException("[tuya:ir-controller] No ir key-code detected");
        }
        for (Long code : res) {
            long addr = mirrorBits((code >> 24) & 0xFF);
            long addrNot = mirrorBits((code >> 16) & 0xFF);
            long data = mirrorBits((code >> 8) & 0xFF);
            long dataNot = mirrorBits(code & 0xFF);

            if (addr != (addrNot ^ 0xFF)) {
                addr = (addr << 8) | addrNot;
            }
            String d = String.format(
                    "{ \"type\": \"nec\", \"uint32\": %d, \"address\": None, \"data\": None, \"hex\": \"%08X\" }", code,
                    code);
            if (data == (dataNot ^ 0xFF)) {
                d = String.format(
                        "{ \"type\": \"nec\", \"uint32\": %d, \"address\": %d, \"data\": %d, \"hex\": \"%08X\" }", code,
                        addr, data, code);
            }
            ret.add(d);
        }
        return ret;
    }

    private static List<Long> necToPulses(long address) {
        return widthEncodedToPulses(address, new PulseParams());
    }

    private static String pulsesToBase64(List<Long> pulses) {
        byte[] bytes = new byte[pulses.size() * 2];

        final Integer[] i = { 0 };

        pulses.forEach(p -> {
            int val = p.shortValue();
            bytes[i[0]] = (byte) (val & 0xFF);
            bytes[i[0] + 1] = (byte) ((val >> 8) & 0xFF);
            i[0] = i[0] + 2;
        });

        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * Convert Nec-format code to base64-format code from Tuya
     *
     * @param code nec-format code
     * @return the string
     */
    public static String necToBase64(long code) {
        List<Long> pulses = necToPulses(code);
        return pulsesToBase64(pulses);
    }

    /**
     * Convert Samsung-format code to base64-format code from Tuya
     *
     * @param code samsung-format code
     * @return the string
     */
    public static String samsungToBase64(long code) {
        List<Long> pulses = samsungToPulses(code);
        return pulsesToBase64(pulses);
    }

    private static List<Long> samsungToPulses(long address) {
        return widthEncodedToPulses(address, new PulseParams());
    }

    private static List<String> pulsesToSamsung(List<Integer> pulses) {
        List<String> ret = new ArrayList<>();
        List<Long> res = pulsesToWidthEncoded(pulses, 4500);
        for (Long code : res) {
            long addr = (code >> 24) & 0xFF;
            long addrNot = (code >> 16) & 0xFF;
            long data = (code >> 8) & 0xFF;
            long dataNot = code & 0xFF;

            String d = String.format(
                    "{ \"type\": \"samsung\", \"uint32\": %d, \"address\": None, \"data\": None, \"hex\": \"%08X\" }",
                    code, code);
            if (addr == addrNot && data == (dataNot ^ 0xFF)) {
                addr = mirrorBits(addr);
                data = mirrorBits(data);
                d = String.format(
                        "{ \"type\": \"samsung\", \"uint32\": %d, \"address\": %d, \"data\": %d, \"hex\": \"%08X\" }",
                        code, addr, data, code);
            }
            ret.add(d);
        }
        return ret;
    }

    private static class PulseParams {
        /**
         * The Start mark.
         */
        public long startMark = 9000;
        /**
         * The Start space.
         */
        public long startSpace = 4500;
        /**
         * The Pulse one.
         */
        public long pulseOne = 563;
        /**
         * The Pulse zero.
         */
        public long pulseZero = 563;
        /**
         * The Space one.
         */
        public long spaceOne = 1688;
        /**
         * The Space zero.
         */
        public long spaceZero = 563;
        /**
         * The Trailing pulse.
         */
        public long trailingPulse = 563;
        /**
         * The Trailing space.
         */
        public long trailingSpace = 30000;
    }
}
