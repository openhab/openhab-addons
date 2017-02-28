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
 * Implements functionality for the RGB/White bulbs for protocol version 3.
 * Color temperature and saturation are not supported for this type of bulbs.
 *
 * @author David Graeff
 * @since 2.0
 *
 */
public class MilightV3RGBW extends MilightV3 {
    protected static final int brightnessLevels = 26;

    public MilightV3RGBW(QueuedSend sendQueue, int zone) {
        super(10, sendQueue, zone);
    }

    // We have no real saturation control for RGBW bulbs. If the saturation is under a 50% threshold
    // we just change to white mode instead.
    @Override
    public void setHSB(int hue, int saturation, int brightness, MilightThingState state) {
        if (saturation < 50) {
            whiteMode(state);
        } else {
            setPower(true, state);

            state.hue360 = hue;
            byte messageBytes[] = new byte[] { 0x40, make_color(hue), 0x55 };
            sendQueue.queueRepeatable(uidc(CAT_COLOR_SET), messageBytes);

            if (brightness != -1) {
                setBrightness(brightness, state);
            }
        }
    }

    static final private byte command_on[] = { (byte) 0x42, (byte) 0x45, (byte) 0x47, (byte) 0x49, (byte) 0x4B };
    static final private byte command_off[] = { (byte) 0x41, (byte) 0x46, (byte) 0x48, (byte) 0x4A, (byte) 0x4C };

    @Override
    public void setPower(boolean on, MilightThingState state) {
        if (on) {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_on[zone], 0x00, 0x55 });
        } else {
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_off[zone], 0x00, 0x55 });
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
        byte command[] = { (byte) 0xC2, (byte) 0xC5, (byte) 0xC7, (byte) 0xC9, (byte) 0xCB };
        setPower(true, state);
        sendQueue.queueRepeatable(uidc(CAT_WHITEMODE), new byte[] { command[zone], 0x00, 0x55 });
    }

    private final static byte nightmode_first[] = { 0x41, 0x46, 0x48, 0x4A, 0x4C };
    private final static byte nightmode_second[] = { (byte) 0xC1, (byte) 0xC6, (byte) 0xC8, (byte) 0xCA, (byte) 0xCC };

    @Override
    public void nightMode(MilightThingState state) {
        // nightMode for RGBW bulbs requires a second message
        sendQueue.queueRepeatable(uidc(CAT_NIGHTMODE), new byte[] { nightmode_first[zone], 0x00, 0x55 },
                new byte[] { nightmode_second[zone], 0x00, 0x55 });
    }

    @Override
    public void setColorTemperature(int color_temp, MilightThingState state) {

    }

    @Override
    public void changeColorTemperature(int color_temp_relative, MilightThingState state) {

    }

    @Override
    public void setBrightness(int value, MilightThingState state) {
        if (value <= 0) {
            setPower(false, state);
            return;
        }

        setPower(true, state);

        int br = (int) Math.ceil((value * brightnessLevels) / 100.0) + 1;
        sendQueue.queueRepeatable(uidc(CAT_BRIGHTNESS_SET), new byte[] { 0x4E, (byte) br, 0x55 });
        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relative_brightness, MilightThingState state) {
        setBrightness(Math.max(100, Math.min(100, state.brightness + relative_brightness)), state);
    }

    @Override
    public void changeSpeed(int relative_speed, MilightThingState state) {
        if (relative_speed == 0) {
            return;
        }

        setPower(true, state);
        sendQueue.queue(QueuedSend.NO_CATEGORY, new byte[] { (byte) (relative_speed > 0 ? 0x44 : 0x43), 0x00, 0x55 });
    }

    private final static byte next_animation_mode[] = { 0x4D, 0x00, 0x55 };

    // This bulb actually doesn't implement a previous animation mode command. We just use the next mode command
    // instead.
    @Override
    public void previousAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue(uidc(CAT_MODE_SET), next_animation_mode);
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        setPower(true, state);
        sendQueue.queue(uidc(CAT_MODE_SET), next_animation_mode);
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }
}
