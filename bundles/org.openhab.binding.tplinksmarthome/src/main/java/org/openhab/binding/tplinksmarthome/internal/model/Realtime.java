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
package org.openhab.binding.tplinksmarthome.internal.model;

/**
 * Data class for reading tp-Link Smart Plug energy monitoring.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Realtime extends ErrorResponse {

    private static final int MILLIWATT_TO_WATT = 1000;
    private static final int MILLIAMP_TO_AMP = 1000;
    private static final int WATTHOUR_TO_KILOWATTHOUR = 1000;
    private static final int MILLIVOLT_TO_VOLT = 1000;

    private double current;
    private double power;
    private double total;
    private double voltage;

    // JSON names used for v2 hardware
    private double currentMa;
    private double powerMw;
    private double totalWh;
    private double voltageMv;

    public double getCurrent() {
        return currentMa > 0.0 ? currentMa / MILLIAMP_TO_AMP : current;
    }

    public double getPower() {
        return powerMw > 0.0 ? powerMw / MILLIWATT_TO_WATT : power;
    }

    public double getTotal() {
        return totalWh > 0.0 ? totalWh / WATTHOUR_TO_KILOWATTHOUR : total;
    }

    public double getVoltage() {
        return voltageMv > 0.0 ? voltageMv / MILLIVOLT_TO_VOLT : voltage;
    }

    @Override
    public String toString() {
        return "current:" + getCurrent() + ", power:" + getPower() + ", total:" + getTotal() + ", voltage:"
                + getVoltage() + super.toString();
    }
}
