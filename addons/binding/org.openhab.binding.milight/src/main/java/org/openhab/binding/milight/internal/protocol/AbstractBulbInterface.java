/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.protocol;

import java.net.SocketException;

import org.openhab.binding.milight.internal.MilightThingState;

/**
 * Implement this bulb interface for each new bulb type. It is used by {@see MilightLedHandler} to handle commands.
 *
 * @author David Graeff <david.graeff@web.de>
 * @since 2.1
 */
public abstract class AbstractBulbInterface {
    /**
     *
     * @param hue A value from 0 to 360
     * @param saturation A saturation value. Can be -1 if not known
     * @param brightness A brightness value. Can be -1 if not known
     * @param state The changed values will be written back to the state
     */
    public abstract void setHSB(int hue, int saturation, int brightness, MilightThingState state);

    public abstract void setPower(boolean on, MilightThingState state);

    /**
     * Switches to white mode (disables color leds).
     *
     * @param state The changed values will be written back to the state
     */
    public abstract void whiteMode(MilightThingState state);

    /**
     * Switches to night mode (low current for all leds).
     *
     * @param state The changed values will be written back to the state
     */
    public abstract void nightMode(MilightThingState state);

    public abstract void setColorTemperature(int color_temp, MilightThingState state);

    public abstract void changeColorTemperature(int color_temp_relative, MilightThingState state);

    public abstract void setBrightness(int value, MilightThingState state);

    public abstract void changeBrightness(int relative_brightness, MilightThingState state);

    public abstract void setSaturation(int value, MilightThingState state);

    public abstract void setLedMode(int mode, MilightThingState state);

    public abstract void previousAnimationMode(MilightThingState state);

    public abstract void nextAnimationMode(MilightThingState state);

    public abstract void changeSpeed(int relative_speed, MilightThingState state);

    /**
     * There can only be one command of a category in the send queue (to avoid
     * having multiple on/off commands in the queue for example). You can assign
     * a category to each command you send and use one of the following constants.
     */
    // Session commands
    public static final int CAT_DISCOVER = 1;
    public static final int CAT_KEEP_ALIVE = 2;
    public static final int CAT_SESSION = 3;

    // Bulb commands
    public static final int CAT_BRIGHTNESS_SET = 10;
    public static final int CAT_SATURATION_SET = 11;
    public static final int CAT_COLOR_SET = 12;
    public static final int CAT_POWER_SET = 13;
    public static final int CAT_TEMPERATURE_SET = 14;
    public static final int CAT_NIGHTMODE = 15;
    public static final int CAT_WHITEMODE = 17;
    public static final int CAT_MODE_SET = 18;
    public static final int CAT_SPEED_CHANGE = 19;
    public static final int CAT_LINK = 20;

    protected final QueuedSend sendQueue;
    protected final int zone;
    // Each bulb type including zone has to be unique. To realise this, each type has an offset.
    protected final int type_offset;

    /**
     * A bulb always belongs to a zone in the milight universe and we need a way to queue commands for being send.
     *
     * @param type_offset Each bulb type including its zone has to be unique. To realise this, each type has an offset.
     * @param sendQueue The send queue.
     * @param zone A zone, usually 0 means all bulbs of the same type. [0-4]
     * @throws SocketException
     */
    public AbstractBulbInterface(int type_offset, QueuedSend sendQueue, int zone) {
        this.sendQueue = sendQueue;
        this.zone = zone;
        this.type_offset = type_offset;
    }

    /**
     * Generates a unique command id for the {@see QueuedSend}. It incorporates the zone, bulb type and command
     * category.
     * @param command_category The category of the command.
     *
     * @return
     */
    protected int uidc(int command_category) {
        return (zone + type_offset + 1) * 64 + command_category;
    }
}
