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
package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PercentType;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "colorcontrol")
public class ColorControlModel {

    private static final double SATURATION_FACTOR = 2.54;

    @XmlAttribute(name = "supported_modes")
    public int supportedModes;
    @XmlAttribute(name = "current_mode")
    public int currentMode;
    public int hue;
    public int saturation;
    @XmlElement(name = "unmapped_hue")
    public @Nullable Integer unmappedHue;
    @XmlElement(name = "unmapped_saturation")
    public @Nullable Integer unmappedSaturation;
    public int temperature;

    /**
     * Converts a FRITZ!Box value to a percent value.
     *
     * @param saturation The FRITZ!Box value to be converted
     * @return The percent value
     */
    public static PercentType toPercent(int saturation) {
        int saturationInPercent = (int) Math.ceil(saturation / SATURATION_FACTOR);
        return restrictToBounds(saturationInPercent);
    }

    /**
     * Converts a percent value to a FRITZ!Box value.
     *
     * @param saturationInPercent The percent value to be converted
     * @return The FRITZ!Box value
     */
    public static int fromPercent(PercentType saturationInPercent) {
        return (int) Math.floor(saturationInPercent.intValue() * SATURATION_FACTOR);
    }

    @Override
    public String toString() {
        return new StringBuilder("[supportedModes=").append(supportedModes).append(",currentMode=").append(currentMode)
                .append(",hue=").append(hue).append(",saturation=").append(saturation).append(",unmapped_hue=")
                .append(unmappedHue).append(",unmapped_saturation=").append(unmappedSaturation).append(",temperature=")
                .append(temperature).append("]").toString();
    }

    private static PercentType restrictToBounds(int percentValue) {
        if (percentValue < 0) {
            return PercentType.ZERO;
        } else if (percentValue > 100) {
            return PercentType.HUNDRED;
        }
        return new PercentType(percentValue);
    }
}
