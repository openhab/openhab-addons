/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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

    private final static byte command_full[] = { (byte) 0xB5, (byte) 0xB8, (byte) 0xBD, (byte) 0xB7, (byte) 0xB2 };

    protected void setFull(int zone, MilightThingState state) {
        sendQueue.queueRepeatable( uidc(CAT_BRIGHTNESS_SET), new byte[] { command_full[zone], 0x00, 0x55 });
        state.brightness = 100;
    }

    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        // This bulb type only supports a brightness value
        if (brightness != -1) {
            setBrightness(brightness, state);
        }
    }

    private final static byte command_on[] = { (byte) 0x35, (byte) 0x38, (byte) 0x3D, (byte) 0x37, (byte) 0x32 };
    private final static byte command_off[] = { (byte) 0x39, (byte) 0x3B, (byte) 0x33, (byte) 0x3A, (byte) 0x36 };

    @Override
    public void setPower(boolean on, MilightThingState state) {
        if (on) {
            sendQueue.queueRepeatable( uidc(CAT_POWER_SET), new byte[] { command_on[zone], 0x00, 0x55 });
        } else {
            sendQueue.queueRepeatable( uidc(CAT_POWER_SET), new byte[] { command_off[zone], 0x00, 0x55 });
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {

    }

    private final static byte command_nightmode[] = { (byte) 0xB9, (byte) 0xBB, (byte) 0xB3, (byte) 0xBA, (byte) 0xB6 };

    @Override
    public void nightMode(MilightThingState state) {
        sendQueue.queueRepeatable( uidc(CAT_NIGHTMODE), new byte[] { command_nightmode[zone], 0x00, 0x55 });
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
        int newPercent = Math.min(100, Math.max(state.colorTemperature + color_temp_relative, 0));
        state.colorTemperature = newPercent;
        sendQueue.queue(
                QueuedSend.NO_CATEGORY, new byte[] { (byte) (color_temp_relative > 0 ? 0x3E : 0x3F), 0x00, 0x55 });
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
        if (state.brightness == 0) {
            setFull(zone, state);
            oldLevel = brightnessLevels;
        } else {
            oldLevel = (int) Math.ceil((state.brightness * brightnessLevels) / 100.0);
        }

        final int repeatCount = Math.abs(newLevel - oldLevel);
        logger.debug("milight: dim from '{}' with command '{}' via '{}' steps.", String.valueOf(state.brightness),
                String.valueOf(value), repeatCount);
        if (newLevel > oldLevel) {
            for (int i = 0; i < repeatCount; i++) {
                changeBrightness(+1, state);
            }
        } else if (newLevel < oldLevel) {
            for (int i = 0; i < repeatCount; i++) {
                changeBrightness(-1, state);
            }
        }

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relative_brightness, MilightThingState state) {
        if (relative_brightness == 0) {
            return;
        }
        state.brightness = Math.max(state.brightness + relative_brightness, 0);

        if (state.brightness == 0) {
            setPower(false, state);
        } else {
            setPower(true, state);
            sendQueue.queue(
                    QueuedSend.NO_CATEGORY, new byte[] { (byte) (relative_brightness < 0 ? 0x34 : 0x3C), 0x00, 0x55 });
        }
    }

    @Override
    public void changeSpeed(int relative_speed, MilightThingState state) {

    }

    private final static byte prev_animation_mode[] = { 0x27, 0x00, 0x55 };

    @Override
    public void previousAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue( uidc(CAT_MODE_SET), prev_animation_mode);
        state.animationMode = Math.max(state.animationMode - 1, 0);
    }

    private final static byte next_animation_mode[] = { 0x27, 0x00, 0x55 };

    @Override
    public void nextAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue( uidc(CAT_MODE_SET), next_animation_mode);
        state.animationMode = Math.min(state.animationMode + 1, MAX_ANIM_MODES);
    }

}
