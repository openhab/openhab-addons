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

import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Thing;

/**
 * Implements functionality for the RGB/White bulbs for protocol version 3.
 * Color temperature and saturation are not supported for this type of bulbs.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightV3RGBWHandler extends AbstractLedV3Handler {
    protected static final int BRIGHTNESS_LEVELS = 26;

    private static final byte COMMAND_ON[] = { (byte) 0x42, (byte) 0x45, (byte) 0x47, (byte) 0x49, (byte) 0x4B };
    private static final byte COMMAND_OFF[] = { (byte) 0x41, (byte) 0x46, (byte) 0x48, (byte) 0x4A, (byte) 0x4C };
    private static final byte COMMAND_WHITEMODE[] = { (byte) 0xC2, (byte) 0xC5, (byte) 0xC7, (byte) 0xC9, (byte) 0xCB };
    private static final byte NIGHTMODE_FIRST[] = { 0x41, 0x46, 0x48, 0x4A, 0x4C };
    private static final byte NIGHTMODE_SECOND[] = { (byte) 0xC1, (byte) 0xC6, (byte) 0xC8, (byte) 0xCA, (byte) 0xCC };
    private static final byte NEXT_ANIMATION_MODE[] = { 0x4D, 0x00, 0x55 };

    public MilightV3RGBWHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 10);
    }

    // We have no real saturation control for RGBW bulbs. If the saturation is under a 50% threshold
    // we just change to white mode instead.
    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        if (saturation < 50) {
            whiteMode(state);
        } else {
            state.hue360 = hue;
            final byte messageBytes[] = new byte[] { 0x40, makeColor(hue), 0x55 };
            final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_COLOR_SET), cOn).addRepeatable(messageBytes));
        }

        if (brightness != -1) {
            setBrightness(brightness, state);
        }
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        if (on) {
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE),
                    new byte[] { COMMAND_ON[config.zone], 0x00, 0x55 }));
        } else {
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE),
                    new byte[] { COMMAND_OFF[config.zone], 0x00, 0x55 }));
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
        final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
        final byte cWhite[] = { COMMAND_WHITEMODE[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_WHITEMODE), cOn).addRepeatable(cWhite));
    }

    @Override
    public void nightMode(MilightThingState state) {
        final byte cN1[] = { NIGHTMODE_FIRST[config.zone], 0x00, 0x55 };
        final byte cN2[] = { NIGHTMODE_SECOND[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE), cN1).addRepeatable(cN2));
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
    }

    @Override
    public void changeColorTemperature(int colorTempRelative, MilightThingState state) {
    }

    @Override
    public void setBrightness(int newvalue, MilightThingState state) {
        int value = Math.min(Math.max(newvalue, 0), 100);

        if (value == 0) {
            state.brightness = value;
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE),
                    new byte[] { COMMAND_OFF[config.zone], 0x00, 0x55 }));
            return;
        }

        int br = (int) Math.ceil((value * BRIGHTNESS_LEVELS) / 100.0) + 1;

        final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_BRIGHTNESS_SET), cOn)
                .addRepeatable(new byte[] { 0x4E, (byte) br, 0x55 }));

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relativeBrightness, MilightThingState state) {
        setBrightness(Math.max(0, Math.min(100, state.brightness + relativeBrightness)), state);
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        if (relativeSpeed == 0) {
            return;
        }

        final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
        final byte cSpeed[] = { (byte) (relativeSpeed > 0 ? 0x44 : 0x43), 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(cSpeed));
    }

    // This bulb actually doesn't implement a previous animation mode command. We just use the next mode command
    // instead.
    @Override
    public void previousAnimationMode(MilightThingState state) {
        final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(NEXT_ANIMATION_MODE));
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        final byte cOn[] = { COMMAND_ON[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(NEXT_ANIMATION_MODE));
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }
}
