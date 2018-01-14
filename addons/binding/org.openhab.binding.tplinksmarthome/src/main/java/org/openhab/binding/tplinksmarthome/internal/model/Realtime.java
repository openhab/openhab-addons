/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

    private double current;
    private double power;
    private double total;
    private double voltage;

    public double getCurrent() {
        return current;
    }

    public double getPower() {
        return power;
    }

    public double getTotal() {
        return total;
    }

    public double getVoltage() {
        return voltage;
    }

    @Override
    public String toString() {
        return "current:" + current + ", power:" + power + ", total:" + total + ", voltage:" + voltage
                + super.toString();
    }

}
