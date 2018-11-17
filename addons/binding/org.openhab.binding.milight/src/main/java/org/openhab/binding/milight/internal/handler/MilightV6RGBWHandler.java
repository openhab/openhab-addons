/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueuedSend;

/**
 * Implements the RGB white bulb. Both leds cannot be on at the same time, so no saturation or colour temperature
 * control. It still allows more colours than the old v3 rgbw bulb (16320 (255*64) vs 4080 (255*16) colors).
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV6RGBWHandler extends AbstractLedV6Handler {
    private static final int ADDR = 0x07;

    public MilightV6RGBWHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 20);
    }

    @Override
    protected byte getAddr() {
        return ADDR;
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        sendNonRepeatable(3, on ? 1 : 2);
    }

    @Override
    public void whiteMode(MilightThingState state) {
        sendRepeatableCat(ProtocolConstants.CAT_POWER_MODE, 3, 5);
    }

    @Override
    public void nightMode(MilightThingState state) {
        setPower(true, state);
        sendRepeatableCat(ProtocolConstants.CAT_POWER_MODE, 3, 6);
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
    public void setLedMode(int newmode, MilightThingState state) {
        int mode = Math.max(Math.min(newmode, 9), 1);
        sendRepeatableCat(ProtocolConstants.CAT_MODE_SET, 6, mode);
        state.animationMode = mode;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        sendNonRepeatable(4, relativeSpeed > 1 ? 3 : 4);
    }
}