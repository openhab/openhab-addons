/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal;

import org.openhab.binding.milight.internal.protocol.MilightCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents the configuration of a openHAB item that is binded to a Mi-Light
 * bulb. It contains the following information:
 *
 * <ul>
 * <li>The channel number the bulb has on the Milight bridge where 0 represents all white bulbs, 1-4 white channels an 5
 * RGB bulbs.</li>
 * <li>The binding type of the Mi-Light item</li>
 * <ul>
 * <li>Brightness</li>
 * <li>Color temperature</li>
 * <li>RGB</li>
 * <li>Night Mode</li>
 * <li>Disco Mode</li>
 * <li>Disco Speed</li>
 * </ul>
 *
 * @author Hans-Joerg Merk
 * @author Kai Kreuzer
 * @since 1.3.0
 */
public class MilightThingState {
    static final Logger logger = LoggerFactory.getLogger(MilightThingState.class);

    /**
     * The channel number under which the bulb is filed in the Mi-Light bridge.
     */
    private final int bulb;
    private final MilightCommunication com;

    private int brightness, discoSpeed, discoMode;
    private int colorTemperature; // only for white leds applicable
    private float hue; // only for rgb(w) leds applicable

    public MilightThingState(int bulb, MilightCommunication com) {
        this.bulb = bulb;
        this.com = com;
    }

    /**
     * @return The deviceId that has been declared in the binding
     *         configuration.
     */
    public String getDeviceId() {
        return com.getBridgeId();
    }

    /**
     * @return The channel number that has been declared in the binding
     *         configuration.
     */
    public int getChannelNumber() {
        return bulb;
    }

    /**
     *
     * @param value A value from 0..100
     */
    public void setBrightness(int value) {
        brightness = com.setBrightness(bulb, value, brightness);
    }

    public void setDiscoSpeed(int value) {
        discoSpeed = com.setDiscoSpeed(bulb, value, discoSpeed);
    }

    public void setDiscoMode(int value) {
        discoMode = com.setDiscoMode(bulb, value, discoMode);
    }

    /**
     * Set the color temperature (cold white ... warm white)
     * Only for white leds applicable.
     */
    public void setColorTemperature(int value) {
        colorTemperature = com.setColorTemperature(bulb, value, colorTemperature);
    }

    public void increaseBrightness() {
        brightness = com.increaseBrightness(bulb, brightness);
    }

    public void decreaseBrightness() {
        brightness = com.decreaseBrightness(bulb, brightness);
    }

    public void warmer() {
        colorTemperature = com.warmer(bulb, colorTemperature);
    }

    public void cooler() {
        colorTemperature = com.cooler(bulb, colorTemperature);
    }

    public void nextDiscoMode() {
        discoMode = com.nextDiscoMode(bulb, discoMode);
    }

    public void previousDiscoMode() {
        discoMode = com.previousDiscoMode(bulb, discoMode);
    }

    public void increaseSpeed() {
        com.increaseSpeed(bulb);
    }

    public void decreaseSpeed() {
        com.decreaseSpeed(bulb);
    }

    public void setNightMode() {
        com.setNightMode(bulb);
    }

    public void setWhiteMode() {
        com.setWhiteMode(bulb);
    }

    public void setFull() {
        brightness = com.setFull(bulb);
    }

    public void setOn() {
        com.setOn(bulb);
    }

    public void setOff() {
        brightness = com.setOff(bulb);
    }

    /**
     * Set color
     *
     * @param hue A value from 0 to 360
     */
    public void setColor(int hue) {
        com.setColor(bulb, hue);
        this.hue = (hue);
    }

    public int getBrightness() {
        return brightness;
    }

    public int getDiscoSpeed() {
        return discoSpeed;
    }

    public int getDiscoMode() {
        return discoMode;
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public float getHue() {
        return hue;
    }
}
