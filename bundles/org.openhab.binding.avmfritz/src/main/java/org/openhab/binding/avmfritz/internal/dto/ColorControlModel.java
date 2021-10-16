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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "colorcontrol")
public class ColorControlModel {

    @XmlAttribute(name = "supported_modes")
    public int supportedModes;
    @XmlAttribute(name = "current_mode")
    public int currentMode;
    public int hue;
    public int saturation;
    public int temperature;

    @Override
    public String toString() {
        return new StringBuilder("[supportedModes=").append(supportedModes).append(",currentMode=").append(currentMode)
                .append(",hue=").append(hue).append(",saturation=").append(saturation).append(",temperature=")
                .append(temperature).append("]").toString();
    }
}
