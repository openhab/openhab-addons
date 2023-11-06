/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "button")
public class ButtonModel {

    @XmlAttribute(name = "identifier")
    private String identifier;

    @XmlAttribute(name = "id")
    private String buttonId;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "lastpressedtimestamp")
    private int lastpressedtimestamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier != null ? identifier.replace(" ", "") : null;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    public int getLastpressedtimestamp() {
        return lastpressedtimestamp;
    }

    public void setLastpressedtimestamp(int lastpressedtimestamp) {
        this.lastpressedtimestamp = lastpressedtimestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (identifier != null ? identifier.hashCode() : 0);
        result = prime * result + (buttonId != null ? buttonId.hashCode() : 0);
        result = prime * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ButtonModel other = (ButtonModel) obj;
        return (identifier != null ? identifier.equals(other.identifier) : other.identifier == null) && //
                (buttonId != null ? buttonId.equals(other.buttonId) : other.buttonId == null) && //
                (name != null ? name.equals(other.name) : other.name == null);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[identifier=").append(getIdentifier()).append(",id=").append(buttonId)
                .append(",name=").append(name).append(",lastpressedtimestamp=").append(lastpressedtimestamp).append("]")
                .toString();
    }
}
