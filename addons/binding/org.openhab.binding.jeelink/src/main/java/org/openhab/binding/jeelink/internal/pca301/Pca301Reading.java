/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.pca301;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Reading of a PCA301 sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class Pca301Reading implements Reading {
    private final String sensorId;
    private final int channel;
    private final boolean on;
    private final float current;
    private final long total;

    public Pca301Reading(String sensorId, int channel, boolean deviceOn, float consumptionCurrent,
            long consumptionTotal) {
        this.sensorId = sensorId;
        this.channel = channel;
        on = deviceOn;
        current = consumptionCurrent;
        total = consumptionTotal;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public int getChannel() {
        return channel;
    }

    public boolean isOn() {
        return on;
    }

    public float getCurrent() {
        return current;
    }

    public long getTotal() {
        return total;
    }
}
