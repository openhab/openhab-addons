/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Data class for reading the retrieved state of a Smart Home light bulb.
 * Mostly only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class LightState extends ErrorResponse {

    private int brightness;
    private int colorTemp;
    private int hue;
    private int ignoreDefault;
    private String mode;
    private int onOff;
    private int saturation;

    public PercentType getBrightness() {
        return new PercentType(brightness);
    }

    public DecimalType getColorTemp() {
        return new DecimalType(colorTemp);
    }

    public DecimalType getHue() {
        return new DecimalType(hue);
    }

    public int getIgnoreDefault() {
        return ignoreDefault;
    }

    public String getMode() {
        return mode;
    }

    public OnOffType getOnOff() {
        return onOff == 1 ? OnOffType.ON : OnOffType.OFF;
    }

    public PercentType getSaturation() {
        return new PercentType(saturation);
    }

    public void setOnOff(OnOffType onOff) {
        this.onOff = onOff == OnOffType.ON ? 1 : 0;
    }

    @Override
    public String toString() {
        return "brightness:" + brightness + ", color_temp:" + colorTemp + ", hue:" + hue + ", ignore_default:"
                + ignoreDefault + ", mode:" + mode + ", on_off:" + onOff + ", saturation:" + saturation
                + super.toString();
    }

}
