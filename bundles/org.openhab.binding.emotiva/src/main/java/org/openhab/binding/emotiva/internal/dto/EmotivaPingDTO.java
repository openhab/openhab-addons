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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaPing message type. Use to discover Emotiva devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaPing")
public class EmotivaPingDTO {

    @XmlAttribute
    private String protocol;

    public EmotivaPingDTO() {
    }

    public EmotivaPingDTO(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}
