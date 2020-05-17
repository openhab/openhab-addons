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

/**
 * Pentair pump status packet specialation of a PentairPacket. Includes public variables for many of the reverse
 * engineered packet content.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairPacketPumpStatus extends PentairPacket { // 15 byte packet format

    protected static final int RUN = STARTOFDATA;
    protected static final int MODE = STARTOFDATA + 1; // Mode in pump status. Means something else in pump
                                                       // write/response?
    protected static final int DRIVESTATE = STARTOFDATA + 2; // ?? Drivestate in pump status. Means something else in
                                                             // pump write/respoonse
    protected static final int WATTSH = STARTOFDATA + 3;
    protected static final int WATTSL = STARTOFDATA + 4;
    protected static final int RPMH = STARTOFDATA + 5;
    protected static final int RPML = STARTOFDATA + 6;
    protected static final int GPM = STARTOFDATA + 7;
    protected static final int PPC = STARTOFDATA + 8; // not sure what this is? always 0
    protected static final int B09 = STARTOFDATA + 9;
    protected static final int ERR = STARTOFDATA + 10;
    protected static final int STATUS11 = STARTOFDATA + 11;
    protected static final int STATUS12 = STARTOFDATA + 12;
    protected static final int HOUR = STARTOFDATA + 13;
    protected static final int MIN = STARTOFDATA + 14;

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
    /** pump gpm */
    public int gpm;
    /** byte in packet indicating an error condition */
    public int error;
    /** byte in packet indicated status */
    public int status11;
    public int status12;
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
        power = ((buf[WATTSH] & 0xFF) * 256) + (buf[WATTSL] & 0xFF);
        rpm = ((buf[RPMH] & 0xFF) * 256) + (buf[RPML] & 0xFF);
        gpm = buf[GPM];

        error = buf[ERR];
        status11 = buf[STATUS11];
        status12 = buf[STATUS12];
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
