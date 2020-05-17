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
package org.openhab.binding.pentair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Pentair status packet specialation of a PentairPacket. Includes public variables for many of the reverse engineered
 * packet content.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairPacketStatus extends PentairPacket { // 29 byte packet format

    protected static final int HOUR = STARTOFDATA;
    protected static final int MIN = STARTOFDATA + 1;
    protected static final int EQUIP1 = STARTOFDATA + 2;
    protected static final int EQUIP2 = STARTOFDATA + 3;
    protected static final int EQUIP3 = STARTOFDATA + 4;
    protected static final int STATUS = STARTOFDATA + 9; // Celsius (0x04) or Farenheit, Service Mode (0x01)
    protected static final int HEATACTIVE = STARTOFDATA + 10; // Heater (0x0C), Solar (0x30), Unknown (0x03)
    protected static final int UNKNOWN = STARTOFDATA + 13; // Something to do with heat?
    protected static final int POOL_TEMP = STARTOFDATA + 14;
    protected static final int SPA_TEMP = STARTOFDATA + 15;
    protected static final int AIR_TEMP = STARTOFDATA + 18;
    protected static final int SOLAR_TEMP = STARTOFDATA + 19;

    // Heat mode defines
    protected static final int HEATMODE_OFF = 0;
    protected static final int HEATMODE_HEATER = 1;
    protected static final int HEATMODE_SOLARPREF = 2;
    protected static final int HEATMODE_SOLARONLY = 3;

    /** hour byte of packet */
    public int hour;
    /** minute byte of packet */
    public int min;

    /** Individual boolean values representing whether a particular ciruit is on or off */
    // public boolean pool, spa, aux1, aux2, aux3, aux4, aux5, aux6, aux7, aux8;
    // public boolean feature1, feature2, feature3, feature4, feature5, feature6, feature7, feature8;
    public boolean pool, spa;
    public boolean[] circuits = new boolean[18];

    /** Unit of Measure - Celsius = true, Farenheit = false */
    public boolean uom;
    public boolean servicemode;
    public boolean heateron;
    public boolean solaron;

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

        int equip = buf[EQUIP3] << 16 | buf[EQUIP2] << 8 | buf[EQUIP1];

        for (int i = 0; i < 8; i++) {
            circuits[i] = ((equip >> i) & 0x0001) == 1;
        }

        uom = (buf[STATUS] & 0x04) != 0;
        servicemode = (buf[STATUS] & 0x01) != 0;

        diag = buf[HEATACTIVE];

        pooltemp = buf[POOL_TEMP];
        spatemp = buf[SPA_TEMP];
        airtemp = buf[AIR_TEMP];
        solartemp = buf[SOLAR_TEMP];

        solaron = (buf[HEATACTIVE] & 0x30) != 0;
        heateron = (buf[HEATACTIVE] & 0x0C) != 0;
    }

    /**
     * Constructure to create an empty status packet
     */
    public PentairPacketStatus() {
        super();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof PentairPacketStatus)) {
            return false;
        }

        PentairPacketStatus p = (PentairPacketStatus) object;

        for (int i = 0; i < 18; i++) {
            if (circuits[i] != p.circuits[i]) {
                return false;
            }
        }

        if (pooltemp != p.pooltemp || spatemp != p.spatemp || airtemp != p.airtemp || solartemp != p.solartemp) {
            return false;
        }

        if (uom != p.uom || servicemode != p.servicemode || solaron != p.solaron || heateron != p.heateron) {
            return false;
        }

        return true;
    }
}
