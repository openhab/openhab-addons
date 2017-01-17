/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Ubiquiti mPower strip binding. This transforms the raw mPower data into a
 * nice object
 *
 * @author magcode
 */

public class MpowerSocketState {
    private int voltage;
    private long energy;
    private double power;
    private boolean on;
    private int socket;
    private long lastUpdate;
    private final Logger logger = LoggerFactory.getLogger(MpowerSocketState.class);

    public MpowerSocketState(String voltage, String power, String energy, String relayState, int socket) {
        try {
            Double voltageAsDouble = Double.valueOf(Double.parseDouble(voltage));
            this.voltage = voltageAsDouble.intValue();
            Double powerRounded = Double.valueOf(Double.parseDouble(power));
            powerRounded = Double.valueOf(powerRounded.doubleValue() * 10D);
            powerRounded = Double.valueOf(Math.round(powerRounded.doubleValue()));
            powerRounded = Double.valueOf(powerRounded.doubleValue() / 10D);
            this.power = powerRounded.doubleValue();
            Double eneryAsDouble = Double.valueOf(Double.parseDouble(energy));
            eneryAsDouble = Double.valueOf(eneryAsDouble.doubleValue() * 0.13250000000000001D);
            this.energy = eneryAsDouble.longValue();
            on = "1".equals(relayState);
            setLastUpdate(System.currentTimeMillis());
        } catch (NumberFormatException nfe) {
            logger.warn("Could not parse mPower response", nfe);
        }
        this.socket = socket;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getSocket() {
        return socket;
    }

    public void setSocket(int socket) {
        this.socket = socket;
    }

    /*
     * Compares two states and decides whether data has changed or not.
     *
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof MpowerSocketState) {
            MpowerSocketState givenState = (MpowerSocketState) object;
            // make volt a bit fuzzy. we don't care about changes by 5%
            int lower = givenState.getVoltage() - givenState.getVoltage() / 20;
            int higher = givenState.getVoltage() + givenState.getVoltage() / 20;
            boolean sameVolt = true;
            if (getVoltage() < lower || getVoltage() > higher) {
                sameVolt = false;
            }
            boolean samePower = givenState.getPower() == getPower();
            boolean sameEnergy = givenState.getEnergy() == getEnergy();
            boolean sameONOFFstate = givenState.isOn() == isOn();
            if (sameVolt && samePower && sameONOFFstate && sameEnergy) {
                return true;
            }
        }
        return false;
    }

    public long getEnergy() {
        return energy;
    }

    public void setEnergy(long energy) {
        this.energy = energy;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
