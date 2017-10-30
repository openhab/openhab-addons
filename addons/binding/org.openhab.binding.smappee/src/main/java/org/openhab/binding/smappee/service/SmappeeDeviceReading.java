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

    public double GetLatestConsumption() {
        if (consumptions.length == 0) {
            return 0;
        }
        return GetLatestReading().consumption;
    }

    public double GetLatestSolar() {
        if (consumptions.length == 0) {
            return 0;
        }
        return GetLatestReading().solar;
    }

    public double GetLatestAlwaysOn() {
        if (consumptions.length == 0) {
            return 0;
        }
        return GetLatestReading().alwaysOn;
    }

    private SmappeeDeviceReadingConsumption GetLatestReading() {
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
