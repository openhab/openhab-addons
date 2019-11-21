/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

/**
 * History item data object.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class SEMPItem {

    private long timestamp;
    private double minPower;
    private double maxPower;
    private double avPower;
    private double stdDevPower;
    private double skewPower;

    public SEMPItem(long timestamp, double avPower, double minPower, double maxPower, double stdDevPower,
            double skewPower) {
        this.timestamp = timestamp;
        this.avPower = avPower;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.stdDevPower = stdDevPower;
        this.skewPower = skewPower;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAvPower() {
        return avPower;
    }

    public void setAvPower(double avPower) {
        this.avPower = avPower;
    }

    public double getMinPower() {
        return minPower;
    }

    public void setMinPower(double minPower) {
        this.minPower = minPower;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }

    public double getStdDevPower() {
        return stdDevPower;
    }

    public void setStdDevPower(double stdDevPower) {
        this.stdDevPower = stdDevPower;
    }

    public double getSkewPower() {
        return skewPower;
    }

    public void setSkewPower(double skewPower) {
        this.skewPower = skewPower;
    }
}
