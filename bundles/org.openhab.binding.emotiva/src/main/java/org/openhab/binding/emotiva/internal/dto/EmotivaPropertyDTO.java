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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The EmotivaProperty DTO. Use by multiple message types to get updates from Emotiva devices.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaPropertyDTO {

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String visible;
    @XmlAttribute
    private String status;

    @SuppressWarnings("unused")
    public EmotivaPropertyDTO() {
    }

    public EmotivaPropertyDTO(String name, String value, String visible) {
        this.name = name;
        this.value = value;
        this.visible = visible;
    }

    public EmotivaPropertyDTO(String name, String value, String visible, String status) {
        this.name = name;
        this.value = value;
        this.visible = visible;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return Objects.requireNonNullElse(value, "");
    }

    public String getVisible() {
        return Objects.requireNonNullElse(visible, "false");
    }

    public String getStatus() {
        return Objects.requireNonNullElse(status, "");
    }
}
