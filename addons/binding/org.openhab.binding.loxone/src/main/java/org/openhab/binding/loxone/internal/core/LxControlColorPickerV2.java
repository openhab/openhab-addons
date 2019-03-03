/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.types.TemperatureHSBType;

/**
 * A Color Picker V2 type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a color picker control covers:
 * <ul>
 * <li>Color (Hue/Saturation/Brightness)
 * </ul>
 *
 * @author Michael Mattan - initial contribution
 *
 */
public class LxControlColorPickerV2 extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlColorPickerV2(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * Color state
     */
    public static final String STATE_COLOR = "color";

    private static final String TYPE_NAME = "colorpickerv2";

    LxControlColorPickerV2(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * get the current Loxone color in HSBType format
     *
     * @return the HSBType color
     */
    public HSBType getColor() {
        HSBType hsbColor = null;
        String color = getStateTextValue(STATE_COLOR);

        if (color != null) {
            hsbColor = this.mapLoxoneToOH(color);
        }

        return hsbColor;
    }

    /**
     * sets the color of the color picker
     *
     * @param hsb the color to set
     */
    public void setColor(HSBType hsb) {
        socketClient.sendAction(uuid, "hsv(" + hsb.toString() + ")");
    }

    /**
     * Sets the color picker to on
     *
     * @throws IOException
     *                         error communicating with the Miniserver
     */
    public void on() throws IOException {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), PercentType.HUNDRED));
        }
    }

    /**
     * Sets the color picker to off
     *
     * @throws IOException
     *                         error communicating with the Miniserver
     */
    public void off() throws IOException {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), PercentType.ZERO));
        }
    }

    /**
     * set the brightness level
     *
     * @param p the brightness percentage
     */
    public void setBrightness(PercentType p) {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), p));
        }
    }

    /**
     * set the brightness level from a decimal type
     *
     * @param d the brightness in decimal
     */
    public void setBrightness(DecimalType d) {
        this.setBrightness(new PercentType(d.toBigDecimal()));
    }

    /**
     * increases/decreases the brightness with a given step
     *
     * @param step the amount to increase/decrease
     */
    public void increaseDecreaseBrightness(int step) {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setBrightness(new PercentType(currentColor.getBrightness().intValue() + step));
        }
    }

    /**
     * map the Loxone color to OpenHab HSBType
     *
     * @param color color in format hsb(h,s,v) or temp(brightness,kelvin)
     * @return HSBType
     */
    private HSBType mapLoxoneToOH(String color) {
        HSBType hsbColor = null;

        try {
            Pattern hsvPattern = Pattern.compile("^hsv\\([0-9]\\d{0,2},[0-9]\\d{0,2},[0-9]\\d{0,2}\\)");
            Pattern tempPattern = Pattern.compile("^temp\\([0-9]\\d{0,2},[0-9]\\d{0,4}\\)");
            Matcher valueMatcher = Pattern.compile("\\((.*?)\\)").matcher(color);

            if (hsvPattern.matcher(color).matches() && valueMatcher.find()) {
                // we have a hsv(hue,saturation,value) pattern
                hsbColor = new HSBType(valueMatcher.group(1));
            } else if (tempPattern.matcher(color).matches() && valueMatcher.find()) {
                // we have a temp(brightness,kelvin) pattern
                hsbColor = TemperatureHSBType.fromBrightnessTemperature(valueMatcher.group(1));
            }
        } catch (IllegalArgumentException e) {
            // an error happened during HSBType creation, we return null
            hsbColor = null;
        }

        return hsbColor;
    }

}
