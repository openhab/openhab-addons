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
 * Implements the RGB white bulb. Both leds cannot be on at the same time, so no saturation or colour temperature
 * control. It still allows more colours than the old v3 rgbw bulb (16320 (255*64) vs 4080 (255*16) colors).
 *
 * @author David Graeff - Initial contribution
 * @since 2.1
 */
public class MilightV6RGBW extends MilightV6 {
    private static final int ADDR = 0x07;

    public MilightV6RGBW(QueuedSend sendQueue, MilightV6SessionManager session, int zone) {
        super(20, sendQueue, session, zone);
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
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(3, 1));
        } else {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(3, 2));
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        sendQueue.queueRepeatable(uidc(CAT_WHITEMODE), makeCommand(3, 5));
    }

    @Override
    public void nightMode(MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        setPower(true, state);
        sendQueue.queueRepeatable(uidc(CAT_NIGHTMODE), makeCommand(3, 6));
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
        logger.info("Color temperature not supported by RGBW led!");
    }

    @Override
    protected byte getBrCmd() {
        return 2;
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
        logger.info("Saturation not supported by RGBW led!");
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
