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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;

/**
 * This JAXB model class is part of the XML response to an <b>getcolordefaults</b>
 * command on a FRITZ!Box device. As of today, this class is able to to bind the
 * devicelist version 1 (currently used by AVM) response:
 *
 * <pre>
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "color")
public class ColorDescriptionModel {

    @XmlAttribute(name = "sat_index")
    public int sat_index; // whatever...
    @XmlAttribute(name = "sat")
    public int saturation; // whatever...
    @XmlAttribute(name = "hue")
    public int hue; // whatever...
    @XmlAttribute(name = "val")
    public int brightness; // i hope that's correct.

    public ColorDescriptionModel() {

    }

    public ColorDescriptionModel(int sat_index, int hue, int saturation, int brightness) {
        this.sat_index = sat_index;
        this.saturation = saturation;
        this.hue = hue;
        this.brightness = brightness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ColorDescriptionModel)) return false;

        ColorDescriptionModel that = (ColorDescriptionModel) o;

        return new EqualsBuilder().append(sat_index, that.sat_index).append(saturation, that.saturation).append(hue, that.hue).append(brightness, that.brightness).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(sat_index).append(saturation).append(hue).append(brightness).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ColorDescriptionModel{");
        sb.append("sat_index=").append(sat_index);
        sb.append(", saturation=").append(saturation);
        sb.append(", hue=").append(hue);
        sb.append(", brightness=").append(brightness);
        sb.append('}');
        return sb.toString();
    }
}
