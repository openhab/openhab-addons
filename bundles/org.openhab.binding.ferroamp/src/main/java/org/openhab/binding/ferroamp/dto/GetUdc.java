/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.ferroamp.dto;

/**
 * The {@link GetUdc} is responsible for parsing of Udc part of received MQTT messages.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

public class GetUdc {
    private String pos;
    private String neg;

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getPos() {
        return pos;
    }

    public void setNeg(String neg) {
        this.neg = neg;
    }

    public String getNeg() {
        return neg;
    }
}
