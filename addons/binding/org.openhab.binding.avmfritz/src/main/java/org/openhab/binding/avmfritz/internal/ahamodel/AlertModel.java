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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlRootElement(name = "alert")
@XmlType(propOrder = { "state" })
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("state", getState()).toString();
    }
}
