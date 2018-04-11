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

/**
 * Implements the RGB cold white / warm white bulb. It is the most feature rich bulb.
 *
 * @author David Graeff - Initial contribution
 * @since 2.1
 */
public class MilightV6RGBCWWW extends MilightV6 {
    private static final int ADDR = 0x08;

    public MilightV6RGBCWWW(QueuedSend sendQueue, MilightV6SessionManager session, int zone) {
        super(10, sendQueue, session, zone);
    }

    @Override
    protected byte getAddr() {
        return ADDR;
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        if (on) {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(4, 1));
        } else {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(4, 2));
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        sendQueue.queueRepeatable(uidc(CAT_WHITEMODE), makeCommand(5, state.colorTemperature));
    }

    @Override
    public void nightMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        setPower(true, state);
        sendQueue.queueRepeatable(uidc(CAT_NIGHTMODE), makeCommand(4, 5));
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int ct = (colorTemp * MAX_TEMP) / 100;
        ct = Math.min(ct, MAX_TEMP);
        ct = Math.max(ct, 0);
        sendQueue.queueRepeatable(uidc(CAT_TEMPERATURE_SET), makeCommand(5, ct));
        state.colorTemperature = colorTemp;
    }

    @Override
    protected byte getBrCmd() {
        return 3;
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        int br = (value * MAX_SAT) / 100; // map value from [0,100] -> [0,MAX_SAT]
        br = MAX_SAT - br; // inverse value
        br = Math.min(br, MAX_SAT); // force maximum value
        br = Math.max(br, 0); // force minimum value
        sendQueue.queueRepeatable(uidc(CAT_SATURATION_SET), makeCommand(2, br));
        state.saturation = value;
    }

    @Override
    public void setLedMode(int mode, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);
        sendQueue.queueRepeatable(uidc(CAT_MODE_SET), makeCommand(6, mode));
        state.animationMode = mode;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        if (relativeSpeed > 1) {
            sendQueue.queue(QueueItem.createNonRepeatable(makeCommand(4, 3)));
        } else if (relativeSpeed < 1) {
            sendQueue.queue(QueueItem.createNonRepeatable(makeCommand(4, 4)));
        }
    }
}
