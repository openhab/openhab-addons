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
 * Pentair pump status packet specialation of a PentairPacket. Includes public variables for many of the reverse
 * engineered packet content.
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairPacketPumpStatus extends PentairPacket { // 15 byte packet format

    protected static final int RUN = 4 + OFFSET;
    protected static final int MODE = 5 + OFFSET; // Mode in pump status. Means something else in pump write/response?
    protected static final int DRIVESTATE = 6 + OFFSET; // ?? Drivestate in pump status. Means something else in pump
                                                        // write/response
    protected static final int WATTSH = 7 + OFFSET;
    protected static final int WATTSL = 8 + OFFSET;
    protected static final int RPMH = 9 + OFFSET;
    protected static final int RPML = 10 + OFFSET;
    protected static final int PPC = 11 + OFFSET; // ??
    protected static final int ERR = 13 + OFFSET;
    protected static final int TIMER = 14 + OFFSET; // ?? Have to explore
    protected static final int HOUR = 17 + OFFSET;
    protected static final int MIN = 18 + OFFSET;

    /** pump is running */
    public boolean run;

    /** pump mode (1-4) */
    public int mode;

    /** pump drivestate - not sure what this specifically represents. */
    public int drivestate;
    /** pump power - in KW */
    public int power;
    /** pump rpm */
    public int rpm;
    /** pump ppc? */
    public int ppc;
    /** byte in packet indicating an error condition */
    public int error;
    /** current timer for pump */
    public int timer;
    /** hour or packet (based on Intelliflo time setting) */
    public int hour;
    /** minute of packet (based on Intelliflo time setting) */
    public int min;

    /**
     * Constructor to create a specialized packet representing the generic packet. Note, the internal buffer array is
     * not
     * duplicated. Fills in public class members appropriate with the correct values.
     *
     * @param p Generic PentairPacket to create specific Status packet
     */
    public PentairPacketPumpStatus(PentairPacket p) {
        super(p);

        run = (buf[RUN] == (byte) 0x0A);
        mode = buf[MODE];
        drivestate = buf[DRIVESTATE];
        power = (buf[WATTSH] << 8) + buf[WATTSL];
        rpm = (buf[RPMH] << 8) + buf[RPML];
        ppc = buf[PPC];
        error = buf[ERR];
        timer = buf[TIMER];
        hour = buf[HOUR];
        min = buf[MIN];
    }

    /**
     * Constructure to create an empty status packet
     */
    public PentairPacketPumpStatus() {
        super();
    }
}
