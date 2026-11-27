/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarMaxFailureCounter} counts failed polls in a row, so a single failed poll doesn't take the thing
 * OFFLINE. The thing only goes OFFLINE once the threshold is reached, every successful poll starts over.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public class SolarMaxFailureCounter {

    private final int threshold;
    private int failuresInARow = 0;

    /**
     * @param threshold number of failed polls in a row after which the thing goes OFFLINE (values below 1 count as
     *            1, i.e. OFFLINE on the first failure)
     */
    public SolarMaxFailureCounter(int threshold) {
        this.threshold = Math.max(1, threshold);
    }

    public void recordSuccess() {
        failuresInARow = 0;
    }

    /**
     * @return true if the threshold has been reached, i.e. the thing has to go (or stay) OFFLINE
     */
    public boolean recordFailure() {
        if (failuresInARow < Integer.MAX_VALUE) {
            failuresInARow++;
        }
        return isThresholdReached();
    }

    public boolean isThresholdReached() {
        return failuresInARow >= threshold;
    }

    public int getFailuresInARow() {
        return failuresInARow;
    }

    public int getThreshold() {
        return threshold;
    }
}
