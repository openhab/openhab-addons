/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Pentair heat set point packet specialization of a PentairPacket. Includes public variables for many of the reverse
 * engineered
 * packet content.
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairPacketHeatSetPoint extends PentairPacket {

    protected static final int POOLTEMP = 5 + OFFSET;
    protected static final int AIRTEMP = 6 + OFFSET;
    protected static final int POOLSETPOINT = 7 + OFFSET;
    protected static final int SPASETPOINT = 8 + OFFSET;
    protected static final int HEATMODE = 9 + OFFSET;
    protected static final int SOLARTEMP = 12 + OFFSET;

    protected final String[] heatmodestrs = { "Off", "Heater", "Solar Pref", "Solar" };

    /** pool temperature set point */
    public int poolsetpoint;
    /** pool heat mode - 0=Off, 1=Heater, 2=Solar Pref, 3=Solar */
    public int poolheatmode;
    /** pool heat mode as a string */
    public String poolheatmodestr;
    /** spa temperature set point */
    public int spasetpoint;
    /** spa heat mode - 0=Off, 1=Heater, 2=Solar Pref, 3=Solar */
    public int spaheatmode;
    /** spa heat mode as a string */
    public String spaheatmodestr;

    /**
     * Constructor to create a specialized packet representing the generic packet. Note, the internal buffer array is
     * not
     * duplicated. Fills in public class members appropriate with the correct values.
     *
     * @param p Generic PentairPacket to create specific Status packet
     */
    public PentairPacketHeatSetPoint(PentairPacket p) {
        super(p);

        poolsetpoint = p.buf[POOLSETPOINT];
        poolheatmode = p.buf[HEATMODE] & 0x03;
        poolheatmodestr = heatmodestrs[poolheatmode];

        spasetpoint = p.buf[SPASETPOINT];
        spaheatmode = (p.buf[HEATMODE] >> 2) & 0x03;
        spaheatmodestr = heatmodestrs[spaheatmode];
    }

    /**
     * Constructure to create an empty status packet
     */
    public PentairPacketHeatSetPoint() {
        super();
    }
}
