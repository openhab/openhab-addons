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
package org.openhab.binding.avmfritz.internal.dto;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "state" })
@XmlRootElement(name = "alert")
public class AlertModel {
    public static final BigDecimal ON = BigDecimal.ONE;
    public static final BigDecimal OFF = BigDecimal.ZERO;

    private BigDecimal state;

    public BigDecimal getState() {
        return state;
    }

    public void setState(BigDecimal state) {
        this.state = state;
    }

    public boolean hasObstructionAlarmOccurred() {
        return state != null && (state.intValue() & 1) != 0;
    }

    public boolean hasTemperaturAlarmOccurred() {
        return state != null && (state.intValue() & 2) != 0;
    }

    public boolean hasUnknownAlarmOccurred() {
        return state != null && ((state.intValue() & 255) >> 2) != 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[state=").append(state).append("]").toString();
    }
}
