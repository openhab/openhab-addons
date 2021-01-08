/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import javax.xml.bind.annotation.*;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "colorcontrol")
public class ColorControlModel {

    @XmlAttribute(name = "supported_modes")
    Integer supportedModes;

    @XmlAttribute(name = "current_mode")
    Integer currentMode;

    public Integer getHue() {
        return hue;
    }

    public Integer getSaturation() {
        return saturation;
    }

    public Integer getTemperature() {
        return temperature;
    }

    Integer hue;

    Integer saturation;

    Integer temperature;

    public Integer getSupportedModes() {
        return supportedModes;
    }

    public Integer getCurrentMode() {
        return currentMode;
    }
}
