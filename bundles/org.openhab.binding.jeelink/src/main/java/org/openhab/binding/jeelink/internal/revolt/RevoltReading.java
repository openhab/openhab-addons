/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.revolt;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Reading of a Revolt Energy Meter sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class RevoltReading implements Reading {
    private int voltage;
    private float current;
    private int frequency;
    private float power;
    private float powerFact;
    private float consumption;
    private String sensorId;

    public RevoltReading(String sensorId, int voltage, float current, int frequency, float power, float powerFactor,
            float consumption) {
        this.sensorId = sensorId;
        this.voltage = voltage;
        this.current = current;
        this.frequency = frequency;
        this.power = power;
        this.powerFact = powerFactor;
        this.consumption = consumption;
    }

    @Override
    public String toString() {
        return "sensorId=" + sensorId + ": voltage=" + voltage + ", current=" + current + ", frequency=" + frequency
                + ", power=" + power + ", powerFact=" + powerFact + ", consumption=" + consumption;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public int getVoltage() {
        return voltage;
    }

    public float getCurrent() {
        return current;
    }

    public int getFrequency() {
        return frequency;
    }

    public float getPower() {
        return power;
    }

    public float getPowerFactor() {
        return powerFact;
    }

    public float getConsumption() {
        return consumption;
    }
}
