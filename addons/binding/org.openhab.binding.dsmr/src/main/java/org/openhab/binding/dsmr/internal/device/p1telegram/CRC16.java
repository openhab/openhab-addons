/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.p1telegram;

/**
 * CRC16 implementation.
 *
 * This class supports 2 ways of using.
 * The first is to give a complete byte array and get the CRC16 {@link #calculate(byte[], int)}
 * The second variant the instance stores the actual CRC16 value giving the possibility to add bytes in subsequent
 * calls to {@link #processByte(byte)}
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class CRC16 {
    public enum Polynom {
        CRC16_IBM(0xA001), // standard CRC-16 x16+x15+x2+1 (CRC-16-IBM)
        CRC16_IBM_REVERSE(0xC002), // standard reverse x16+x14+x+1 (CRC-16-IBM)
        CRC16_CCIT(0x8408), // CCITT/SDLC/HDLC X16+X12+X5+1 (CRC-16-CCITT)
        // The initial CRC value is usually 0xFFFF and the result is complemented.
        CRC16_CCIT_REVERSE(0x8810), // CCITT reverse X16+X11+X4+1 (CRC-16-CCITT)
        CRC16_LRCC(0x8000); // LRCC-16 X16+1

        // the polynom to use
        public final int polynom;

        /**
         * Constructs a new Polynom using the required polynom value
         *
         * @param polynom
         */
        private Polynom(int polynom) {
            this.polynom = polynom;
        }
    }

    // The cached CRC16 table based on the requested CRC16 variant
    private short[] crcTable;

    // The current crcValue
    private int crcValue;

    /**
     * Constructs a new CRC16 object using the requested polynom
     *
     * @param polynom the CRC16 polynom to use
     */
    public CRC16(Polynom polynom) {
        crcTable = genCrc16Table(polynom);
    }

    /**
     * Calculate a CRC16 based on the specified data and the initial CRCvalue
     *
     * @param data byes to calculate the CRC16 for
     * @param initialCrcValue initial CRC value
     * @return the CRC16 value
     */
    public int calculate(byte[] data, int initialCrcValue) {
        int crc = initialCrcValue;
        for (int p = 0; p < data.length; p++) {
            crc = (crc >> 8) ^ (crcTable[(crc & 0xFF) ^ (data[p] & 0xFF)] & 0xFFFF);
        }
        return crc;
    }

    /**
     * Initializes the CRC16 code to 0.
     */
    public void initialize() {
        initialize(0);
    }

    /**
     * Initializes the CRC16 code to the given initial value.
     *
     * @param initialCrcValue the initial value to set.
     */
    public void initialize(int initialCrcValue) {
        crcValue = initialCrcValue;
    }

    /**
     * Processed a single byte and updates the internal CRC16 value
     *
     * @param b the byte to process
     */
    public void processByte(byte b) {
        crcValue = (crcValue >> 8) ^ (crcTable[(crcValue & 0xFF) ^ (b & 0xFF)] & 0xFFFF);
    }

    /**
     * Returns the current CRC16 code
     *
     * @return integer containing the current CRC16 code.
     */
    public int getCurrentCRCCode() {
        return crcValue;
    }

    /**
     * Generates the CRC16 table
     *
     * @param polynom the polynonm to use
     *
     * @return the generated CRC16 table
     */
    private short[] genCrc16Table(Polynom polynom) {
        short[] table = new short[256];
        for (int x = 0; x < 256; x++) {
            int w = x;
            for (int i = 0; i < 8; i++) {
                if ((w & 1) != 0) {
                    w = (w >> 1) ^ polynom.polynom;
                } else {
                    w = w >> 1;
                }
            }
            table[x] = (short) w;
        }
        return table;
    }
}