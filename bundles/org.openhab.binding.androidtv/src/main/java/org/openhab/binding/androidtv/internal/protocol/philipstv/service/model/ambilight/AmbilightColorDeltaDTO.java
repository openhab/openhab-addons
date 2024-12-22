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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link AmbilightColorSettingsDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */

public class AmbilightColorDeltaDTO {

    @JsonProperty("saturation")
    private int saturation;

    @JsonProperty("brightness")
    private int brightness;

    @JsonProperty("hue")
    private int hue;

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getHue() {
        return hue;
    }

    @Override
    public String toString() {
        return "ColorDelta{" + "saturation = '" + saturation + '\'' + ",brightness = '" + brightness + '\'' + ",hue = '"
                + hue + '\'' + "}";
    }
}
