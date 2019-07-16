/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link HeliosVentilationVariable} is a description of a variable in the Helios ventilation system.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationVariable {
    public enum type {
        Temperature,
        Fanspeed,
        Bit,
        BytePercent,
        Percent,
        Number
    }

    /**
     * mapping from temperature byte values to °C
     */
    private static final int tempMap[] = { -74, -70, -66, -62, -59, -56, -54, -52, -50, -48, -47, -46, -44, -43, -42,
            -41, -40, -39, -38, -37, -36, -35, -34, -33, -33, -32, -31, -30, -30, -29, -28, -28, -27, -27, -26, -25,
            -25, -24, -24, -23, -23, -22, -22, -21, -21, -20, -20, -19, -19, -19, -18, -18, -17, -17, -16, -16, -16,
            -15, -15, -14, -14, -14, -13, -13, -12, -12, -12, -11, -11, -11, -10, -10, -9, -9, -9, -8, -8, -8, -7, -7,
            -7, -6, -6, -6, -5, -5, -5, -4, -4, -4, -3, -3, -3, -2, -2, -2, -1, -1, -1, -1, 0, 0, 0, 1, 1, 1, 2, 2, 2,
            3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13,
            13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 21, 21, 21, 22, 22, 22,
            23, 23, 24, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34, 35,
            35, 36, 36, 37, 37, 38, 38, 39, 40, 40, 41, 41, 42, 43, 43, 44, 45, 45, 46, 47, 48, 48, 49, 50, 51, 52, 53,
            53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 65, 66, 68, 69, 71, 73, 75, 77, 79, 81, 82, 86, 90, 93, 97, 100,
            100, 100, 100, 100, 100, 100, 100, 100 };

    /**
     * mapping from human readable fanspeed to raw value
     */
    private static final int fanspeedMap[] = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };

    private ChannelUID channelUID;

    public HeliosVentilationVariable(Thing thing, String name, byte address, boolean writable, type datatype) {
        this.datatype = datatype;
        this.writable = writable;
        this.name = name;
        this.address = address;
        channelUID = new ChannelUID(thing.getUID(), name);
    }

    private String name;
    private boolean writable;
    private type datatype;
    private byte address;

    /**
     * interpret the given byte b and return the value as string.
     *
     * @param b
     * @return sting representation of byte value b in current datatype
     */
    public String asString(byte b) {
        int val = b & 0xff;

        if (datatype == type.Temperature) {
            return String.format("%d °C", tempMap[val]);
        } else if (datatype == type.Fanspeed) {
            int i = 1;
            while (i < fanspeedMap.length && fanspeedMap[i] < val) {
                i++;
            }
            return String.format("%d", i);
        } else if (datatype == type.BytePercent) {
            return String.format("%d %%", val * 100 / 255);
        } else if (datatype == type.Percent) {
            return String.format("%d %%", val);
        }

        return "<unknown type>" + String.format("%02X ", b);
    }

    public byte getTransmitDataFor(DecimalType value) {
        byte result = 0;
        if (datatype == type.Temperature) {
            int temp = (int) Math.round(value.doubleValue());
            int i = 0;
            while (i < tempMap.length && tempMap[i] < temp) {
                i++;
            }
            result = (byte) i;
        } else if (datatype == type.Fanspeed) {
            int i = value.intValue();
            if (i < 0) {
                i = 0;
            } else if (i > 8) {
                i = 8;
            }
            result = (byte) fanspeedMap[i];
        } else if (datatype == type.BytePercent) {
            result = (byte) (Math.round(value.doubleValue() * 255.0 / 100));
        } else if (datatype == type.Percent) {
            double d = (Math.round(value.doubleValue() * 100.0));
            if (d < 0.0) {
                d = 0.0;
            } else if (d > 100.0) {
                d = 100.0;
            }
            result = (byte) d;
        }

        return result;
    }

    /* @return the channelUID for this variable */
    public ChannelUID channelUID() {
        return channelUID;
    }

    public boolean isWritable() {
        return writable;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @return the name of the variable, which is also the channel name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return address of the variable
     */
    public byte address() {
        return address;
    }

    public DecimalType asDecimal(byte val) {
        if (datatype == type.Temperature) {
            return new DecimalType(tempMap[val & 0xff]);
        } else if (datatype == type.BytePercent) {
            return new DecimalType(val * 100.0 / 255.0);
        } else if (datatype == type.Percent) {
            return new DecimalType(val * 100.0);
        } else if (datatype == type.Fanspeed) {
            int i = 1;
            while (i < fanspeedMap.length && fanspeedMap[i] < val) {
                i++;
            }
            return new DecimalType(i);
        }

        return new DecimalType(999.9);
    }

}
