/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "humidity")
public class HumidityModel {

    @XmlElement(name = "rel_humidity")
    private BigDecimal relativeHumidity;

    public BigDecimal getRelativeHumidity() {
        return relativeHumidity != null ? relativeHumidity : BigDecimal.ZERO;
    }

    public void setRelativeHumidity(BigDecimal relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[rel_humidity=").append(getRelativeHumidity()).append("]").toString();
    }
}
