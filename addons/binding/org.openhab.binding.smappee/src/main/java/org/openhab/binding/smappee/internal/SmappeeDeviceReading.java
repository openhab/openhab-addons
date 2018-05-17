/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The result of a smappee reading
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeDeviceReading {

    public int serviceLocationId;
    public SmappeeDeviceReadingConsumption[] consumptions;

    public double getLatestConsumption() {
        if (consumptions.length == 0) {
            return 0;
        }
        return round(getLatestReading().consumption, 0);
    }

    public double getLatestSolar() {
        if (consumptions.length == 0) {
            return 0;
        }
        return round(getLatestReading().solar, 0);
    }

    public double getLatestAlwaysOn() {
        if (consumptions.length == 0) {
            return 0;
        }
        return round(getLatestReading().alwaysOn, 0);
    }

    private SmappeeDeviceReadingConsumption getLatestReading() {
        SmappeeDeviceReadingConsumption latestReading = consumptions[0];

        for (SmappeeDeviceReadingConsumption reading : consumptions) {
            if (reading.timestamp > latestReading.timestamp) {
                latestReading = reading;
            }
        }
        return latestReading;
    }

    private double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
