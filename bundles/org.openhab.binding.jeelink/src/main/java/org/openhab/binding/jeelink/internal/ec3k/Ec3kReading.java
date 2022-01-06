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
