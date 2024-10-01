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

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * Defines elements used by common notification DTO classes.
 *
 * @author Espen Fossen - Initial contribution
 */
public class AbstractNotificationDTO {

    // Only present with PROTOCOL_V2 or older
    @XmlAnyElement(lax = true)
    List<Object> tags;

    // Only present with PROTOCOL_V3 or newer
    @XmlElement(name = "property")
    List<EmotivaPropertyDTO> properties;

    public List<EmotivaPropertyDTO> getProperties() {
        return properties;
    }

    public List<Object> getTags() {
        return tags;
    }
}
