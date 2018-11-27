/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.ec3k;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Reading of a EC3000 sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kReading implements Reading {
    private float currentWatt;
    private float maxWatt;
    private long consumptionTotal;
    private long applianceTime;
    private long sensorTime;
    private String sensorId;
    private int resets;

    public Ec3kReading(String sensorId, float currentWatt, float maxWatt, long consumptionTotal, long applianceTime,
            long sensorTime, int resets) {
        this.currentWatt = currentWatt;
        this.maxWatt = maxWatt;
        this.consumptionTotal = consumptionTotal;
        this.applianceTime = applianceTime;
        this.sensorTime = sensorTime;
        this.sensorId = sensorId;
        this.resets = resets;
    }

    @Override
    public String toString() {
        return "sensorId=" + sensorId + ": currWatt=" + currentWatt + ", maxWatt=" + maxWatt + ", consumption="
                + consumptionTotal + ", applianceTime=" + applianceTime + ", sensorTime=" + sensorTime + ", resets="
                + resets;
    }

    public float getCurrentWatt() {
        return currentWatt;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public float getMaxWatt() {
        return maxWatt;
    }

    public long getConsumptionTotal() {
        return consumptionTotal;
    }

    public long getApplianceTime() {
        return applianceTime;
    }

    public long getSensorTime() {
        return sensorTime;
    }

    public int getResets() {
        return resets;
    }
}
