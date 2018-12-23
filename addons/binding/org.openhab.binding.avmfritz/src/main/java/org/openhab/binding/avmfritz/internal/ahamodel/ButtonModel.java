/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@XmlRootElement(name = "button")
@XmlType(propOrder = { "name", "lastpressedtimestamp" })
public class ButtonModel {

    @XmlAttribute(name = "identifier")
    private @Nullable String identifier;

    @XmlAttribute(name = "id")
    private @Nullable String buttonId;

    @XmlElement(name = "name")
    private @Nullable String name;

    @XmlElement(name = "lastpressedtimestamp")
    private int lastpressedtimestamp;

    public int getLastpressedtimestamp() {
        return lastpressedtimestamp;
    }

    public void setLastpressedtimestamp(int lastpressedtimestamp) {
        this.lastpressedtimestamp = lastpressedtimestamp;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getIdentifier() {
        return identifier != null ? identifier.replace(" ", "") : null;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public @Nullable String getButtonId() {
        return buttonId;
    }

    public void setButtonId(String buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return buttonId != null ? buttonId.equals(((ButtonModel) obj).getButtonId()) : false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identifier", getIdentifier()).append("id", getButtonId())
                .append("name", getName()).append("lastpressedtimestamp", getLastpressedtimestamp()).toString();
    }
}
