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
 * The {@link GetGeneralLx} is responsible for parsing of Lx parts of received MQTT messages.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

public class GetGeneralLx {
    private String L1;
    private String L2;
    private String L3;

    public void setL1(String L1) {
        this.L1 = L1;
    }

    public String getL1() {
        return L1;
    }

    public void setL2(String L2) {
        this.L2 = L2;
    }

    public String getL2() {
        return L2;
    }

    public void setL3(String L3) {
        this.L3 = L3;
    }

    public String getL3() {
        return L3;
    }
}
