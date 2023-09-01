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
package org.openhab.binding.velbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.util.ColorUtil;

/**
 * The {@link VelbusDALIConverter} represents a class with properties that manage DALI values.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusDALIConverter {
    /*
     * private final Double[] daliLevel = { 0.0, 0.1, 0.103, 0.106, 0.109, 0.112, 0.115, 0.118, 0.121, 0.124, 0.128,
     * 0.131,
     * 0.135, 0.139, 0.143, 0.147, 0.151, 0.155, 0.159, 0.163, 0.168, 0.173, 0.177, 0.182, 0.187, 0.193, 0.198,
     * 0.203, 0.209, 0.215, 0.221, 0.227, 0.233, 0.240, 0.246, 0.253, 0.260, 0.267, 0.275, 0.282, 0.290, 0.298,
     * 0.306, 0.315, 0.324, 0.332, 0.342, 0.351, 0.361, 0.371, 0.381, 0.392, 0.402, 0.414, 0.425, 0.437, 0.449,
     * 0.461, 0.474, 0.487, 0.501, 0.515, 0.529, 0.543, 0.559, 0.574, 0.590, 0.606, 0.623, 0.640, 0.658, 0.676,
     * 0.695, 0.714, 0.734, 0.754, 0.775, 0.796, 0.819, 0.841, 0.864, 0.888, 0.913, 0.938, 0.964, 0.991, 1.018,
     * 1.047, 1.076, 1.105, 1.136, 1.167, 1.200, 1.233, 1.267, 1.302, 1.338, 1.375, 1.413, 1.452, 1.492, 1.534,
     * 1.576, 1.620, 1.665, 1.711, 1.758, 1.807, 1.857, 1.908, 1.961, 2.02, 2.07, 2.13, 2.19, 2.25, 2.31, 2.37,
     * 2.44, 2.51, 2.58, 2.65, 2.72, 2.80, 3.87, 3.95, 3.04, 3.12, 3.21, 3.29, 3.39, 3.48, 3.58, 3.67, 3.78, 3.88,
     * 3.99, 4.10, 4.21, 4.33, 4.45, 4.57, 4.70, 4.83, 4.96, 5.10, 5.24, 5.39, 5.53, 5.69, 5.85, 6.01, 6.17, 6.34,
     * 6.52, 6.70, 6.89, 7.08, 7.27, 7.47, 7.68, 7.89, 8.11, 8.34, 8.57, 8.80, 9.05, 9.30, 9.56, 9.82, 10.09,
     * 10.37, 10.66, 10.95, 11.26, 11.57, 11.89, 12.22, 12.55, 12.90, 13.26, 13.63, 14.00, 14.39, 14.79, 15.20,
     * 15.62, 16.05, 16.50, 16.95, 17.42, 17.90, 18.40, 18.91, 19.43, 19.97, 20.52, 21.09, 21.68, 22.28, 22.89,
     * 23.53, 24.18, 24.85, 25.53, 26.24, 26.97, 27.71, 28.48, 29.27, 30.08, 30.91, 31.77, 32.65, 33.55, 34.48,
     * 35.43, 36.41, 37.42, 38.46, 39.52, 40.62, 41.74, 42.90, 44.08, 45.30, 46.56, 47.85, 49.17, 50.53, 51.93,
     * 53.37, 54.84, 56.36, 57.92, 59.53, 61.17, 62.87, 64.61, 66.39, 68.23, 70.12, 72.06, 74.06, 76.11, 78.21,
     * 80.38, 82.60, 84.89, 87.24, 89.65, 92.14, 94.69, 97.31, 100.0 };
     */

    private final int[] percentLevel = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 4, 4,
            4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 9, 10,
            10, 10, 10, 11, 11, 11, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 16, 16, 17, 17, 17, 18, 18, 19, 19, 20,
            21, 21, 22, 22, 23, 24, 24, 25, 26, 26, 27, 28, 28, 29, 30, 31, 32, 33, 34, 34, 35, 36, 37, 38, 40, 41, 42,
            43, 44, 45, 47, 48, 49, 51, 52, 53, 55, 56, 58, 60, 61, 63, 65, 66, 68, 70, 72, 74, 76, 78, 80, 83, 85, 87,
            90, 92, 95, 97, 100 };

    private final byte MIN_VALUE = (byte) 0x00;
    private final byte MAX_VALUE = (byte) 0xFE;

    public byte getBrightness(PercentType percentState) {
        int brightness = percentState.intValue();

        for (int i = 0; i < percentLevel.length; i++) {
            if (percentLevel[i] >= brightness) {
                return Integer.valueOf(i).byteValue();
            }
        }

        return MAX_VALUE;
    }

    public byte getBrightness(OnOffType onOffState) {
        return (onOffState == OnOffType.OFF) ? MIN_VALUE : MAX_VALUE;
    }

    public byte getBrightness(HSBType hsbState) {
        return getBrightness(hsbState.getBrightness());
    }

    public PercentType getBrightness(byte brightness) {
        return new PercentType(percentLevel[Byte.toUnsignedInt(brightness)]);
    }

    public byte getTemperature(PercentType percentState) {
        return Integer.valueOf(percentState.intValue() * 254 / 100).byteValue();
    }

    public byte getTemperature(OnOffType onOffState) {
        return (onOffState == OnOffType.OFF) ? MIN_VALUE : MAX_VALUE;
    }

    public PercentType getTemperature(byte white) {
        int value = Byte.toUnsignedInt(white) * 100 / 254;
        return new PercentType(value);
    }

    public byte[] getRgb(HSBType hsbState) {
        int[] rgb = ColorUtil.hsbToRgb(hsbState);

        rgb[0] = (rgb[0] >= 255) ? 254 : rgb[0];
        rgb[1] = (rgb[1] >= 255) ? 254 : rgb[1];
        rgb[2] = (rgb[2] >= 255) ? 254 : rgb[2];

        return new byte[] { Integer.valueOf(rgb[0]).byteValue(), Integer.valueOf(rgb[1]).byteValue(),
                Integer.valueOf(rgb[2]).byteValue() };
    }

    public HSBType getHsb(byte[] rgb) {
        int[] rgbValue = { Byte.toUnsignedInt(rgb[0]), Byte.toUnsignedInt(rgb[1]), Byte.toUnsignedInt(rgb[2]) };

        return ColorUtil.rgbToHsb(rgbValue);
    }
}
