/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal.handler.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairPumpStatus } class contains status fields from the pump status packet specialation of a
 * PentairPacket.
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairPumpStatus { // 15 byte packet format
    private final Logger logger = LoggerFactory.getLogger(PentairPumpStatus.class);

    private static final int RUN = 0 + PentairStandardPacket.STARTOFDATA;
    private static final int MODE = 1 + PentairStandardPacket.STARTOFDATA; // Mode in pump status. Means something
                                                                           // else in pump
    // write/response?
    private static final int DRIVESTATE = 2 + PentairStandardPacket.STARTOFDATA; // ?? Drivestate in pump status.
                                                                                 // Means something else in
    // pump write/response
    private static final int WATTSH = 3 + PentairStandardPacket.STARTOFDATA;
    private static final int WATTSL = 4 + PentairStandardPacket.STARTOFDATA;
    private static final int RPMH = 5 + PentairStandardPacket.STARTOFDATA;
    private static final int RPML = 6 + PentairStandardPacket.STARTOFDATA;
    private static final int GPM = 7 + PentairStandardPacket.STARTOFDATA;
    @SuppressWarnings("unused")
    private static final int PPC = 8 + PentairStandardPacket.STARTOFDATA; // not sure what this is? always 0
    private static final int STATUS1 = 11 + PentairStandardPacket.STARTOFDATA;
    private static final int STATUS2 = 12 + PentairStandardPacket.STARTOFDATA;
    private static final int HOUR = 13 + PentairStandardPacket.STARTOFDATA;
    private static final int MIN = 14 + PentairStandardPacket.STARTOFDATA;

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
    public int status1;
    public int status2;
    /** current timer for pump */
    public int timer;
    /** hour or packet (based on Intelliflo time setting) */
    public int hour;
    /** minute of packet (based on Intelliflo time setting) */
    public int min;

    public void parsePacket(PentairStandardPacket p) {
        if (p.getPacketLengthHeader() != 15) {
            logger.debug("Pump status packet not 15 bytes long");
            return;
        }

        run = (p.getByte(RUN) == (byte) 0x0A);
        mode = p.getByte(MODE);
        drivestate = p.getByte(DRIVESTATE);
        power = ((p.getByte(WATTSH) & 0xFF) * 256) + (p.getByte(WATTSL) & 0xFF);
        rpm = ((p.getByte(RPMH) & 0xFF) * 256) + (p.getByte(RPML) & 0xFF);
        gpm = p.getByte(GPM) & 0xFF;

        status1 = p.getByte(STATUS1);
        status2 = p.getByte(STATUS2);
        hour = p.getByte(HOUR);
        min = p.getByte(MIN);
    }

    @Override
    public String toString() {
        String str = String.format("%02d:%02d run:%b mode:%d power:%d rpm:%d gpm:%d status11:0x%h status12:0x%h", hour,
                min, run, mode, power, rpm, gpm, status1, status2);

        return str;
    }
}
