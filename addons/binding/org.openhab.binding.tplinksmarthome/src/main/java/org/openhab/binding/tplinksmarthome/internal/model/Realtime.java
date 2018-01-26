/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

/**
 * Data class for reading tp-Link Smart Plug energy monitoring.
 * Only getter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class Realtime extends ErrorResponse {

    private static final int V2_TO_V1 = 1000;

    private double current;
    private double power;
    private double total;
    private double voltage;

    // JSON names used for v2 hardware
    private double current_ma;
    private double power_mw;
    private double total_wh;
    private double voltage_mv;

    public double getCurrent() {
        if (current == 0 && current_ma != 0) {
         return current_ma / V2_TO_V1; // v1 displays as amps
        }

        return current;
    }

    public double getPower() {
        if (power == 0.0 && power_mw != 0.0) {
          return power_mw / V2_TO_V1; // v1 displays as watts
        }

        return power;
    }

    public double getTotal() {
        if (total == 0.0 && total_wh != 0.0) {
          return total_wh / V2_TO_V1; // v1 displays as kWh
        }

        return total;
    }

    public double getVoltage() {
        if (total == 0.0 && voltage_mv != 0.0) {
          return voltage_mv / V2_TO_V1; // v1 displays as volts
        }
        return voltage;
    }


    @Override
    public String toString() {
        return "current:" + current + ", power:" + power + ", total:" + total + ", voltage:" + voltage
                + super.toString();
    }

}
