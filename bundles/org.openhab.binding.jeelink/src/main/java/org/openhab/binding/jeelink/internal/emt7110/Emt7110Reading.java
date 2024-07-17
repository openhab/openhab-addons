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
package org.openhab.binding.jeelink.internal.emt7110;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Handler for a EMT7110 energy Sensor thing.
 *
 * @author Timo Schober - Initial contribution
 */
public class Emt7110Reading implements Reading {
    private final String sensorId;
    private final float voltage;
    private final float current;
    private final float power;
    private final float aPower;
    private final boolean on;

    public Emt7110Reading(String sensorId, float voltage, float current, float power, float aPower, boolean deviceOn) {
        this.sensorId = sensorId;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.aPower = aPower;
        this.on = deviceOn;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public int getChannel() {
        return 0;
    }

    public float getVoltage() {
        return voltage;
    }

    public float getCurrent() {
        return current;
    }

    public boolean isOn() {
        return on;
    }

    public float getPower() {
        return power;
    }

    public float getaPower() {
        return aPower;
    }
}
