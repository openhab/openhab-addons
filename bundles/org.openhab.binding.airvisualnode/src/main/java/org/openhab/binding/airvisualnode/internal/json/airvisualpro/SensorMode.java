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
package org.openhab.binding.airvisualnode.internal.json.airvisualpro;

/**
 * Sensor Operating Mode
 *
 * @author Oleg Davydyuk - Initial contribution
 */
public class SensorMode {

    private long customModeInterval;

    private long mode;

    public SensorMode(long customModeInterval, long mode) {
        this.customModeInterval = customModeInterval;
        this.mode = mode;
    }

    public long getCustomModeInterval() {
        return customModeInterval;
    }

    public void setCustomModeInterval(long customModeInterval) {
        this.customModeInterval = customModeInterval;
    }

    public long getMode() {
        return mode;
    }

    public void setMode(long mode) {
        this.mode = mode;
    }
}
