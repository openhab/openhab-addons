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
import java.util.List;

/**
 * This JAXB model class is part of the XML response to an <b>getcolordefaults</b>
 * command on a FRITZ!Box device. As of today, this class is able to to bind the
 * devicelist version 1 (currently used by AVM) response:
 *
 * <pre>
 *
 * @author Joshua Bacher - Initial contribution
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "hs")
public class HueSaturationModel {

    public HueSaturationModel() {
    }

    public HueSaturationModel(int index, ColorNameModel colorNameModel, List<ColorDescriptionModel> colorDescriptionModels) {
        this.index = index;
        this.colorNameModel = colorNameModel;
        this.colorDescriptionModels = colorDescriptionModels;
    }

    @XmlAttribute(name = "hue_index")
    public int index;

    @XmlElement(name="name")
    public ColorNameModel colorNameModel;

    @XmlElement(name="color")
    public List<ColorDescriptionModel> colorDescriptionModels;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof HueSaturationModel)) return false;

        HueSaturationModel that = (HueSaturationModel) o;

        return new EqualsBuilder().append(index, that.index).append(colorNameModel, that.colorNameModel).append(colorDescriptionModels, that.colorDescriptionModels).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(index).append(colorNameModel).append(colorDescriptionModels).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HueSaturationModel{");
        sb.append("index=").append(index);
        sb.append(", colorNameModel=").append(colorNameModel);
        sb.append(", colorDescriptionModels=").append(colorDescriptionModels);
        sb.append('}');
        return sb.toString();
    }
}
