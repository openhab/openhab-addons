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
 * The EmotivaBarNotify message type. Received from a device if subscribed to the
 * {@link org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags#bar_update} type. Uses the
 * {@link EmotivaBarNotifyWrapper} to handle unmarshalling.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmotivaBarNotifyDTO {

    @XmlValue
    private String name = "bar";

    // Possible values “bar”, “centerBar”, “bigText’, “off”
    @XmlAttribute
    private String type;
    @XmlAttribute
    private String text;
    @XmlAttribute
    private String units;
    @XmlAttribute
    private String value;
    @XmlAttribute
    private String min;
    @XmlAttribute
    private String max;

    @SuppressWarnings("unused")
    public EmotivaBarNotifyDTO() {
    }

    public EmotivaBarNotifyDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String formattedMessage() {
        StringBuilder sb = new StringBuilder();

        if (type != null) {
            if (!"off".equals(type)) {
                if (text != null) {
                    sb.append(text);
                }
                if (value != null) {
                    sb.append(" ");
                    try {
                        Double doubleValue = Double.valueOf(value);
                        sb.append(String.format("%.1f", doubleValue));
                    } catch (NumberFormatException e) {
                        sb.append(value);
                    }
                }
                if (units != null) {
                    sb.append(" ").append(units);
                }
            }
        }
        return sb.toString();
    }
}
