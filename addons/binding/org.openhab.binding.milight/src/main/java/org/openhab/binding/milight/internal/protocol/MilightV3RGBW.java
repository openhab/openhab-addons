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
 * Implements functionality for the RGB/White bulbs for protocol version 3.
 * Color temperature and saturation are not supported for this type of bulbs.
 *
 * @author David Graeff
 * @since 2.0
 *
 */
public class MilightV3RGBW extends MilightV3 {
    protected static final int brightnessLevels = 26;

    private static final byte command_on[] = { (byte) 0x42, (byte) 0x45, (byte) 0x47, (byte) 0x49, (byte) 0x4B };
    private static final byte command_off[] = { (byte) 0x41, (byte) 0x46, (byte) 0x48, (byte) 0x4A, (byte) 0x4C };
    private static final byte command_whitemode[] = { (byte) 0xC2, (byte) 0xC5, (byte) 0xC7, (byte) 0xC9, (byte) 0xCB };
    private static final byte nightmode_first[] = { 0x41, 0x46, 0x48, 0x4A, 0x4C };
    private static final byte nightmode_second[] = { (byte) 0xC1, (byte) 0xC6, (byte) 0xC8, (byte) 0xCA, (byte) 0xCC };
    private static final byte next_animation_mode[] = { 0x4D, 0x00, 0x55 };

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

            state.hue360 = hue;
            final byte messageBytes[] = new byte[] { 0x40, make_color(hue), 0x55 };
            final byte c_on[] = { command_on[zone], 0x00, 0x55 };
            sendQueue.queue(QueueItem.createRepeatable(uidc(CAT_COLOR_SET), c_on).addRepeatable(messageBytes));
        }

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
        }
    }

    @Override
    public void whiteMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        final byte c_white[] = { command_whitemode[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(uidc(CAT_WHITEMODE), c_on).addRepeatable(c_white));
    }

    @Override
    public void nightMode(MilightThingState state) {
        final byte c_n1[] = { nightmode_first[zone], 0x00, 0x55 };
        final byte c_n2[] = { nightmode_second[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(uidc(CAT_NIGHTMODE), c_n1).addRepeatable(c_n2));
    }

    @Override
    public void setColorTemperature(int color_temp, MilightThingState state) {

    }

    @Override
    public void changeColorTemperature(int color_temp_relative, MilightThingState state) {

    }

    @Override
    public void setBrightness(int value, MilightThingState state) {
        value = Math.min(Math.max(value, 0), 100);

        if (value == 0) {
            state.brightness = value;
            sendQueue.queueRepeatable(uidc(CAT_POWER_SET), new byte[] { command_off[zone], 0x00, 0x55 });
            return;
        }

        int br = (int) Math.ceil((value * brightnessLevels) / 100.0) + 1;

        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(uidc(CAT_BRIGHTNESS_SET), c_on)
                .addRepeatable(new byte[] { 0x4E, (byte) br, 0x55 }));

        state.brightness = value;
    }

    @Override
    public void changeBrightness(int relative_brightness, MilightThingState state) {
        setBrightness(Math.max(0, Math.min(100, state.brightness + relative_brightness)), state);
    }

    @Override
    public void changeSpeed(int relative_speed, MilightThingState state) {
        if (relative_speed == 0) {
            return;
        }

        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        final byte c_speed[] = { (byte) (relative_speed > 0 ? 0x44 : 0x43), 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(c_speed));
    }

    // This bulb actually doesn't implement a previous animation mode command. We just use the next mode command
    // instead.
    @Override
    public void previousAnimationMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(next_animation_mode));
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }

    @Override
    public void nextAnimationMode(MilightThingState state) {
        final byte c_on[] = { command_on[zone], 0x00, 0x55 };
        sendQueue.queue(QueueItem.createRepeatable(c_on).addNonRepeatable(next_animation_mode));
        state.animationMode = (state.animationMode + 1) % (MAX_ANIM_MODES + 1);
    }
}
