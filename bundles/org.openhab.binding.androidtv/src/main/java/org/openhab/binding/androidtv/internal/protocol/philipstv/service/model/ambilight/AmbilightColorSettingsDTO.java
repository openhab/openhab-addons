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
 * Part of {@link AmbilightConfigDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */

public class AmbilightColorSettingsDTO {

    @JsonProperty("color")
    private AmbilightColorDTO color;

    @JsonProperty("colorDelta")
    private AmbilightColorDeltaDTO colorDelta;

    @JsonProperty("speed")
    private int speed;

    public AmbilightColorSettingsDTO(AmbilightColorDTO color, AmbilightColorDeltaDTO colorDelta) {
        this.color = color;
        this.colorDelta = colorDelta;
    }

    public void setColor(AmbilightColorDTO color) {
        this.color = color;
    }

    public AmbilightColorDTO getColor() {
        return color;
    }

    public void setColorDelta(AmbilightColorDeltaDTO colorDelta) {
        this.colorDelta = colorDelta;
    }

    public AmbilightColorDeltaDTO getColorDelta() {
        return colorDelta;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "ColorSettings{" + "color = '" + color + '\'' + ",colorDelta = '" + colorDelta + '\'' + ",speed = '"
                + speed + '\'' + "}";
    }
}
