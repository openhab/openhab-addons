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
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueueItem;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
import org.openhab.core.thing.Thing;

/**
 * Implements functionality for the Dual White bulbs for protocol version 3.
 * Color temperature is supported for this type of bulbs.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV3WhiteHandler extends AbstractLedV3Handler {
    protected static final int BRIGHTNESS_LEVELS = 11;

    public MilightV3WhiteHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 0);
    }

    private static final byte[] COMMAND_FULL = { (byte) 0xB5, (byte) 0xB8, (byte) 0xBD, (byte) 0xB7, (byte) 0xB2 };
    private static final byte[] COMMAND_ON = { (byte) 0x35, (byte) 0x38, (byte) 0x3D, (byte) 0x37, (byte) 0x32 };
    private static final byte[] COMMAND_OFF = { (byte) 0x39, (byte) 0x3B, (byte) 0x33, (byte) 0x3A, (byte) 0x36 };
    private static final byte[] COMMAND_NIGHTMODE = { (byte) 0xB9, (byte) 0xBB, (byte) 0xB3, (byte) 0xBA, (byte) 0xB6 };
    private static final byte[] PREV_ANIMATION_MODE = { 0x27, 0x00, 0x55 };
    private static final byte[] NEXT_ANIMATION_MODE = { 0x27, 0x00, 0x55 };

    protected void setFull(int zone, MilightThingState state) {
        sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_BRIGHTNESS_SET),
                new byte[] { COMMAND_FULL[zone], 0x00, 0x55 }));
        state.brightness = 100;
    }

    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        // This bulb type only supports a brightness value
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
            state.brightness = 0;
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
    }

    @Override
    public void nightMode(MilightThingState state) {
        final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
        final byte[] cNight = { COMMAND_NIGHTMODE[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE), cOn).addRepeatable(cNight));
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
        // White Bulbs: 11 levels of temperature + Off.
        int newLevel;
        int oldLevel;
        // Reset bulb to known state
        if (colorTemp <= 0) {
            colorTemp = 0;
            newLevel = 1;
            oldLevel = BRIGHTNESS_LEVELS;
        } else if (colorTemp >= 100) {
            colorTemp = 100;
            newLevel = BRIGHTNESS_LEVELS;
            oldLevel = 1;
        } else {
            newLevel = (int) Math.ceil((colorTemp * BRIGHTNESS_LEVELS) / 100.0);
            oldLevel = (int) Math.ceil((state.colorTemperature * BRIGHTNESS_LEVELS) / 100.0);
        }

        final int repeatCount = Math.abs(newLevel - oldLevel);
        if (newLevel > oldLevel) {
            for (int i = 0; i < repeatCount; i++) {
                changeColorTemperature(1, state);
            }
        } else if (newLevel < oldLevel) {
            for (int i = 0; i < repeatCount; i++) {
                changeColorTemperature(-1, state);
            }
        }

        state.colorTemperature = colorTemp;
    }

    @Override
    public void changeColorTemperature(int colorTempRelative, MilightThingState state) {
        state.colorTemperature = Math.min(100, Math.max(state.colorTemperature + colorTempRelative, 0));
        final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
        final byte[] cTemp = { (byte) (colorTempRelative > 0 ? 0x3E : 0x3F), 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(cTemp));
    }

    // This just emulates an absolute brightness command with the relative commands.
    @Override
    public void setBrightness(int value, MilightThingState state) {
        if (value <= 0) {
            setPower(false, state);
            return;
        } else if (value >= 100) {
            setFull(config.zone, state);
            return;
        }

        // White Bulbs: 11 levels of brightness + Off.
        final int newLevel = (int) Math.ceil((value * BRIGHTNESS_LEVELS) / 100.0);

        // When turning on start from full brightness
        int oldLevel;
        final byte[] cFull = { COMMAND_FULL[config.zone], 0x00, 0x55 };
        QueueItem item = createRepeatable(cFull);
        boolean skipFirst = false;

        if (state.brightness == 0) {
            oldLevel = BRIGHTNESS_LEVELS;
        } else {
            oldLevel = (int) Math.ceil((state.brightness * BRIGHTNESS_LEVELS) / 100.0);
            skipFirst = true;

            if (newLevel == oldLevel) {
                return;
            }
        }

        final int repeatCount = Math.abs(newLevel - oldLevel);

        logger.debug("milight: dim from '{}' with command '{}' via '{}' steps.", String.valueOf(state.brightness),
                String.valueOf(value), repeatCount);

        int op = newLevel > oldLevel ? +1 : -1;
        final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
        for (int i = 0; i < repeatCount; i++) {
            final byte[] cBr = { (byte) (op < 0 ? 0x34 : 0x3C), 0x00, 0x55 };
            item = item.addRepeatable(cOn).addNonRepeatable(cBr);
        }

        final QueueItem nextItem = item.next;
        if (nextItem != null && skipFirst) {
            sendQueue.queue(nextItem);
        } else {
            sendQueue.queue(item);
        }

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relativeBrightness, MilightThingState state) {
        if (relativeBrightness == 0) {
            return;
        }
        state.brightness = Math.min(Math.max(state.brightness + relativeBrightness, 0), 100);

        if (state.brightness == 0) {
            sendQueue.queue(createRepeatable(uidc(ProtocolConstants.CAT_POWER_MODE),
                    new byte[] { COMMAND_OFF[config.zone], 0x00, 0x55 }));
        } else {
            final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
            final byte[] cBr = { (byte) (relativeBrightness < 0 ? 0x34 : 0x3C), 0x00, 0x55 };
            sendQueue.queue(createRepeatable(cOn).addNonRepeatable(cBr));
        }
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
    }

    @Override
    public void previousAnimationMode(MilightThingState state) {
        final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(PREV_ANIMATION_MODE));
        state.animationMode = Math.max(state.animationMode - 1, 0);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        final byte[] cOn = { COMMAND_ON[config.zone], 0x00, 0x55 };
        sendQueue.queue(createRepeatable(cOn).addNonRepeatable(NEXT_ANIMATION_MODE));
        state.animationMode = Math.min(state.animationMode + 1, MAX_ANIM_MODES);
    }
}
