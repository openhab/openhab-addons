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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The EmotivaNotify message type. Received from a device if subscribed to
 * {@link org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags} values. Uses
 * the {@link EmotivaNotifyWrapper} to handle unmarshalling.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaNotifyDTO {

    @XmlValue
    private String tagName;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String visible;
    @XmlAttribute
    private String status;
    @XmlAttribute
    private String ack;

    @SuppressWarnings("unused")
    public EmotivaNotifyDTO() {
    }

    public EmotivaNotifyDTO(String tag) {
        this.tagName = tag;
    }

    public String getName() {
        return tagName;
    }

    public String getValue() {
        return value;
    }

    public String getVisible() {
        return visible;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.tagName = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }
}
