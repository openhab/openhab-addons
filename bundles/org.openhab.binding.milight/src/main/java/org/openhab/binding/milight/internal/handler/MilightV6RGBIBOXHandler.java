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
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Thing;

/**
 * Implements the iBox led bulb. The bulb is integrated into the iBox and does not include a white channel, so no
 * saturation or colour temperature available.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV6RGBIBOXHandler extends AbstractLedV6Handler {
    private static final byte ADDR = 0x00;

    public MilightV6RGBIBOXHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 0);
    }

    @Override
    protected byte getAddr() {
        return ADDR;
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        sendNonRepeatable(3, on ? 3 : 4);
    }

    @Override
    public void whiteMode(MilightThingState state) {
        sendRepeatableCat(ProtocolConstants.CAT_WHITEMODE, 3, 5);
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
    public void setLedMode(int newmode, MilightThingState state) {
        int mode = Math.max(Math.min(newmode, 9), 1);
        sendRepeatableCat(ProtocolConstants.CAT_MODE_SET, 4, Math.max(Math.min(newmode, 9), 1));
        state.animationMode = mode;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        sendNonRepeatable(3, relativeSpeed > 1 ? 2 : 1);
    }
}
