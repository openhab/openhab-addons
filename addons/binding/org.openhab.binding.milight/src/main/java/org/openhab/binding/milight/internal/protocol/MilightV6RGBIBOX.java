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
 * Implements the iBox led bulb. The bulb is integrated into the iBox and does not include a white channel, so no
 * saturation or colour temperature available.
 *
 * @author David Graeff - Initial contribution
 * @since 2.1
 */
public class MilightV6RGBIBOX extends MilightV6 {
    private static final byte ADDR = 0x00;

    public MilightV6RGBIBOX(QueuedSend sendQueue, MilightV6SessionManager session) {
        super(0, sendQueue, session, 1);
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
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(3, 3));
        } else {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), makeCommand(3, 4));
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
        logger.info("Night mode not supported by iBox led!");
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
        logger.info("Color temperature not supported by iBox led!");
    }

    @Override
    public void changeColorTemperature(int colorTempRelative, MilightThingState state) {
        logger.info("Color temperature not supported by iBox led!");
    }

    @Override
    protected byte getBrCmd() {
        return 2;
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
        logger.info("Saturation not supported by iBox led!");
    }

    @Override
    public void setLedMode(int mode, MilightThingState state) {
        if (!session.isValid()) {
            logger.error("Bridge communication session not valid yet!");
            return;
        }

        mode = Math.min(mode, 9);
        mode = Math.max(mode, 1);
        sendQueue.queueRepeatable(uidc(CAT_MODE_SET), makeCommand(4, mode));
        state.animationMode = mode;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        if (relativeSpeed > 1) {
            sendQueue.queue(QueueItem.createNonRepeatable(makeCommand(3, 2)));
        } else if (relativeSpeed < 1) {
            sendQueue.queue(QueueItem.createNonRepeatable(makeCommand(3, 1)));
        }
    }

    @Override
    public void link(int zone) {
    }

    @Override
    public void unlink(int zone) {
    }

}
