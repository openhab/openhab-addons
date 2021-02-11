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
@XmlRootElement(name = "name")
public class ColorNameModel {

    @XmlAttribute(name = "enum")
    public int internalId;
    @XmlValue
    public String name;

    public ColorNameModel() {
    }

    public ColorNameModel(int internalId, String name) {
        this.internalId = internalId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ColorNameModel)) return false;

        ColorNameModel that = (ColorNameModel) o;

        return new EqualsBuilder().append(internalId, that.internalId).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(internalId).append(name).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ColorNameModel{");
        sb.append("internalId=").append(internalId);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
