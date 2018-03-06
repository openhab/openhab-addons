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
    private final String fSensorId;
    private final int fChannel;
    private final boolean fConfigReading;
    private final boolean fOn;
    private final float fCurrent;
    private final long fTotal;

    public Pca301Reading(String sensorId, int channel) {
        fSensorId = sensorId;
        fChannel = channel;
        fConfigReading = true;

        fOn = false;
        fCurrent = 0;
        fTotal = 0;
    }

    public Pca301Reading(String sensorId, int channel, boolean deviceOn, float consumptionCurrent,
            long consumptionTotal) {
        fSensorId = sensorId;
        fChannel = channel;
        fOn = deviceOn;
        fConfigReading = false;

        fCurrent = consumptionCurrent;
        fTotal = consumptionTotal;
    }

    @Override
    public String getSensorId() {
        return fSensorId;
    }

    public int getChannel() {
        return fChannel;
    }

    public boolean isOn() {
        return fOn;
    }

    public float getCurrent() {
        return fCurrent;
    }

    public long getTotal() {
        return fTotal;
    }

    public boolean isConfigReading() {
        return fConfigReading;
    }
}
