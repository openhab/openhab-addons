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
package org.openhab.binding.emotiva.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * The EmotivaSubscriptionDTO message type. Used to send commands via {@link EmotivaSubscriptionRequest} to Emotiva
 * devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaSubscriptionDTO {

    @XmlValue
    private String propertyName;

    @SuppressWarnings("unused")
    public EmotivaSubscriptionDTO() {
    }

    public EmotivaSubscriptionDTO(EmotivaSubscriptionTags property) {
        this.propertyName = property.name();
    }

    public static EmotivaSubscriptionDTO fromType(EmotivaSubscriptionTags tag) {
        return new EmotivaSubscriptionDTO(tag);
    }

    public String getName() {
        return propertyName;
    }
}
