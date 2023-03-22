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
package org.openhab.binding.pentair.internal;

/**
 * Pentair status packet specialation of a PentairPacket. Includes public variables for many of the reverse engineered
 * packet content.
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairPacketStatus extends PentairPacket { // 29 byte packet format

    protected static final int HOUR = 4 + OFFSET;
    protected static final int MIN = 5 + OFFSET;
    protected static final int EQUIP1 = 6 + OFFSET;
    protected static final int EQUIP2 = 7 + OFFSET;
    protected static final int EQUIP3 = 8 + OFFSET;
    protected static final int UOM = 13 + OFFSET; // Celsius (0x04) or Farenheit
    protected static final int VALVES = 14 + OFFSET; // Not sure what this actually is? Doesn't seem to be valves
    protected static final int UNKNOWN = 17 + OFFSET; // Something to do with heat?
    protected static final int POOL_TEMP = 18 + OFFSET;
    protected static final int SPA_TEMP = 19 + OFFSET;
    protected static final int HEATACTIVE = 20 + OFFSET; // Does not seem to toggle for my system
    protected static final int AIR_TEMP = 22 + OFFSET;
    protected static final int SOLAR_TEMP = 23 + OFFSET;
    protected static final int HEATMODE = 26 + OFFSET;

    /** hour byte of packet */
    public int hour;
    /** minute byte of packet */
    public int min;

    /** Individual boolean values representing whether a particular ciruit is on or off */
    public boolean pool, spa, aux1, aux2, aux3, aux4, aux5, aux6, aux7;
    public boolean feature1, feature2, feature3, feature4, feature5, feature6, feature7, feature8;

    /** Unit of Measure - Celsius = true, Farenheit = false */
    public boolean uom;

    /** pool temperature */
    public int pooltemp;
    /** spa temperature */
    public int spatemp;
    /** air temperature */
    public int airtemp;
    /** solar temperature */
    public int solartemp;

    /** spa heat mode - 0 = Off, 1 = Heater, 2 = Solar Pref, 3 = Solar */
    public int spaheatmode;
    /** pool heat mode - 0 = Off, 1 = Heater, 2 = Solar Pref, 3 = Solar */
    public int poolheatmode;
    /** Heat is currently active - note this does not work for my system, but has been documented on the internet */
    public int heatactive;

    /** used to store packet value for reverse engineering, not used in normal operation */
    public int diag;

    /**
     * Constructor to create a specialized packet representing the generic packet. Note, the internal buffer array is
     * not
     * duplicated. Fills in public class members appropriate with the correct values.
     *
     * @param p Generic PentairPacket to create specific Status packet
     */
    public PentairPacketStatus(PentairPacket p) {
        super(p);

        hour = buf[HOUR];
        min = buf[MIN];
        pool = (buf[EQUIP1] & 0x20) != 0;
        spa = (buf[EQUIP1] & 0x01) != 0;
        aux1 = (buf[EQUIP1] & 0x02) != 0;
        aux2 = (buf[EQUIP1] & 0x04) != 0;
        aux3 = (buf[EQUIP1] & 0x08) != 0;
        aux4 = (buf[EQUIP1] & 0x10) != 0;
        aux5 = (buf[EQUIP1] & 0x40) != 0;
        aux6 = (buf[EQUIP1] & 0x80) != 0;
        aux7 = (buf[EQUIP2] & 0x01) != 0;

        feature1 = (buf[EQUIP2] & 0x04) != 0;
        feature2 = (buf[EQUIP2] & 0x08) != 0;
        feature3 = (buf[EQUIP2] & 0x10) != 0;
        feature4 = (buf[EQUIP2] & 0x20) != 0;
        feature5 = (buf[EQUIP2] & 0x40) != 0;
        feature6 = (buf[EQUIP2] & 0x80) != 0;
        feature7 = (buf[EQUIP3] & 0x01) != 0;
        feature8 = (buf[EQUIP3] & 0x02) != 0;

        uom = (buf[UOM] & 0x04) != 0;

        diag = buf[HEATACTIVE];

        pooltemp = buf[POOL_TEMP];
        spatemp = buf[SPA_TEMP];
        airtemp = buf[AIR_TEMP];
        solartemp = buf[SOLAR_TEMP];

        spaheatmode = (buf[HEATMODE] >> 2) & 0x03;
        poolheatmode = buf[HEATMODE] & 0x03;
        heatactive = buf[HEATACTIVE];
    }

    /**
     * Constructure to create an empty status packet
     */
    public PentairPacketStatus() {
        super();
    }
}
