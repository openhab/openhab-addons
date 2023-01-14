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
package org.openhab.binding.souliss.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper class to conver half precision float to int int are used on analogue
 * typicals (2 bytes) and should be reversed because of endianess
 * http://stackoverflow.com/users/237321/x4u
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public final class HalfFloatUtils {

    public static boolean isNaN(float x) {
        return x != x;
    }

    // ignores the higher 16 bits
    public static float toFloat(int hbits) {
        // 10 bits mantissa
        int mant = hbits & 0x03ff;
        // 5 bits exponent
        int exp = hbits & 0x7c00;
        if (exp == 0x7c00) {
            // -> NaN/Inf
            exp = 0x3fc00;
            // normalized value
        } else if (exp != 0) {
            // exp - 15 + 127
            exp += 0x1c000;
            if (mant == 0 && exp > 0x1c400) {
                return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
            }
            // && exp==0 -> subnormal
        } else if (mant != 0) {
            // make it normal
            exp = 0x1c400;
            do {
                // mantissa * 2
                mant <<= 1;
                // decrease exp by 1
                exp -= 0x400;
                // while not normal
            } while ((mant & 0x400) == 0);
            // discard subnormal bit
            mant &= 0x3ff;
            // else +/-0 -> +/-0
        }
        // combine all parts
        return Float.intBitsToFloat(
                // sign << ( 31 - 15 )
                (hbits & 0x8000) << 16
                        // value << ( 23 - 10 )
                        | (exp | mant) << 13);
    }

    // returns all higher 16 bits as 0 for all results
    public static int fromFloat(float fval) {
        var fbits = Float.floatToIntBits(fval);
        // sign only
        int sign = fbits >>> 16 & 0x8000;
        // rounded value
        int val = (fbits & 0x7fffffff) + 0x1000;

        // might be or become NaN/Inf
        if (val >= 0x47800000)
        // avoid Inf due to rounding
        {
            // is or must become
            // NaN/Inf
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000) {
                    // make it +/-Inf
                    return sign | 0x7c00;
                }
                // remains +/-Inf or NaN
                return sign | 0x7c00 |
                // keep NaN (and Inf) bits
                        (fbits & 0x007fffff) >>> 13;
            }
            // unrounded not quite Inf
            return sign | 0x7bff;
        }
        if (val >= 0x38800000) {
            // exp - 127 + 15
            return sign | val - 0x38000000 >>> 13;
        }
        if (val < 0x33000000) {
            // becomes +/-0
            return sign;
        }
        // tmp exp for subnormal calc
        val = (fbits & 0x7fffffff) >>> 23;
        // add subnormal bit
        return sign | ((fbits & 0x7fffff | 0x800000)
                // round depending on cut off
                + (0x800000 >>> val - 102)
                // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
                >>> 126 - val);
    }
}
