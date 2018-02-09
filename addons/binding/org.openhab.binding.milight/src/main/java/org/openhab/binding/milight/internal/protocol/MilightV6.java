/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import org.openhab.binding.milight.internal.MilightThingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements basic V6 bulb functionally. But commands are different for different v6 bulbs, so almost all the work is
 * done in subclasses.
 *
 * @author David Graeff <david.graeff@web.de>
 * @since 2.1
 */
public abstract class MilightV6 extends AbstractBulbInterface {
    protected final Logger logger = LoggerFactory.getLogger(MilightV6.class);

    protected static final int MAX_BR = 100; // Maximum brightness (0x64)
    protected static final int MAX_SAT = 100; // Maximum saturation (0x64)
    protected static final int MAX_TEMP = 100; // Maximum colour temperature (0x64)

    protected MilightV6SessionManager session;

    public MilightV6(int type_offset, QueuedSend sendQueue, MilightV6SessionManager session, int zone) {
        super(type_offset, sendQueue, zone);
        this.session = session;
        if (session == null) {
            throw new RuntimeException("Session object is null!");
        }
    }

    protected abstract byte getAddr();

    protected abstract byte getBrCmd();

    /**
     * Constructs a 0x80... command which us used for all colour,brightness,saturation,mode operations.
     * The zone, session ID, password and sequence number is automatically inserted from the session object.
     *
     * Produces data like:
     * SN: Sequence number
     * S1: SessionID1
     * S2: SessionID2
     * P1/P2: Password bytes
     * WB: Remote (08) or iBox integrated bulb (00)
     * ZN: Zone {Zone1-4 0=All}
     * CK: Checksum
     *
     * #zone 1 on
     * @ 80 00 00 00 11 84 00 00 0c 00 31 00 00 08 04 01 00 00 00 01 00 3f
     *
     * Colors:
     * CC: Color value (hue)
     * 80 00 00 00 11 S1 S2 SN SN 00 31 P1 P2 WB 01 CC CC CC CC ZN 00 CK
     *
     * 80 00 00 00 11 D4 00 00 12 00 31 00 00 08 01 FF FF FF FF 01 00 38
     *
     * @return
     */
    protected byte[] make_command(int... data) {
        byte[] t = { (byte) 0x80, 0x00, 0x00, 0x00, 0x11, session.getSid1(), session.getSid2(),
                session.getNextSequenceNo1(), session.getNextSequenceNo2(), 0x00, 0x31, session.getPw1(),
                session.getPw2(), getAddr(), 0, 0, 0, 0, 0, (byte) zone, 0, 0 };

        for (int i = 0; i < data.length; ++i) {
            t[14 + i] = (byte) data[i];
        }

        byte chksum = (byte) (t[10 + 0] + t[10 + 1] + t[10 + 2] + t[10 + 3] + t[10 + 4] + t[10 + 5] + t[10 + 6]
                + t[10 + 7] + t[10 + 8] + zone);
        t[21] = chksum;
        return t;
    }

    protected byte[] make_link(boolean link) {
        byte[] t = { (link ? (byte) 0x3D : (byte) 0x3E), 0x00, 0x00, 0x00, 0x11, session.getSid1(), session.getSid2(),
                session.getNextSequenceNo1(), session.getNextSequenceNo2(), 0x00, 0x31, session.getPw1(),
                session.getPw2(), getAddr(), 0x00, 0x00, 0x00, 0x00, 0x00, (byte) zone, 0x00, 0x00 };

        byte chksum = (byte) (t[10 + 0] + t[10 + 1] + t[10 + 2] + t[10 + 3] + t[10 + 4] + t[10 + 5] + t[10 + 6]
                + t[10 + 7] + t[10 + 8] + zone);
        t[21] = chksum;
        return t;
    }

    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (hue > 360 || hue < 0) {
            logger.error("Hue argument out of range");
            return;
        }

        // 0xFF = Red, D9 = Lavender, BA = Blue, 85 = Aqua, 7A = Green, 54 = Lime, 3B = Yellow, 1E = Orange
        // we have to map [0,360] to [0,0xFF], where red equals hue=0 and the milight color 0xFF (=255)
        // Integer milightColorNo = (256 + 0xFF - (int) (hue / 360.0 * 255.0)) % 256;

        // Compute destination hue and current hue value, each mapped to 256 values.
        // int cHue = state.hue360 * 255 / 360; // map to 256 values
        int dHue = hue * 255 / 360; // map to 256 values
        sendQueue.queueRepeatable(uidc(CAT_COLOR_SET), make_command(1, dHue, dHue, dHue, dHue));

        state.hue360 = hue;

        if (brightness != -1) {
            setBrightness(brightness, state);
        }

        if (saturation != -1) {
            setSaturation(saturation, state);
        }
    }

    @Override
    public void setBrightness(int value, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        value = Math.min(Math.max(value, 0), 100);

        if (value == 0) {
            setPower(false, state);
        } else if (state.brightness == 0) {
            // If if was dimmed to minimum (off), turn it on again
            setPower(true, state);
        }

        int br = (value * MAX_BR) / 100;
        br = Math.min(br, MAX_BR);
        br = Math.max(br, 0);
        sendQueue.queueRepeatable(uidc(CAT_BRIGHTNESS_SET), make_command(getBrCmd(), br));

        state.brightness = value;
    }

    @Override
    public void changeColorTemperature(int color_temp_relative, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (color_temp_relative == 0) {
            return;
        }

        int ct = (state.colorTemperature * MAX_TEMP) / 100 + color_temp_relative;
        ct = Math.min(ct, MAX_TEMP);
        ct = Math.max(ct, 0);
        setColorTemperature(ct * 100 / MAX_TEMP, state);
    }

    @Override
    public void changeBrightness(int relative_brightness, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (relative_brightness == 0) {
            return;
        }

        int br = (state.brightness * MAX_BR) / 100 + relative_brightness;
        br = Math.min(br, MAX_BR);
        br = Math.max(br, 0);

        setBrightness(br * 100 / MAX_BR, state);
    }

    @Override
    public void previousAnimationMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int mode = state.animationMode - 1;
        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);

        setLedMode(mode, state);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int mode = state.animationMode + 1;
        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);

        setLedMode(mode, state);
    }

    public void link(int zone) {
        sendQueue.queueRepeatable(uidc(CAT_LINK), make_link(true));
    }

    public void unlink(int zone) {
        sendQueue.queueRepeatable(uidc(CAT_LINK), make_link(false));
    }
}