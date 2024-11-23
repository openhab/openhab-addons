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
package org.openhab.binding.tplinksmarthome.internal.model;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

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
        return onOff > 0 ? new PercentType(brightness) : PercentType.ZERO;
    }

    public int getColorTemp() {
        return colorTemp;
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
        return OnOffType.from(onOff == 1);
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
