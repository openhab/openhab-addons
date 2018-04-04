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
 * Implements functionality for the Dual White bulbs for protocol version 3.
 * Color temperature is supported for this type of bulbs.
 *
 * @author David Graeff
 * @since 2.0
 *
 */
public class MilightV3White extends MilightV3 {
    protected static final int brightnessLevels = 11;

    public MilightV3White(QueuedSend sendQueue, int zone) {
        super(0, sendQueue, zone);
    }

    private static final byte command_full[] = { (byte) 0xB5, (byte) 0xB8, (byte) 0xBD, (byte) 0xB7, (byte) 0xB2 };
    private static final byte command_on[] = { (byte) 0x35, (byte) 0x38, (byte) 0x3D, (byte) 0x37, (byte) 0x32 };
    private static final byte command_off[] = { (byte) 0x39, (byte) 0x3B, (byte) 0x33, (byte) 0x3A, (byte) 0x36 };
    private static final byte command_nightmode[] = { (byte) 0xB9, (byte) 0xBB, (byte) 0xB3, (byte) 0xBA, (byte) 0xB6 };
    private static final byte prev_animation_mode[] = { 0x27, 0x00, 0x55 };
    private static final byte next_animation_mode[] = { 0x27, 0x00, 0x55 };

    protected void setFull(int zone, MilightThingState state) {
        sendQueue.queueRepeatable(uidc(CAT_BRIGHTNESS_SET), new byte[] { command_full[zone], 0x00, 0x55 });
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
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_on[zone], 0x00, 0x55 });
        } else {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_off[zone], 0x00, 0x55 });
            state.brightness = 0;
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {

    }

    @Override
    public void nightMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        final byte c_night[] = { command_nightmode[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(uidc(CAT_NIGHTMODE), c_on).addRepeatable(c_night));
    }

    @Override
    public void setColorTemperature(int color_temp, MilightThingState state) {
        // White Bulbs: 11 levels of temperature + Off.
        int newLevel;
        int oldLevel;
        // Reset bulb to known state
        if (color_temp <= 0) {
            color_temp = 0;
            newLevel = 1;
            oldLevel = brightnessLevels;
        } else if (color_temp >= 100) {
            color_temp = 100;
            newLevel = brightnessLevels;
            oldLevel = 1;
        } else {
            newLevel = (int) Math.ceil((color_temp * brightnessLevels) / 100.0);
            oldLevel = (int) Math.ceil((state.colorTemperature * brightnessLevels) / 100.0);
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

        state.colorTemperature = color_temp;
    }

    @Override
    public void changeColorTemperature(int color_temp_relative, MilightThingState state) {
        state.colorTemperature = Math.min(100, Math.max(state.colorTemperature + color_temp_relative, 0));
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        final byte c_temp[] = { (byte) (color_temp_relative > 0 ? 0x3E : 0x3F), 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(c_temp));
    }

    // This just emulates an absolute brightness command with the relative commands.
    @Override
    public void setBrightness(int value, MilightThingState state) {
        if (value <= 0) {
            setPower(false, state);
            return;
        } else if (value >= 100) {
            setFull(zone, state);
            return;
        }

        // White Bulbs: 11 levels of brightness + Off.
        final int newLevel = (int) Math.ceil((value * brightnessLevels) / 100.0);

        // When turning on start from full brightness
        int oldLevel;
        final byte c_full[] = { command_full[zone], 0x00, 0x55 };
        QueueItem item = QueueItem.createRepeatable(c_full);
        boolean skipFirst = false;

        if (state.brightness == 0) {
            oldLevel = brightnessLevels;
        } else {
            oldLevel = (int) Math.ceil((state.brightness * brightnessLevels) / 100.0);
            skipFirst = true;
        }

        if (newLevel == oldLevel) {
            return;
        }

        final int repeatCount = Math.abs(newLevel - oldLevel);

        logger.debug("milight: dim from '{}' with command '{}' via '{}' steps.", String.valueOf(state.brightness),
                String.valueOf(value), repeatCount);

        int op = newLevel > oldLevel ? +1 : -1;
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        for (int i = 0; i < repeatCount; i++) {
            final byte[] c_br = { (byte) (op < 0 ? 0x34 : 0x3C), 0x00, 0x55 };
            item = item.addRepeatable(c_on).addNonRepeatable(c_br);
        }

        sendQueue.queue(skipFirst ? item.next : item);

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relative_brightness, MilightThingState state) {
        if (relative_brightness == 0) {
            return;
        }
        state.brightness = Math.min(Math.max(state.brightness + relative_brightness, 0), 100);

        if (state.brightness == 0) {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_off[zone], 0x00, 0x55 });
        } else {
            final byte c_on[] = { command_on[zone], 0x00, 0x55 };
            final byte c_br[] = { (byte) (relative_brightness < 0 ? 0x34 : 0x3C), 0x00, 0x55 };
            sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(c_br));
        }
    }

    @Override
    public void changeSpeed(int relative_speed, MilightThingState state) {

    }

    @Override
    public void previousAnimationMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(prev_animation_mode));
        state.animationMode = Math.max(state.animationMode - 1, 0);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(next_animation_mode));
        state.animationMode = Math.min(state.animationMode + 1, MAX_ANIM_MODES);
    }

}
