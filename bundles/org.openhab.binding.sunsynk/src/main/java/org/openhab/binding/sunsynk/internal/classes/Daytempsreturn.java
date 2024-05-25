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
package org.openhab.binding.sunsynk.internal.classes;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Daytempsretun} is the internal class for Inverter temperature historyinformation
 * from the sunsynk Account.
 * The minute following midnight returns an empty array
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Daytempsreturn {
    private String status = "";
    public double dc;
    public double ac;

    public Daytempsreturn(String status, double dc, double ac) {
        this.status = status;
        this.dc = dc;
        this.ac = ac;
    }

    public String getStatus() {
        return this.status;
    }

    public double getACTemperature() {
        return this.dc;
    }

    public double getDCTemperature() {
        return this.ac;
    }
}
