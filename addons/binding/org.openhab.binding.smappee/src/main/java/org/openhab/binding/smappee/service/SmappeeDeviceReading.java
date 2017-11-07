/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.service;

/**
 * The result of a smappee reading
 *
 * @author Niko Tanghe
 */

public class SmappeeDeviceReading {
    public int serviceLocationId;
    public SmappeeDeviceReadingConsumption[] consumptions;

    public double getLatestConsumption() {
        if (consumptions.length == 0) {
            return 0;
        }
        return getLatestReading().consumption;
    }

    public double getLatestSolar() {
        if (consumptions.length == 0) {
            return 0;
        }
        return getLatestReading().solar;
    }

    public double getLatestAlwaysOn() {
        if (consumptions.length == 0) {
            return 0;
        }
        return getLatestReading().alwaysOn;
    }

    private SmappeeDeviceReadingConsumption getLatestReading() {
        if (consumptions.length == 0) {
            return null;
        }

        SmappeeDeviceReadingConsumption latestReading = consumptions[0];

        for (SmappeeDeviceReadingConsumption reading : consumptions) {
            if (reading.timestamp > latestReading.timestamp) {
                latestReading = reading;
            }
        }
        return latestReading;
    }
}
